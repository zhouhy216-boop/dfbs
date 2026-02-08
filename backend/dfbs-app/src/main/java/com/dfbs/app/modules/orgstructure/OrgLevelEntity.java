package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_level")
@Getter
@Setter
public class OrgLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

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
