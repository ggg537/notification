package com.a.comment.dto;

public record CommentCreateRequest(Long postId, String content, Long parentId) {
}
