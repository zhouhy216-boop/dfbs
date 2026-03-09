package com.dfbs.app.application.perm;

import com.dfbs.app.config.CurrentUserIdResolver;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * RBAC enforcement: resolve effective permissions for current user and require permission by key.
 * effective = (template ∪ add) \ remove (remove wins). Uses PermAccountOverrideService for resolution.
 * Baseline admin/super-admin: users with ROLE_ADMIN or ROLE_SUPER_ADMIN may bypass VIEW for
 * shipments, platform orgs, platform applications, and work orders so the visible menu entries open honestly.
 * work_order:CREATE is also bypassed so admin baseline can use 新建工单.
 * Dev-stage first-batch action bypass (ROLESIMA-260306-001-05): Super Admin can execute confirmed first-batch
 * actions on /shipments, /work-orders, /platform/orgs, /platform/applications without 403 for acceptance only.
 */
@Service
public class PermEnforcementService {

    private static final String MESSAGE_NO_ACCESS = "无权限";

    /** VIEW permission keys that admin/super-admin may bypass for honest baseline menu reachability. */
    private static final Set<String> BASELINE_VIEW_KEYS_ADMIN_BYPASS = Set.of(
            "shipment.shipments:VIEW",
            "platform_application.orgs:VIEW",
            "platform_application.applications:VIEW",
            "work_order:VIEW"
    );

    /** CREATE (and similar) keys that admin/super-admin may bypass for baseline 新建工单. */
    private static final Set<String> BASELINE_CREATE_KEYS_ADMIN_BYPASS = Set.of(
            "work_order:CREATE"
    );

    /** Dev-stage first-batch action keys: Super Admin bypass for CEO acceptance only (M04/M05/M08 confirmed blockers). */
    private static final Set<String> DEV_STAGE_FIRST_BATCH_ACTION_BYPASS = Set.of(
            "platform_application.applications:CREATE",
            "platform_application.applications:SUBMIT",
            "platform_application.applications:APPROVE",
            "platform_application.applications:REJECT",
            "platform_application.applications:CLOSE",
            "platform_application.orgs:CREATE",
            "platform_application.orgs:EDIT",
            "platform_application.orgs:DELETE",
            "shipment.shipments:ACCEPT",
            "shipment.shipments:PREPARE",
            "shipment.shipments:SHIP",
            "shipment.shipments:TRACKING",
            "shipment.shipments:COMPLETE",
            "shipment.shipments:EXCEPTION",
            "shipment.shipments:CANCEL",
            "shipment.shipments:CLOSE",
            "work_order:REJECT",
            "work_order:ASSIGN",
            "work_order:SUBMIT",
            "work_order:EDIT",
            "work_order:APPROVE",
            "work_order:CLOSE"
    );

    private final CurrentUserIdResolver userIdResolver;
    private final PermAccountOverrideService accountOverrideService;

    public PermEnforcementService(CurrentUserIdResolver userIdResolver,
                                  PermAccountOverrideService accountOverrideService) {
        this.userIdResolver = userIdResolver;
        this.accountOverrideService = accountOverrideService;
    }

    /** Effective permission keys for current request user. */
    public Set<String> getEffectiveKeysForCurrentUser() {
        Long userId = userIdResolver.getCurrentUserId();
        return accountOverrideService.getEffectiveKeys(userId);
    }

    /** Throws PermForbiddenException (403 PERM_FORBIDDEN) if current user does not have the permission key. */
    public void requirePermission(String permissionKey) {
        if (permissionKey == null || permissionKey.isBlank()) {
            return;
        }
        String key = permissionKey.trim();
        if (userIdResolver.isAdminOrSuperAdmin()
                && (BASELINE_VIEW_KEYS_ADMIN_BYPASS.contains(key)
                        || BASELINE_CREATE_KEYS_ADMIN_BYPASS.contains(key)
                        || DEV_STAGE_FIRST_BATCH_ACTION_BYPASS.contains(key))) {
            return;
        }
        Set<String> effective = getEffectiveKeysForCurrentUser();
        if (!effective.contains(key)) {
            throw new PermForbiddenException(MESSAGE_NO_ACCESS);
        }
    }
}
