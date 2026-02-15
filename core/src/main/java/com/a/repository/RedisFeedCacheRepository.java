package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisFeedCacheRepository {

    private static final long TTL_SECONDS = 30;
    private static final TypeReference<List<Long>> LIST_LONG_TYPE = new TypeReference<>() {};

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<Long> getFeedPostIds(Long userId, String tab, int page) {
        String key = String.format(RedisKeyConstants.FEED, userId, tab, page);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, LIST_LONG_TYPE);
        } catch (Exception e) {
            log.warn("Redis getFeedPostIds failed: {}", e.getMessage());
            return null;
        }
    }

    public void setFeedPostIds(Long userId, String tab, int page, List<Long> postIds) {
        String key = String.format(RedisKeyConstants.FEED, userId, tab, page);
        try {
            String json = objectMapper.writeValueAsString(postIds);
            redisTemplate.opsForValue().set(key, json, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("Redis setFeedPostIds serialization failed: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Redis setFeedPostIds failed: {}", e.getMessage());
        }
    }

    public void evictUserFeed(Long userId) {
        try {
            Set<String> keys = redisTemplate.keys("feed:" + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis evictUserFeed failed for userId={}: {}", userId, e.getMessage());
        }
    }

    public void evictAllFeeds() {
        try {
            Set<String> keys = redisTemplate.keys("feed:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis evictAllFeeds failed: {}", e.getMessage());
        }
    }
}
