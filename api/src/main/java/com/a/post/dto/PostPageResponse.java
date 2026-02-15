package com.a.post.dto;

import java.util.List;

public record PostPageResponse(
    List<PostResponse> posts,
    int page,
    int totalPages,
    long totalElements,
    boolean hasNext
) {
}
