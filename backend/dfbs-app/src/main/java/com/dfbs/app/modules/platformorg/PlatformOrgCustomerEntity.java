package com.dfbs.app.modules.platformorg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "platform_org_customers")
@IdClass(PlatformOrgCustomerId.class)
@Getter
@Setter
public class PlatformOrgCustomerEntity {

    @Id
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private PlatformOrgEntity org;
}
