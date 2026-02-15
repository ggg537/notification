package com.a.post.dto;

public record PostStatsResponse(
    long likeCount,
    long commentCount,
    boolean liked
) {
}
