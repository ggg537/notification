package com.a.response;

import com.a.service.dto.GetUserNotificationsResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유저 알림 목록 응답")
public class UserNotificationListResponse {

  @Schema(description = "알림 목록")
  private List<UserNotificationResponse> notifications;

  @Schema(description = "다음 페이지 존재 여부")
  private boolean hasNext;

  @Schema(description = "다음 페이지 요청시 전달할 pivot 파라미터")
  private Instant pivot;

  public static UserNotificationListResponse of(GetUserNotificationsResult result) {
    List<UserNotificationResponse> notifications = result.getNotifications().stream()
        .map(UserNotificationResponse::of)
        .toList();

    Instant pivot = (result.isHasNext() && !notifications.isEmpty())
        ? notifications.getLast().getOccurredAt() : null;

    return new UserNotificationListResponse(
        notifications,
        result.isHasNext(),
        pivot
    );
  }
}
