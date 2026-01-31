package com.dfbs.app.modules.repair;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface RepairRecordRepo extends JpaRepository<RepairRecordEntity, Long>, JpaSpecificationExecutor<RepairRecordEntity> {

    /** Find existing oldWorkOrderNo in DB for deduplication. */
    List<RepairRecordEntity> findByOldWorkOrderNoIn(Set<String> oldWorkOrderNos);

    boolean existsByOldWorkOrderNo(String oldWorkOrderNo);
}
