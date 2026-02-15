package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisOnlineStatusRepository {

    private static final long TTL_SECONDS = 60;

    private final RedisTemplate<String, String> redisTemplate;

    public void markOnline(Long userId) {
        String key = String.format(RedisKeyConstants.ONLINE, userId);
        try {
            redisTemplate.opsForValue().set(key, "1", TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis markOnline failed for userId={}: {}", userId, e.getMessage());
        }
    }

    public boolean isOnline(Long userId) {
        String key = String.format(RedisKeyConstants.ONLINE, userId);
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis isOnline failed for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }
}
