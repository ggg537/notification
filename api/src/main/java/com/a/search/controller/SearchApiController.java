package com.a.search.controller;

import com.a.entity.PostEntity;
import com.a.entity.UserEntity;
import com.a.follow.service.FollowService;
import com.a.post.dto.PostResponse;
import com.a.post.service.PostEnrichmentService;
import com.a.search.dto.*;
import com.a.search.service.SearchService;
import com.a.common.config.RateLimit;
import com.a.repository.RedisSearchPopularRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchApiController {

    private final SearchService searchService;
    private final PostEnrichmentService postEnrichmentService;
    private final FollowService followService;
    private final RedisSearchPopularRepository redisSearchPopularRepository;

    @GetMapping
    @RateLimit(maxRequests = 30, windowSeconds = 60)
    public ResponseEntity<?> search(
        @RequestParam String q,
        @RequestParam(defaultValue = "posts") String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        // 검색어 추적
        redisSearchPopularRepository.trackSearch(q);

        return switch (type) {
            case "users" -> {
                Page<UserEntity> userPage = searchService.searchUsers(q, page, size);
                List<UserSearchResult> users = userPage.getContent().stream()
                    .map(u -> UserSearchResult.from(u, followService.isFollowing(userId, u.getId())))
                    .toList();
                yield ResponseEntity.ok(new SearchUsersResponse(users, userPage.getNumber(), userPage.hasNext()));
            }
            case "tags" -> {
                var tagPage = searchService.searchTags(q.startsWith("#") ? q.substring(1) : q, page, size);
                List<TrendingTag> tags = tagPage.getContent().stream()
                    .map(t -> new TrendingTag(t.getTag(), t.getPostCount()))
                    .toList();
                yield ResponseEntity.ok(new SearchTagsResponse(tags, tagPage.getNumber(), tagPage.hasNext()));
            }
            default -> {
                Page<PostEntity> postPage = searchService.searchPosts(q, page, size);
                List<PostResponse> posts = postEnrichmentService.enrichPosts(
                    postPage.getContent(), userId);
                yield ResponseEntity.ok(new SearchPostsResponse(posts, postPage.getNumber(), postPage.hasNext()));
            }
        };
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingTag>> getTrendingTags() {
        var tags = searchService.getTrendingTags(10).stream()
            .map(t -> new TrendingTag(t.getTag(), t.getPostCount()))
            .toList();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularSearchResponse> getPopularSearches() {
        List<String> keywords = redisSearchPopularRepository.getPopularSearches(10);
        return ResponseEntity.ok(new PopularSearchResponse(keywords));
    }
}
