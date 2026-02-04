package com.dfbs.app.interfaces.platformaccount;

import com.dfbs.app.application.platformaccount.PlatformAccountApplicationService;
import com.dfbs.app.application.platformaccount.dto.*;
import com.dfbs.app.modules.platformaccount.PlatformAccountApplicationStatus;
import com.dfbs.app.modules.platformorg.PlatformOrgPlatform;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/platform-account-applications")
public class PlatformAccountApplicationController {

    private final PlatformAccountApplicationService service;

    public PlatformAccountApplicationController(PlatformAccountApplicationService service) {
        this.service = service;
    }

    @PostMapping(value = "/create", name = "create")
    public PlatformAccountApplicationResponse create(@RequestBody @Valid PlatformAccountApplicationCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/page")
    public Page<PlatformAccountApplicationResponse> page(
            @RequestParam(name = "status", required = false) PlatformAccountApplicationStatus status,
            @RequestParam(name = "platform", required = false) PlatformOrgPlatform platform,
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.page(Optional.ofNullable(status), Optional.ofNullable(platform), Optional.ofNullable(customerId), page, size);
    }

    @GetMapping("/{id}")
    public PlatformAccountApplicationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/check-customer-name")
    public java.util.Map<String, Boolean> checkCustomerName(@RequestParam String name) {
        return java.util.Map.of("exists", service.existsCustomerByName(name));
    }

    @GetMapping("/check-org-match")
    public OrgMatchCheckResult checkOrgMatch(
            @RequestParam PlatformOrgPlatform platform,
            @RequestParam String orgCodeShort) {
        return service.checkOrgMatch(platform, orgCodeShort);
    }

    @PutMapping("/{id}/planner-submit")
    public PlatformAccountApplicationResponse plannerSubmit(@PathVariable Long id,
                                                            @RequestBody @Valid PlatformAccountPlannerSubmitRequest request) {
        return service.plannerSubmit(id, request);
    }

    @PostMapping("/{id}/approve")
    public PlatformAccountApplicationResponse approve(@PathVariable Long id,
                                                      @RequestBody @Valid PlatformAccountApproveRequest request) {
        return service.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public PlatformAccountApplicationResponse reject(@PathVariable Long id,
                                                     @RequestBody @Valid PlatformAccountRejectRequest request) {
        return service.reject(id, request);
    }
}
