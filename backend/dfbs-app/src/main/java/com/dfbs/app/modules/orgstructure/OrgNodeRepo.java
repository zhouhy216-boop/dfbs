package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrgNodeRepo extends JpaRepository<OrgNodeEntity, Long> {

    List<OrgNodeEntity> findByParentIdIsNullOrderByNameAsc();

    List<OrgNodeEntity> findByParentIdIsNullAndIsEnabledTrueOrderByNameAsc();

    List<OrgNodeEntity> findByParentIdOrderByNameAsc(Long parentId);

    List<OrgNodeEntity> findByLevelId(Long levelId);

    List<OrgNodeEntity> findByParentIdAndIsEnabledTrueOrderByNameAsc(Long parentId);

    boolean existsByParentId(Long parentId);

    long countByLevelId(Long levelId);

    long countByLevelIdAndIsEnabledTrue(Long levelId);

    @Query("SELECT n FROM OrgNodeEntity n WHERE n.parentId = :parentId ORDER BY n.name")
    List<OrgNodeEntity> findChildrenByParentId(Long parentId);
}
