package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MachineRepo extends JpaRepository<MachineEntity, Long>, JpaSpecificationExecutor<MachineEntity> {
    Optional<MachineEntity> findByMachineNo(String machineNo);
    Optional<MachineEntity> findBySerialNo(String serialNo);
    boolean existsByMachineNo(String machineNo);
    boolean existsBySerialNo(String serialNo);
}
