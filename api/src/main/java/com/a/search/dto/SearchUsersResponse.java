package com.a.search.dto;

import java.util.List;

public record SearchUsersResponse(List<UserSearchResult> users, int page, boolean hasNext) {}
