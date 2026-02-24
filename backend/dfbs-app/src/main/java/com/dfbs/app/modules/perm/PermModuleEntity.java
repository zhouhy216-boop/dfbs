package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perm_module")
@Getter
@Setter
public class PermModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, unique = true, length = 64)
    private String moduleKey;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "parent_id")
    private Long parentId;
}
