package com.dfbs.app.interfaces.perm;

import com.dfbs.app.modules.perm.PermModuleEntity;

/**
 * Request/response DTOs for PERM module management APIs.
 */
public final class PermModuleDto {

    /** parentId null = root node. enabled optional (default true when absent). */
    public record CreateModuleRequest(String moduleKey, String label, Long parentId, Boolean enabled) {}

    /** enabled optional: if null, keep unchanged. */
    public record UpdateModuleRequest(String label, Long parentId, Boolean enabled) {}

    public record SetModuleActionsRequest(java.util.List<String> actionKeys) {
        public SetModuleActionsRequest {
            if (actionKeys == null) actionKeys = java.util.List.of();
        }
    }

    public record ModuleResponse(Long id, String moduleKey, String label, Long parentId, Boolean enabled) {
        public static ModuleResponse from(PermModuleEntity e) {
            return new ModuleResponse(e.getId(), e.getModuleKey(), e.getLabel(), e.getParentId(),
                    e.getEnabled() != null ? e.getEnabled() : true);
        }
    }

    private PermModuleDto() {}
}
