package com.a.event;

import java.time.Instant;
import lombok.Data;

@Data
public class LikeEvent {

  private LikeEventType type;    // 이벤트 종료
  private Long postId;              // 어떤 게시글에 댓글이 달렸는지
  private Long userId;              // 누가 작성했는지
  private Instant createdAt;           // 언제 좋아요를 눌렀는지

}
