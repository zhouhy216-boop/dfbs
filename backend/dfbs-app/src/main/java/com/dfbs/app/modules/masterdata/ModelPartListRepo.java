package com.dfbs.app.modules.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ModelPartListRepo extends JpaRepository<ModelPartListEntity, Long>, JpaSpecificationExecutor<ModelPartListEntity> {
    List<ModelPartListEntity> findByModelId(Long modelId);
}
