package com.dfbs.app.modules.damage;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_record")
@Data
public class DamageRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "shipment_machine_id", nullable = false)
    private Long shipmentMachineId;

    @Column(name = "occurrence_time", nullable = false)
    private LocalDateTime occurrenceTime;

    @Column(name = "damage_type_id", nullable = false)
    private Long damageTypeId;

    @Column(name = "treatment_id", nullable = false)
    private Long treatmentId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Mandatory. JSON array of photo URLs. */
    @Column(name = "attachment_urls", nullable = false, columnDefinition = "TEXT")
    private String attachmentUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_stage", length = 32)
    private RepairStage repairStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "compensation_status", length = 32)
    private CompensationStatus compensationStatus;

    @Column(name = "settlement_details", columnDefinition = "TEXT")
    private String settlementDetails;

    @Column(name = "compensation_amount", precision = 19, scale = 4)
    private BigDecimal compensationAmount;

    @Column(name = "repair_fee", precision = 19, scale = 4)
    private BigDecimal repairFee;

    @Column(name = "penalty_amount", precision = 19, scale = 4)
    private BigDecimal penaltyAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "operator_id")
    private Long operatorId;

    public DamageRecordEntity() {}
}
