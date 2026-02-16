package com.a.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("notifications")
@TypeAlias("FollowNotification")
@Getter
public class FollowNotification extends Notification{

  private final Long followerId;    // 팔로우를 신청한 팔로워의 ID

  public FollowNotification(
      String id,
      Long userId,
      NotificationType type,
      LocalDateTime occurredAt,
      LocalDateTime createdAt,
      LocalDateTime lastUpdatedAt,
      LocalDateTime deletedAt,
      Long followerId
  ) {
    super(id, userId, type, occurredAt, createdAt, lastUpdatedAt, deletedAt);
    this.followerId = followerId;
  }
}
