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

        // ✅ 业务主键不复用：即使软删除也不能再创建相同 customerNo
        repo.findByCustomerCode(customerNo).ifPresent(e -> {
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

    /**
     * 软删除：只写 deletedAt，不做物理删除
     */
    @Transactional
    public void softDelete(UUID id) {
        CustomerEntity customer = repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalStateException("Customer not found or already deleted"));

        OffsetDateTime now = OffsetDateTime.now();
        customer.setDeletedAt(now);
        customer.setUpdatedAt(now);

        repo.save(customer);
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
