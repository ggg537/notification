package com.a.common.dto;

public record RateLimitExceededResponse(
    String message,
    int retryAfterSeconds
) {}
