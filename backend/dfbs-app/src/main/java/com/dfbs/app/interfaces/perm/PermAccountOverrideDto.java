package com.dfbs.app.interfaces.perm;

import java.util.List;

/** DTOs for GET/PUT /api/v1/admin/perm/accounts/{userId}/override and user search */
public final class PermAccountOverrideDto {

    /** Minimal user info for account selection (GET /users?query=). */
    public record UserSummary(Long id, String username, String nickname) {}

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

    private PermAccountOverrideDto() {}
}
