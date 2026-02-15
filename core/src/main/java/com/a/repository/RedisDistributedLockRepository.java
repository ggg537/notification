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
public class RedisDistributedLockRepository {

    private static final long LOCK_TTL_SECONDS = 2;

    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(Long userId, String action, Long targetId) {
        String key = String.format(RedisKeyConstants.LOCK, userId, action, targetId);
        try {
            Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Redis tryLock failed for key={}: {}", key, e.getMessage());
            return true; // fail-open: DB unique constraint protects
        }
    }

    public void unlock(Long userId, String action, Long targetId) {
        String key = String.format(RedisKeyConstants.LOCK, userId, action, targetId);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis unlock failed for key={}: {}", key, e.getMessage());
        }
    }
}
