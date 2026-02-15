package com.a.post.controller;

import com.a.client.FileStorageClient;
import com.a.comment.dto.CommentResponse;
import com.a.comment.service.CommentService;
import com.a.common.dto.MessageResponse;
import com.a.entity.CommentEntity;
import com.a.entity.PostEntity;
import com.a.entity.PostVisibility;
import com.a.entity.UserEntity;
import com.a.follow.service.FollowService;
import com.a.hashtag.service.HashtagService;
import com.a.like.service.LikeService;
import com.a.post.dto.*;
import com.a.post.service.PostEnrichmentService;
import com.a.post.service.PostService;
import com.a.repository.BookmarkRepository;
import com.a.user.service.UserProfileService;
import com.a.common.config.RateLimit;
import com.a.repository.RedisFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;
    private final PostEnrichmentService postEnrichmentService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final FollowService followService;
    private final UserProfileService userProfileService;
    private final BookmarkRepository bookmarkRepository;
    private final FileStorageClient fileStorageClient;
    private final HashtagService hashtagService;
    private final RedisFeedCacheRepository redisFeedCacheRepository;

    @GetMapping
    @RateLimit(maxRequests = 60, windowSeconds = 60)
    public ResponseEntity<PostPageResponse> getFeed(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "all") String tab,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostEntity> postPage = postService.getFeed(userId, page, size, tab);

        // 캐시에 피드 게시글 ID 저장
        List<Long> postIds = postPage.getContent().stream()
            .map(PostEntity::getId)
            .toList();
        redisFeedCacheRepository.setFeedPostIds(userId, tab, page, postIds);

        List<PostResponse> posts = postEnrichmentService.enrichPosts(
            postPage.getContent(), userId);

        return ResponseEntity.ok(new PostPageResponse(
            posts,
            postPage.getNumber(),
            postPage.getTotalPages(),
            postPage.getTotalElements(),
            postPage.hasNext()
        ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        PostEntity post = postService.getPost(postId);
        boolean isFollowing = followService.isFollowing(userId, post.getUserId());
        postService.checkPostAccess(post, userId, isFollowing);

        UserEntity author = userProfileService.getUserById(post.getUserId());
        long likeCount = likeService.getLikeCount(postId);
        long commentCount = commentService.getCommentCount(postId);
        boolean liked = likeService.isLiked(postId, userId);
        boolean bookmarked = bookmarkRepository.existsByUserIdAndPostId(userId, postId);
        boolean isOwner = post.getUserId().equals(userId);
        List<String> tags = hashtagService.getPostTags(postId);

        return ResponseEntity.ok(PostDetailResponse.from(
            post, author.getName(), author.getHandle(), author.getProfileImageUrl(),
            likeCount, commentCount, liked, bookmarked, isOwner, tags
        ));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimit(maxRequests = 30, windowSeconds = 60)
    public ResponseEntity<Map<String, Long>> createPostWithImage(
        @RequestParam("content") String content,
        @RequestParam(value = "visibility", required = false) PostVisibility visibility,
        @RequestParam(value = "image", required = false) MultipartFile image,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageClient.store(image, "posts");
        }
        PostEntity post = postService.createPost(userId, content, imageUrl, visibility);
        return ResponseEntity.ok(Map.of("id", post.getId()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @RateLimit(maxRequests = 30, windowSeconds = 60)
    public ResponseEntity<Map<String, Long>> createPost(
        @RequestBody PostCreateRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        PostEntity post = postService.createPost(userId, request.content(), null, request.visibility());
        return ResponseEntity.ok(Map.of("id", post.getId()));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<MessageResponse> updatePost(
        @PathVariable Long postId,
        @RequestBody PostUpdateRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        postService.updatePost(postId, userId, request.content());
        return ResponseEntity.ok(new MessageResponse("Updated"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<MessageResponse> deletePost(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(new MessageResponse("Deleted"));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<CommentEntity> topLevel = commentService.getTopLevelCommentsByPostId(postId);
        Map<Long, UserEntity> cache = new HashMap<>();

        List<CommentResponse> result = topLevel.stream()
            .map(c -> {
                UserEntity author = cache.computeIfAbsent(c.getUserId(), id -> userProfileService.getUserById(id));
                List<CommentEntity> replies = commentService.getReplies(c.getId());
                List<CommentResponse> replyResponses = replies.stream()
                    .map(r -> {
                        UserEntity replyAuthor = cache.computeIfAbsent(r.getUserId(), id -> userProfileService.getUserById(id));
                        return CommentResponse.from(
                            r, replyAuthor.getName(), replyAuthor.getHandle(), replyAuthor.getProfileImageUrl(),
                            r.getUserId().equals(userId), List.of()
                        );
                    })
                    .toList();
                return CommentResponse.from(
                    c, author.getName(), author.getHandle(), author.getProfileImageUrl(),
                    c.getUserId().equals(userId), replyResponses
                );
            })
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<Long, PostStatsResponse>> getPostStats(
        @RequestParam List<Long> ids,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Map<Long, Long> likeCounts = likeService.getLikeCountBatch(ids);
        Map<Long, Long> commentCounts = commentService.getCommentCountBatch(ids);
        Set<Long> likedPostIds = likeService.getLikedPostIds(ids, userId);

        Map<Long, PostStatsResponse> stats = new HashMap<>();
        for (Long postId : ids) {
            stats.put(postId, new PostStatsResponse(
                likeCounts.getOrDefault(postId, 0L),
                commentCounts.getOrDefault(postId, 0L),
                likedPostIds.contains(postId)
            ));
        }
        return ResponseEntity.ok(stats);
    }
}
