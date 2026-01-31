package com.dfbs.app.modules.carrier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarrierRuleRepo extends JpaRepository<CarrierRuleEntity, Long> {

    @Query("SELECT r FROM CarrierRuleEntity r JOIN FETCH r.carrier c WHERE c.isActive = true ORDER BY r.priority DESC")
    List<CarrierRuleEntity> findAllWithCarrierOrderByPriorityDesc();
}
