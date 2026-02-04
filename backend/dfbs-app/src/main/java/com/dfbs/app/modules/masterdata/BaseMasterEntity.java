package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base entity for master data: id, audit fields, is_temp (Smart Select temp pool), last_used_at (MRU).
 * Subclasses add status and business fields.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Temp record (pending confirmation); excluded from standard search (Rule 2.4). */
    @Column(name = "is_temp", nullable = false)
    private Boolean isTemp = false;

    /** Last used at (for Global MRU sorting in Smart Select). */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
