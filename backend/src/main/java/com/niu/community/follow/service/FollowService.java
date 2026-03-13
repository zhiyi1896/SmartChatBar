package com.niu.community.follow.service;

import com.niu.community.cache.service.RedisJsonCacheService;
import com.niu.community.mq.dto.NotificationMessage;
import com.niu.community.mq.producer.CommunityProducer;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

    private final StringRedisTemplate redisTemplate;
    private final CommunityProducer communityProducer;
    private final UserMapper userMapper;
    private final RedisJsonCacheService redisJsonCacheService;

    public FollowService(StringRedisTemplate redisTemplate, CommunityProducer communityProducer,
                         UserMapper userMapper, RedisJsonCacheService redisJsonCacheService) {
        this.redisTemplate = redisTemplate;
        this.communityProducer = communityProducer;
        this.userMapper = userMapper;
        this.redisJsonCacheService = redisJsonCacheService;
    }

    public Map<String, Object> toggleFollow(Long userId, Long targetUserId) {
        String followingKey = "follow:following:" + userId;
        String followerKey = "follow:follower:" + targetUserId;
        boolean following = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(followingKey, String.valueOf(targetUserId)));
        if (following) {
            redisTemplate.opsForSet().remove(followingKey, String.valueOf(targetUserId));
            redisTemplate.opsForSet().remove(followerKey, String.valueOf(userId));
            following = false;
        } else {
            redisTemplate.opsForSet().add(followingKey, String.valueOf(targetUserId));
            redisTemplate.opsForSet().add(followerKey, String.valueOf(userId));
            following = true;
            UserEntity user = userMapper.selectById(userId);
            String sender = user == null ? "Someone" : user.getNickname();
            communityProducer.sendNotification(new NotificationMessage(targetUserId, userId, "FOLLOW", sender + " followed you", userId));
        }
        redisJsonCacheService.delete("profile:summary:" + userId, "profile:summary:" + targetUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("following", following);
        result.put("followingCount", sizeOf(followingKey));
        result.put("followerCount", sizeOf(followerKey));
        return result;
    }

    public Set<Long> getFollowingIds(Long userId) {
        Set<String> ids = redisTemplate.opsForSet().members("follow:following:" + userId);
        if (ids == null) {
            return Set.of();
        }
        return ids.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

    private long sizeOf(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0L : size;
    }
}