package com.niu.community.post.service;

import com.niu.community.cache.service.RedisJsonCacheService;
import com.niu.community.common.exception.BusinessException;
import com.niu.community.common.model.PageResult;
import com.niu.community.es.entity.PostDocument;
import com.niu.community.es.service.PostEsService;
import com.niu.community.mq.dto.PostSyncMessage;
import com.niu.community.mq.producer.CommunityProducer;
import com.niu.community.post.dto.PostDTO;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.post.vo.PostVO;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private static final Duration DETAIL_TTL = Duration.ofMinutes(5);
    private static final Duration EMPTY_TTL = Duration.ofSeconds(60);
    private static final Duration REBUILD_LOCK_TTL = Duration.ofSeconds(30);

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CommunityProducer communityProducer;
    private final PostEsService postEsService;
    private final RedisJsonCacheService redisJsonCacheService;
    private final Executor cacheRebuildExecutor;

    public PostService(PostMapper postMapper, UserMapper userMapper, CommunityProducer communityProducer,
                       PostEsService postEsService, RedisJsonCacheService redisJsonCacheService,
                       @Qualifier("cacheRebuildExecutor") Executor cacheRebuildExecutor) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.communityProducer = communityProducer;
        this.postEsService = postEsService;
        this.redisJsonCacheService = redisJsonCacheService;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    @Transactional
    @CacheEvict(value = {"postList", "postDetail"}, allEntries = true)
    public PostVO publishPost(PostDTO postDTO, Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND");
        }
        PostEntity entity = new PostEntity();
        entity.setUserId(userId);
        entity.setTitle(postDTO.getTitle());
        entity.setContent(postDTO.getContent());
        entity.setStatus(1);
        entity.setIsTop(0);
        entity.setIsWonderful(0);
        entity.setScore(0D);
        entity.setViewCount(0);
        entity.setLikeCount(0);
        entity.setCommentCount(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        postMapper.insert(entity);
        evictDetailCache(entity.getId());
        evictProfileSummaryCache(userId);
        syncPost(entity.getId(), "UPSERT");
        return buildVO(entity, user, userId);
    }

    @Cacheable(value = "postList", key = "#page + ':' + #pageSize + ':' + #userId + ':' + #keyword + ':' + #currentUserId")
    public PageResult<PostVO> getPostList(int page, int pageSize, Long userId, String keyword, Long currentUserId) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> rows = postMapper.selectPostListWithUser(offset, pageSize, userId, keyword);
        List<PostVO> list = rows.stream().map(row -> buildVO(row, currentUserId)).collect(Collectors.toList());
        long total = list.size();
        return new PageResult<>(list, total, page, pageSize);
    }

    @Transactional
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        String cacheKey = detailCacheKey(postId);
        String cached = redisJsonCacheService.getRaw(cacheKey);
        if (RedisJsonCacheService.NULL_MARKER.equals(cached)) {
            throw new BusinessException("POST_NOT_FOUND");
        }
        if (cached != null) {
            RedisJsonCacheService.CachePayload<PostVO> payload = redisJsonCacheService.readPayload(cached, PostVO.class);
            PostVO cachedVo = payload.data();
            if (payload.expired()) {
                rebuildDetailAsync(postId);
            }
            return enrichDetail(cachedVo, currentUserId);
        }

        postMapper.incrementViewCount(postId);
        Map<String, Object> row = postMapper.selectPostDetailWithUser(postId);
        if (row == null || row.isEmpty()) {
            redisJsonCacheService.writeNull(cacheKey, EMPTY_TTL);
            throw new BusinessException("POST_NOT_FOUND");
        }
        PostVO vo = buildVO(row, null);
        redisJsonCacheService.writeWithLogicalExpire(cacheKey, vo, DETAIL_TTL);
        return enrichDetail(vo, currentUserId);
    }

    @Transactional
    @CacheEvict(value = {"postList", "postDetail"}, allEntries = true)
    public void deletePost(Long postId, Long userId) {
        PostEntity entity = requireActivePost(postId);
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException("FORBIDDEN_DELETE_POST");
        }
        entity.setStatus(0);
        entity.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(entity);
        evictDetailCache(postId);
        evictProfileSummaryCache(entity.getUserId());
        syncPost(postId, "DELETE");
    }

    @Transactional
    @CacheEvict(value = {"postList", "postDetail"}, allEntries = true)
    public void markWonderful(Long postId, boolean wonderful) {
        PostEntity entity = requireActivePost(postId);
        entity.setIsWonderful(wonderful ? 1 : 0);
        entity.setScore(calculateScore(entity));
        entity.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(entity);
        evictDetailCache(postId);
        syncPost(postId, "UPSERT");
    }

    @Transactional
    @CacheEvict(value = {"postList", "postDetail"}, allEntries = true)
    public void markTop(Long postId, boolean top) {
        PostEntity entity = requireActivePost(postId);
        entity.setIsTop(top ? 1 : 0);
        entity.setScore(calculateScore(entity));
        entity.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(entity);
        evictDetailCache(postId);
        syncPost(postId, "UPSERT");
    }

    public void evictDetailCache(Long postId) {
        redisJsonCacheService.delete(detailCacheKey(postId));
    }

    public void evictProfileSummaryCache(Long userId) {
        redisJsonCacheService.delete("profile:summary:" + userId);
    }

    private void rebuildDetailAsync(Long postId) {
        String lockKey = detailCacheKey(postId) + ":lock";
        if (!redisJsonCacheService.tryLock(lockKey, REBUILD_LOCK_TTL)) {
            return;
        }
        cacheRebuildExecutor.execute(() -> {
            try {
                Map<String, Object> latest = postMapper.selectPostDetailWithUser(postId);
                if (latest == null || latest.isEmpty()) {
                    redisJsonCacheService.writeNull(detailCacheKey(postId), EMPTY_TTL);
                    return;
                }
                PostVO refreshed = buildVO(latest, null);
                redisJsonCacheService.writeWithLogicalExpire(detailCacheKey(postId), refreshed, DETAIL_TTL);
            } finally {
                redisJsonCacheService.unlock(lockKey);
            }
        });
    }

    private String detailCacheKey(Long postId) {
        return "post:detail:" + postId;
    }

    private PostVO enrichDetail(PostVO vo, Long currentUserId) {
        vo.setAuthor(currentUserId != null && currentUserId.equals(vo.getUserId()));
        if (vo.getLiked() == null) {
            vo.setLiked(false);
        }
        return vo;
    }

    private PostEntity requireActivePost(Long postId) {
        PostEntity entity = postMapper.selectById(postId);
        if (entity == null || entity.getStatus() == 0) {
            throw new BusinessException("POST_NOT_FOUND");
        }
        return entity;
    }

    private double calculateScore(PostEntity entity) {
        return (entity.getIsWonderful() == 1 ? 1000D : 0D) + (entity.getIsTop() == 1 ? 500D : 0D)
            + entity.getLikeCount() * 5D + entity.getCommentCount() * 3D + entity.getViewCount() * 0.2D;
    }

    private void syncPost(Long postId, String action) {
        communityProducer.syncPost(new PostSyncMessage(postId, action));
        if ("DELETE".equalsIgnoreCase(action)) {
            postEsService.delete(postId);
            return;
        }
        PostEntity entity = postMapper.selectById(postId);
        if (entity == null) {
            return;
        }
        UserEntity user = userMapper.selectById(entity.getUserId());
        if (user != null) {
            postEsService.save(toDocument(entity, user));
        }
    }

    private PostVO buildVO(PostEntity entity, UserEntity user, Long currentUserId) {
        PostVO vo = new PostVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setAuthorName(user.getNickname());
        vo.setAuthorAvatar(user.getAvatar());
        vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        vo.setViewCount(entity.getViewCount());
        vo.setLikeCount(entity.getLikeCount());
        vo.setCommentCount(entity.getCommentCount());
        vo.setLiked(false);
        vo.setAuthor(currentUserId != null && currentUserId.equals(entity.getUserId()));
        return vo;
    }

    private PostVO buildVO(Map<String, Object> row, Long currentUserId) {
        PostVO vo = new PostVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setUserId(((Number) row.get("user_id")).longValue());
        vo.setTitle((String) row.get("title"));
        vo.setContent((String) row.get("content"));
        vo.setAuthorName((String) row.get("user_nickname"));
        vo.setAuthorAvatar((String) row.get("user_avatar"));
        vo.setCreateTime(((LocalDateTime) row.get("create_time")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        vo.setViewCount(((Number) row.get("view_count")).intValue());
        vo.setLikeCount(((Number) row.get("like_count")).intValue());
        vo.setCommentCount(((Number) row.get("comment_count")).intValue());
        vo.setLiked(false);
        vo.setAuthor(currentUserId != null && currentUserId.equals(vo.getUserId()));
        return vo;
    }

    private PostDocument toDocument(PostEntity entity, UserEntity user) {
        PostDocument document = new PostDocument();
        document.setId(entity.getId());
        document.setUserId(entity.getUserId());
        document.setTitle(entity.getTitle());
        document.setContent(entity.getContent());
        document.setAuthorName(user.getNickname());
        document.setLikeCount(entity.getLikeCount());
        document.setCommentCount(entity.getCommentCount());
        document.setViewCount(entity.getViewCount());
        document.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return document;
    }
}