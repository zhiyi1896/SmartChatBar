package com.niu.community.cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisJsonCacheService {

    public static final String NULL_MARKER = "__NULL__";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisJsonCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String getRaw(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> CachePayload<T> readPayload(String raw, Class<T> clazz) {
        if (raw == null || NULL_MARKER.equals(raw)) {
            return null;
        }
        try {
            CacheEnvelope envelope = objectMapper.readValue(raw, CacheEnvelope.class);
            T value = objectMapper.treeToValue(envelope.data(), clazz);
            return new CachePayload<>(value, envelope.expireAt().isBefore(LocalDateTime.now()));
        } catch (IOException ex) {
            throw new IllegalStateException("CACHE_DESERIALIZE_FAILED", ex);
        }
    }

    public <T> CachePayload<T> readPayload(String raw, TypeReference<T> typeReference) {
        if (raw == null || NULL_MARKER.equals(raw)) {
            return null;
        }
        try {
            CacheEnvelope envelope = objectMapper.readValue(raw, CacheEnvelope.class);
            T value = objectMapper.readValue(objectMapper.treeAsTokens(envelope.data()), typeReference);
            return new CachePayload<>(value, envelope.expireAt().isBefore(LocalDateTime.now()));
        } catch (IOException ex) {
            throw new IllegalStateException("CACHE_DESERIALIZE_FAILED", ex);
        }
    }

    public void writeWithLogicalExpire(String key, Object value, Duration logicalTtl) {
        CacheEnvelope envelope = new CacheEnvelope(LocalDateTime.now().plus(logicalTtl), objectMapper.valueToTree(value));
        Duration physicalTtl = logicalTtl.plusMinutes(Math.max(5, logicalTtl.toMinutes()));
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(envelope), withJitter(physicalTtl));
        } catch (IOException ex) {
            throw new IllegalStateException("CACHE_SERIALIZE_FAILED", ex);
        }
    }

    public void writeNull(String key, Duration ttl) {
        redisTemplate.opsForValue().set(key, NULL_MARKER, ttl);
    }

    public boolean tryLock(String key, Duration ttl) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String key) {
        redisTemplate.delete(List.of(key));
    }

    public void delete(String... keys) {
        redisTemplate.delete(List.of(keys));
    }

    private Duration withJitter(Duration ttl) {
        long seconds = Math.max(1, ttl.getSeconds());
        long jitter = Math.max(5, Math.min(60, seconds / 5));
        long extra = ThreadLocalRandom.current().nextLong(jitter + 1);
        return ttl.plusSeconds(extra);
    }

    public record CachePayload<T>(T data, boolean expired) {
    }

    private record CacheEnvelope(LocalDateTime expireAt, JsonNode data) {
    }
}