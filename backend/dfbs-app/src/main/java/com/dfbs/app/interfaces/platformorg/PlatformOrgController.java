package com.dfbs.app.interfaces.platformorg;

import com.dfbs.app.application.platformorg.PlatformOrgService;
import com.dfbs.app.application.platformorg.dto.PlatformOrgRequest;
import com.dfbs.app.application.platformorg.dto.PlatformOrgResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/platform-orgs")
public class PlatformOrgController {

    private final PlatformOrgService service;

    public PlatformOrgController(PlatformOrgService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlatformOrgResponse> list(
            @RequestParam(name = "platform", required = false) String platform,
            @RequestParam(name = "customerId", required = false) Long customerId) {
        return service.list(Optional.ofNullable(platform), Optional.ofNullable(customerId));
    }

    @GetMapping("/{id}")
    public PlatformOrgResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/platform/{platform}/customer/{customerId}")
    public List<PlatformOrgResponse> findByPlatformAndCustomer(@PathVariable String platform,
                                                               @PathVariable Long customerId) {
        return service.findByPlatformAndCustomer(platform, customerId);
    }

    @PostMapping
    public PlatformOrgResponse create(@RequestBody @Valid PlatformOrgRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PlatformOrgResponse update(@PathVariable Long id,
                                      @RequestBody @Valid PlatformOrgRequest request) {
        return service.update(id, request);
    }
}
