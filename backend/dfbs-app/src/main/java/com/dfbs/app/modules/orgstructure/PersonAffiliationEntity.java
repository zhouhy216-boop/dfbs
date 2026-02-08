package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "person_affiliation",
        uniqueConstraints = @UniqueConstraint(name = "uk_pa_person_org", columnNames = {"person_id", "org_node_id"}))
@Getter
@Setter
public class PersonAffiliationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private OrgPersonEntity person;

    @Column(name = "org_node_id", nullable = false)
    private Long orgNodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_node_id", insertable = false, updatable = false)
    private OrgNodeEntity orgNode;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
}
