package com.dfbs.app.interfaces.perm;

import com.dfbs.app.application.perm.PermAccountOverrideService;
import com.dfbs.app.application.perm.PermAuditService;
import com.dfbs.app.application.perm.PermTestAccountKitService;
import com.dfbs.app.application.perm.PermVisionStore;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.config.PermSuperAdminGuard;
import com.dfbs.app.config.PermTestUtilitiesGuard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test-only PERM utilities (Role-Vision). Super-admin + testUtilitiesEnabled only; 404 otherwise.
 * Does NOT affect enforcement (PermEnforcementService always uses real user).
 */
@RestController
@RequestMapping("/api/v1/admin/perm/test")
public class PermTestController {

    private final PermSuperAdminGuard permSuperAdminGuard;
    private final PermTestUtilitiesGuard testUtilitiesGuard;
    private final CurrentUserIdResolver userIdResolver;
    private final PermVisionStore visionStore;
    private final PermAccountOverrideService accountOverrideService;
    private final PermTestAccountKitService testAccountKitService;
    private final PermAuditService auditService;

    public PermTestController(PermSuperAdminGuard permSuperAdminGuard,
                              PermTestUtilitiesGuard testUtilitiesGuard,
                              CurrentUserIdResolver userIdResolver,
                              PermVisionStore visionStore,
                              PermAccountOverrideService accountOverrideService,
                              PermTestAccountKitService testAccountKitService,
                              PermAuditService auditService) {
        this.permSuperAdminGuard = permSuperAdminGuard;
        this.testUtilitiesGuard = testUtilitiesGuard;
        this.userIdResolver = userIdResolver;
        this.visionStore = visionStore;
        this.accountOverrideService = accountOverrideService;
        this.testAccountKitService = testAccountKitService;
        this.auditService = auditService;
    }

    private void guard() {
        permSuperAdminGuard.requirePermSuperAdmin();
        testUtilitiesGuard.requireTestUtilitiesEnabled();
    }

    /** POST vision: set or clear Role-Vision for current admin. */
    @PostMapping("/vision")
    public ResponseEntity<VisionResponse> setVision(@RequestBody VisionRequest request) {
        guard();
        Long adminId = userIdResolver.getCurrentUserId();
        PermVisionStore.Mode mode = "USER".equalsIgnoreCase(request.mode()) ? PermVisionStore.Mode.USER : PermVisionStore.Mode.OFF;
        Long targetUserId = mode == PermVisionStore.Mode.USER ? request.userId() : null;
        if (mode == PermVisionStore.Mode.USER && (targetUserId == null)) {
            return ResponseEntity.badRequest().build();
        }
        visionStore.setVision(adminId, mode, targetUserId);
        auditService.log(PermAuditService.ACTION_VISION_SET, PermAuditService.TARGET_SYSTEM, null, null,
                "mode=" + mode + (targetUserId != null ? ", targetUserId=" + targetUserId : ""));
        return ResponseEntity.ok(toResponse(visionStore.getVision(adminId)));
    }

    /** GET vision: current vision status for admin. */
    @GetMapping("/vision")
    public ResponseEntity<VisionResponse> getVision() {
        guard();
        Long adminId = userIdResolver.getCurrentUserId();
        PermVisionStore.VisionEntry e = visionStore.getVision(adminId);
        return ResponseEntity.ok(toResponse(e));
    }

    /** GET effective-keys under vision (for UI only). When vision OFF, returns real user keys. */
    @GetMapping("/me/effective-keys")
    public ResponseEntity<Map<String, List<String>>> getEffectiveKeysUnderVision() {
        guard();
        Long adminId = userIdResolver.getCurrentUserId();
        PermVisionStore.VisionEntry vision = visionStore.getVision(adminId);
        Long effectiveUserId = adminId;
        if (vision != null && vision.getMode() == PermVisionStore.Mode.USER && vision.getTargetUserId() != null) {
            effectiveUserId = vision.getTargetUserId();
        }
        var keys = accountOverrideService.getEffectiveKeys(effectiveUserId).stream().sorted().collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("effectiveKeys", keys));
    }

    /** POST test accounts reset: create/reset 4 kit accounts. */
    @PostMapping("/accounts/reset")
    public ResponseEntity<List<PermTestAccountKitService.KitAccountSummary>> resetTestAccounts() {
        guard();
        var list = testAccountKitService.reset();
        auditService.log(PermAuditService.ACTION_TEST_KIT_RESET, PermAuditService.TARGET_SYSTEM, null, null, "count=" + list.size());
        return ResponseEntity.ok(list);
    }

    /** GET test accounts: current kit state for UI. */
    @GetMapping("/accounts")
    public ResponseEntity<List<PermTestAccountKitService.KitAccountSummary>> getTestAccounts() {
        guard();
        return ResponseEntity.ok(testAccountKitService.getCurrentKit());
    }

    private static VisionResponse toResponse(PermVisionStore.VisionEntry e) {
        if (e == null) {
            return new VisionResponse("OFF", null);
        }
        return new VisionResponse(e.getMode().name(), e.getTargetUserId());
    }

    public record VisionRequest(String mode, Long userId) {}
    public record VisionResponse(String mode, Long userId) {}
}
