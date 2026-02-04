package com.dfbs.app.modules.workorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepo extends JpaRepository<WorkOrderEntity, Long>, JpaSpecificationExecutor<WorkOrderEntity> {

    Optional<WorkOrderEntity> findByOrderNo(String orderNo);

    List<WorkOrderEntity> findByStatus(WorkOrderStatus status);

    List<WorkOrderEntity> findByStatusIn(java.util.Collection<WorkOrderStatus> statuses);

    List<WorkOrderEntity> findByServiceManagerId(Long serviceManagerId);

    List<WorkOrderEntity> findByStatusAndServiceManagerId(WorkOrderStatus status, Long serviceManagerId);

    List<WorkOrderEntity> findByCustomerId(Long customerId);
}
