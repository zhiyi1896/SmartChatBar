package com.niu.community.comment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.niu.community.cache.service.RedisJsonCacheService;
import com.niu.community.comment.dto.CommentDTO;
import com.niu.community.comment.entity.CommentEntity;
import com.niu.community.comment.mapper.CommentMapper;
import com.niu.community.comment.vo.CommentVO;
import com.niu.community.common.exception.BusinessException;
import com.niu.community.mq.dto.NotificationMessage;
import com.niu.community.mq.producer.CommunityProducer;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.post.service.PostService;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private static final Duration COMMENT_TTL = Duration.ofMinutes(3);
    private static final Duration EMPTY_TTL = Duration.ofSeconds(45);
    private static final Duration REBUILD_LOCK_TTL = Duration.ofSeconds(30);

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommunityProducer communityProducer;
    private final RedisJsonCacheService redisJsonCacheService;
    private final PostService postService;
    private final Executor cacheRebuildExecutor;

    public CommentService(CommentMapper commentMapper, UserMapper userMapper, PostMapper postMapper,
                          CommunityProducer communityProducer, RedisJsonCacheService redisJsonCacheService,
                          PostService postService, @Qualifier("cacheRebuildExecutor") Executor cacheRebuildExecutor) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.communityProducer = communityProducer;
        this.redisJsonCacheService = redisJsonCacheService;
        this.postService = postService;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    @Transactional
    public void publishComment(CommentDTO dto, Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND");
        }
        Long ownerId = null;
        if (dto.getType() == 1) {
            PostEntity post = postMapper.selectById(dto.getTargetId());
            if (post == null || post.getStatus() == 0) {
                throw new BusinessException("POST_NOT_FOUND");
            }
            postMapper.incrementCommentCount(post.getId());
            ownerId = post.getUserId();
            postService.evictDetailCache(post.getId());
        }
        CommentEntity entity = new CommentEntity();
        entity.setTargetId(dto.getTargetId());
        entity.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        entity.setReplyUserId(dto.getReplyUserId());
        entity.setUserId(userId);
        entity.setType(dto.getType());
        entity.setContent(dto.getContent());
        entity.setLikeCount(0);
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(entity);
        evictCommentCache(dto.getTargetId(), dto.getType());
        if (ownerId != null && !ownerId.equals(userId)) {
            communityProducer.sendNotification(new NotificationMessage(ownerId, userId, "COMMENT", user.getNickname() + " commented on your post", dto.getTargetId()));
        }
    }

    public List<CommentVO> getComments(Long targetId, Integer type) {
        String cacheKey = commentCacheKey(targetId, type);
        String cached = redisJsonCacheService.getRaw(cacheKey);
        if (RedisJsonCacheService.NULL_MARKER.equals(cached)) {
            return List.of();
        }
        if (cached != null) {
            RedisJsonCacheService.CachePayload<List<CommentVO>> payload = redisJsonCacheService.readPayload(cached, new TypeReference<List<CommentVO>>() {});
            if (payload.expired()) {
                rebuildCommentsAsync(targetId, type);
            }
            return payload.data();
        }

        List<CommentVO> comments = loadComments(targetId, type);
        if (comments.isEmpty()) {
            redisJsonCacheService.writeNull(cacheKey, EMPTY_TTL);
            return List.of();
        }
        redisJsonCacheService.writeWithLogicalExpire(cacheKey, comments, COMMENT_TTL);
        return comments;
    }

    public void evictCommentCache(Long targetId, Integer type) {
        redisJsonCacheService.delete(commentCacheKey(targetId, type));
    }

    private void rebuildCommentsAsync(Long targetId, Integer type) {
        String lockKey = commentCacheKey(targetId, type) + ":lock";
        if (!redisJsonCacheService.tryLock(lockKey, REBUILD_LOCK_TTL)) {
            return;
        }
        cacheRebuildExecutor.execute(() -> {
            try {
                List<CommentVO> comments = loadComments(targetId, type);
                if (comments.isEmpty()) {
                    redisJsonCacheService.writeNull(commentCacheKey(targetId, type), EMPTY_TTL);
                    return;
                }
                redisJsonCacheService.writeWithLogicalExpire(commentCacheKey(targetId, type), comments, COMMENT_TTL);
            } finally {
                redisJsonCacheService.unlock(lockKey);
            }
        });
    }

    private List<CommentVO> loadComments(Long targetId, Integer type) {
        List<Map<String, Object>> rows = commentMapper.selectCommentTree(targetId, type);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Map<String, Object>>> childrenMap = rows.stream()
            .filter(row -> ((Number) row.get("parent_id")).longValue() != 0L)
            .collect(Collectors.groupingBy(row -> ((Number) row.get("parent_id")).longValue()));

        return rows.stream()
            .filter(row -> ((Number) row.get("parent_id")).longValue() == 0L)
            .map(row -> buildComment(row, childrenMap))
            .collect(Collectors.toList());
    }

    private String commentCacheKey(Long targetId, Integer type) {
        return "comment:list:" + type + ":" + targetId;
    }

    private CommentVO buildComment(Map<String, Object> row, Map<Long, List<Map<String, Object>>> childrenMap) {
        CommentVO vo = mapRow(row);
        List<Map<String, Object>> children = childrenMap.getOrDefault(vo.getId(), new ArrayList<>());
        vo.setChildren(children.stream().map(this::mapRow).collect(Collectors.toList()));
        return vo;
    }

    private CommentVO mapRow(Map<String, Object> row) {
        CommentVO vo = new CommentVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setUserId(((Number) row.get("user_id")).longValue());
        vo.setParentId(((Number) row.get("parent_id")).longValue());
        if (row.get("reply_user_id") != null) {
            vo.setReplyUserId(((Number) row.get("reply_user_id")).longValue());
        }
        vo.setAuthorName((String) row.get("user_nickname"));
        vo.setAuthorAvatar((String) row.get("user_avatar"));
        vo.setReplyUserName((String) row.get("reply_user_name"));
        vo.setContent((String) row.get("content"));
        vo.setLikeCount(((Number) row.get("like_count")).intValue());
        vo.setCreateTime(((LocalDateTime) row.get("create_time")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return vo;
    }
}