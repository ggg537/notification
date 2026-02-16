package com.a.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("notifications")
@TypeAlias("CommentNotification")
public class CommentNotification extends Notification{

  private final Long postId;
  private final Long writerId;
  private final String comment;
  private final Long commentId;

  public CommentNotification(
      String id,
      Long userId,
      NotificationType type,
      LocalDateTime occurredAt,
      LocalDateTime createdAt,
      LocalDateTime lastUpdatedAt,
      LocalDateTime deletedAt,
      Long postId,
      Long writerId,
      String  comment,
      Long commentId
  ) {
    super(id, userId, type, occurredAt, createdAt, lastUpdatedAt, deletedAt);
    this.postId = postId;
    this.writerId = writerId;
    this. comment =  comment;
    this. commentId =  commentId;

  }
}
