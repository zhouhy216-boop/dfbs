package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "machine_ownership_log")
@Getter
@Setter
public class MachineOwnershipLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_id", nullable = false)
    private Long machineId;

    @Column(name = "old_customer_id")
    private Long oldCustomerId;

    @Column(name = "new_customer_id")
    private Long newCustomerId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", length = 64)
    private String changedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
