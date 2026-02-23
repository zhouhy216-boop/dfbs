package com.dfbs.app.modules.dicttype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DictItemRepo extends JpaRepository<DictItemEntity, Long>, JpaSpecificationExecutor<DictItemEntity> {

    boolean existsByTypeIdAndItemValue(Long typeId, String itemValue);

    long countByTypeId(Long typeId);

    long countByParentId(Long parentId);

    java.util.Optional<DictItemEntity> findByTypeIdAndItemValue(Long typeId, String itemValue);
}
