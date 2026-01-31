package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "machines", uniqueConstraints = {
    @UniqueConstraint(columnNames = "machine_no"),
    @UniqueConstraint(columnNames = "serial_no")
})
@Getter
@Setter
public class MachineEntity extends BaseMasterEntity {

    @Column(name = "machine_no", nullable = false, unique = true, length = 64)
    private String machineNo;

    @Column(name = "serial_no", nullable = false, unique = true, length = 64)
    private String serialNo;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "model_id")
    private Long modelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MasterDataStatus status = MasterDataStatus.ENABLE;
}
