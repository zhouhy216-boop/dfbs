package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermRolePermissionRepo extends JpaRepository<PermRolePermissionEntity, Long> {

    List<PermRolePermissionEntity> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
