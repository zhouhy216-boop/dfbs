package com.dfbs.app.interfaces.customer;

import com.dfbs.app.application.customer.CustomerMasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

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

        var result = service.createCustomer(req.customerNo(), req.name());

        return new CreateCustomerResponse(
                result.customerNo(),
                result.name(),
                result.createdAt()
        );
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
