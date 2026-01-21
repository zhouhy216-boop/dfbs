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
    public CustomerEntity create(String customerNo, String name) {
        repo.findByCustomerCode(customerNo)
                .ifPresent(c -> {
                    throw new IllegalStateException("customerNo already exists: " + customerNo);
                });

        CustomerEntity entity = CustomerEntity.create(customerNo, name);
        return repo.save(entity);
    }

    // ===== 新增：Read（按 id，过滤软删除）=====
    @Transactional(readOnly = true)
    public CustomerEntity getById(UUID id) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer not found");
        }

        return entity;
    }

    @Transactional
    public CustomerEntity updateName(UUID id, String name) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer already deleted");
        }

        entity.updateName(name);
        return repo.save(entity);
    }

    @Transactional
    public void softDelete(UUID id) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer already deleted");
        }

        entity.setDeletedAt(OffsetDateTime.now());
        repo.save(entity);
    }
}
