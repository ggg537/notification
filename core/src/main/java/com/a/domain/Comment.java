package com.a.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Comment {

  private Long id;
  private Long userId;
  private String content;
  private LocalDateTime createdAt;

}
