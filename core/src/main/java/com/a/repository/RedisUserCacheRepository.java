package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import com.a.repository.dto.CachedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisUserCacheRepository {

    private static final long TTL_MINUTES = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CachedUser getUser(Long userId) {
        String key = String.format(RedisKeyConstants.USER, userId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, CachedUser.class);
        } catch (Exception e) {
            log.warn("Redis getUser failed for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    public void setUser(CachedUser user) {
        String key = String.format(RedisKeyConstants.USER, user.id());
        try {
            String json = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, json, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("Redis setUser serialization failed for userId={}: {}", user.id(), e.getMessage());
        } catch (Exception e) {
            log.warn("Redis setUser failed for userId={}: {}", user.id(), e.getMessage());
        }
    }

    public void evictUser(Long userId) {
        String key = String.format(RedisKeyConstants.USER, userId);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis evictUser failed for userId={}: {}", userId, e.getMessage());
        }
    }
}
