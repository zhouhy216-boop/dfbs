package com.dfbs.app.modules.bizperm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BizPermUserOpScopeRepo extends JpaRepository<BizPermUserOpScopeEntity, BizPermUserOpScopeId> {

    List<BizPermUserOpScopeEntity> findByUserId(Long userId);
}
