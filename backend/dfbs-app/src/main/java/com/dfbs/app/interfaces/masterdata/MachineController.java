package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.MachineMasterDataService;
import com.dfbs.app.modules.masterdata.MachineEntity;
import com.dfbs.app.modules.masterdata.MachineOwnershipLogEntity;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Master Data - Machines", description = "Machine master data (soft delete, ownership history)")
@RestController
@RequestMapping("/api/v1/masterdata/machines")
public class MachineController {

    private final MachineMasterDataService service;

    public MachineController(MachineMasterDataService service) {
        this.service = service;
    }

    @Operation(summary = "List machines", description = "Pageable, search by keyword and status")
    @GetMapping
    public Page<MachineEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterDataStatus status,
            Pageable pageable) {
        return service.page(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public MachineEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("Machine not found: id=" + id));
    }

    @Operation(summary = "Get machine ownership history")
    @GetMapping("/{id}/history")
    public List<MachineOwnershipLogEntity> getHistory(@PathVariable Long id, Pageable pageable) {
        return service.getOwnershipHistory(id, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineEntity create(@RequestBody MachineCreateRequest req) {
        return service.save(req.machineNo(), req.serialNo(), req.customerId(), req.contractId(), req.modelId(), req.createdBy());
    }

    @PutMapping("/{id}")
    public MachineEntity update(@PathVariable Long id, @RequestBody MachineUpdateRequest req) {
        return service.update(id, req.machineNo(), req.serialNo(), req.customerId(), req.contractId(), req.modelId(), req.updatedBy());
    }

    @PostMapping("/{id}/disable")
    public MachineEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record MachineCreateRequest(String machineNo, String serialNo, Long customerId, Long contractId, Long modelId, String createdBy) {}
    public record MachineUpdateRequest(String machineNo, String serialNo, Long customerId, Long contractId, Long modelId, String updatedBy) {}
}
