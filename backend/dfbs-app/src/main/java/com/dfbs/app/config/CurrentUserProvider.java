package com.dfbs.app.config;

import org.springframework.stereotype.Component;

/**
 * Provides current user identifier (username or userId string) for the request.
 * Default: "system". When X-User-Id header is present, the filter sets it per-request so PERM and other guards see the real user.
 */
@Component
public class CurrentUserProvider {

    private static final ThreadLocal<String> REQUEST_USER = new ThreadLocal<>();

    public String getCurrentUser() {
        String v = REQUEST_USER.get();
        return (v != null && !v.isBlank()) ? v : "system";
    }

    /** Set by CurrentUserFilter from X-User-Id header; cleared in filter finally. */
    public void setRequestUser(String userId) {
        REQUEST_USER.set(userId);
    }

    public void clearRequestUser() {
        REQUEST_USER.remove();
    }
}
