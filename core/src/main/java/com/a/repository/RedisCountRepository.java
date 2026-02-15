package com.a.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisCountRepository {

    private static final long TTL_HOURS = 1;

    private final RedisTemplate<String, String> redisTemplate;

    public Long getCount(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : null;
        } catch (Exception e) {
            log.warn("Redis getCount failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public void setCount(String key, long count) {
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(count), TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis setCount failed for key={}: {}", key, e.getMessage());
        }
    }

    public void increment(String key) {
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            if (result != null && result == 1) {
                redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.warn("Redis increment failed for key={}: {}", key, e.getMessage());
        }
    }

    public void decrement(String key) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key);
            if (result != null && result < 0) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("Redis decrement failed for key={}: {}", key, e.getMessage());
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis evict failed for key={}: {}", key, e.getMessage());
        }
    }
}
