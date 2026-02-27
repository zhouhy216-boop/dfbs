package com.dfbs.app.application.perm;

/**
 * Thrown when current user lacks required permission key. Handler returns 403 with PERM_FORBIDDEN.
 */
public class PermForbiddenException extends RuntimeException {

    public static final String MACHINE_CODE = "PERM_FORBIDDEN";

    public PermForbiddenException(String message) {
        super(message != null ? message : "无权限");
    }
}
