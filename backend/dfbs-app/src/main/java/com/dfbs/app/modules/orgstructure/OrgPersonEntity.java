package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "org_person")
@Getter
@Setter
public class OrgPersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "phone", nullable = false, length = 64)
    private String phone;

    @Column(name = "email", length = 256)
    private String email;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "job_level_id", nullable = false)
    private Long jobLevelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_level_id", insertable = false, updatable = false)
    private JobLevelEntity jobLevel;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @OneToMany(mappedBy = "personId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonAffiliationEntity> affiliations = new ArrayList<>();
}
