package com.a.search.dto;

import java.util.List;

public record SearchTagsResponse(List<TrendingTag> tags, int page, boolean hasNext) {}
