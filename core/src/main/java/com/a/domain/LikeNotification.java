package com.a.domain;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;

@Getter
@TypeAlias("LikeNotification")
public class LikeNotification extends Notification{

  private final Long postId;
  private final List<Long> likerIds;  // 좋아요를 한 유저 id들


  public LikeNotification(String id, Long userId, NotificationType type, Instant occurredAt,
      Instant createdAt, Instant lastUpdatedAt, Instant deletedAt, Long postId, List<Long> likerIds) {
    super(id, userId, type, occurredAt, createdAt, lastUpdatedAt, deletedAt);
    this.postId = postId;
    this.likerIds = likerIds;
  }

  public void addLiker(Long likerId, Instant occuredAt, Instant now, Instant retention) {
    this.likerIds.add(likerId);
    this.setOccurredAt(occuredAt);
    this.setLastUpdatedAt(now);
    this.setDeletedAt(retention);   // 유지기한 값
  }

  public void removeLiker(Long userId, Instant now) {
    this.likerIds.remove(userId);
    this.setLastUpdatedAt(now);
  }
}
