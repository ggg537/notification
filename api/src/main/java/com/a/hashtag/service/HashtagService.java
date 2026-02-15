package com.a.hashtag.service;

import com.a.entity.HashtagEntity;
import com.a.entity.MentionEntity;
import com.a.entity.PostHashtagEntity;
import com.a.entity.UserEntity;
import com.a.repository.HashtagRepository;
import com.a.repository.MentionRepository;
import com.a.repository.PostHashtagRepository;
import com.a.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final MentionRepository mentionRepository;
    private final UserRepository userRepository;

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    public List<String> extractHashtags(String content) {
        if (content == null) return List.of();
        Set<String> tags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return new ArrayList<>(tags);
    }

    public List<String> extractMentions(String content) {
        if (content == null) return List.of();
        Set<String> handles = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            handles.add(matcher.group(1));
        }
        return new ArrayList<>(handles);
    }

    @Transactional
    public void processPostHashtags(Long postId, String content) {
        // 기존 해시태그 제거 전 게시글 수 감소
        List<PostHashtagEntity> existing = postHashtagRepository.findAllByPostId(postId);
        for (PostHashtagEntity ph : existing) {
            hashtagRepository.findById(ph.getHashtagId()).ifPresent(hashtag -> {
                hashtag.setPostCount(Math.max(0, hashtag.getPostCount() - 1));
                hashtagRepository.save(hashtag);
            });
        }
        postHashtagRepository.deleteAllByPostId(postId);

        List<String> tags = extractHashtags(content);
        if (tags.isEmpty()) return;

        for (String tag : tags) {
            HashtagEntity hashtag = hashtagRepository.findByTag(tag)
                .orElseGet(() -> hashtagRepository.save(
                    HashtagEntity.builder().tag(tag).postCount(0).build()
                ));
            hashtag.setPostCount(hashtag.getPostCount() + 1);
            hashtagRepository.save(hashtag);

            postHashtagRepository.save(
                PostHashtagEntity.builder()
                    .postId(postId)
                    .hashtagId(hashtag.getId())
                    .build()
            );
        }
    }

    @Transactional
    public void processPostMentions(Long postId, Long mentionerUserId, String content) {
        mentionRepository.deleteAllByPostId(postId);

        List<String> handles = extractMentions(content);
        if (handles.isEmpty()) return;

        for (String handle : handles) {
            userRepository.findByHandle(handle).ifPresent(user -> {
                if (!user.getId().equals(mentionerUserId)) {
                    mentionRepository.save(
                        MentionEntity.builder()
                            .postId(postId)
                            .mentionedUserId(user.getId())
                            .mentionerUserId(mentionerUserId)
                            .build()
                    );
                }
            });
        }
    }

    @Transactional
    public void removePostHashtags(Long postId) {
        List<PostHashtagEntity> postHashtags = postHashtagRepository.findAllByPostId(postId);
        for (PostHashtagEntity ph : postHashtags) {
            hashtagRepository.findById(ph.getHashtagId()).ifPresent(hashtag -> {
                hashtag.setPostCount(Math.max(0, hashtag.getPostCount() - 1));
                hashtagRepository.save(hashtag);
            });
        }
        postHashtagRepository.deleteAllByPostId(postId);
        mentionRepository.deleteAllByPostId(postId);
    }

    public List<String> getPostTags(Long postId) {
        List<PostHashtagEntity> postHashtags = postHashtagRepository.findAllByPostId(postId);
        if (postHashtags.isEmpty()) return List.of();

        List<Long> hashtagIds = postHashtags.stream()
            .map(PostHashtagEntity::getHashtagId)
            .toList();

        return hashtagRepository.findAllById(hashtagIds).stream()
            .map(HashtagEntity::getTag)
            .collect(Collectors.toList());
    }

    public Map<Long, List<String>> getPostTagsBatch(Collection<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();

        List<PostHashtagEntity> allPostHashtags = postHashtagRepository.findAllByPostIdIn(postIds);
        if (allPostHashtags.isEmpty()) {
            Map<Long, List<String>> empty = new HashMap<>();
            postIds.forEach(id -> empty.put(id, List.of()));
            return empty;
        }

        // postId -> hashtagIds 매핑
        Map<Long, List<Long>> postToHashtagIds = new HashMap<>();
        Set<Long> allHashtagIds = new HashSet<>();
        for (PostHashtagEntity ph : allPostHashtags) {
            postToHashtagIds.computeIfAbsent(ph.getPostId(), k -> new ArrayList<>())
                .add(ph.getHashtagId());
            allHashtagIds.add(ph.getHashtagId());
        }

        // 모든 해시태그를 한 번에 조회
        Map<Long, String> hashtagIdToTag = new HashMap<>();
        hashtagRepository.findAllById(allHashtagIds).forEach(h ->
            hashtagIdToTag.put(h.getId(), h.getTag())
        );

        // 결과 구성
        Map<Long, List<String>> result = new HashMap<>();
        for (Long postId : postIds) {
            List<Long> hashtagIds = postToHashtagIds.getOrDefault(postId, List.of());
            List<String> tags = hashtagIds.stream()
                .map(hashtagIdToTag::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            result.put(postId, tags);
        }
        return result;
    }
}
