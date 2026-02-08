package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgLevelService;
import com.dfbs.app.application.orgstructure.dto.OrgLevelCreateRequest;
import com.dfbs.app.application.orgstructure.dto.OrgLevelUpdateRequest;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgLevelEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/org-structure/levels")
public class OrgLevelController {

    private final OrgLevelService service;
    private final SuperAdminGuard superAdminGuard;

    public OrgLevelController(OrgLevelService service, SuperAdminGuard superAdminGuard) {
        this.service = service;
        this.superAdminGuard = superAdminGuard;
    }

    @GetMapping
    public List<OrgLevelEntity> list() {
        superAdminGuard.requireSuperAdmin();
        return service.listOrdered();
    }

    /** Configurable levels only (excludes system-fixed 公司). For Level Config page. */
    @GetMapping("/configurable")
    public List<OrgLevelEntity> listConfigurable() {
        superAdminGuard.requireSuperAdmin();
        return service.listConfigurableOrdered();
    }

    /** Enabled levels only (e.g. for create-org level selector). */
    @GetMapping("/enabled")
    public List<OrgLevelEntity> listEnabled() {
        superAdminGuard.requireSuperAdmin();
        return service.listEnabledOrdered();
    }

    /** Whether reset-to-default is allowed (0 org nodes, 0 affiliations). Returns canReset, message, nodeCount, affiliationCount. */
    @GetMapping("/can-reset")
    public Map<String, Object> canReset() {
        superAdminGuard.requireSuperAdmin();
        return service.getCanResetLevelsResult();
    }

    @GetMapping("/{id:\\d+}")
    public OrgLevelEntity get(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.getById(id);
    }

    @PostMapping
    public OrgLevelEntity create(@RequestBody OrgLevelCreateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.create(request.orderIndex(), request.displayName());
    }

    @PutMapping("/{id:\\d+}")
    public OrgLevelEntity update(@PathVariable Long id, @RequestBody OrgLevelUpdateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.update(id, request.orderIndex(), request.displayName(), request.isEnabled());
    }

    /** Reset all levels to default (公司/本部/部/课/系/班). Only when canReset. */
    @PostMapping("/reset-to-default")
    public List<OrgLevelEntity> resetToDefault() {
        superAdminGuard.requireSuperAdmin();
        return service.resetLevelsToDefault();
    }
}
