package com.dfbs.app.modules.permission;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "permission_request")
@Data
public class PermissionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "expected_time", nullable = false, length = 128)
    private String expectedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "snapshot_before", columnDefinition = "TEXT")
    private String snapshotBefore;

    @Column(name = "snapshot_after", columnDefinition = "TEXT")
    private String snapshotAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PermissionRequestEntity() {}
}
