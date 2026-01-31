package com.dfbs.app.modules.bom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BomVersionRepo extends JpaRepository<BomVersionEntity, Long> {

    List<BomVersionEntity> findByMachineIdOrderByVersionDesc(Long machineId);

    Optional<BomVersionEntity> findByMachineIdAndIsActiveTrue(Long machineId);

    Optional<BomVersionEntity> findFirstByMachineIdOrderByVersionDesc(Long machineId);
}
