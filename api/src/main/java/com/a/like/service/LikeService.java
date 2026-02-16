package com.a.like.service;

import com.a.config.redis.RedisKeyConstants;
import com.a.entity.LikeEntity;
import com.a.event.LikeEvent;
import com.a.event.LikeEventType;
import com.a.repository.LikeRepository;
import com.a.repository.RedisCountRepository;
import com.a.repository.RedisDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisCountRepository redisCountRepository;
    private final RedisDistributedLockRepository redisDistributedLockRepository;

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (!redisDistributedLockRepository.tryLock(userId, "like", postId)) {
            throw new IllegalStateException("Too many requests, please try again");
        }
        try {
            return doToggleLike(postId, userId);
        } finally {
            redisDistributedLockRepository.unlock(userId, "like", postId);
        }
    }

    private boolean doToggleLike(Long postId, Long userId) {
        Optional<LikeEntity> existing = likeRepository.findByPostIdAndUserId(postId, userId);

        String countKey = String.format(RedisKeyConstants.LIKE_COUNT, postId);

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            redisCountRepository.decrement(countKey);

            LikeEvent event = new LikeEvent();
            event.setType(LikeEventType.REMOVE);
            event.setPostId(postId);
            event.setUserId(userId);
            event.setCreatedAt(LocalDateTime.now());
            kafkaTemplate.send("like", event);

            return false;
        } else {
            LikeEntity like = LikeEntity.builder()
                .postId(postId)
                .userId(userId)
                .build();
            likeRepository.save(like);
            redisCountRepository.increment(countKey);

            LikeEvent event = new LikeEvent();
            event.setType(LikeEventType.ADD);
            event.setPostId(postId);
            event.setUserId(userId);
            event.setCreatedAt(LocalDateTime.now());
            kafkaTemplate.send("like", event);

            return true;
        }
    }

    public boolean isLiked(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public long getLikeCount(Long postId) {
        String key = String.format(RedisKeyConstants.LIKE_COUNT, postId);
        Long cached = redisCountRepository.getCount(key);
        if (cached != null) {
            return cached;
        }
        long count = likeRepository.countByPostId(postId);
        redisCountRepository.setCount(key, count);
        return count;
    }

    public Map<Long, Long> getLikeCountBatch(Collection<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        Map<Long, Long> result = new HashMap<>();
        List<Long> cacheMisses = new ArrayList<>();

        for (Long postId : postIds) {
            String key = String.format(RedisKeyConstants.LIKE_COUNT, postId);
            Long cached = redisCountRepository.getCount(key);
            if (cached != null) {
                result.put(postId, cached);
            } else {
                cacheMisses.add(postId);
            }
        }

        if (!cacheMisses.isEmpty()) {
            List<Object[]> dbCounts = likeRepository.countByPostIdIn(cacheMisses);
            Map<Long, Long> dbMap = new HashMap<>();
            for (Object[] row : dbCounts) {
                dbMap.put((Long) row[0], (Long) row[1]);
            }
            for (Long postId : cacheMisses) {
                long count = dbMap.getOrDefault(postId, 0L);
                result.put(postId, count);
                redisCountRepository.setCount(
                    String.format(RedisKeyConstants.LIKE_COUNT, postId), count);
            }
        }
        return result;
    }

    public Set<Long> getLikedPostIds(Collection<Long> postIds, Long userId) {
        if (postIds.isEmpty()) return Set.of();
        return new HashSet<>(likeRepository.findLikedPostIds(postIds, userId));
    }
}
