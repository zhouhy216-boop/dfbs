package com.dfbs.app.application.contract;

import com.dfbs.app.modules.contract.ContractEntity;
import com.dfbs.app.modules.contract.ContractRepo;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ContractMasterDataService {

    private final ContractRepo contractRepo;
    private final CustomerRepo customerRepo;

    public ContractMasterDataService(
            ContractRepo contractRepo,
            CustomerRepo customerRepo
    ) {
        this.contractRepo = contractRepo;
        this.customerRepo = customerRepo;
    }

    public ContractEntity create(String contractNo, String customerCode) {
        customerRepo.findByCustomerCodeAndDeletedAtIsNull(customerCode)
                .orElseThrow(() -> new IllegalStateException("customer not found"));

        ContractEntity entity = new ContractEntity();
        entity.setId(UUID.randomUUID());
        entity.setContractNo(contractNo);
        entity.setCustomerCode(customerCode);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        return contractRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<ContractEntity> search(String keyword, Pageable pageable) {
        Specification<ContractEntity> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            Specification<ContractEntity> keywordSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("contractNo")), "%" + keyword.toLowerCase() + "%");
            spec = spec.and(keywordSpec);
        }

        return contractRepo.findAll(spec, pageable);
    }
}
