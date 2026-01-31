package com.dfbs.app.modules.customer;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "md_customer_merge_log")
public class CustomerMergeLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "source_customer_id", nullable = false)
    private Long sourceCustomerId;

    @Column(name = "target_customer_id", nullable = false)
    private Long targetCustomerId;

    @Column(name = "source_snapshot", columnDefinition = "TEXT")
    private String sourceSnapshot;

    @Column(name = "target_snapshot", columnDefinition = "TEXT")
    private String targetSnapshot;

    @Column(name = "merge_reason", length = 512)
    private String mergeReason;

    public CustomerMergeLogEntity() {
    }

    public Long getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Long getSourceCustomerId() { return sourceCustomerId; }
    public void setSourceCustomerId(Long sourceCustomerId) { this.sourceCustomerId = sourceCustomerId; }
    public Long getTargetCustomerId() { return targetCustomerId; }
    public void setTargetCustomerId(Long targetCustomerId) { this.targetCustomerId = targetCustomerId; }
    public String getSourceSnapshot() { return sourceSnapshot; }
    public void setSourceSnapshot(String sourceSnapshot) { this.sourceSnapshot = sourceSnapshot; }
    public String getTargetSnapshot() { return targetSnapshot; }
    public void setTargetSnapshot(String targetSnapshot) { this.targetSnapshot = targetSnapshot; }
    public String getMergeReason() { return mergeReason; }
    public void setMergeReason(String mergeReason) { this.mergeReason = mergeReason; }
}
