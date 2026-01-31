package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.SparePartMasterDataService;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import com.dfbs.app.modules.masterdata.SparePartEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Master Data - Spare Parts", description = "Spare part master data (soft delete)")
@RestController
@RequestMapping("/api/v1/masterdata/spare-parts")
public class SparePartController {

    private final SparePartMasterDataService service;

    public SparePartController(SparePartMasterDataService service) {
        this.service = service;
    }

    @Operation(summary = "List spare parts", description = "Pageable, search by keyword and status")
    @GetMapping
    public Page<SparePartEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterDataStatus status,
            Pageable pageable) {
        return service.page(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public SparePartEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("SparePart not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SparePartEntity create(@RequestBody SparePartCreateRequest req) {
        return service.save(req.partNo(), req.name(), req.spec(), req.unit(), req.referencePrice(), req.createdBy());
    }

    @PutMapping("/{id}")
    public SparePartEntity update(@PathVariable Long id, @RequestBody SparePartUpdateRequest req) {
        return service.update(id, req.partNo(), req.name(), req.spec(), req.unit(), req.referencePrice(), req.updatedBy());
    }

    @PostMapping("/{id}/disable")
    public SparePartEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record SparePartCreateRequest(String partNo, String name, String spec, String unit, BigDecimal referencePrice, String createdBy) {}
    public record SparePartUpdateRequest(String partNo, String name, String spec, String unit, BigDecimal referencePrice, String updatedBy) {}
}
