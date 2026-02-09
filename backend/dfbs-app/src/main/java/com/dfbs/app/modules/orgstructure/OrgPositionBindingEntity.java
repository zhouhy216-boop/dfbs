package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_position_binding",
        uniqueConstraints = @UniqueConstraint(name = "uk_opb_org_position_person", columnNames = {"org_node_id", "position_id", "person_id"}))
@Getter
@Setter
public class OrgPositionBindingEntity {

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

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private OrgPersonEntity person;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
