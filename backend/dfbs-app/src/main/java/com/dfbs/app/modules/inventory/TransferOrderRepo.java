package com.dfbs.app.modules.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferOrderRepo extends JpaRepository<TransferOrderEntity, Long> {
    List<TransferOrderEntity> findBySourceWarehouseIdOrderByCreatedAtDesc(Long sourceWarehouseId);

    List<TransferOrderEntity> findByTargetWarehouseIdOrderByCreatedAtDesc(Long targetWarehouseId);
}
