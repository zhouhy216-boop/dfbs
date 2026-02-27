package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermRoleRepo extends JpaRepository<PermRoleEntity, Long> {

    List<PermRoleEntity> findAllByOrderByIdAsc();

    List<PermRoleEntity> findByEnabledTrueOrderByIdAsc();

    boolean existsByRoleKey(String roleKey);

    java.util.Optional<PermRoleEntity> findByRoleKey(String roleKey);
}
