package com.a.comment.controller;

import com.a.comment.dto.CommentCreateRequest;
import com.a.comment.service.CommentService;
import com.a.common.dto.MessageResponse;
import com.a.entity.CommentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> createComment(
        @RequestBody CommentCreateRequest request,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CommentEntity comment = commentService.createComment(
            request.postId(), userId, request.content(), request.parentId()
        );
        return ResponseEntity.ok(Map.of("id", comment.getId()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
        @PathVariable Long commentId,
        Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(new MessageResponse("Deleted"));
    }
}
