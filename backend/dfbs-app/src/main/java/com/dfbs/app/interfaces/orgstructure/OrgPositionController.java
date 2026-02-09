package com.dfbs.app.interfaces.orgstructure;

import com.dfbs.app.application.orgstructure.OrgPositionCatalogService;
import com.dfbs.app.application.orgstructure.OrgPositionConfigService;
import com.dfbs.app.application.orgstructure.dto.*;
import com.dfbs.app.config.SuperAdminGuard;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org-structure/positions")
public class OrgPositionController {

    private final OrgPositionCatalogService catalogService;
    private final OrgPositionConfigService configService;
    private final SuperAdminGuard superAdminGuard;

    public OrgPositionController(OrgPositionCatalogService catalogService,
                                 OrgPositionConfigService configService,
                                 SuperAdminGuard superAdminGuard) {
        this.catalogService = catalogService;
        this.configService = configService;
        this.superAdminGuard = superAdminGuard;
    }

    /** Catalog (read-only v1): ordered list with baseName, grade, displayName, shortName, isEnabled. */
    @GetMapping("/catalog")
    public List<PositionCatalogItemDto> getCatalog() {
        superAdminGuard.requireSuperAdmin();
        return catalogService.listEnabledCatalog();
    }

    /** Per-org enabled positions with bound people (isPartTime when primary org != current org). */
    @GetMapping("/by-org")
    public PositionsByOrgResponseDto getByOrg(@RequestParam Long orgNodeId) {
        superAdminGuard.requireSuperAdmin();
        return configService.getByOrg(orgNodeId);
    }

    @PostMapping("/by-org/enable")
    public void enable(@RequestBody PositionEnableRequest request) {
        superAdminGuard.requireSuperAdmin();
        configService.enablePosition(request.orgNodeId(), request.positionId());
    }

    @PostMapping("/by-org/disable")
    public void disable(@RequestBody PositionEnableRequest request) {
        superAdminGuard.requireSuperAdmin();
        configService.disablePosition(request.orgNodeId(), request.positionId());
    }

    @PutMapping("/by-org/bindings")
    public void putBindings(@RequestBody PositionBindingsUpdateRequest request) {
        superAdminGuard.requireSuperAdmin();
        configService.putBindings(request.orgNodeId(), request.positionId(),
                request.personIds() != null ? request.personIds() : List.of());
    }

    /** Query API: org+position -> people contact info (for downstream modules). */
    @GetMapping("/bindings/query")
    public List<BindingQueryPersonDto> queryBindings(
            @RequestParam Long orgNodeId,
            @RequestParam Long positionId) {
        superAdminGuard.requireSuperAdmin();
        return configService.queryBindings(orgNodeId, positionId);
    }
}
