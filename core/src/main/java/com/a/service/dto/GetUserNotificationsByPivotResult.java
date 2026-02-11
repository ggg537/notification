package com.a.service.dto;

import com.a.domain.Notification;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetUserNotificationsByPivotResult {

  private List<Notification> notifications;
  private boolean hasNext;
}
