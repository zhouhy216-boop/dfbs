package com.dfbs.app.interfaces.perm;

import java.util.List;

/**
 * DTOs for GET /api/v1/admin/perm/permission-tree (v1 read-only contract).
 * Permission key format: "&lt;moduleKey&gt;:&lt;actionKey&gt;" (stable, extensible).
 * Data source: perm_action (v1 default actions seeded), perm_module + perm_module_action (tree; modules empty by default).
 */
public final class PermissionTreeDto {

    public static final String KEY_FORMAT = "<moduleKey>:<actionKey>";

    /** Single action in the default actions set. */
    public record ActionItem(String key, String label) {}

    /** Module node in the permission tree. id/parentId/enabled allow UI to edit all modules. */
    public record ModuleNode(
            String key,
            String label,
            List<String> actions,
            List<ModuleNode> children,
            Long id,
            Long parentId,
            Boolean enabled
    ) {}

    /** Root response of permission-tree. */
    public record PermissionTreeResponse(
            String keyFormat,
            List<ActionItem> actions,
            List<ModuleNode> modules
    ) {}

    private PermissionTreeDto() {}
}
