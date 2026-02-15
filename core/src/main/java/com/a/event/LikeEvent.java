package com.a.event;

import java.time.Instant;
import lombok.Data;

@Data
public class LikeEvent {

  private LikeEventType type;
  private Long postId;
  private Long userId;
  private Instant createdAt;

}
