package com.dfbs.app.modules.iccid;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "md_iccid")
public class IccidEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "iccid_no", nullable = false, length = 32, unique = true)
    private String iccidNo;

    @Column(name = "machine_sn", length = 64)
    private String machineSn; // 允许为空/解绑

    @Column(name = "plan", length = 128)
    private String plan;  // e.g. "100MB/Month"

    @Column(name = "platform", length = 128)
    private String platform;  // e.g. "China Mobile"

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public IccidEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIccidNo() { return iccidNo; }
    public void setIccidNo(String iccidNo) { this.iccidNo = iccidNo; }

    public String getMachineSn() { return machineSn; }
    public void setMachineSn(String machineSn) { this.machineSn = machineSn; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
