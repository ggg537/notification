package com.a.auth.dto;

public record OAuthLoginRequest(String code, String redirectUri) {}
