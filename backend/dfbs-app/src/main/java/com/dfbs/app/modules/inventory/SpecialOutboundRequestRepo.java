package com.dfbs.app.modules.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialOutboundRequestRepo extends JpaRepository<SpecialOutboundRequestEntity, Long> {
    List<SpecialOutboundRequestEntity> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
}
