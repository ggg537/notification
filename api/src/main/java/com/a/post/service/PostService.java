package com.a.post.service;

import com.a.entity.FollowEntity;
import com.a.entity.PostEntity;
import com.a.entity.PostVisibility;
import com.a.hashtag.service.HashtagService;
import com.a.repository.FollowRepository;
import com.a.repository.PostRepository;
import com.a.repository.RedisFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final HashtagService hashtagService;
    private final RedisFeedCacheRepository redisFeedCacheRepository;

    public Page<PostEntity> getFeed(Long userId, int page, int size, String tab) {
        Pageable pageable = PageRequest.of(page, size);
        List<Long> followingIds = followRepository.findAllByFollowerId(userId).stream()
            .map(FollowEntity::getFollowingId)
            .toList();

        return switch (tab) {
            case "following" -> postRepository.findFollowingFeedPosts(userId, followingIds, pageable);
            case "popular" -> postRepository.findPopularFeedPosts(pageable);
            default -> postRepository.findFeedPosts(userId, followingIds, pageable);
        };
    }

    public PostEntity getPost(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    public void checkPostAccess(PostEntity post, Long viewerUserId, boolean isFollowing) {
        if (post.getVisibility() == PostVisibility.PRIVATE && !post.getUserId().equals(viewerUserId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (post.getVisibility() == PostVisibility.FOLLOWERS_ONLY
            && !post.getUserId().equals(viewerUserId) && !isFollowing) {
            throw new IllegalArgumentException("Access denied");
        }
    }

    @Transactional
    public PostEntity createPost(Long userId, String content, String imageUrl, PostVisibility visibility) {
        PostEntity post = PostEntity.builder()
            .userId(userId)
            .content(content)
            .imageUrl(imageUrl)
            .visibility(visibility != null ? visibility : PostVisibility.PUBLIC)
            .build();
        PostEntity saved = postRepository.save(post);

        hashtagService.processPostHashtags(saved.getId(), content);
        hashtagService.processPostMentions(saved.getId(), userId, content);

        redisFeedCacheRepository.evictAllFeeds();

        return saved;
    }

    @Transactional
    public PostEntity updatePost(Long postId, Long userId, String content) {
        PostEntity post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to update this post");
        }
        post.setContent(content);
        PostEntity saved = postRepository.save(post);

        hashtagService.processPostHashtags(postId, content);
        hashtagService.processPostMentions(postId, userId, content);

        return saved;
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this post");
        }
        hashtagService.removePostHashtags(postId);
        postRepository.delete(post);

        redisFeedCacheRepository.evictAllFeeds();
    }

    public Page<PostEntity> getPostsByUser(Long userId, int page, int size) {
        return postRepository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Page<PostEntity> getUserPostsWithVisibility(Long profileUserId, Long viewerUserId, boolean isFollowing, int page, int size) {
        return postRepository.findUserPostsWithVisibility(profileUserId, viewerUserId, isFollowing, PageRequest.of(page, size));
    }

    public long getPostCountByUser(Long userId) {
        return postRepository.countByUserId(userId);
    }
}
