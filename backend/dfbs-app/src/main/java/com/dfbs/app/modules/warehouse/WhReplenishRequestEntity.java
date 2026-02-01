package com.dfbs.app.modules.warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 补货申请单. Target is satellite warehouse; L1/L2 approval fields.
 */
@Entity
@Table(name = "wh_replenish_request", uniqueConstraints = @UniqueConstraint(columnNames = {"request_no"}))
@Getter
@Setter
public class WhReplenishRequestEntity extends BaseAuditEntity {

    @Column(name = "request_no", nullable = false, length = 64)
    private String requestNo;

    @Column(name = "target_warehouse_id", nullable = false)
    private Long targetWarehouseId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "part_no", nullable = false, length = 128)
    private String partNo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReplenishStatus status;

    @Column(name = "l1_approver_id")
    private Long l1ApproverId;

    @Column(name = "l1_comment", length = 500)
    private String l1Comment;

    @Column(name = "l1_time")
    private LocalDateTime l1Time;

    @Column(name = "l2_approver_id")
    private Long l2ApproverId;

    @Column(name = "l2_comment", length = 500)
    private String l2Comment;

    @Column(name = "l2_time")
    private LocalDateTime l2Time;
}
