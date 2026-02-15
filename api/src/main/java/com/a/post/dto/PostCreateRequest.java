package com.a.post.dto;

import com.a.entity.PostVisibility;

public record PostCreateRequest(
    String content,
    PostVisibility visibility
) {
}
