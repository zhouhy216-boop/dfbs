package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermissionTreeDto;
import com.dfbs.app.modules.perm.PermActionEntity;
import com.dfbs.app.modules.perm.PermActionRepo;
import com.dfbs.app.modules.perm.PermModuleActionRepo;
import com.dfbs.app.modules.perm.PermModuleEntity;
import com.dfbs.app.modules.perm.PermModuleRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * v1 permission model: default actions set + module tree (read-only, persisted).
 * keyFormat: "&lt;moduleKey&gt;:&lt;actionKey&gt;". Extensible via new moduleKey/actionKey in DB.
 */
@Service
public class PermPermissionTreeService {

    private final PermActionRepo actionRepo;
    private final PermModuleRepo moduleRepo;
    private final PermModuleActionRepo moduleActionRepo;

    /** v1 default actions fallback when DB has no actions (e.g. pre-migration). */
    private static final List<PermissionTreeDto.ActionItem> FALLBACK_ACTIONS = List.of(
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

    public PermPermissionTreeService(PermActionRepo actionRepo,
                                    PermModuleRepo moduleRepo,
                                    PermModuleActionRepo moduleActionRepo) {
        this.actionRepo = actionRepo;
        this.moduleRepo = moduleRepo;
        this.moduleActionRepo = moduleActionRepo;
    }

    public PermissionTreeDto.PermissionTreeResponse getPermissionTree() {
        List<PermissionTreeDto.ActionItem> actions = loadActions();
        List<PermissionTreeDto.ModuleNode> modules = loadModuleTree();
        return new PermissionTreeDto.PermissionTreeResponse(
                PermissionTreeDto.KEY_FORMAT,
                actions,
                modules
        );
    }

    private List<PermissionTreeDto.ActionItem> loadActions() {
        List<PermActionEntity> entities = actionRepo.findAllByOrderByIdAsc();
        if (entities.isEmpty()) {
            return FALLBACK_ACTIONS;
        }
        return entities.stream()
                .map(e -> new PermissionTreeDto.ActionItem(e.getActionKey(), e.getLabel()))
                .collect(Collectors.toList());
    }

    private List<PermissionTreeDto.ModuleNode> loadModuleTree() {
        List<PermModuleEntity> roots = moduleRepo.findByParentIdIsNullOrderByIdAsc();
        List<PermissionTreeDto.ModuleNode> result = new ArrayList<>();
        for (PermModuleEntity root : roots) {
            result.add(toModuleNode(root));
        }
        return result;
    }

    private PermissionTreeDto.ModuleNode toModuleNode(PermModuleEntity entity) {
        List<String> actionKeys = moduleActionRepo.findByModuleId(entity.getId()).stream()
                .map(ma -> ma.getActionKey())
                .collect(Collectors.toList());
        List<PermissionTreeDto.ModuleNode> children = moduleRepo.findByParentIdOrderByIdAsc(entity.getId()).stream()
                .map(this::toModuleNode)
                .collect(Collectors.toList());
        Boolean enabled = entity.getEnabled() != null ? entity.getEnabled() : true;
        return new PermissionTreeDto.ModuleNode(
                entity.getModuleKey(), entity.getLabel(), actionKeys, children,
                entity.getId(), entity.getParentId(), enabled);
    }
}
