package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgLevelPositionTemplateRepo extends JpaRepository<OrgLevelPositionTemplateEntity, Long> {

    List<OrgLevelPositionTemplateEntity> findByLevelIdAndIsEnabledTrue(Long levelId);
}
