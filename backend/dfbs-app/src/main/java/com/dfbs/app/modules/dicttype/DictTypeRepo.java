package com.dfbs.app.modules.dicttype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DictTypeRepo extends JpaRepository<DictTypeEntity, Long>, JpaSpecificationExecutor<DictTypeEntity> {

    boolean existsByTypeCode(String typeCode);
}
