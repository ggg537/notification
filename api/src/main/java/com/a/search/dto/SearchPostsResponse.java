package com.a.search.dto;

import com.a.post.dto.PostResponse;
import java.util.List;

public record SearchPostsResponse(List<PostResponse> posts, int page, boolean hasNext) {}
