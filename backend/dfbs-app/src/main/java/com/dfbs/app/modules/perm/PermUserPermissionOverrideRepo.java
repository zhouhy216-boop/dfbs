package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermUserPermissionOverrideRepo extends JpaRepository<PermUserPermissionOverrideEntity, Long> {

    List<PermUserPermissionOverrideEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
