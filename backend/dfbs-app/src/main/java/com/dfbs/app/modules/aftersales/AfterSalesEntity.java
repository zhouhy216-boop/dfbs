package com.dfbs.app.modules.aftersales;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "after_sales")
@Data
public class AfterSalesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private AfterSalesType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AfterSalesStatus status = AfterSalesStatus.DRAFT;

    @Column(name = "source_shipment_id", nullable = false)
    private Long sourceShipmentId;

    @Column(name = "machine_no", nullable = false, length = 128)
    private String machineNo;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /** JSON array of attachment URLs or file names. Required when status becomes SUBMITTED. */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "related_new_shipment_id")
    private Long relatedNewShipmentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AfterSalesEntity() {}
}
