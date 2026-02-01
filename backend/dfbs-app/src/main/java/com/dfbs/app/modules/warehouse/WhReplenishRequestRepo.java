package com.dfbs.app.modules.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhReplenishRequestRepo extends JpaRepository<WhReplenishRequestEntity, Long> {

    Optional<WhReplenishRequestEntity> findByRequestNo(String requestNo);

    List<WhReplenishRequestEntity> findByStatus(ReplenishStatus status);

    List<WhReplenishRequestEntity> findByTargetWarehouseId(Long targetWarehouseId);

    List<WhReplenishRequestEntity> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
