package com.dfbs.app.modules.masterdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@Setter
public class ContractEntity extends BaseMasterEntity {

    @Column(name = "contract_no", nullable = false, unique = true, length = 64)
    private String contractNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Attachment reference (URL or JSON). Required. */
    @Column(name = "attachment", nullable = false, columnDefinition = "TEXT")
    private String attachment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MasterDataStatus status = MasterDataStatus.ENABLE;
}
