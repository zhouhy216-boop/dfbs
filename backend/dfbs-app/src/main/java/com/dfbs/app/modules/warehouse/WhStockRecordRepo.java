package com.dfbs.app.modules.warehouse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhStockRecordRepo extends JpaRepository<WhStockRecordEntity, Long> {

    List<WhStockRecordEntity> findByWarehouseIdAndPartNoOrderByCreatedAtDesc(Long warehouseId, String partNo, Pageable pageable);
}
