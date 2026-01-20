package com.dfbs.app.application.customer;

import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CustomerMasterDataService {

    private final CustomerRepo repo;

    public CustomerMasterDataService(CustomerRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public CreateCustomerResult createCustomer(String customerNoRaw, String nameRaw) {

        String customerNo = normalizeRequired(customerNoRaw, "customerNo");
        String name = normalizeRequired(nameRaw, "name");

        // 内部字段叫 customerCode，但对外字段统一为 customerNo
        repo.findByCustomerCodeAndDeletedAtIsNull(customerNo).ifPresent(e -> {
            throw new IllegalStateException("customerNo already exists: " + customerNo);
        });

        CustomerEntity entity = new CustomerEntity();
        entity.setId(UUID.randomUUID());
        entity.setCustomerCode(customerNo);
        entity.setName(name);

        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        CustomerEntity saved = repo.save(entity);

        return new CreateCustomerResult(
                customerNo,
                saved.getName(),
                saved.getCreatedAt()
        );
    }

    private String normalizeRequired(String raw, String field) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return raw.trim();
    }

    public record CreateCustomerResult(
            String customerNo,
            String name,
            OffsetDateTime createdAt
    ) {}
}
