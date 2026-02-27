package com.dfbs.app.modules.perm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermAuditLogRepo extends JpaRepository<PermAuditLogEntity, Long> {

    @Query("SELECT e FROM PermAuditLogEntity e WHERE (:actionType IS NULL OR e.actionType = :actionType) " +
           "AND (:targetType IS NULL OR e.targetType = :targetType) AND (:targetId IS NULL OR e.targetId = :targetId) " +
           "ORDER BY e.createdAt DESC")
    Page<PermAuditLogEntity> findRecent(
            @Param("actionType") String actionType,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            Pageable pageable);
}
