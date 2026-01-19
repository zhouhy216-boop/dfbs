package com.dfbs.app.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByCustomerCode(String customerCode);
}
