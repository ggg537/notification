package com.a.user.service;

import com.a.entity.UserEntity;
import com.a.repository.RedisUserCacheRepository;
import com.a.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final RedisUserCacheRepository redisUserCacheRepository;

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public UserEntity updateProfile(Long userId, String name, String bio, String handle) {
        UserEntity user = getUserById(userId);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (handle != null && !handle.isBlank()) {
            userRepository.findByHandle(handle).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new IllegalArgumentException("Handle already taken: " + handle);
                }
            });
            user.setHandle(handle);
        }
        UserEntity saved = userRepository.save(user);
        redisUserCacheRepository.evictUser(userId);
        return saved;
    }

    @Transactional
    public UserEntity updateProfileImage(Long userId, String profileImageUrl) {
        UserEntity user = getUserById(userId);
        user.setProfileImageUrl(profileImageUrl);
        UserEntity saved = userRepository.save(user);
        redisUserCacheRepository.evictUser(userId);
        return saved;
    }
}
