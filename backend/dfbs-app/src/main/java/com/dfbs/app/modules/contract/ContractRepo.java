package com.dfbs.app.modules.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepo extends JpaRepository<ContractEntity, UUID> {
    Optional<ContractEntity> findByContractNo(String contractNo);
}
