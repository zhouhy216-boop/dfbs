package com.dfbs.app.interfaces.platformaccount;

import com.dfbs.app.application.perm.PermEnforcementService;
import com.dfbs.app.application.platformaccount.PlatformAccountApplicationService;
import com.dfbs.app.application.platformaccount.dto.*;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/platform-account-applications")
public class PlatformAccountApplicationController {

    private static final String PERM_APPS_VIEW = "platform_application.applications:VIEW";
    private static final String PERM_APPS_CREATE = "platform_application.applications:CREATE";
    private static final String PERM_APPS_SUBMIT = "platform_application.applications:SUBMIT";
    private static final String PERM_APPS_APPROVE = "platform_application.applications:APPROVE";
    private static final String PERM_APPS_REJECT = "platform_application.applications:REJECT";
    private static final String PERM_APPS_CLOSE = "platform_application.applications:CLOSE";

    private final PlatformAccountApplicationService service;
    private final PermEnforcementService permEnforcement;

    public PlatformAccountApplicationController(PlatformAccountApplicationService service,
                                                PermEnforcementService permEnforcement) {
        this.service = service;
        this.permEnforcement = permEnforcement;
    }

    @PostMapping(value = "/create", name = "create")
    public PlatformAccountApplicationResponse create(@RequestBody @Valid PlatformAccountApplicationCreateRequest request) {
        permEnforcement.requirePermission(PERM_APPS_CREATE);
        return service.create(request);
    }

    @GetMapping("/page")
    public Page<PlatformAccountApplicationResponse> page(
            @RequestParam(name = "status", required = false) PlatformAccountApplicationStatus status,
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        permEnforcement.requirePermission(PERM_APPS_VIEW);
        return service.page(Optional.ofNullable(status), Optional.ofNullable(platform), Optional.ofNullable(customerId), page, size);
    }

    @GetMapping("/{id}")
    public PlatformAccountApplicationResponse get(@PathVariable Long id) {
        permEnforcement.requirePermission(PERM_APPS_VIEW);
        return service.get(id);
    }

    @GetMapping("/check-customer-name")
    public java.util.Map<String, Boolean> checkCustomerName(@RequestParam String name) {
        permEnforcement.requirePermission(PERM_APPS_VIEW);
        return java.util.Map.of("exists", service.existsCustomerByName(name));
    }

    @PostMapping("/check-duplicates")
    public java.util.List<CheckDuplicateMatchItem> checkDuplicates(@RequestBody @Valid CheckDuplicatesRequest request) {
        permEnforcement.requirePermission(PERM_APPS_VIEW);
        return service.checkDuplicates(request);
    }

    @GetMapping("/check-org-match")
    public OrgMatchCheckResult checkOrgMatch(
            @RequestParam String platform,
            @RequestParam String orgCodeShort) {
        permEnforcement.requirePermission(PERM_APPS_VIEW);
        return service.checkOrgMatch(platform, orgCodeShort);
    }

    @PutMapping("/{id}/planner-submit")
    public PlatformAccountApplicationResponse plannerSubmit(@PathVariable Long id,
                                                            @RequestBody @Valid PlatformAccountPlannerSubmitRequest request) {
        permEnforcement.requirePermission(PERM_APPS_SUBMIT);
        return service.plannerSubmit(id, request);
    }

    @PostMapping("/{id}/approve")
    public PlatformAccountApplicationResponse approve(@PathVariable Long id,
                                                      @RequestBody @Valid PlatformAccountApproveRequest request) {
        permEnforcement.requirePermission(PERM_APPS_APPROVE);
        return service.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public PlatformAccountApplicationResponse reject(@PathVariable Long id,
                                                     @RequestBody @Valid PlatformAccountRejectRequest request) {
        permEnforcement.requirePermission(PERM_APPS_REJECT);
        return service.reject(id, request);
    }

    @PostMapping("/{id}/close")
    public PlatformAccountApplicationResponse close(@PathVariable Long id) {
        permEnforcement.requirePermission(PERM_APPS_CLOSE);
        return service.close(id);
    }
}
