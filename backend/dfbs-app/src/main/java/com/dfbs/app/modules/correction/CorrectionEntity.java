package com.dfbs.app.modules.correction;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "correction_record")
@Data
public class CorrectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correction_no", nullable = false, unique = true, length = 64)
    private String correctionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private CorrectionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CorrectionStatus status = CorrectionStatus.DRAFT;

    @Column(name = "changes_json", columnDefinition = "TEXT")
    private String changesJson;

    @Column(name = "new_record_id")
    private Long newRecordId;

    @Column(name = "occurred_date", nullable = false)
    private LocalDate occurredDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public CorrectionEntity() {}
}
