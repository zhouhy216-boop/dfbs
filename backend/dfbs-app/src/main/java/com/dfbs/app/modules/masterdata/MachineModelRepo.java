package com.dfbs.app.modules.masterdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MachineModelRepo extends JpaRepository<MachineModelEntity, Long>, JpaSpecificationExecutor<MachineModelEntity> {
    Optional<MachineModelEntity> findByModelNo(String modelNo);
    boolean existsByModelNo(String modelNo);
}
