package com.dfbs.app.modules.settings;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "business_line")
@Data
public class BusinessLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "leader_ids", length = 1000)
    private String leaderIds;  // JSON array or comma-separated user IDs

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public BusinessLineEntity() {}
}
