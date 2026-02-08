package com.dfbs.app.modules.orgstructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrgPersonRepo extends JpaRepository<OrgPersonEntity, Long> {

    @Query("SELECT p FROM OrgPersonEntity p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR (p.email IS NOT NULL AND LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND (:primaryOrgId IS NULL OR EXISTS (SELECT 1 FROM PersonAffiliationEntity pa WHERE pa.personId = p.id AND pa.orgNodeId = :primaryOrgId AND pa.isPrimary = true)) " +
            "ORDER BY p.name")
    Page<OrgPersonEntity> search(@Param("keyword") String keyword, @Param("primaryOrgId") Long primaryOrgId, Pageable pageable);

    @Query("SELECT p FROM OrgPersonEntity p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR (p.email IS NOT NULL AND LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND (:activeOnly = false OR p.isActive = true) ORDER BY p.name")
    Page<OrgPersonEntity> searchAllWithActive(@Param("keyword") String keyword, @Param("activeOnly") boolean activeOnly, Pageable pageable);

    /** People linked to any of the given org nodes (primary or secondary when includeSecondaries). */
    @Query("SELECT DISTINCT p FROM OrgPersonEntity p INNER JOIN PersonAffiliationEntity pa ON pa.personId = p.id AND pa.orgNodeId IN :nodeIds " +
            "AND (pa.isPrimary = true OR :includeSecondaries = true) " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR (p.email IS NOT NULL AND LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND (:activeOnly = false OR p.isActive = true) ORDER BY p.name")
    Page<OrgPersonEntity> findByOrgSubtree(@Param("nodeIds") List<Long> nodeIds, @Param("includeSecondaries") boolean includeSecondaries,
                                           @Param("keyword") String keyword, @Param("activeOnly") boolean activeOnly, Pageable pageable);

    List<OrgPersonEntity> findByIsActiveTrueOrderByNameAsc();

    Optional<OrgPersonEntity> findByIdAndIsActiveTrue(Long id);
}
