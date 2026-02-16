package com.a.event;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FollowEvent {

  private FollowEventType type;
  private Long userId;
  private Long targetUserId;
  private LocalDateTime createdAt;

}
