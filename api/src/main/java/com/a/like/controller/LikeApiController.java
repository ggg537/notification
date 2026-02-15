package com.a.like.controller;

import com.a.like.dto.LikeToggleResponse;
import com.a.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeApiController {

    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<LikeToggleResponse> toggleLike(
        @PathVariable Long postId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        boolean liked = likeService.toggleLike(postId, userId);
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(new LikeToggleResponse(liked, count));
    }
}
