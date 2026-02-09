package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_position_enabled",
        uniqueConstraints = @UniqueConstraint(name = "uk_ope_org_position", columnNames = {"org_node_id", "position_id"}))
@Getter
@Setter
public class OrgPositionEnabledEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_node_id", nullable = false)
    private Long orgNodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_node_id", insertable = false, updatable = false)
    private OrgNodeEntity orgNode;

    @Column(name = "position_id", nullable = false)
    private Long positionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", insertable = false, updatable = false)
    private OrgPositionCatalogEntity position;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
