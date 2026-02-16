package com.a.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자가 알림 목록을 읽은 시간 기록 응답")
public class SetLastReadAtResponse {

  @Schema(description = "기록된 읽은 시간")
  private LocalDateTime lastReadAt;
}
