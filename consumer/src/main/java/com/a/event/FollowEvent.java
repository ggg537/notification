package com.a.event;

import java.time.Instant;
import lombok.Data;

@Data
public class FollowEvent {

  private FollowEventType type;    // 이벤트 종료
  private Long userId;              // 누가 작성 했는지
  private Long targetUserId;              // 누구를 팔로우 했는지
  private Instant createdAt;           // 언제 팔로우 했는지

}
