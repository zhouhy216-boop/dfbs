package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermRoleDto;
import com.dfbs.app.modules.perm.PermRoleEntity;
import com.dfbs.app.modules.perm.PermRolePermissionEntity;
import com.dfbs.app.modules.perm.PermRolePermissionRepo;
import com.dfbs.app.modules.perm.PermRoleRepo;
import com.dfbs.app.modules.perm.PermModuleRepo;
import com.dfbs.app.modules.perm.PermActionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PERM role CRUD and role-permission assignment. permission_key format: &lt;moduleKey&gt;:&lt;actionKey&gt;.
 * Validation: roleKey non-empty unique; assigned permission keys must have moduleKey in perm_module and actionKey in perm_action.
 */
@Service
public class PermRoleService {

    private final PermRoleRepo roleRepo;
    private final PermRolePermissionRepo rolePermissionRepo;
    private final PermModuleRepo moduleRepo;
    private final PermActionRepo actionRepo;

    public PermRoleService(PermRoleRepo roleRepo,
                           PermRolePermissionRepo rolePermissionRepo,
                           PermModuleRepo moduleRepo,
                           PermActionRepo actionRepo) {
        this.roleRepo = roleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.moduleRepo = moduleRepo;
        this.actionRepo = actionRepo;
    }

    public List<PermRoleEntity> list(Boolean enabledOnly) {
        if (Boolean.TRUE.equals(enabledOnly)) {
            return roleRepo.findByEnabledTrueOrderByIdAsc();
        }
        return roleRepo.findAllByOrderByIdAsc();
    }

    @Transactional
    public PermRoleEntity create(String roleKey, String label, Boolean enabled) {
        if (roleKey == null || roleKey.isBlank()) {
            throw new IllegalArgumentException("roleKey 不能为空");
        }
        String key = roleKey.trim();
        if (roleRepo.existsByRoleKey(key)) {
            throw new RoleKeyExistsException("角色 key 已存在: " + key);
        }
        PermRoleEntity e = new PermRoleEntity();
        e.setRoleKey(key);
        e.setLabel(label != null && !label.isBlank() ? label.trim() : key);
        e.setEnabled(enabled == null || enabled);
        return roleRepo.save(e);
    }

    @Transactional
    public PermRoleEntity update(Long id, String label, Boolean enabled) {
        PermRoleEntity e = roleRepo.findById(id).orElseThrow(() -> new RoleNotFoundException("角色不存在: id=" + id));
        if (label != null && !label.isBlank()) {
            e.setLabel(label.trim());
        }
        if (enabled != null) {
            e.setEnabled(enabled);
        }
        return roleRepo.save(e);
    }

    @Transactional
    public PermRoleEntity saveTemplate(Long id, String label, Boolean enabled, List<String> permissionKeys) {
        PermRoleEntity role = roleRepo.findById(id).orElseThrow(() -> new RoleNotFoundException("角色不存在: id=" + id));
        if (permissionKeys == null) {
            permissionKeys = List.of();
        }
        for (String key : permissionKeys) {
            if (key == null || key.isBlank()) continue;
            validatePermissionKey(key.trim());
        }
        if (label != null && !label.isBlank()) {
            role.setLabel(label.trim());
        }
        if (enabled != null) {
            role.setEnabled(enabled);
        }
        roleRepo.save(role);
        rolePermissionRepo.deleteByRoleId(id);
        List<PermRolePermissionEntity> toSave = new ArrayList<>();
        for (String key : permissionKeys.stream().filter(k -> k != null && !k.isBlank()).map(String::trim).distinct().toList()) {
            PermRolePermissionEntity p = new PermRolePermissionEntity();
            p.setRoleId(id);
            p.setPermissionKey(key);
            toSave.add(p);
        }
        rolePermissionRepo.saveAll(toSave);
        return role;
    }

    @Transactional
    public void delete(Long id) {
        PermRoleEntity e = roleRepo.findById(id).orElseThrow(() -> new RoleNotFoundException("角色不存在: id=" + id));
        rolePermissionRepo.deleteByRoleId(id);
        roleRepo.delete(e);
    }

    public List<String> getPermissions(Long roleId) {
        if (!roleRepo.existsById(roleId)) {
            throw new RoleNotFoundException("角色不存在: id=" + roleId);
        }
        return rolePermissionRepo.findByRoleId(roleId).stream()
                .map(PermRolePermissionEntity::getPermissionKey)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setPermissions(Long roleId, List<String> permissionKeys) {
        PermRoleEntity role = roleRepo.findById(roleId).orElseThrow(() -> new RoleNotFoundException("角色不存在: id=" + roleId));
        if (permissionKeys == null) {
            permissionKeys = List.of();
        }
        for (String key : permissionKeys) {
            if (key == null || key.isBlank()) continue;
            validatePermissionKey(key.trim());
        }
        rolePermissionRepo.deleteByRoleId(roleId);
        List<PermRolePermissionEntity> toSave = new ArrayList<>();
        for (String key : permissionKeys.stream().filter(k -> k != null && !k.isBlank()).map(String::trim).distinct().toList()) {
            PermRolePermissionEntity p = new PermRolePermissionEntity();
            p.setRoleId(roleId);
            p.setPermissionKey(key);
            toSave.add(p);
        }
        rolePermissionRepo.saveAll(toSave);
    }

    /** Public for use by PermAccountOverrideService; validates moduleKey:actionKey and existence. */
    public void validatePermissionKey(String permissionKey) {
        int colon = permissionKey.indexOf(':');
        if (colon <= 0 || colon == permissionKey.length() - 1) {
            throw new InvalidPermissionKeyException("权限 key 格式须为 moduleKey:actionKey: " + permissionKey);
        }
        String moduleKey = permissionKey.substring(0, colon);
        String actionKey = permissionKey.substring(colon + 1);
        if (!moduleRepo.existsByModuleKey(moduleKey)) {
            throw new InvalidPermissionKeyException("模块不存在: " + moduleKey);
        }
        if (!actionRepo.existsByActionKey(actionKey)) {
            throw new InvalidPermissionKeyException("动作不存在: " + actionKey);
        }
    }

    public static final class RoleKeyExistsException extends RuntimeException {
        public RoleKeyExistsException(String message) { super(message); }
    }

    public static final class InvalidPermissionKeyException extends RuntimeException {
        public InvalidPermissionKeyException(String message) { super(message); }
    }

    public static final class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException(String message) { super(message); }
    }
}
