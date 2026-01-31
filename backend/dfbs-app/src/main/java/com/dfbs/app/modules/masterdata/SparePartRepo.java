package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SparePartRepo extends JpaRepository<SparePartEntity, Long>, JpaSpecificationExecutor<SparePartEntity> {
    Optional<SparePartEntity> findByPartNo(String partNo);
    boolean existsByPartNo(String partNo);
}
