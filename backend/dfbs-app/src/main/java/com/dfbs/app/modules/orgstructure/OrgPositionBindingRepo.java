package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrgPositionBindingRepo extends JpaRepository<OrgPositionBindingEntity, Long> {

    List<OrgPositionBindingEntity> findByOrgNodeIdAndPositionIdAndIsActiveTrue(Long orgNodeId, Long positionId);

    List<OrgPositionBindingEntity> findByOrgNodeIdAndPositionId(Long orgNodeId, Long positionId);

    List<OrgPositionBindingEntity> findByPersonIdAndIsActiveTrue(Long personId);

    @Query("SELECT COUNT(b) FROM OrgPositionBindingEntity b WHERE b.orgNodeId = :orgNodeId AND b.positionId = :positionId AND b.isActive = true")
    long countActiveByOrgNodeIdAndPositionId(@Param("orgNodeId") Long orgNodeId, @Param("positionId") Long positionId);

    void deleteByOrgNodeIdAndPositionId(Long orgNodeId, Long positionId);
}
