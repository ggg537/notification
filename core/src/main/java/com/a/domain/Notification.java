package com.a.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Setter
@Getter
@AllArgsConstructor
@Document("notifications")
public abstract class Notification {

  @Field(targetType = FieldType.STRING)
  public String id;
  public Long userId;
  public NotificationType type;

  public Instant occurredAt;           // 알림 대상인 실제 이벤트가 발생한 시간
  public Instant createdAt;             // 알림 생성된 시간
  public Instant lastUpdatedAt;
  public Instant deletedAt;           // 알림이 삭제될 시간
}