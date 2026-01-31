package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.SimCardMasterDataService;
import com.dfbs.app.modules.masterdata.SimBindingLogEntity;
import com.dfbs.app.modules.masterdata.SimCardEntity;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Master Data - Sim Cards", description = "Sim card master data (soft delete, binding history)")
@RestController
@RequestMapping("/api/v1/masterdata/sim-cards")
public class SimCardController {

    private final SimCardMasterDataService service;

    public SimCardController(SimCardMasterDataService service) {
        this.service = service;
    }

    @Operation(summary = "List sim cards", description = "Pageable, search by keyword and status")
    @GetMapping
    public Page<SimCardEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterDataStatus status,
            Pageable pageable) {
        return service.page(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public SimCardEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("SimCard not found: id=" + id));
    }

    @Operation(summary = "Get sim card binding history")
    @GetMapping("/{id}/history")
    public List<SimBindingLogEntity> getHistory(@PathVariable Long id, Pageable pageable) {
        return service.getBindingHistory(id, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimCardEntity create(@RequestBody SimCardCreateRequest req) {
        return service.save(req.cardNo(), req.operator(), req.planInfo(), req.machineId(), req.createdBy());
    }

    @PutMapping("/{id}")
    public SimCardEntity update(@PathVariable Long id, @RequestBody SimCardUpdateRequest req) {
        return service.update(id, req.cardNo(), req.operator(), req.planInfo(), req.machineId(), req.updatedBy());
    }

    @PostMapping("/{id}/disable")
    public SimCardEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record SimCardCreateRequest(String cardNo, String operator, String planInfo, Long machineId, String createdBy) {}
    public record SimCardUpdateRequest(String cardNo, String operator, String planInfo, Long machineId, String updatedBy) {}
}
