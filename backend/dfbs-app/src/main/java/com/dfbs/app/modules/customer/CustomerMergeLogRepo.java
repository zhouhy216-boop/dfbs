package com.dfbs.app.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerMergeLogRepo extends JpaRepository<CustomerMergeLogEntity, Long> {
}
