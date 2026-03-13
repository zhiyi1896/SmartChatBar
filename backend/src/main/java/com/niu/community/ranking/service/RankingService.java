package com.niu.community.ranking.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.niu.community.common.model.PageResult;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.post.vo.PostVO;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public RankingService(PostMapper postMapper, UserMapper userMapper, StringRedisTemplate redisTemplate) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public void refreshHotRanking() {
        List<PostEntity> posts = postMapper.selectList(Wrappers.<PostEntity>lambdaQuery().eq(PostEntity::getStatus, 1));
        String key = "ranking:hot";
        redisTemplate.delete(key);
        for (PostEntity post : posts) {
            double score = calculateScore(post);
            redisTemplate.opsForZSet().add(key, String.valueOf(post.getId()), score);
        }
    }

    public PageResult<PostVO> getHotPosts(int page, int pageSize) {
        long start = (long) (page - 1) * pageSize;
        long end = start + pageSize - 1;
        var ids = redisTemplate.opsForZSet().reverseRange("ranking:hot", start, end);
        if (ids == null || ids.isEmpty()) {
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
        List<Long> postIds = ids.stream().map(Long::parseLong).toList();
        List<PostEntity> posts = postMapper.selectBatchIds(postIds);
        Map<Long, UserEntity> userMap = userMapper.selectBatchIds(posts.stream().map(PostEntity::getUserId).toList())
            .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        List<PostVO> list = posts.stream().map(post -> mapToVo(post, userMap.get(post.getUserId()))).toList();
        return new PageResult<>(list, list.size(), page, pageSize);
    }

    private double calculateScore(PostEntity post) {
        LocalDateTime createTime = post.getCreateTime() == null ? LocalDateTime.now() : post.getCreateTime();
        long ageHours = java.time.Duration.between(createTime, LocalDateTime.now()).toHours();
        return post.getLikeCount() * 5D + post.getCommentCount() * 3D + post.getViewCount() * 0.2D - ageHours * 0.1D;
    }

    private PostVO mapToVo(PostEntity post, UserEntity user) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setAuthorName(user == null ? "匿名" : user.getNickname());
        vo.setAuthorAvatar(user == null ? null : user.getAvatar());
        vo.setCreateTime(post.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setLiked(false);
        vo.setAuthor(false);
        return vo;
    }
}
