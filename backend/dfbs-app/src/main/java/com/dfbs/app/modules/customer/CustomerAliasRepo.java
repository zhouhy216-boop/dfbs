package com.dfbs.app.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAliasRepo extends JpaRepository<CustomerAliasEntity, Long> {

    Optional<CustomerAliasEntity> findByAliasName(String aliasName);

    boolean existsByAliasName(String aliasName);
}
