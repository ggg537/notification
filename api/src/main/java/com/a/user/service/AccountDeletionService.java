package com.a.user.service;

import com.a.config.redis.RedisKeyConstants;
import com.a.entity.UserEntity;
import com.a.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkCollectionRepository bookmarkCollectionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final MentionRepository mentionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final RedisUserCacheRepository redisUserCacheRepository;
    private final RedisCountRepository redisCountRepository;
    private final RedisFeedCacheRepository redisFeedCacheRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void deleteAccount(Long userId, String password) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Account already deleted");
        }

        // 비밀번호 검증 (OAuth 전용 사용자는 비밀번호가 없을 수 있음)
        if (user.getPassword() != null) {
            if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        // 사용자 게시글 및 관련 데이터 삭제
        var posts = postRepository.findAllByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged());
        posts.forEach(post -> {
            postHashtagRepository.deleteAllByPostId(post.getId());
            mentionRepository.deleteAllByPostId(post.getId());
        });
        postRepository.deleteAllByUserId(userId);

        // 댓글 삭제
        commentJpaRepository.deleteAllByUserId(userId);

        // 좋아요 삭제
        likeRepository.deleteAllByUserId(userId);

        // 팔로우 관계 삭제
        followRepository.deleteAllByFollowerIdOrFollowingId(userId, userId);

        // 북마크 및 컬렉션 삭제
        bookmarkRepository.deleteAllByUserId(userId);
        bookmarkCollectionRepository.deleteAllByUserId(userId);

        // 토큰 삭제
        refreshTokenRepository.deleteAllByUserId(userId);
        emailVerificationTokenRepository.deleteAllByUserId(userId);
        passwordResetTokenRepository.deleteAllByUserId(userId);

        // 소프트 삭제 처리
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        // Redis 데이터 정리
        cleanupRedisData(userId);
    }

    private void cleanupRedisData(Long userId) {
        try {
            redisRefreshTokenRepository.deleteAllByUserId(userId);
            redisUserCacheRepository.evictUser(userId);
            redisFeedCacheRepository.evictUserFeed(userId);
            redisCountRepository.evict(String.format(RedisKeyConstants.FOLLOW_FOLLOWERS, userId));
            redisCountRepository.evict(String.format(RedisKeyConstants.FOLLOW_FOLLOWING, userId));

            String onlineKey = String.format(RedisKeyConstants.ONLINE, userId);
            redisTemplate.delete(onlineKey);

            String notifKey = String.format(RedisKeyConstants.NOTIFICATION_LAST_READ, userId);
            redisTemplate.delete(notifKey);
        } catch (Exception e) {
            log.warn("Redis cleanup failed for userId={}: {}", userId, e.getMessage());
        }
    }
}
