package com.dfbs.app.modules.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepo extends JpaRepository<WarehouseEntity, Long> {
    List<WarehouseEntity> findByType(WarehouseType type);
}
