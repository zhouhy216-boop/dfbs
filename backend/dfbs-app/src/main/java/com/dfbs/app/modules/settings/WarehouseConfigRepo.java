package com.dfbs.app.modules.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseConfigRepo extends JpaRepository<WarehouseConfigEntity, Long> {
    // Singleton pattern: always use id=1
    Optional<WarehouseConfigEntity> findById(Long id);
}
