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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * PERM role CRUD and role-permission assignment. permission_key format: &lt;moduleKey&gt;:&lt;actionKey&gt;.
 * roleKey: if provided must be unique; if absent/blank, auto-generated (acctperm_yyyyMMdd_8alnum).
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

    private static final String ROLE_KEY_PREFIX = "acctperm_";
    private static final int GENERATE_RETRIES = 5;

    private String generateUniqueRoleKey() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int attempt = 0; attempt < GENERATE_RETRIES; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            ThreadLocalRandom r = ThreadLocalRandom.current();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(r.nextInt(chars.length())));
            }
            String key = ROLE_KEY_PREFIX + datePart + "_" + sb;
            if (!roleRepo.existsByRoleKey(key)) {
                return key;
            }
        }
        throw new IllegalStateException("无法生成唯一 roleKey，请重试");
    }

    @Transactional
    public PermRoleEntity create(String roleKey, String label, Boolean enabled, String description) {
        String key;
        if (roleKey == null || roleKey.isBlank()) {
            key = generateUniqueRoleKey();
        } else {
            key = roleKey.trim();
            if (roleRepo.existsByRoleKey(key)) {
                throw new RoleKeyExistsException("角色 key 已存在: " + key);
            }
        }
        PermRoleEntity e = new PermRoleEntity();
        e.setRoleKey(key);
        e.setLabel(label != null && !label.isBlank() ? label.trim() : key);
        e.setEnabled(enabled == null || enabled);
        e.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        return roleRepo.save(e);
    }

    /** Backward-compatible for callers that do not pass description (e.g. test kit, old perm controller). */
    @Transactional
    public PermRoleEntity create(String roleKey, String label, Boolean enabled) {
        return create(roleKey, label, enabled, null);
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
    public PermRoleEntity saveTemplate(Long id, String label, Boolean enabled, List<String> permissionKeys, String description) {
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
        if (description != null) {
            role.setDescription(description.trim().isEmpty() ? null : description.trim());
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

    /** Clone source role: new role with label + "-副本", enabled=false, copied description and permissions; roleKey auto-generated. */
    @Transactional
    public PermRoleEntity clone(Long sourceRoleId) {
        PermRoleEntity source = roleRepo.findById(sourceRoleId).orElseThrow(() -> new RoleNotFoundException("角色不存在: id=" + sourceRoleId));
        String newLabel = (source.getLabel() != null ? source.getLabel().trim() : "角色") + "-副本";
        PermRoleEntity created = create(null, newLabel, false, source.getDescription());
        List<PermRolePermissionEntity> sourcePerms = rolePermissionRepo.findByRoleId(sourceRoleId);
        List<PermRolePermissionEntity> toSave = new ArrayList<>();
        for (PermRolePermissionEntity p : sourcePerms) {
            PermRolePermissionEntity copy = new PermRolePermissionEntity();
            copy.setRoleId(created.getId());
            copy.setPermissionKey(p.getPermissionKey());
            toSave.add(copy);
        }
        rolePermissionRepo.saveAll(toSave);
        return roleRepo.findById(created.getId()).orElse(created);
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
