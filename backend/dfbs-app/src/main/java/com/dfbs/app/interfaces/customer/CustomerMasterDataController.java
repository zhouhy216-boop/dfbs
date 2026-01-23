package com.dfbs.app.interfaces.customer;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.modules.customer.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class CustomerMasterDataController {

    private final CustomerMasterDataService service;

    public CustomerMasterDataController(CustomerMasterDataService service) {
        this.service = service;
    }

    @PostMapping("/api/masterdata/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerEntity create(@RequestBody Map<String, String> body) {
        return service.create(body.get("customerNo"), body.get("name"));
    }

    // ===== Read（按 id，过滤软删除）=====
    @GetMapping("/api/masterdata/customers/{id}")
    public CustomerEntity getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // ===== Update（最小）=====
    @PatchMapping("/api/masterdata/customers/{id}")
    public CustomerEntity updateName(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        return service.updateName(id, body.get("name"));
    }

    // ===== Soft Delete =====
    @DeleteMapping("/api/masterdata/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.softDelete(id);
    }

    // ===== Search =====
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
}
