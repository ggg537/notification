package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisRefreshTokenRepository {

    private static final long TTL_DAYS = 7;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveToken(String token, Long userId) {
        try {
            String tokenKey = String.format(RedisKeyConstants.REFRESH_TOKEN, token);
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId), TTL_DAYS, TimeUnit.DAYS);

            String userKey = String.format(RedisKeyConstants.REFRESH_USER, userId);
            redisTemplate.opsForSet().add(userKey, token);
            redisTemplate.expire(userKey, TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis saveToken failed: {}", e.getMessage());
        }
    }

    public Long getUserIdByToken(String token) {
        try {
            String tokenKey = String.format(RedisKeyConstants.REFRESH_TOKEN, token);
            String userId = redisTemplate.opsForValue().get(tokenKey);
            return userId != null ? Long.parseLong(userId) : null;
        } catch (Exception e) {
            log.warn("Redis getUserIdByToken failed: {}", e.getMessage());
            return null;
        }
    }

    public void deleteToken(String token) {
        try {
            String tokenKey = String.format(RedisKeyConstants.REFRESH_TOKEN, token);
            String userIdStr = redisTemplate.opsForValue().get(tokenKey);
            redisTemplate.delete(tokenKey);

            if (userIdStr != null) {
                String userKey = String.format(RedisKeyConstants.REFRESH_USER, Long.parseLong(userIdStr));
                redisTemplate.opsForSet().remove(userKey, token);
            }
        } catch (Exception e) {
            log.warn("Redis deleteToken failed: {}", e.getMessage());
        }
    }

    public void deleteAllByUserId(Long userId) {
        try {
            String userKey = String.format(RedisKeyConstants.REFRESH_USER, userId);
            Set<String> tokens = redisTemplate.opsForSet().members(userKey);
            if (tokens != null) {
                for (String token : tokens) {
                    String tokenKey = String.format(RedisKeyConstants.REFRESH_TOKEN, token);
                    redisTemplate.delete(tokenKey);
                }
            }
            redisTemplate.delete(userKey);
        } catch (Exception e) {
            log.warn("Redis deleteAllByUserId failed for userId={}: {}", userId, e.getMessage());
        }
    }
}
