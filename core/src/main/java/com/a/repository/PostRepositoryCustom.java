package com.a.repository;

import com.a.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<PostEntity> findFeedPosts(Long userId, List<Long> followingIds, Pageable pageable);
    Page<PostEntity> findFollowingFeedPosts(Long userId, List<Long> followingIds, Pageable pageable);
    Page<PostEntity> findPopularFeedPosts(Pageable pageable);
    Page<PostEntity> findUserPostsWithVisibility(Long profileUserId, Long viewerUserId, boolean isFollowing, Pageable pageable);
    Page<PostEntity> searchPosts(String keyword, Pageable pageable);
}
