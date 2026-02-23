package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermissionTreeDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * v1 permission model: default actions set + minimal module tree (read-only).
 * No DB; constants only. Extensible via new moduleKey/actionKey later.
 */
@Service
public class PermPermissionTreeService {

    /** v1 default common actions (stable keys). */
    private static final List<PermissionTreeDto.ActionItem> DEFAULT_ACTIONS = List.of(
            new PermissionTreeDto.ActionItem("VIEW", "查看"),
            new PermissionTreeDto.ActionItem("CREATE", "创建"),
            new PermissionTreeDto.ActionItem("EDIT", "编辑"),
            new PermissionTreeDto.ActionItem("SUBMIT", "提交"),
            new PermissionTreeDto.ActionItem("APPROVE", "审批"),
            new PermissionTreeDto.ActionItem("REJECT", "拒绝"),
            new PermissionTreeDto.ActionItem("ASSIGN", "分配"),
            new PermissionTreeDto.ActionItem("CLOSE", "关闭"),
            new PermissionTreeDto.ActionItem("DELETE", "删除"),
            new PermissionTreeDto.ActionItem("EXPORT", "导出")
    );

    /** v1 minimal module: platform_application with all default actions. */
    private static final List<String> MODULE_ACTION_KEYS = List.of(
            "VIEW", "CREATE", "EDIT", "SUBMIT", "APPROVE", "REJECT", "ASSIGN", "CLOSE", "DELETE", "EXPORT"
    );

    public PermissionTreeDto.PermissionTreeResponse getPermissionTree() {
        PermissionTreeDto.ModuleNode platformApp = new PermissionTreeDto.ModuleNode(
                "platform_application",
                "平台应用",
                MODULE_ACTION_KEYS,
                List.of()
        );
        return new PermissionTreeDto.PermissionTreeResponse(
                PermissionTreeDto.KEY_FORMAT,
                DEFAULT_ACTIONS,
                List.of(platformApp)
        );
    }
}
