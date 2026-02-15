package com.a.event;

import java.time.Instant;
import lombok.Data;

@Data
public class FollowEvent {

  private FollowEventType type;
  private Long userId;
  private Long targetUserId;
  private Instant createdAt;

}
