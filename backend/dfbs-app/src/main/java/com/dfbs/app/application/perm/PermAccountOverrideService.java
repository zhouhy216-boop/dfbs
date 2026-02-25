package com.dfbs.app.application.perm;

import com.dfbs.app.interfaces.perm.PermAccountOverrideDto;
import com.dfbs.app.modules.perm.*;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

/**
 * Per-account override: assign role template + add/remove permission keys.
 * effective = (templateKeys ∪ addKeys) \ removeKeys (remove wins).
 */
@Service
public class PermAccountOverrideService {

    private static final String OP_ADD = "ADD";
    private static final String OP_REMOVE = "REMOVE";

    private final UserRepo userRepo;
    private final PermUserRoleTemplateRepo userRoleTemplateRepo;
    private final PermUserPermissionOverrideRepo overrideRepo;
    private final PermRoleRepo roleRepo;
    private final PermRolePermissionRepo rolePermissionRepo;
    private final PermRoleService roleService;

    public PermAccountOverrideService(UserRepo userRepo,
                                     PermUserRoleTemplateRepo userRoleTemplateRepo,
                                     PermUserPermissionOverrideRepo overrideRepo,
                                     PermRoleRepo roleRepo,
                                     PermRolePermissionRepo rolePermissionRepo,
                                     PermRoleService roleService) {
        this.userRepo = userRepo;
        this.userRoleTemplateRepo = userRoleTemplateRepo;
        this.overrideRepo = overrideRepo;
        this.roleRepo = roleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.roleService = roleService;
    }

    public PermAccountOverrideDto.AccountOverrideResponse getOverride(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException("用户不存在: id=" + userId);
        }
        List<String> templateKeys = new ArrayList<>();
        Long roleTemplateId = null;
        String roleTemplateKey = null;
        Optional<PermUserRoleTemplateEntity> templateOpt = userRoleTemplateRepo.findByUserId(userId);
        if (templateOpt.isPresent() && templateOpt.get().getRoleId() != null) {
            Long roleId = templateOpt.get().getRoleId();
            roleTemplateId = roleId;
            roleTemplateKey = roleRepo.findById(roleId).map(PermRoleEntity::getRoleKey).orElse(null);
            templateKeys.addAll(rolePermissionRepo.findByRoleId(roleId).stream()
                    .map(PermRolePermissionEntity::getPermissionKey)
                    .toList());
        }
        List<PermUserPermissionOverrideEntity> overrides = overrideRepo.findByUserId(userId);
        List<String> addKeys = overrides.stream().filter(o -> OP_ADD.equals(o.getOp())).map(PermUserPermissionOverrideEntity::getPermissionKey).distinct().toList();
        List<String> removeKeys = overrides.stream().filter(o -> OP_REMOVE.equals(o.getOp())).map(PermUserPermissionOverrideEntity::getPermissionKey).distinct().toList();
        Set<String> effective = new HashSet<>(templateKeys);
        effective.addAll(addKeys);
        effective.removeAll(removeKeys);
        List<String> effectiveKeys = effective.stream().sorted().toList();
        return new PermAccountOverrideDto.AccountOverrideResponse(userId, roleTemplateId, roleTemplateKey,
                addKeys, removeKeys, effectiveKeys);
    }

    @Transactional
    public PermAccountOverrideDto.AccountOverrideResponse saveOverride(Long userId, Long roleTemplateId, List<String> addKeys, List<String> removeKeys) {
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException("用户不存在: id=" + userId);
        }
        if (roleTemplateId != null) {
            PermRoleEntity role = roleRepo.findById(roleTemplateId).orElseThrow(() -> new PermRoleService.RoleNotFoundException("角色不存在: id=" + roleTemplateId));
            if (!Boolean.TRUE.equals(role.getEnabled())) {
                throw new RoleDisabledException("只能分配已启用的角色模板: " + role.getRoleKey());
            }
        }
        for (String key : Stream.concat(addKeys.stream(), removeKeys.stream()).filter(Objects::nonNull).map(String::trim).filter(k -> !k.isBlank()).distinct().toList()) {
            roleService.validatePermissionKey(key);
        }
        addKeys = addKeys != null ? addKeys.stream().filter(Objects::nonNull).map(String::trim).filter(k -> !k.isBlank()).distinct().toList() : List.of();
        removeKeys = removeKeys != null ? removeKeys.stream().filter(Objects::nonNull).map(String::trim).filter(k -> !k.isBlank()).distinct().toList() : List.of();

        Optional<PermUserRoleTemplateEntity> existing = userRoleTemplateRepo.findByUserId(userId);
        PermUserRoleTemplateEntity templateRow = existing.orElseGet(() -> {
            PermUserRoleTemplateEntity e = new PermUserRoleTemplateEntity();
            e.setUserId(userId);
            return e;
        });
        templateRow.setRoleId(roleTemplateId);
        userRoleTemplateRepo.save(templateRow);

        overrideRepo.deleteByUserId(userId);
        for (String key : addKeys) {
            PermUserPermissionOverrideEntity o = new PermUserPermissionOverrideEntity();
            o.setUserId(userId);
            o.setPermissionKey(key);
            o.setOp(OP_ADD);
            overrideRepo.save(o);
        }
        for (String key : removeKeys) {
            PermUserPermissionOverrideEntity o = new PermUserPermissionOverrideEntity();
            o.setUserId(userId);
            o.setPermissionKey(key);
            o.setOp(OP_REMOVE);
            overrideRepo.save(o);
        }
        return getOverride(userId);
    }

    public static final class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }

    public static final class RoleDisabledException extends RuntimeException {
        public RoleDisabledException(String message) { super(message); }
    }
}
