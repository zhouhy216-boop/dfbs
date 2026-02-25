package com.dfbs.app.interfaces.perm;

import com.dfbs.app.modules.perm.PermRoleEntity;

/**
 * Request/response DTOs for PERM role and role-permission APIs.
 */
public final class PermRoleDto {

    public record CreateRoleRequest(String roleKey, String label, Boolean enabled) {}

    public record UpdateRoleRequest(String label, Boolean enabled) {}

    /** Body for PUT /roles/{id}/template — atomic save label + enabled + permissions. */
    public record TemplateUpdateRequest(String label, Boolean enabled, java.util.List<String> permissionKeys) {
        public TemplateUpdateRequest {
            if (permissionKeys == null) permissionKeys = java.util.List.of();
        }
    }

    public record RoleResponse(Long id, String roleKey, String label, Boolean enabled) {
        public static RoleResponse from(PermRoleEntity e) {
            return new RoleResponse(e.getId(), e.getRoleKey(), e.getLabel(),
                    e.getEnabled() != null ? e.getEnabled() : true);
        }
    }

    public record RolePermissionsResponse(java.util.List<String> permissionKeys) {}

    public record SetRolePermissionsRequest(java.util.List<String> permissionKeys) {
        public SetRolePermissionsRequest {
            if (permissionKeys == null) permissionKeys = java.util.List.of();
        }
    }

    private PermRoleDto() {}
}
