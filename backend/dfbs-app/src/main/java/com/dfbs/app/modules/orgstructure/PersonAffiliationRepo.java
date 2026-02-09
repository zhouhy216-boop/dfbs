package com.dfbs.app.modules.orgstructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonAffiliationRepo extends JpaRepository<PersonAffiliationEntity, Long> {

    List<PersonAffiliationEntity> findByPersonId(Long personId);

    Optional<PersonAffiliationEntity> findByPersonIdAndIsPrimaryTrue(Long personId);

    List<PersonAffiliationEntity> findByOrgNodeId(Long orgNodeId);

    @Query("SELECT COUNT(DISTINCT pa.personId) FROM PersonAffiliationEntity pa WHERE pa.orgNodeId IN :nodeIds")
    long countDistinctPersonIdByOrgNodeIdIn(@Param("nodeIds") List<Long> nodeIds);

    /** Count distinct persons with affiliations in given nodes, only where org_person.is_active = true (在岗/启用人员). */
    @Query("SELECT COUNT(DISTINCT pa.personId) FROM PersonAffiliationEntity pa INNER JOIN OrgPersonEntity p ON p.id = pa.personId AND p.isActive = true WHERE pa.orgNodeId IN :nodeIds")
    long countDistinctActivePersonIdByOrgNodeIdIn(@Param("nodeIds") List<Long> nodeIds);

    void deleteByPersonId(Long personId);

    void deleteByPersonIdAndOrgNodeId(Long personId, Long orgNodeId);

    boolean existsByPersonIdAndOrgNodeId(Long personId, Long orgNodeId);
}
