package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgStructureDevResetService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Dev-only: full reset. Tooling: safe reset (tree + logs + levels only; blocked if people/affiliations exist).
 */
@RestController
@RequestMapping("/api/v1/org-structure")
public class OrgStructureDevController {

    private final OrgStructureDevResetService devResetService;
    private final SuperAdminGuard superAdminGuard;
    private final Environment environment;

    public OrgStructureDevController(OrgStructureDevResetService devResetService,
                                     SuperAdminGuard superAdminGuard,
                                     Environment environment) {
        this.devResetService = devResetService;
        this.superAdminGuard = superAdminGuard;
        this.environment = environment;
    }

    /** Reset availability for tooling button: allowed, reason, personCount, affiliationCount, nodeCount. */
    @GetMapping("/reset-availability")
    public Map<String, Object> resetAvailability() {
        superAdminGuard.requireSuperAdmin();
        return devResetService.getResetToolingAvailability();
    }

    /** Safe reset: clear org tree + change logs + levels, restore default levels. Confirm body required; blocked if people/affiliations exist. */
    @PostMapping("/reset")
    public List<OrgLevelEntity> resetTooling(@RequestBody(required = false) Map<String, String> body) {
        superAdminGuard.requireSuperAdmin();
        String confirm = body != null ? body.get("confirmText") : null;
        if (!"RESET".equals(confirm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请在请求体中提供 confirmText: \"RESET\"");
        }
        return devResetService.resetTooling();
    }

    @PostMapping("/reset-dev")
    public List<OrgLevelEntity> resetDev() {
        superAdminGuard.requireSuperAdmin();
        if (!isDevProfile()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅开发环境可用");
        }
        return devResetService.resetDev();
    }

    private boolean isDevProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile)) return true;
        }
        return false;
    }
}
