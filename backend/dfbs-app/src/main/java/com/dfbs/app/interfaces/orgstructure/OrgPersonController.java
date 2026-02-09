package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgPersonService;
import com.dfbs.app.application.orgstructure.dto.OrgPersonCreateRequest;
import com.dfbs.app.application.orgstructure.dto.OrgPersonResponse;
import com.dfbs.app.application.orgstructure.dto.OrgPersonUpdateRequest;
import com.dfbs.app.application.orgstructure.dto.PersonOptionDto;
import com.dfbs.app.application.orgstructure.dto.PersonPositionAssignmentDto;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.modules.orgstructure.OrgPersonEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/org-structure/people")
public class OrgPersonController {

    private final OrgPersonService service;
    private final SuperAdminGuard superAdminGuard;

    public OrgPersonController(OrgPersonService service, SuperAdminGuard superAdminGuard) {
        this.service = service;
        this.superAdminGuard = superAdminGuard;
    }

    @GetMapping
    public Page<OrgPersonResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long primaryOrgId,
            Pageable pageable) {
        superAdminGuard.requireSuperAdmin();
        return service.searchResponses(keyword, primaryOrgId, pageable);
    }

    /**
     * People by org subtree for Org Tree right panel. Omit orgNodeId for root => all people.
     * Params: orgNodeId (optional), includeDescendants (default true), includeSecondaries (default true), activeOnly (default true), keyword, page, size.
     */
    @GetMapping("/by-org")
    public Map<String, Object> searchByOrg(
            @RequestParam(required = false) Long orgNodeId,
            @RequestParam(required = false, defaultValue = "true") boolean includeDescendants,
            @RequestParam(required = false, defaultValue = "true") boolean includeSecondaries,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        superAdminGuard.requireSuperAdmin();
        Page<OrgPersonResponse> page = service.searchByOrgSubtree(orgNodeId, includeDescendants, includeSecondaries, activeOnly, keyword, pageable);
        return Map.of(
                "content", page.getContent(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "size", page.getSize(),
                "number", page.getNumber()
        );
    }

    /** Options for PersonSelect (id, name, phone, email). */
    @GetMapping("/options")
    public List<PersonOptionDto> options(@RequestParam(required = false) String keyword) {
        superAdminGuard.requireSuperAdmin();
        return service.listOptions(keyword);
    }

    @GetMapping("/{id}")
    public OrgPersonResponse get(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.getResponseById(id);
    }

    /** Person's position assignments (org path + position name + isPartTime). */
    @GetMapping("/{id}/positions")
    public List<PersonPositionAssignmentDto> getPersonPositions(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.getPersonPositionAssignments(id);
    }

    @PostMapping
    public OrgPersonEntity create(@RequestBody OrgPersonCreateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.create(request.name(), request.phone(), request.email(), request.remark(),
                request.jobLevelId(), request.primaryOrgNodeId(), request.secondaryOrgNodeIds());
    }

    @PutMapping("/{id}")
    public OrgPersonEntity update(@PathVariable Long id, @RequestBody OrgPersonUpdateRequest request) {
        superAdminGuard.requireSuperAdmin();
        return service.update(id, request.name(), request.phone(), request.email(), request.remark(),
                request.jobLevelId(), request.primaryOrgNodeId(), request.secondaryOrgNodeIds());
    }

    @PostMapping("/{id}/disable")
    public OrgPersonEntity disable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.disable(id);
    }

    @PostMapping("/{id}/enable")
    public OrgPersonEntity enable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        return service.enable(id);
    }
}
