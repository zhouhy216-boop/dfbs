package com.dfbs.app.quote;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_version")
public class QuoteVersionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "quote_no", nullable = false)
    private String quoteNo;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "active_at")
    private OffsetDateTime activeAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuoteNo() { return quoteNo; }
    public void setQuoteNo(String quoteNo) { this.quoteNo = quoteNo; }

    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public OffsetDateTime getActiveAt() { return activeAt; }
    public void setActiveAt(OffsetDateTime activeAt) { this.activeAt = activeAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
