package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SimCardRepo extends JpaRepository<SimCardEntity, Long>, JpaSpecificationExecutor<SimCardEntity> {
    Optional<SimCardEntity> findByCardNo(String cardNo);
    boolean existsByCardNo(String cardNo);
}
