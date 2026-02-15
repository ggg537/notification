package com.a.user.controller;

import com.a.client.FileStorageClient;
import com.a.common.dto.MessageResponse;
import com.a.entity.UserEntity;
import com.a.entity.PostEntity;
import com.a.follow.service.FollowService;
import com.a.post.dto.PostPageResponse;
import com.a.post.dto.PostResponse;
import com.a.post.service.PostEnrichmentService;
import com.a.post.service.PostService;
import com.a.repository.RedisOnlineStatusRepository;
import com.a.user.dto.OnlineStatusResponse;
import com.a.user.dto.UserProfileResponse;
import com.a.user.dto.UserUpdateRequest;
import com.a.user.service.UserProfileService;
import com.a.common.config.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserProfileService userProfileService;
    private final PostService postService;
    private final PostEnrichmentService postEnrichmentService;
    private final FollowService followService;
    private final FileStorageClient fileStorageClient;
    private final RedisOnlineStatusRepository redisOnlineStatusRepository;

    @GetMapping("/{userId}")
    @RateLimit(maxRequests = 60, windowSeconds = 60)
    public ResponseEntity<UserProfileResponse> getProfile(
        @PathVariable Long userId,
        Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        UserEntity user = userProfileService.getUserById(userId);
        long postCount = postService.getPostCountByUser(userId);
        long followerCount = followService.getFollowerCount(userId);
        long followingCount = followService.getFollowingCount(userId);
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        boolean isOwnProfile = currentUserId.equals(userId);

        return ResponseEntity.ok(UserProfileResponse.from(
            user, postCount, followerCount, followingCount, isFollowing, isOwnProfile
        ));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<MessageResponse> updateProfile(
        @PathVariable Long userId,
        @RequestBody UserUpdateRequest request,
        Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body(new MessageResponse("Not authorized"));
        }
        userProfileService.updateProfile(userId, request.name(), request.bio(), request.handle());
        return ResponseEntity.ok(new MessageResponse("Updated"));
    }

    @PutMapping("/{userId}/image")
    public ResponseEntity<MessageResponse> updateProfileImage(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body(new MessageResponse("Not authorized"));
        }
        UserEntity user = userProfileService.getUserById(userId);
        if (user.getProfileImageUrl() != null) {
            fileStorageClient.delete(user.getProfileImageUrl());
        }
        String imageUrl = fileStorageClient.store(file, "profiles");
        userProfileService.updateProfileImage(userId, imageUrl);
        return ResponseEntity.ok(new MessageResponse(imageUrl));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<PostPageResponse> getUserPosts(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        Page<PostEntity> postPage = postService.getUserPostsWithVisibility(
            userId, currentUserId, isFollowing, page, size
        );

        List<PostResponse> posts = postEnrichmentService.enrichPosts(
            postPage.getContent(), currentUserId);

        return ResponseEntity.ok(new PostPageResponse(
            posts,
            postPage.getNumber(),
            postPage.getTotalPages(),
            postPage.getTotalElements(),
            postPage.hasNext()
        ));
    }

    @GetMapping("/{userId}/online")
    @RateLimit(maxRequests = 60, windowSeconds = 60)
    public ResponseEntity<OnlineStatusResponse> getOnlineStatus(
        @PathVariable Long userId
    ) {
        boolean online = redisOnlineStatusRepository.isOnline(userId);
        return ResponseEntity.ok(new OnlineStatusResponse(online));
    }
}
