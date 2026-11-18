package com.loyalty.common.constants;

public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    
    // Gateway forwarded headers
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_CUSTOMER_ID = "X-Customer-Id";
    public static final String HEADER_CUSTOMER_NUMBER = "X-Customer-Number";

    // Standard Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STAFF = "ROLE_STAFF";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    // JWT Secret Key (256-bit secret for HMAC-SHA256, overridable by environment variable)
    public static final String DEFAULT_JWT_SECRET = "EnterpriseLoyaltyAndRewardsManagementPlatformSuperSecretKey2026!#*";
    public static final long JWT_EXPIRATION_MS = 86400000L; // 24 hours
}
