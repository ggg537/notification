package com.a.notification.dto;

import com.a.domain.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ConvertedNotification {
  protected String id;
  protected NotificationType type;
  protected LocalDateTime occurredAt;
  protected LocalDateTime lastUpdatedAt;
}
