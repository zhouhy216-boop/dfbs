package com.dfbs.app.interfaces.platformconfig;

import com.dfbs.app.application.platformconfig.PlatformConfigService;
import com.dfbs.app.application.platformconfig.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform-configs")
public class PlatformConfigController {

    private final PlatformConfigService service;

    public PlatformConfigController(PlatformConfigService service) {
        this.service = service;
    }

    /**
     * Active platforms for dropdowns (label/value).
     */
    @GetMapping("/options")
    public List<PlatformOptionDto> getOptions() {
        return service.getOptions();
    }

    /**
     * Validation rules for a platform code (org code format, uniqueness rules).
     */
    @GetMapping("/{code}/rules")
    public PlatformRulesDto getRulesByCode(@PathVariable String code) {
        return service.getRulesByCode(code);
    }

    @GetMapping
    public List<PlatformConfigResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public PlatformConfigResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public PlatformConfigResponse create(@RequestBody @Valid PlatformConfigRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PlatformConfigResponse update(@PathVariable Long id, @RequestBody @Valid PlatformConfigRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    public PlatformConfigResponse toggleActive(@PathVariable Long id) {
        return service.toggleActive(id);
    }
}
