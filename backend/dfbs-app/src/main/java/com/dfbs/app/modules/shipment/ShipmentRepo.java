package com.dfbs.app.modules.shipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentRepo extends JpaRepository<ShipmentEntity, Long>, JpaSpecificationExecutor<ShipmentEntity> {

    /** For freight bill: available shipments by carrier, not yet linked to any bill, status SHIPPED or COMPLETED. */
    List<ShipmentEntity> findByCarrierAndFreightBillIdIsNullAndStatusIn(String carrier, List<ShipmentStatus> statuses);

    /** By carrier_id, not linked, status in list, createdAt between. */
    List<ShipmentEntity> findByCarrierIdAndFreightBillIdIsNullAndStatusInAndCreatedAtBetween(
            Long carrierId, List<ShipmentStatus> statuses, LocalDateTime from, LocalDateTime to);
}
