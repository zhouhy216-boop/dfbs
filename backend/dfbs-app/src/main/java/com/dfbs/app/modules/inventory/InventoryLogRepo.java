package com.dfbs.app.modules.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLogRepo extends JpaRepository<InventoryLogEntity, Long> {
    List<InventoryLogEntity> findByWarehouseIdAndSkuOrderByCreatedAtDesc(Long warehouseId, String sku, org.springframework.data.domain.Pageable pageable);
}
