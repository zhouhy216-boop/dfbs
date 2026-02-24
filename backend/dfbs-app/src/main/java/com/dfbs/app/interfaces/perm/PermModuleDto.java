package com.dfbs.app.interfaces.perm;

import com.dfbs.app.modules.perm.PermModuleEntity;

/**
 * Request/response DTOs for PERM module management APIs.
 */
public final class PermModuleDto {

    /** parentId null = root node. */
    public record CreateModuleRequest(String moduleKey, String label, Long parentId) {}

    public record UpdateModuleRequest(String label, Long parentId) {}

    public record SetModuleActionsRequest(java.util.List<String> actionKeys) {
        public SetModuleActionsRequest {
            if (actionKeys == null) actionKeys = java.util.List.of();
        }
    }

    public record ModuleResponse(Long id, String moduleKey, String label, Long parentId) {
        public static ModuleResponse from(PermModuleEntity e) {
            return new ModuleResponse(e.getId(), e.getModuleKey(), e.getLabel(), e.getParentId());
        }
    }

    private PermModuleDto() {}
}
