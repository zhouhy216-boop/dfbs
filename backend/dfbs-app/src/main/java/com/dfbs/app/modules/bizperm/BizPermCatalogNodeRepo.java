package com.dfbs.app.modules.bizperm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BizPermCatalogNodeRepo extends JpaRepository<BizPermCatalogNodeEntity, Long> {

    List<BizPermCatalogNodeEntity> findAllByOrderBySortOrderAscIdAsc();

    List<BizPermCatalogNodeEntity> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);
}
