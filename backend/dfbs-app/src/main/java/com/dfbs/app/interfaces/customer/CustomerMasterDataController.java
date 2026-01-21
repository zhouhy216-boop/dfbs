package com.dfbs.app.interfaces.customer;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/masterdata/customers")
public class CustomerMasterDataController {

    private final CustomerMasterDataService service;

    public CustomerMasterDataController(CustomerMasterDataService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCustomerResponse create(@RequestBody CreateCustomerRequest req) {
        try {
            var result = service.createCustomer(req.customerNo(), req.name());
            return new CreateCustomerResponse(
                    result.customerNo(),
                    result.name(),
                    result.createdAt()
            );
        } catch (IllegalArgumentException e) {
            // 参数问题
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            // 业务冲突（如 customerNo 已存在）
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    /**
     * 软删除（不物理删除）
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        try {
            service.softDelete(id);
        } catch (IllegalStateException e) {
            // 找不到或已删除
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    public record CreateCustomerRequest(
            String customerNo,
            String name
    ) {}

    public record CreateCustomerResponse(
            String customerNo,
            String name,
            OffsetDateTime createdAt
    ) {}
}
