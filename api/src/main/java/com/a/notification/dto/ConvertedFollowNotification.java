package com.a.notification.dto;

import com.a.domain.NotificationType;
import java.time.Instant;
import lombok.Getter;

@Getter
public class ConvertedFollowNotification extends ConvertedNotification {

  private final String userName;
  private final String userProfileImageUrl;
  private final boolean isFollowing;

  public ConvertedFollowNotification(
      String id,
      NotificationType type,
      Instant occurredAt,
      Instant lastUpdatedAt,
      String userName,
      String userProfileImageUrl,
      boolean isFollowing
  ) {
    super(id, type, occurredAt, lastUpdatedAt);
    this.userName = userName;
    this.userProfileImageUrl = userProfileImageUrl;
    this.isFollowing = isFollowing;
  }
}
