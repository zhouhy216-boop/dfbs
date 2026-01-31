package com.dfbs.app.interfaces.masterdata;

import com.dfbs.app.application.masterdata.ModelPartListBomService;
import com.dfbs.app.application.masterdata.ModelPartListMasterDataService;
import com.dfbs.app.application.masterdata.dto.CreateDraftRequest;
import com.dfbs.app.application.masterdata.dto.CreateDraftResponse;
import com.dfbs.app.modules.masterdata.BomConflictEntity;
import com.dfbs.app.modules.masterdata.BomStatus;
import com.dfbs.app.modules.masterdata.ModelPartListEntity;
import com.dfbs.app.modules.masterdata.ResolutionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Master Data - Model Part Lists (BOM)", description = "BOM draft, publish, conflicts")
@RestController
@RequestMapping("/api/v1/masterdata/model-part-lists")
public class ModelPartListController {

    private final ModelPartListMasterDataService service;
    private final ModelPartListBomService bomService;

    public ModelPartListController(ModelPartListMasterDataService service, ModelPartListBomService bomService) {
        this.service = service;
        this.bomService = bomService;
    }

    @Operation(summary = "List model part lists", description = "Pageable, search by keyword, status (DRAFT/PUBLISHED/DEPRECATED), modelId")
    @GetMapping
    public Page<ModelPartListEntity> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BomStatus status,
            @RequestParam(required = false) Long modelId,
            Pageable pageable) {
        return service.page(keyword, status, modelId, pageable);
    }

    @GetMapping("/{id}")
    public ModelPartListEntity getById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("ModelPartList not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelPartListEntity create(@RequestBody ModelPartListCreateRequest req) {
        return service.save(req.modelId(), req.version(), req.effectiveDate(), req.items(), req.createdBy());
    }

    @PutMapping("/{id}")
    public ModelPartListEntity update(@PathVariable Long id, @RequestBody ModelPartListUpdateRequest req) {
        return service.update(id, req.modelId(), req.version(), req.effectiveDate(), req.items(), req.updatedBy());
    }

    @PostMapping("/{id}/disable")
    public ModelPartListEntity disable(@PathVariable Long id, @RequestBody(required = false) DisableRequest req) {
        return service.disable(id, req != null ? req.updatedBy() : null);
    }

    @Operation(summary = "Create BOM draft", description = "Auto-create SpareParts for unknown partNo; create conflicts for name mismatch or missing partNo")
    @PostMapping("/draft")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDraftResponse createDraft(@RequestBody CreateDraftRequest request) {
        return bomService.createDraft(request);
    }

    @Operation(summary = "Publish BOM", description = "Fails if any PENDING conflicts exist")
    @PostMapping("/{id}/publish")
    public ModelPartListEntity publish(@PathVariable Long id) {
        return bomService.publish(id);
    }

    @Operation(summary = "List conflicts for a BOM")
    @GetMapping("/{id}/conflicts")
    public List<BomConflictEntity> getConflicts(@PathVariable Long id) {
        return bomService.getConflicts(id);
    }

    @Operation(summary = "Resolve a conflict", description = "ResolutionType: KEEP_MASTER, OVERWRITE_MASTER, ADD_ALIAS, FIX_NO (customValue = new PartNo for MISSING_NO)")
    @PostMapping("/conflicts/{conflictId}/resolve")
    public BomConflictEntity resolveConflict(
            @PathVariable Long conflictId,
            @RequestBody ResolveConflictRequest request) {
        return bomService.resolveConflict(conflictId, request.resolutionType(), request.customValue());
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {}

    public record ModelPartListCreateRequest(Long modelId, String version, LocalDate effectiveDate, String items, String createdBy) {}
    public record ModelPartListUpdateRequest(Long modelId, String version, LocalDate effectiveDate, String items, String updatedBy) {}
    public record ResolveConflictRequest(ResolutionType resolutionType, String customValue) {}
}
