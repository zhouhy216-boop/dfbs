package com.dfbs.app.modules.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MachineRepo extends JpaRepository<MachineEntity, UUID> {
    Optional<MachineEntity> findByMachineSn(String machineSn);
}
