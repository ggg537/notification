package com.a.event;

import lombok.Data;

@Data
public class CommentEvent {

  private CommentEventType type;    // 이벤트 종료
  private Long postId;              // 어떤 게시글에 댓글이 달렸는지
  private Long userId;              // 누가 작성했는지
  private Long commentId;           // 댓글은 뭔지

}
