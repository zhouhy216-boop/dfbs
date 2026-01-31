package com.dfbs.app.modules.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ExpenseRepo extends JpaRepository<ExpenseEntity, Long>, JpaSpecificationExecutor<ExpenseEntity> {

    List<ExpenseEntity> findByClaimIdOrderByIdAsc(Long claimId);
}
