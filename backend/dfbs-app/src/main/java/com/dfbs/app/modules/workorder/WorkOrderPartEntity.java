package com.dfbs.app.modules.workorder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单用料清单. Links to warehouse outbound via stockRecordId.
 */
@Entity
@Table(name = "work_order_part")
@Getter
@Setter
public class WorkOrderPartEntity extends BaseAuditEntity {

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "part_no", nullable = false, length = 128)
    private String partNo;

    @Column(name = "part_name", length = 256)
    private String partName;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_status", nullable = false, length = 32)
    private PartUsageStatus usageStatus;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "stock_record_id")
    private Long stockRecordId;
}
