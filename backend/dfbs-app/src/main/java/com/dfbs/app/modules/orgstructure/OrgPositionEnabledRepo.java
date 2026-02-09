package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgPositionEnabledRepo extends JpaRepository<OrgPositionEnabledEntity, Long> {

    List<OrgPositionEnabledEntity> findByOrgNodeIdAndIsEnabledTrueOrderByIdAsc(Long orgNodeId);

    List<OrgPositionEnabledEntity> findByOrgNodeId(Long orgNodeId);

    Optional<OrgPositionEnabledEntity> findByOrgNodeIdAndPositionId(Long orgNodeId, Long positionId);

    boolean existsByOrgNodeIdAndPositionId(Long orgNodeId, Long positionId);
}
