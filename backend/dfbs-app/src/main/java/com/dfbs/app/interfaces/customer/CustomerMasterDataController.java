package com.dfbs.app.interfaces.customer;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import com.dfbs.app.modules.customer.CustomerEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/masterdata/customers")
public class CustomerMasterDataController {

    private final CustomerMasterDataService service;

    public CustomerMasterDataController(CustomerMasterDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerEntity create(@RequestBody Map<String, String> body) {
        return service.create(body.get("customerNo"), body.get("name"));
    }

    // ===== Read（按 id，过滤软删除）=====
    @GetMapping("/{id}")
    public CustomerEntity getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // ===== Update（最小）=====
    @PatchMapping("/{id}")
    public CustomerEntity updateName(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        return service.updateName(id, body.get("name"));
    }

    // ===== Soft Delete =====
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.softDelete(id);
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
