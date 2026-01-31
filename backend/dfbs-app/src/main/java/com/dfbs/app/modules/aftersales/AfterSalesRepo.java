package com.dfbs.app.modules.aftersales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AfterSalesRepo extends JpaRepository<AfterSalesEntity, Long>, JpaSpecificationExecutor<AfterSalesEntity> {
    Page<AfterSalesEntity> findBySourceShipmentId(Long sourceShipmentId, Pageable pageable);
}
