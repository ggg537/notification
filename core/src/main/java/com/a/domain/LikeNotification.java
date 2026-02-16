package com.a.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("notifications")
@TypeAlias("LikeNotification")
public class LikeNotification extends Notification{

  private final Long postId;
  private final List<Long> likerIds;  // 좋아요를 한 유저 id들


  public LikeNotification(String id, Long userId, NotificationType type, LocalDateTime occurredAt,
      LocalDateTime createdAt, LocalDateTime lastUpdatedAt, LocalDateTime deletedAt, Long postId, List<Long> likerIds) {
    super(id, userId, type, occurredAt, createdAt, lastUpdatedAt, deletedAt);
    this.postId = postId;
    this.likerIds = likerIds;
  }

  public void addLiker(Long likerId, LocalDateTime occuredAt, LocalDateTime now, LocalDateTime retention) {
    this.likerIds.add(likerId);
    this.setOccurredAt(occuredAt);
    this.setLastUpdatedAt(now);
    this.setDeletedAt(retention);   // 유지기한 값
  }

  public void removeLiker(Long userId, LocalDateTime now) {
    this.likerIds.remove(userId);
    this.setLastUpdatedAt(now);
  }
}
