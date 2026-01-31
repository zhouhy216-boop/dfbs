package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sim_binding_log")
@Getter
@Setter
public class SimBindingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sim_id", nullable = false)
    private Long simId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private SimBindingAction action;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", length = 64)
    private String changedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public enum SimBindingAction {
        BIND,
        UNBIND
    }
}
