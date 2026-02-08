package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgLevelRepo extends JpaRepository<OrgLevelEntity, Long> {

    List<OrgLevelEntity> findAllByOrderByOrderIndexAsc();

    List<OrgLevelEntity> findByIsEnabledTrueOrderByOrderIndexAsc();

    long count();
}
