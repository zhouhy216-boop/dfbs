package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perm_role")
@Getter
@Setter
public class PermRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key", nullable = false, unique = true, length = 64)
    private String roleKey;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
}
