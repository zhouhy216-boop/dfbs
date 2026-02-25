package com.dfbs.app.modules.perm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermModuleRepo extends JpaRepository<PermModuleEntity, Long> {

    List<PermModuleEntity> findByParentIdIsNullOrderByIdAsc();

    List<PermModuleEntity> findByParentIdOrderByIdAsc(Long parentId);

    boolean existsByModuleKey(String moduleKey);

    boolean existsByParentId(Long parentId);

    Optional<PermModuleEntity> findByModuleKey(String moduleKey);
}
