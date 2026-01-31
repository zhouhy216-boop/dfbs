package com.dfbs.app.modules.masterdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ContractRepo extends JpaRepository<ContractEntity, Long>, JpaSpecificationExecutor<ContractEntity> {
    Optional<ContractEntity> findByContractNo(String contractNo);
    boolean existsByContractNo(String contractNo);
}
