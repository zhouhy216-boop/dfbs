package com.dfbs.app.application.contract;

import com.dfbs.app.modules.contract.ContractEntity;
import com.dfbs.app.modules.contract.ContractRepo;
import com.dfbs.app.modules.customer.CustomerRepo;
import org.springframework.stereotype.Service;

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
}
