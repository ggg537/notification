package com.a.follow.service;

import com.a.config.redis.RedisKeyConstants;
import com.a.entity.FollowEntity;
import com.a.event.FollowEvent;
import com.a.event.FollowEventType;
import com.a.repository.FollowRepository;
import com.a.repository.RedisCountRepository;
import com.a.repository.RedisDistributedLockRepository;
import com.a.repository.RedisFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisCountRepository redisCountRepository;
    private final RedisDistributedLockRepository redisDistributedLockRepository;
    private final RedisFeedCacheRepository redisFeedCacheRepository;

    @Transactional
    public boolean toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (!redisDistributedLockRepository.tryLock(followerId, "follow", followingId)) {
            throw new IllegalStateException("Too many requests, please try again");
        }
        try {
            return doToggleFollow(followerId, followingId);
        } finally {
            redisDistributedLockRepository.unlock(followerId, "follow", followingId);
        }
    }

    private boolean doToggleFollow(Long followerId, Long followingId) {
        Optional<FollowEntity> existing =
            followRepository.findByFollowerIdAndFollowingId(followerId, followingId);

        String followersKey = String.format(RedisKeyConstants.FOLLOW_FOLLOWERS, followingId);
        String followingKey = String.format(RedisKeyConstants.FOLLOW_FOLLOWING, followerId);

        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            redisCountRepository.decrement(followersKey);
            redisCountRepository.decrement(followingKey);

            FollowEvent event = new FollowEvent();
            event.setType(FollowEventType.REMOVE);
            event.setUserId(followerId);
            event.setTargetUserId(followingId);
            event.setCreatedAt(LocalDateTime.now());
            kafkaTemplate.send("follow", event);

            redisFeedCacheRepository.evictUserFeed(followerId);

            return false;
        } else {
            FollowEntity follow = FollowEntity.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
            followRepository.save(follow);
            redisCountRepository.increment(followersKey);
            redisCountRepository.increment(followingKey);

            FollowEvent event = new FollowEvent();
            event.setType(FollowEventType.ADD);
            event.setUserId(followerId);
            event.setTargetUserId(followingId);
            event.setCreatedAt(LocalDateTime.now());
            kafkaTemplate.send("follow", event);

            redisFeedCacheRepository.evictUserFeed(followerId);

            return true;
        }
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public Set<Long> getFollowingIds(Long followerId, Collection<Long> targetUserIds) {
        if (targetUserIds.isEmpty()) return Set.of();
        return new HashSet<>(followRepository.findFollowingIds(followerId, targetUserIds));
    }

    public long getFollowerCount(Long userId) {
        String key = String.format(RedisKeyConstants.FOLLOW_FOLLOWERS, userId);
        Long cached = redisCountRepository.getCount(key);
        if (cached != null) {
            return cached;
        }
        long count = followRepository.countByFollowingId(userId);
        redisCountRepository.setCount(key, count);
        return count;
    }

    public long getFollowingCount(Long userId) {
        String key = String.format(RedisKeyConstants.FOLLOW_FOLLOWING, userId);
        Long cached = redisCountRepository.getCount(key);
        if (cached != null) {
            return cached;
        }
        long count = followRepository.countByFollowerId(userId);
        redisCountRepository.setCount(key, count);
        return count;
    }
}
