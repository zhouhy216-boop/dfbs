package com.dfbs.app.interfaces.platformorg;

import com.dfbs.app.application.perm.PermEnforcementService;
import com.dfbs.app.application.platformorg.PlatformOrgService;
import com.dfbs.app.application.platformorg.dto.PlatformOrgRequest;
import com.dfbs.app.application.platformorg.dto.PlatformOrgResponse;
import com.dfbs.app.modules.platformorg.PlatformOrgStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/platform-orgs")
public class PlatformOrgController {

    private static final String PERM_ORGS_VIEW = "platform_application.orgs:VIEW";
    private static final String PERM_ORGS_CREATE = "platform_application.orgs:CREATE";
    private static final String PERM_ORGS_EDIT = "platform_application.orgs:EDIT";
    private static final String PERM_ORGS_DELETE = "platform_application.orgs:DELETE";

    private final PlatformOrgService service;
    private final PermEnforcementService permEnforcement;

    public PlatformOrgController(PlatformOrgService service, PermEnforcementService permEnforcement) {
        this.service = service;
        this.permEnforcement = permEnforcement;
    }

    @GetMapping
    public List<PlatformOrgResponse> list(
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "customerId", required = false) Long customerId) {
        permEnforcement.requirePermission(PERM_ORGS_VIEW);
        return service.list(Optional.ofNullable(platform), Optional.ofNullable(customerId));
    }

    @GetMapping("/{id}")
    public PlatformOrgResponse get(@PathVariable Long id) {
        permEnforcement.requirePermission(PERM_ORGS_VIEW);
        return service.get(id);
    }

    @GetMapping("/platform/{platform}/customer/{customerId}")
    public List<PlatformOrgResponse> findByPlatformAndCustomer(@PathVariable String platform,
                                                               @PathVariable Long customerId) {
        permEnforcement.requirePermission(PERM_ORGS_VIEW);
        return service.findByPlatformAndCustomer(platform, customerId);
    }

    @PostMapping
    public PlatformOrgResponse create(@RequestBody @Valid PlatformOrgRequest request) {
        permEnforcement.requirePermission(PERM_ORGS_CREATE);
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PlatformOrgResponse update(@PathVariable Long id,
                                      @RequestBody @Valid PlatformOrgRequest request) {
        if (request.status() == PlatformOrgStatus.DELETED) {
            permEnforcement.requirePermission(PERM_ORGS_DELETE);
        } else {
            permEnforcement.requirePermission(PERM_ORGS_EDIT);
        }
        return service.update(id, request);
    }
}
