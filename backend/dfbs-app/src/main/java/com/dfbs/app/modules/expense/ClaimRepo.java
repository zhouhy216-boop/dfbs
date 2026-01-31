package com.dfbs.app.modules.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClaimRepo extends JpaRepository<ClaimEntity, Long>, JpaSpecificationExecutor<ClaimEntity> {
}
