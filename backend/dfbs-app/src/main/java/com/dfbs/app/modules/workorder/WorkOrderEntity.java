package com.dfbs.app.modules.workorder;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_order")
@Data
public class WorkOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkOrderStatus status = WorkOrderStatus.CREATED;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public WorkOrderEntity() {}
}
