package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgNodeService;
import com.dfbs.app.application.orgstructure.dto.OrgNodeCreateRequest;
import com.dfbs.app.application.orgstructure.dto.OrgNodeDto;
import com.dfbs.app.application.orgstructure.dto.OrgNodeMoveRequest;
import com.dfbs.app.application.orgstructure.dto.OrgNodeUpdateRequest;
import com.dfbs.app.application.orgstructure.dto.OrgTreeNodeDto;
import com.dfbs.app.config.SuperAdminGuard;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org-structure/nodes")
public class OrgNodeController {

    private final OrgNodeService service;
    private final SuperAdminGuard superAdminGuard;

    public OrgNodeController(OrgNodeService service, SuperAdminGuard superAdminGuard) {
        this.service = service;
        this.superAdminGuard = superAdminGuard;
    }

    /** Full tree (optionally include disabled). For admin and for reusable read API. */
    @GetMapping("/tree")
    public List<OrgTreeNodeDto> getTree(@RequestParam(defaultValue = "false") boolean includeDisabled) {
        superAdminGuard.requireSuperAdmin();
        return service.getTreeDto(includeDisabled);
    }

    @GetMapping("/children")
    public List<OrgNodeDto> getChildren(@RequestParam(required = false) Long parentId,
                                        @RequestParam(defaultValue = "false") boolean includeDisabled) {
        superAdminGuard.requireSuperAdmin();
        return service.getChildrenAsDtos(parentId, includeDisabled);
    }

    @GetMapping("/{id}")
    public OrgNodeDto get(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.getByIdAsDto(id);
    }

    @GetMapping("/{id}/impact")
    public OrgNodeService.ImpactSummary getImpact(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.getImpactSummary(id);
    }

    @PostMapping
    public OrgNodeDto create(@RequestBody OrgNodeCreateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.create(request.levelId(), request.parentId(), request.name(), request.remark(), request.isEnabled());
    }

    @PutMapping("/{id}")
    public OrgNodeDto update(@PathVariable Long id, @RequestBody OrgNodeUpdateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.update(id, request.name(), request.remark(), request.isEnabled());
    }

    @PostMapping("/{id}/move")
    public OrgNodeDto move(@PathVariable Long id, @RequestBody OrgNodeMoveRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.move(id, request.newParentId());
    }

    @PostMapping("/{id}/disable")
    public OrgNodeDto disable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.disable(id);
    }

    @PostMapping("/{id}/enable")
    public OrgNodeDto enable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.enable(id);
    }
}
