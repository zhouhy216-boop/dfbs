package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrgChangeLogRepo extends JpaRepository<OrgChangeLogEntity, Long>, JpaSpecificationExecutor<OrgChangeLogEntity> {
}
