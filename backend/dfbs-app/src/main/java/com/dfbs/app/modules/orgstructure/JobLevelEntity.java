package com.dfbs.app.modules.orgstructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_level")
@Getter
@Setter
public class JobLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
