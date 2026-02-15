package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RedisSearchPopularRepository {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RedisTemplate<String, String> redisTemplate;

    public void trackSearch(String query) {
        String normalizedQuery = query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) return;

        String key = getTodayKey();
        try {
            redisTemplate.opsForZSet().incrementScore(key, normalizedQuery, 1);
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis trackSearch failed: {}", e.getMessage());
        }
    }

    public List<String> getPopularSearches(int limit) {
        String key = getTodayKey();
        try {
            Set<ZSetOperations.TypedTuple<String>> results =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
            if (results == null) {
                return Collections.emptyList();
            }
            return results.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .toList();
        } catch (Exception e) {
            log.warn("Redis getPopularSearches failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getTodayKey() {
        return String.format(RedisKeyConstants.SEARCH_POPULAR, LocalDate.now().format(DATE_FORMAT));
    }
}
