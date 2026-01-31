package com.dfbs.app.modules.workorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepo extends JpaRepository<WorkOrderEntity, Long> {
}
