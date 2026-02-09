package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgPositionCatalogRepo extends JpaRepository<OrgPositionCatalogEntity, Long> {

    List<OrgPositionCatalogEntity> findByIsEnabledTrueOrderByOrderIndexAsc();

    List<OrgPositionCatalogEntity> findAllByOrderByOrderIndexAsc();
}
