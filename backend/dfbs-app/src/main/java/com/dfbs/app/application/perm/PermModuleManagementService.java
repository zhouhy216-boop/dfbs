package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermModuleDto;
import com.dfbs.app.modules.perm.PermModuleActionEntity;
import com.dfbs.app.modules.perm.PermModuleActionRepo;
import com.dfbs.app.modules.perm.PermModuleEntity;
import com.dfbs.app.modules.perm.PermModuleRepo;
import com.dfbs.app.modules.perm.PermActionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * PERM module tree management: create/update/delete nodes, set module actions.
 * All action keys must exist in perm_action. Module key must be unique. Delete forbidden if node has children.
 */
@Service
public class PermModuleManagementService {

    private final PermModuleRepo moduleRepo;
    private final PermModuleActionRepo moduleActionRepo;
    private final PermActionRepo actionRepo;

    public PermModuleManagementService(PermModuleRepo moduleRepo,
                                       PermModuleActionRepo moduleActionRepo,
                                       PermActionRepo actionRepo) {
        this.moduleRepo = moduleRepo;
        this.moduleActionRepo = moduleActionRepo;
        this.actionRepo = actionRepo;
    }

    @Transactional
    public PermModuleEntity create(String moduleKey, String label, Long parentId, Boolean enabled) {
        if (moduleKey == null || moduleKey.isBlank()) {
            throw new IllegalArgumentException("moduleKey 不能为空");
        }
        String key = moduleKey.trim();
        if (moduleRepo.existsByModuleKey(key)) {
            throw new ModuleKeyExistsException("模块 key 已存在: " + key);
        }
        if (parentId != null && !moduleRepo.existsById(parentId)) {
            throw new IllegalArgumentException("父级模块不存在");
        }
        PermModuleEntity e = new PermModuleEntity();
        e.setModuleKey(key);
        e.setLabel(label != null && !label.isBlank() ? label.trim() : key);
        e.setParentId(parentId);
        e.setEnabled(enabled != null ? enabled : true);
        return moduleRepo.save(e);
    }

    @Transactional
    public PermModuleEntity update(Long id, String label, Long parentId, Boolean enabled) {
        PermModuleEntity e = moduleRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("模块不存在: id=" + id));
        if (label != null && !label.isBlank()) {
            e.setLabel(label.trim());
        }
        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new IllegalArgumentException("不能将父级设为自己");
            }
            if (!moduleRepo.existsById(parentId)) {
                throw new IllegalArgumentException("父级模块不存在");
            }
            e.setParentId(parentId);
        } else {
            e.setParentId(null);
        }
        if (enabled != null) {
            e.setEnabled(enabled);
        }
        return moduleRepo.save(e);
    }

    @Transactional
    public void delete(Long id) {
        PermModuleEntity e = moduleRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("模块不存在: id=" + id));
        if (moduleRepo.existsByParentId(id)) {
            throw new ModuleHasChildrenException("该模块存在子节点，无法删除");
        }
        moduleActionRepo.deleteAll(moduleActionRepo.findByModuleId(id));
        moduleRepo.delete(e);
    }

    @Transactional
    public void setModuleActions(Long moduleId, List<String> actionKeys) {
        if (!moduleRepo.existsById(moduleId)) {
            throw new IllegalArgumentException("模块不存在: id=" + moduleId);
        }
        if (actionKeys == null) {
            actionKeys = List.of();
        }
        for (String key : actionKeys) {
            if (key == null || key.isBlank()) continue;
            if (!actionRepo.existsByActionKey(key.trim())) {
                throw new ActionKeyNotFoundException("动作不存在: " + key);
            }
        }
        moduleActionRepo.deleteAll(moduleActionRepo.findByModuleId(moduleId));
        List<String> distinctKeys = actionKeys.stream()
                .filter(k -> k != null && !k.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        List<PermModuleActionEntity> toSave = new ArrayList<>();
        for (String k : distinctKeys) {
            if (actionRepo.existsByActionKey(k)) {
                PermModuleActionEntity ma = new PermModuleActionEntity();
                ma.setModuleId(moduleId);
                ma.setActionKey(k);
                toSave.add(ma);
            }
        }
        moduleActionRepo.saveAll(toSave);
    }

    public static final class ModuleKeyExistsException extends RuntimeException {
        public ModuleKeyExistsException(String message) { super(message); }
    }

    public static final class ModuleHasChildrenException extends RuntimeException {
        public ModuleHasChildrenException(String message) { super(message); }
    }

    public static final class ActionKeyNotFoundException extends RuntimeException {
        public ActionKeyNotFoundException(String message) { super(message); }
    }
}
