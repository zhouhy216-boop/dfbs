package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomConflictRepo extends JpaRepository<BomConflictEntity, Long> {
    List<BomConflictEntity> findByBomIdOrderByIdAsc(Long bomId);
    List<BomConflictEntity> findByBomIdAndStatus(Long bomId, BomConflictStatus status);
    boolean existsByBomIdAndStatus(Long bomId, BomConflictStatus status);
}
