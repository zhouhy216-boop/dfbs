package com.dfbs.app.modules.workorder;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单过程记录. createdBy in BaseAuditEntity tracks who did it.
 */
@Entity
@Table(name = "work_order_record")
@Getter
@Setter
public class WorkOrderRecordEntity extends BaseAuditEntity {

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "attachment_url", length = 512)
    private String attachmentUrl;
}
