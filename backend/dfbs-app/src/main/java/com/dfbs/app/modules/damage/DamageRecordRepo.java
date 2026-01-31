package com.dfbs.app.modules.damage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageRecordRepo extends JpaRepository<DamageRecordEntity, Long> {
    List<DamageRecordEntity> findByShipmentIdOrderByOccurrenceTimeDesc(Long shipmentId);
}
