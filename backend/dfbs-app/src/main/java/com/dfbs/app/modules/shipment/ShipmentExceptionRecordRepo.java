package com.dfbs.app.modules.shipment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentExceptionRecordRepo extends JpaRepository<ShipmentExceptionRecordEntity, Long> {

    List<ShipmentExceptionRecordEntity> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);

    List<ShipmentExceptionRecordEntity> findByShipmentIdAndMachineIdOrderByCreatedAtDesc(Long shipmentId, Long machineId);
}
