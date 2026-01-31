package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "machine_models")
@Getter
@Setter
public class MachineModelEntity extends BaseMasterEntity {

    @Column(name = "model_name", length = 200)
    private String modelName;

    @Column(name = "model_no", nullable = false, unique = true, length = 64)
    private String modelNo;

    @Column(name = "freight_info", columnDefinition = "TEXT")
    private String freightInfo;

    @Column(name = "warranty_info", columnDefinition = "TEXT")
    private String warrantyInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MasterDataStatus status = MasterDataStatus.ENABLE;
}
