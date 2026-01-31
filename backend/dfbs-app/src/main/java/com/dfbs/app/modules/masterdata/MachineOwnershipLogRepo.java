package com.dfbs.app.modules.masterdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineOwnershipLogRepo extends JpaRepository<MachineOwnershipLogEntity, Long> {
    List<MachineOwnershipLogEntity> findByMachineIdOrderByChangedAtDesc(Long machineId, Pageable pageable);
}
