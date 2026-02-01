package com.dfbs.app.modules.workorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderPartRepo extends JpaRepository<WorkOrderPartEntity, Long> {

    List<WorkOrderPartEntity> findByWorkOrderId(Long workOrderId);
}
