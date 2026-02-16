package com.a.repository;

import com.a.config.redis.RedisKeyConstants;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NotificationReadRepository {

  private final RedisTemplate<String, String> redisTemplate;

  public LocalDateTime setLastReadAt(long userId) {
    long lastReadAt = System.currentTimeMillis();
    String key = getKey(userId);
    try {
      redisTemplate.opsForValue().set(key, String.valueOf(lastReadAt));
      redisTemplate.expire(key, 90, TimeUnit.DAYS);
    } catch (Exception e) {
      log.warn("Redis setLastReadAt failed for userId={}: {}", userId, e.getMessage());
    }
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastReadAt), ZoneId.systemDefault());
  }

  public LocalDateTime getLastReadAt(long userId) {
    String key = getKey(userId);
    try {
      String lastReadAtStr = redisTemplate.opsForValue().get(key);
      if (lastReadAtStr == null) {
        return null;
      }
      long lastReadAtLong = Long.parseLong(lastReadAtStr);
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastReadAtLong), ZoneId.systemDefault());
    } catch (Exception e) {
      log.warn("Redis getLastReadAt failed for userId={}: {}", userId, e.getMessage());
      return null;
    }
  }

  private String getKey(long userId) {
    return String.format(RedisKeyConstants.NOTIFICATION_LAST_READ, userId);
  }
}
