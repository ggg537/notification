package com.a.search.service;

import com.a.entity.HashtagEntity;
import com.a.entity.PostEntity;
import com.a.entity.PostHashtagEntity;
import com.a.entity.UserEntity;
import com.a.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;

    public Page<PostEntity> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 키워드가 #으로 시작하면 해시태그로 검색
        if (keyword.startsWith("#")) {
            String tag = keyword.substring(1).toLowerCase();
            return searchPostsByHashtag(tag, pageable);
        }

        return postRepository.searchPosts(keyword, pageable);
    }

    public Page<UserEntity> searchUsers(String keyword, int page, int size) {
        return userRepository.searchByNameOrHandle(keyword, PageRequest.of(page, size));
    }

    public Page<HashtagEntity> searchTags(String keyword, int page, int size) {
        List<HashtagEntity> tags = hashtagRepository.findByTagContainingIgnoreCase(keyword, PageRequest.of(page, size));
        return new PageImpl<>(tags, PageRequest.of(page, size), tags.size());
    }

    public List<HashtagEntity> getTrendingTags(int limit) {
        return hashtagRepository.findAllByOrderByPostCountDesc(PageRequest.of(0, limit));
    }

    private Page<PostEntity> searchPostsByHashtag(String tag, Pageable pageable) {
        return hashtagRepository.findByTag(tag)
            .map(hashtag -> {
                List<PostHashtagEntity> postHashtags = postHashtagRepository.findAllByHashtagId(hashtag.getId());
                List<Long> postIds = postHashtags.stream()
                    .map(PostHashtagEntity::getPostId)
                    .collect(Collectors.toList());
                if (postIds.isEmpty()) {
                    return new PageImpl<PostEntity>(List.of(), pageable, 0);
                }
                List<PostEntity> posts = postRepository.findAllById(postIds);
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), posts.size());
                if (start >= posts.size()) {
                    return new PageImpl<PostEntity>(List.of(), pageable, posts.size());
                }
                return new PageImpl<>(posts.subList(start, end), pageable, posts.size());
            })
            .orElse(new PageImpl<>(List.of(), pageable, 0));
    }
}
