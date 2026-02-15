package com.a.comment.service;

import com.a.config.redis.RedisKeyConstants;
import com.a.entity.CommentEntity;
import com.a.event.CommentEvent;
import com.a.event.CommentEventType;
import com.a.repository.CommentJpaRepository;
import com.a.repository.PostRepository;
import com.a.repository.RedisCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentJpaRepository commentJpaRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisCountRepository redisCountRepository;

    public List<CommentEntity> getTopLevelCommentsByPostId(Long postId) {
        return commentJpaRepository.findAllByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId);
    }

    public List<CommentEntity> getReplies(Long parentId) {
        return commentJpaRepository.findAllByParentIdOrderByCreatedAtAsc(parentId);
    }

    public List<CommentEntity> getCommentsByPostId(Long postId) {
        return commentJpaRepository.findAllByPostIdOrderByCreatedAtAsc(postId);
    }

    public long getCommentCount(Long postId) {
        String key = String.format(RedisKeyConstants.COMMENT_COUNT, postId);
        Long cached = redisCountRepository.getCount(key);
        if (cached != null) {
            return cached;
        }
        long count = commentJpaRepository.countByPostId(postId);
        redisCountRepository.setCount(key, count);
        return count;
    }

    public Map<Long, Long> getCommentCountBatch(Collection<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        Map<Long, Long> result = new HashMap<>();
        List<Long> cacheMisses = new ArrayList<>();

        for (Long postId : postIds) {
            String key = String.format(RedisKeyConstants.COMMENT_COUNT, postId);
            Long cached = redisCountRepository.getCount(key);
            if (cached != null) {
                result.put(postId, cached);
            } else {
                cacheMisses.add(postId);
            }
        }

        if (!cacheMisses.isEmpty()) {
            List<Object[]> dbCounts = commentJpaRepository.countByPostIdIn(cacheMisses);
            Map<Long, Long> dbMap = new HashMap<>();
            for (Object[] row : dbCounts) {
                dbMap.put((Long) row[0], (Long) row[1]);
            }
            for (Long postId : cacheMisses) {
                long count = dbMap.getOrDefault(postId, 0L);
                result.put(postId, count);
                redisCountRepository.setCount(
                    String.format(RedisKeyConstants.COMMENT_COUNT, postId), count);
            }
        }
        return result;
    }

    @Transactional
    public CommentEntity createComment(Long postId, Long userId, String content, Long parentId) {
        postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        int depth = 0;
        if (parentId != null) {
            CommentEntity parent = commentJpaRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (parent.getDepth() >= 1) {
                throw new IllegalArgumentException("Cannot reply to a reply (max depth is 1)");
            }
            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }
            depth = 1;
        }

        CommentEntity comment = CommentEntity.builder()
            .postId(postId)
            .userId(userId)
            .content(content)
            .parentId(parentId)
            .depth(depth)
            .build();
        CommentEntity saved = commentJpaRepository.save(comment);

        String countKey = String.format(RedisKeyConstants.COMMENT_COUNT, postId);
        redisCountRepository.increment(countKey);

        CommentEvent event = new CommentEvent();
        event.setType(CommentEventType.ADD);
        event.setPostId(postId);
        event.setUserId(userId);
        event.setCommentId(saved.getId());
        event.setParentId(parentId);
        kafkaTemplate.send("comment", event);

        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommentEntity comment = commentJpaRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }

        int deletedCount = 1;
        // 최상위 댓글인 경우 대댓글도 함께 삭제
        if (comment.getDepth() == 0) {
            List<CommentEntity> replies = commentJpaRepository.findAllByParentIdOrderByCreatedAtAsc(commentId);
            deletedCount += replies.size();
            commentJpaRepository.deleteAll(replies);
        }

        commentJpaRepository.delete(comment);

        String countKey = String.format(RedisKeyConstants.COMMENT_COUNT, comment.getPostId());
        for (int i = 0; i < deletedCount; i++) {
            redisCountRepository.decrement(countKey);
        }

        CommentEvent event = new CommentEvent();
        event.setType(CommentEventType.REMOVE);
        event.setPostId(comment.getPostId());
        event.setUserId(userId);
        event.setCommentId(commentId);
        event.setParentId(comment.getParentId());
        kafkaTemplate.send("comment", event);
    }

    @Transactional
    public CommentEntity updateComment(Long commentId, Long userId, String content) {
        CommentEntity comment = commentJpaRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }
        comment.setContent(content);
        return commentJpaRepository.save(comment);
    }
}
