package com.dfbs.app.modules.perm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perm_action")
@Getter
@Setter
public class PermActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_key", nullable = false, unique = true, length = 64)
    private String actionKey;

    @Column(name = "label", nullable = false, length = 128)
    private String label;
}
