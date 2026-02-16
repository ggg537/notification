package com.a.repository;

import com.a.domain.Notification;
import com.a.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

  Optional<Notification> findById(String id);

  Notification save(Notification notification);

  void deleteById(String id);

  @Query("{ 'type':  ?0, 'commentId': ?1 }")
  Optional<Notification> findByTypeAndCommentId(NotificationType type, Long commentId);

  @Query("{ 'type':  ?0, 'postId': ?1 }")
  Optional<Notification> findByTypeAndPostId(NotificationType type, Long postId);

  @Query("{ 'type':  ?0, 'userId': ?1, 'followerId': ?2 }")
  Optional<Notification> findByTypeAndUserIdAndFollowerId(NotificationType type, Long userId, Long followerId);

  // occurredAt 없는 경우
  Slice<Notification> findAllByUserIdOrderByOccurredAtDesc(long userId, Pageable page);
  Slice<Notification> findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(long userId, LocalDateTime occurredAt, Pageable page);

  Optional<Notification> findFirstByUserIdOrderByLastUpdatedAtDesc(long userId);
}
