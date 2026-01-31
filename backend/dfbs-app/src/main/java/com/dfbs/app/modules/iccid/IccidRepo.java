package com.dfbs.app.modules.iccid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface IccidRepo extends JpaRepository<IccidEntity, UUID>, JpaSpecificationExecutor<IccidEntity> {
    Optional<IccidEntity> findByIccidNo(String iccidNo);

    Page<IccidEntity> findByIccidNoContainingIgnoreCase(String keyword, Pageable pageable);

    Page<IccidEntity> findByIccidNoContainingIgnoreCaseAndMachineSnIsNotNull(String keyword, Pageable pageable);

    Page<IccidEntity> findByIccidNoContainingIgnoreCaseAndMachineSnIsNull(String keyword, Pageable pageable);

    Page<IccidEntity> findByMachineSnIsNotNull(Pageable pageable);

    Page<IccidEntity> findByMachineSnIsNull(Pageable pageable);
}
