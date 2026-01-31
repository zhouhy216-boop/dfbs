package com.dfbs.app.modules.shipment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentMachineRepo extends JpaRepository<ShipmentMachineEntity, Long> {
    List<ShipmentMachineEntity> findByShipmentIdOrderByIdAsc(Long shipmentId);

    long countByShipmentId(Long shipmentId);

    /** Latest shipment for a machine (Machine -> Shipment). */
    java.util.Optional<ShipmentMachineEntity> findTopByMachineNoOrderByShipmentIdDesc(String machineNo);

    void deleteByShipmentId(Long shipmentId);
}
