package com.dfbs.app.modules.orgstructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OrgChangeLogRepo extends JpaRepository<OrgChangeLogEntity, Long> {

    @Query("SELECT l FROM OrgChangeLogEntity l WHERE " +
            "(:objectType IS NULL OR l.objectType = :objectType) " +
            "AND (:operatorId IS NULL OR l.operatorId = :operatorId) " +
            "AND (:from IS NULL OR l.timestamp >= :from) " +
            "AND (:to IS NULL OR l.timestamp <= :to) " +
            "ORDER BY l.timestamp DESC")
    Page<OrgChangeLogEntity> findWithFilters(
            @Param("objectType") String objectType,
            @Param("operatorId") Long operatorId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
