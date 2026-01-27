package com.dfbs.app.interfaces.settings;

import com.dfbs.app.application.settings.BusinessLineService;
import com.dfbs.app.modules.settings.BusinessLineEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-lines")
public class BusinessLineController {

    private final BusinessLineService service;

    public BusinessLineController(BusinessLineService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessLineEntity create(@RequestBody CreateBusinessLineRequest req) {
        return service.create(req.name(), req.leaderIds());
    }

    @PutMapping("/{id}")
    public BusinessLineEntity update(@PathVariable Long id, @RequestBody UpdateBusinessLineRequest req) {
        return service.update(id, req.name(), req.leaderIds(), req.isActive());
    }

    @GetMapping
    public List<BusinessLineEntity> list() {
        return service.list();
    }

    @GetMapping("/active")
    public List<BusinessLineEntity> listActive() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public BusinessLineEntity get(@PathVariable Long id) {
        return service.get(id);
    }

    public record CreateBusinessLineRequest(
            String name,
            String leaderIds  // JSON array or comma-separated
    ) {}

    public record UpdateBusinessLineRequest(
            String name,
            String leaderIds,
            Boolean isActive
    ) {}
}
