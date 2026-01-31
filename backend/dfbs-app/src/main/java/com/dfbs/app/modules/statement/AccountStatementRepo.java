package com.dfbs.app.modules.statement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountStatementRepo extends JpaRepository<AccountStatementEntity, Long> {

    List<AccountStatementEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<AccountStatementEntity> findByStatusOrderByCreatedAtDesc(StatementStatus status);

    List<AccountStatementEntity> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, StatementStatus status);

    long countByStatementNoStartingWith(String prefix);
}
