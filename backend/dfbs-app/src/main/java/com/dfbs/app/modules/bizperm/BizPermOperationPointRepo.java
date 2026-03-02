package com.dfbs.app.modules.bizperm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BizPermOperationPointRepo extends JpaRepository<BizPermOperationPointEntity, Long> {

    List<BizPermOperationPointEntity> findByNodeIdIsNullOrderBySortOrderAscIdAsc();

    List<BizPermOperationPointEntity> findByNodeIdOrderBySortOrderAscIdAsc(Long nodeId);

    Optional<BizPermOperationPointEntity> findByPermissionKey(String permissionKey);

    long countByNodeId(Long nodeId);
}
