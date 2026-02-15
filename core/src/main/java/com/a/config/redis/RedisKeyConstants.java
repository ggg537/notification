package com.a.config.redis;

public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    // Count caching
    public static final String LIKE_COUNT = "like:count:%d";
    public static final String COMMENT_COUNT = "comment:count:%d";
    public static final String FOLLOW_FOLLOWERS = "follow:followers:%d";
    public static final String FOLLOW_FOLLOWING = "follow:following:%d";

    // Feed caching
    public static final String FEED = "feed:%d:%s:%d";

    // User caching
    public static final String USER = "user:%d";

    // Rate limiting
    public static final String RATE_LIMIT = "ratelimit:%d:%s";

    // Popular search
    public static final String SEARCH_POPULAR = "search:popular:%s";

    // Refresh token
    public static final String REFRESH_TOKEN = "refresh:token:%s";
    public static final String REFRESH_USER = "refresh:user:%d";

    // Online status
    public static final String ONLINE = "online:%d";

    // Distributed lock
    public static final String LOCK = "lock:%d:%s:%d";

    // Notification lastReadAt
    public static final String NOTIFICATION_LAST_READ = "notification:lastread:%d";
}
