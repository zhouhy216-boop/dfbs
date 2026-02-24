package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermActionRepo extends JpaRepository<PermActionEntity, Long> {

    List<PermActionEntity> findAllByOrderByIdAsc();

    boolean existsByActionKey(String actionKey);
}
