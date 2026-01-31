package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sim_cards")
@Getter
@Setter
public class SimCardEntity extends BaseMasterEntity {

    @Column(name = "card_no", nullable = false, unique = true, length = 64)
    private String cardNo;

    @Column(name = "operator", length = 64)
    private String operator;

    @Column(name = "plan_info", columnDefinition = "TEXT")
    private String planInfo;

    @Column(name = "machine_id")
    private Long machineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MasterDataStatus status = MasterDataStatus.ENABLE;
}
