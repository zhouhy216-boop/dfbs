package com.dfbs.app.modules.platformorg;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformOrgCustomerRepo extends JpaRepository<PlatformOrgCustomerEntity, PlatformOrgCustomerId> {

    List<PlatformOrgCustomerEntity> findByOrgId(Long orgId);

    void deleteByOrgIdAndCustomerId(Long orgId, Long customerId);

    boolean existsByOrgIdAndCustomerId(Long orgId, Long customerId);
}
