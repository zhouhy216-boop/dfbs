package com.dfbs.app.interfaces.perm;

import java.util.List;

/** DTOs for GET/PUT /api/v1/admin/perm/accounts/{userId}/override and user search */
public final class PermAccountOverrideDto {

    /** Minimal user info for account selection (GET /users?query=, GET /users/{id}). */
    public record UserSummary(Long id, String username, String nickname, Boolean enabled) {
        public UserSummary(Long id, String username, String nickname) {
            this(id, username, nickname, null);
        }
    }

    /** Request body for POST /accounts (create account by binding person). */
    public record CreateAccountRequest(Long orgPersonId, String username, String nickname, Long roleTemplateId) {}

    /** Response for created account or account summary. */
    public record AccountSummaryResponse(Long id, String username, String nickname, Boolean enabled, Long orgPersonId) {}

    /** Request body for PUT /accounts/{userId}/enabled. */
    public record SetEnabledRequest(boolean enabled) {}

    /** Request body for POST /accounts/{userId}/reset-password. */
    public record ResetPasswordRequest(String newPassword) {}

    /** Response: account override state + computed effective keys (template ∪ add - remove). */
    public record AccountOverrideResponse(
            Long userId,
            Long roleTemplateId,
            String roleTemplateKey,
            List<String> addKeys,
            List<String> removeKeys,
            List<String> effectiveKeys
    ) {}

    /** Request body for PUT: replace-style. */
    public record SaveAccountOverrideRequest(
            Long roleTemplateId,
            List<String> addKeys,
            List<String> removeKeys
    ) {
        public SaveAccountOverrideRequest {
            if (addKeys == null) addKeys = List.of();
            if (removeKeys == null) removeKeys = List.of();
        }
    }

    /** One row for GET /account-list (admin-only account list with position/department/role). */
    public record AccountListItemResponse(
            Long userId,
            String username,
            String nickname,
            Boolean enabled,
            Long orgPersonId,
            String position,
            String department,
            Long roleTemplateId,
            String roleTemplateLabel
    ) {}

    private PermAccountOverrideDto() {}
}
