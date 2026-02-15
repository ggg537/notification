package com.a.search.dto;

import java.util.List;

public record PopularSearchResponse(
    List<String> keywords
) {}
