package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.ContractMasterDataService;
import com.dfbs.app.modules.masterdata.ContractEntity;
import com.dfbs.app.modules.masterdata.MasterDataStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Master Data - Contracts", description = "Contract master data (soft delete)")
@RestController
@RequestMapping("/api/v1/masterdata/contracts")
public class ContractController {

    private final ContractMasterDataService service;

    public ContractController(ContractMasterDataService service) {
        this.service = service;
    }

    @Operation(summary = "List contracts", description = "Pageable, search by keyword and status")
    @GetMapping
    public Page<ContractEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterDataStatus status,
            Pageable pageable) {
        return service.page(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public ContractEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("Contract not found: id=" + id));
    }

    @Operation(summary = "Create contract")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractEntity create(@RequestBody ContractCreateRequest req) {
        return service.save(
                req.contractNo(),
                req.customerId(),
                req.startDate(),
                req.endDate(),
                req.attachment(),
                req.createdBy()
        );
    }

    @PutMapping("/{id}")
    public ContractEntity update(@PathVariable Long id, @RequestBody ContractUpdateRequest req) {
        return service.update(id, req.contractNo(), req.customerId(), req.startDate(), req.endDate(),
                req.attachment(), req.updatedBy());
    }

    @Operation(summary = "Soft disable contract")
    @PostMapping("/{id}/disable")
    public ContractEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record ContractCreateRequest(String contractNo, Long customerId, LocalDate startDate, LocalDate endDate,
                                        String attachment, String createdBy) {}
    public record ContractUpdateRequest(String contractNo, Long customerId, LocalDate startDate, LocalDate endDate,
                                        String attachment, String updatedBy) {}
}
