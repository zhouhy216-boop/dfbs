package com.dfbs.app.application.perm;

import com.dfbs.app.modules.perm.PermRoleEntity;
import com.dfbs.app.modules.perm.PermRoleRepo;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Test-only: create/reset 4 deterministic accounts for Step-05 verification.
 * Idempotent; only touches users with username prefix perm_t.
 */
@Service
public class PermTestAccountKitService {

    private static final String ROLE_KEY = "perm_test_kit_template";
    private static final String AUTHORITIES = "[\"ROLE_USER\"]";

    private final UserRepo userRepo;
    private final PermRoleRepo roleRepo;
    private final PermRoleService roleService;
    private final PermAccountOverrideService accountOverrideService;

    public PermTestAccountKitService(UserRepo userRepo, PermRoleRepo roleRepo,
                                     PermRoleService roleService,
                                     PermAccountOverrideService accountOverrideService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.roleService = roleService;
        this.accountOverrideService = accountOverrideService;
    }

    @Transactional
    public List<KitAccountSummary> reset() {
        Long roleId = ensureRole();
        List<KitAccountSummary> result = new ArrayList<>();

        // 1) perm_t1_none: no VIEW
        result.add(ensureUser("perm_t1_none", "T1无权限", roleId, List.of(), List.of()));

        // 2) perm_t2_view: VIEW only orgs + applications
        result.add(ensureUser("perm_t2_view", "T2仅查看",
                roleId,
                List.of("platform_application.orgs:VIEW", "platform_application.applications:VIEW"),
                List.of()));

        // 3) perm_t3_org_editor: orgs VIEW+CREATE+EDIT, applications VIEW only
        result.add(ensureUser("perm_t3_org_editor", "T3机构编辑",
                roleId,
                List.of(
                        "platform_application.orgs:VIEW", "platform_application.orgs:CREATE", "platform_application.orgs:EDIT",
                        "platform_application.applications:VIEW"),
                List.of()));

        // 4) perm_t4_app_approver: applications VIEW+APPROVE+REJECT, orgs VIEW only
        result.add(ensureUser("perm_t4_app_approver", "T4申请审批",
                roleId,
                List.of(
                        "platform_application.applications:VIEW", "platform_application.applications:APPROVE", "platform_application.applications:REJECT",
                        "platform_application.orgs:VIEW"),
                List.of()));

        return result;
    }

    public List<KitAccountSummary> getCurrentKit() {
        Long roleId = roleRepo.findByRoleKey(ROLE_KEY).map(PermRoleEntity::getId).orElse(null);
        if (roleId == null) {
            return List.of();
        }
        List<KitAccountSummary> result = new ArrayList<>();
        for (String username : List.of("perm_t1_none", "perm_t2_view", "perm_t3_org_editor", "perm_t4_app_approver")) {
            userRepo.findByUsername(username).ifPresent(u -> {
                var keys = accountOverrideService.getEffectiveKeys(u.getId());
                result.add(new KitAccountSummary(u.getUsername(), u.getNickname(), u.getId(), keys.size(), keys.stream().sorted().limit(5).toList()));
            });
        }
        return result;
    }

    private Long ensureRole() {
        var existing = roleRepo.findByRoleKey(ROLE_KEY);
        if (existing.isPresent()) return existing.get().getId();
        try {
            PermRoleEntity r = roleService.create(ROLE_KEY, "测试套件模板", true);
            return r.getId();
        } catch (PermRoleService.RoleKeyExistsException e) {
            return roleRepo.findByRoleKey(ROLE_KEY).map(PermRoleEntity::getId).orElseThrow();
        }
    }

    private KitAccountSummary ensureUser(String username, String nickname, Long roleId,
                                         List<String> addKeys, List<String> removeKeys) {
        UserEntity user = userRepo.findByUsername(username)
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setUsername(username);
                    u.setNickname(nickname);
                    u.setAuthorities(AUTHORITIES);
                    u.setCanRequestPermission(false);
                    u.setAllowNormalNotification(true);
                    u.setCanManageStatements(false);
                    return userRepo.save(u);
                });
        user.setNickname(nickname);
        userRepo.save(user);

        accountOverrideService.saveOverride(user.getId(), roleId, addKeys, removeKeys);
        var effective = accountOverrideService.getEffectiveKeys(user.getId());
        return new KitAccountSummary(user.getUsername(), user.getNickname(), user.getId(),
                effective.size(), effective.stream().sorted().limit(8).toList());
    }

    public record KitAccountSummary(String username, String nickname, Long userId, int effectiveKeyCount, List<String> effectiveKeySample) {}
}
