package com.a.post.service;

import com.a.comment.service.CommentService;
import com.a.entity.PostEntity;
import com.a.entity.UserEntity;
import com.a.follow.service.FollowService;
import com.a.hashtag.service.HashtagService;
import com.a.like.service.LikeService;
import com.a.post.dto.PostResponse;
import com.a.repository.BookmarkRepository;
import com.a.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostEnrichmentService {

    private final LikeService likeService;
    private final CommentService commentService;
    private final FollowService followService;
    private final UserProfileService userProfileService;
    private final BookmarkRepository bookmarkRepository;
    private final HashtagService hashtagService;

    public List<PostResponse> enrichPosts(List<PostEntity> posts, Long currentUserId) {
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(PostEntity::getId).toList();
        Set<Long> authorIds = posts.stream().map(PostEntity::getUserId).collect(Collectors.toSet());

        // 배치 조회: 좋아요 수, 댓글 수, 좋아요 여부, 북마크 여부, 태그, 팔로우 여부
        Map<Long, Long> likeCounts = likeService.getLikeCountBatch(postIds);
        Map<Long, Long> commentCounts = commentService.getCommentCountBatch(postIds);
        Set<Long> likedPostIds = likeService.getLikedPostIds(postIds, currentUserId);
        Set<Long> bookmarkedPostIds = new HashSet<>(
            bookmarkRepository.findBookmarkedPostIds(currentUserId, postIds));
        Map<Long, List<String>> tagsByPost = hashtagService.getPostTagsBatch(postIds);

        // 작성자가 현재 유저가 아닌 경우만 팔로우 조회
        Set<Long> otherAuthorIds = authorIds.stream()
            .filter(id -> !id.equals(currentUserId))
            .collect(Collectors.toSet());
        Set<Long> followingIds = followService.getFollowingIds(currentUserId, otherAuthorIds);

        // 작성자 정보 배치 조회
        Map<Long, UserEntity> authorMap = new HashMap<>();
        for (Long authorId : authorIds) {
            authorMap.computeIfAbsent(authorId, id -> userProfileService.getUserById(id));
        }

        return posts.stream()
            .map(post -> {
                Long postId = post.getId();
                UserEntity author = authorMap.get(post.getUserId());
                boolean isOwnPost = post.getUserId().equals(currentUserId);
                return PostResponse.from(
                    post,
                    author.getName(),
                    author.getHandle(),
                    author.getProfileImageUrl(),
                    likeCounts.getOrDefault(postId, 0L),
                    commentCounts.getOrDefault(postId, 0L),
                    likedPostIds.contains(postId),
                    bookmarkedPostIds.contains(postId),
                    !isOwnPost && followingIds.contains(post.getUserId()),
                    isOwnPost,
                    tagsByPost.getOrDefault(postId, List.of())
                );
            })
            .toList();
    }
}
