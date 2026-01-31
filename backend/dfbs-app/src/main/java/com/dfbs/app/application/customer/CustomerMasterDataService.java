package com.dfbs.app.application.customer;

import com.dfbs.app.modules.customer.CustomerEntity;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.JoinType;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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
        ensureNameUniqueForCreate(name);

        CustomerEntity entity = CustomerEntity.create(customerNo, name);
        return repo.save(entity);
    }

    @Transactional(readOnly = true)
    public CustomerEntity getById(Long id) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer not found");
        }

        return entity;
    }

    @Transactional
    public CustomerEntity updateName(Long id, String name) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer already deleted");
        }

        ensureNameUniqueForUpdate(name, id);
        entity.updateName(name);
        return repo.save(entity);
    }

    @Transactional
    public void softDelete(Long id) {
        CustomerEntity entity = repo.findById(id)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        if (entity.getDeletedAt() != null) {
            throw new IllegalStateException("customer already deleted");
        }

        entity.setDeletedAt(OffsetDateTime.now());
        repo.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<CustomerEntity> search(String keyword, Pageable pageable) {
        Specification<CustomerEntity> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "ACTIVE"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            Specification<CustomerEntity> keywordSpec = (root, query, cb) -> {
                query.distinct(true);
                var nameMatch = cb.like(cb.lower(root.get("name")), kw);
                var aliasJoin = root.join("aliases", JoinType.LEFT);
                var aliasMatch = cb.like(cb.lower(aliasJoin.get("aliasName")), kw);
                return cb.or(nameMatch, aliasMatch);
            };
            spec = spec.and(keywordSpec);
        }

        return repo.findAll(spec, pageable);
    }

    private void ensureNameUniqueForCreate(String name) {
        if (name == null || name.isBlank()) return;
        if (repo.existsByNameAndStatusAndDeletedAtIsNull(name.trim(), "ACTIVE")) {
            throw new DataIntegrityViolationException("客户名称已存在");
        }
    }

    private void ensureNameUniqueForUpdate(String name, Long currentId) {
        if (name == null || name.isBlank()) return;
        if (repo.existsByNameAndStatusAndDeletedAtIsNullAndIdNot(name.trim(), "ACTIVE", currentId)) {
            throw new DataIntegrityViolationException("客户名称已存在");
        }
    }
}
