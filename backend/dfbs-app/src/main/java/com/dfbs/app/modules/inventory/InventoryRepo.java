package com.dfbs.app.modules.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepo extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByWarehouseIdAndSku(Long warehouseId, String sku);

    List<InventoryEntity> findByWarehouseId(Long warehouseId);
}
