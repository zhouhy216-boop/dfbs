package com.dfbs.app.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SuperAdminGuard {

    private final CurrentUserIdResolver userIdResolver;

    public SuperAdminGuard(CurrentUserIdResolver userIdResolver) {
        this.userIdResolver = userIdResolver;
    }

    /** Throws 403 if current user is not Super Admin. Call at start of admin-only endpoints. */
    public void requireSuperAdmin() {
        if (!userIdResolver.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅超管可操作");
        }
    }
}
