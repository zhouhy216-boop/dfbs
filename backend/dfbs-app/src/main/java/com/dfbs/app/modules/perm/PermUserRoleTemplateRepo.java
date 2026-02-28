package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermUserRoleTemplateRepo extends JpaRepository<PermUserRoleTemplateEntity, Long> {

    Optional<PermUserRoleTemplateEntity> findByUserId(Long userId);

    List<PermUserRoleTemplateEntity> findAllByUserIdIn(List<Long> userIds);
}
