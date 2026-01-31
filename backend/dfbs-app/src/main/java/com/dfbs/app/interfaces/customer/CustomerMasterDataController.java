package com.dfbs.app.interfaces.customer;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.application.customer.CustomerMergeService;
import com.dfbs.app.application.customer.dto.CustomerMergeResponse;
import com.dfbs.app.modules.customer.CustomerEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "MasterData (Customer)", description = "Customer CRUD, merge, search")
@RestController
public class CustomerMasterDataController {

    private final CustomerMasterDataService service;
    private final CustomerMergeService mergeService;

    public CustomerMasterDataController(CustomerMasterDataService service, CustomerMergeService mergeService) {
        this.service = service;
        this.mergeService = mergeService;
    }

    @PostMapping("/api/masterdata/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerEntity create(@RequestBody Map<String, String> body) {
        return service.create(body.get("customerNo"), body.get("name"));
    }

    @GetMapping("/api/masterdata/customers/{id}")
    public CustomerEntity getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/api/masterdata/customers/{id}")
    public CustomerEntity updateName(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return service.updateName(id, body.get("name"));
    }

    @DeleteMapping("/api/masterdata/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }

    @PostMapping("/api/masterdata/customers/merge")
    public CustomerMergeResponse merge(@RequestBody CustomerMergeRequest body) {
        return mergeService.merge(body.targetId(), body.sourceId(), body.fieldOverrides(), body.mergeReason(), body.operatorId());
    }

    @PostMapping("/api/masterdata/customers/merge/{logId}/undo")
    public void mergeUndo(@PathVariable Long logId) {
        mergeService.undo(logId);
    }

    @Operation(summary = "Search customers", description = "Paginated search by keyword")
    @GetMapping("/api/v1/customers")
    public ResponseEntity<Page<CustomerDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<CustomerEntity> page = service.search(keyword, pageable);
        Page<CustomerDto> dtoPage = page.map(CustomerDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    // ==================================================
    // 关键修复点：业务 not found → HTTP 404
    // ==================================================
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(IllegalStateException ex) {
        // 不需要返回 body，404 即可
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleDuplicateName(DataIntegrityViolationException ex) {
        // 客户名称已存在 → 409
    }
}
