package com.dfbs.app.modules.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepo extends JpaRepository<ContractEntity, UUID>, JpaSpecificationExecutor<ContractEntity> {
    Optional<ContractEntity> findByContractNo(String contractNo);
}
