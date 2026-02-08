package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "org_node")
@Getter
@Setter
public class OrgNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", insertable = false, updatable = false)
    private OrgLevelEntity level;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private OrgNodeEntity parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<OrgNodeEntity> children = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by", length = 64)
    private String updatedBy;
}
