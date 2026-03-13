package com.niu.community.like.service;

import com.niu.community.common.exception.BusinessException;
import com.niu.community.mq.dto.NotificationMessage;
import com.niu.community.mq.producer.CommunityProducer;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private static final DefaultRedisScript<String> TOGGLE_LIKE_SCRIPT = new DefaultRedisScript<>("""
        local userLikeKey = KEYS[1]
        local targetLikeKey = KEYS[2]
        local receivedKey = KEYS[3]
        local member = ARGV[1]
        local userId = ARGV[2]
        local existed = redis.call('SISMEMBER', userLikeKey, member)
        local liked = 0
        if existed == 1 then
            redis.call('SREM', userLikeKey, member)
            redis.call('SREM', targetLikeKey, userId)
            local current = tonumber(redis.call('GET', receivedKey) or '0')
            if current > 0 then
                redis.call('DECR', receivedKey)
            end
        else
            redis.call('SADD', userLikeKey, member)
            redis.call('SADD', targetLikeKey, userId)
            redis.call('INCR', receivedKey)
            liked = 1
        end
        local likeCount = redis.call('SCARD', targetLikeKey)
        local receivedCount = tonumber(redis.call('GET', receivedKey) or '0')
        return liked .. ':' .. likeCount .. ':' .. receivedCount
        """, String.class);

    private final StringRedisTemplate redisTemplate;
    private final CommunityProducer communityProducer;
    private final UserMapper userMapper;

    public LikeService(StringRedisTemplate redisTemplate, CommunityProducer communityProducer, UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.communityProducer = communityProducer;
        this.userMapper = userMapper;
    }

    public Map<String, Object> toggleLike(Long userId, String targetType, Long targetId, Long targetOwnerId) {
        if (userId.equals(targetOwnerId) && "user".equals(targetType)) {
            throw new BusinessException("CANNOT_LIKE_SELF");
        }
        String userLikeKey = "like:user:" + userId;
        String targetLikeKey = "like:" + targetType + ":" + targetId;
        String member = targetType + ":" + targetId;
        String receivedLikeKey = "like:received:user:" + targetOwnerId;

        String scriptResult = redisTemplate.execute(
            TOGGLE_LIKE_SCRIPT,
            List.of(userLikeKey, targetLikeKey, receivedLikeKey),
            member,
            String.valueOf(userId)
        );
        if (scriptResult == null) {
            throw new BusinessException("LIKE_FAILED");
        }

        String[] parts = scriptResult.split(":");
        if (parts.length != 3) {
            throw new BusinessException("LIKE_RESULT_INVALID");
        }

        boolean liked = Objects.equals(parts[0], "1");
        long likeCount = Long.parseLong(parts[1]);
        long receivedLikeCount = Long.parseLong(parts[2]);
        if (liked && !userId.equals(targetOwnerId)) {
            UserEntity user = userMapper.selectById(userId);
            String sender = user == null ? "Someone" : user.getNickname();
            communityProducer.sendNotification(new NotificationMessage(targetOwnerId, userId, "LIKE", sender + " liked your content", targetId));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        result.put("receivedLikeCount", receivedLikeCount);
        return result;
    }

    public boolean hasLiked(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("like:user:" + userId, targetType + ":" + targetId));
    }
}
