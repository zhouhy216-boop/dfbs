package com.dfbs.app.modules.repair;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_record", uniqueConstraints = @UniqueConstraint(columnNames = "old_work_order_no"))
@Data
public class RepairRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 256)
    private String customerName;

    @Column(name = "machine_no", nullable = false, length = 128)
    private String machineNo;

    @Column(name = "machine_model", nullable = false, length = 256)
    private String machineModel;

    @Column(name = "repair_date", nullable = false)
    private LocalDateTime repairDate;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "resolution", nullable = false, columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "person_in_charge", nullable = false, length = 128)
    private String personInCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_status", nullable = false, length = 32)
    private WarrantyStatus warrantyStatus;

    @Column(name = "old_work_order_no", nullable = false, unique = true, length = 128)
    private String oldWorkOrderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private RepairSource sourceType;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "operator_id")
    private Long operatorId;

    public RepairRecordEntity() {}
}
