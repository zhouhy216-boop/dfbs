package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "perm_audit_log")
@Getter
@Setter
public class PermAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_username", length = 128)
    private String actorUsername;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_key", length = 128)
    private String targetKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "note", length = 512)
    private String note;
}
