package com.dfbs.app.modules.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhInventoryRepo extends JpaRepository<WhInventoryEntity, Long> {

    Optional<WhInventoryEntity> findByWarehouseIdAndPartNo(Long warehouseId, String partNo);

    List<WhInventoryEntity> findByWarehouseId(Long warehouseId);
}
