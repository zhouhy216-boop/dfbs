package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.MachineModelMasterDataService;
import com.dfbs.app.modules.masterdata.MachineModelEntity;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Master Data - Machine Models", description = "Machine model master data (soft delete)")
@RestController
@RequestMapping("/api/v1/masterdata/machine-models")
public class MachineModelController {

    private final MachineModelMasterDataService service;

    public MachineModelController(MachineModelMasterDataService service) {
        this.service = service;
    }

    @Operation(summary = "List machine models", description = "Pageable, search by keyword and status")
    @GetMapping
    public Page<MachineModelEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterDataStatus status,
            Pageable pageable) {
        return service.page(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public MachineModelEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("MachineModel not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineModelEntity create(@RequestBody MachineModelCreateRequest req) {
        return service.save(req.modelName(), req.modelNo(), req.freightInfo(), req.warrantyInfo(), req.createdBy());
    }

    @PutMapping("/{id}")
    public MachineModelEntity update(@PathVariable Long id, @RequestBody MachineModelUpdateRequest req) {
        return service.update(id, req.modelName(), req.modelNo(), req.freightInfo(), req.warrantyInfo(), req.updatedBy());
    }

    @PostMapping("/{id}/disable")
    public MachineModelEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record MachineModelCreateRequest(String modelName, String modelNo, String freightInfo, String warrantyInfo, String createdBy) {}
    public record MachineModelUpdateRequest(String modelName, String modelNo, String freightInfo, String warrantyInfo, String updatedBy) {}
}
