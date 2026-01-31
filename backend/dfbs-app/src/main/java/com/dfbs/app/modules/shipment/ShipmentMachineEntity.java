package com.dfbs.app.modules.shipment;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shipment_machine")
@Data
public class ShipmentMachineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "model", length = 256)
    private String model;

    @Column(name = "machine_no", nullable = false, length = 128)
    private String machineNo;

    public ShipmentMachineEntity() {}
}
