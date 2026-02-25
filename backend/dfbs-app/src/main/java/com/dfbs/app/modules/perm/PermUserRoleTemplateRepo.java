package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermUserRoleTemplateRepo extends JpaRepository<PermUserRoleTemplateEntity, Long> {

    Optional<PermUserRoleTemplateEntity> findByUserId(Long userId);
}
