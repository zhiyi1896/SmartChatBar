package com.niu.community.stats.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final StringRedisTemplate redisTemplate;

    public StatsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void recordUv(String ip) {
        String key = "uv:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        redisTemplate.opsForHyperLogLog().add(key, ip);
    }

    public Map<String, Long> getUvSummary(int days) {
        Map<String, Long> result = new HashMap<>();
        for (int i = 0; i < days; i++) {
            String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_DATE);
            Long count = redisTemplate.opsForHyperLogLog().size("uv:" + date);
            result.put(date, count == null ? 0L : count);
        }
        return result;
    }
}
