package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermModuleActionRepo extends JpaRepository<PermModuleActionEntity, Long> {

    List<PermModuleActionEntity> findByModuleId(Long moduleId);

    boolean existsByModuleIdAndActionKey(Long moduleId, String actionKey);
}
