package com.dfbs.app.modules.workorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRecordRepo extends JpaRepository<WorkOrderRecordEntity, Long> {

    List<WorkOrderRecordEntity> findByWorkOrderIdOrderByCreatedAtAsc(Long workOrderId);
}
