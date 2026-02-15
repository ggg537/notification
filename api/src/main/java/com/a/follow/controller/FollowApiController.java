package com.a.follow.controller;

import com.a.follow.dto.FollowToggleResponse;
import com.a.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowApiController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowToggleResponse> toggleFollow(
        @PathVariable Long targetUserId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        boolean following = followService.toggleFollow(userId, targetUserId);
        return ResponseEntity.ok(new FollowToggleResponse(following));
    }
}
