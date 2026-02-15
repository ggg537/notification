package com.a.event;

import lombok.Data;

@Data
public class CommentEvent {

  private CommentEventType type;
  private Long postId;
  private Long userId;
  private Long commentId;
  private Long parentId;

}
