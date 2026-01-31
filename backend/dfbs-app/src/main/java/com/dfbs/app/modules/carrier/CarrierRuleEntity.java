package com.dfbs.app.modules.carrier;

import jakarta.persistence.*;

@Entity
@Table(name = "md_carrier_rule")
public class CarrierRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private CarrierEntity carrier;

    @Column(name = "match_keyword", nullable = false, length = 256)
    private String matchKeyword;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    public CarrierRuleEntity() {}

    public Long getId() { return id; }
    public CarrierEntity getCarrier() { return carrier; }
    public void setCarrier(CarrierEntity carrier) { this.carrier = carrier; }
    public String getMatchKeyword() { return matchKeyword; }
    public void setMatchKeyword(String matchKeyword) { this.matchKeyword = matchKeyword; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
