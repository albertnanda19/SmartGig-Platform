package com.smartgig.common.constants;

public final class AppConstants {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}

