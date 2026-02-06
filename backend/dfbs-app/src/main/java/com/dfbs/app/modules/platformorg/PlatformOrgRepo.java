package com.dfbs.app.modules.platformorg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlatformOrgRepo extends JpaRepository<PlatformOrgEntity, Long> {

    Optional<PlatformOrgEntity> findByPlatformAndOrgCodeShort(String platform, String orgCodeShort);

    @Query("select o from PlatformOrgEntity o where o.platform = :platform and lower(trim(o.contactEmail)) = lower(:email)")
    List<PlatformOrgEntity> findByPlatformAndContactEmailNormalized(@Param("platform") String platform,
                                                                    @Param("email") String email);

    List<PlatformOrgEntity> findByPlatform(String platform);

    @Query("select o from PlatformOrgEntity o where o.platform = :platform and lower(trim(o.orgFullName)) = lower(trim(:orgFullName))")
    List<PlatformOrgEntity> findByPlatformAndOrgFullName(@Param("platform") String platform,
                                                        @Param("orgFullName") String orgFullName);

    @Query("select o from PlatformOrgEntity o join o.customerLinks c where o.platform = :platform and c.customerId = :customerId")
    List<PlatformOrgEntity> findByPlatformAndCustomerId(@Param("platform") String platform,
                                                        @Param("customerId") Long customerId);

    @Query("select o from PlatformOrgEntity o join o.customerLinks c where c.customerId = :customerId")
    List<PlatformOrgEntity> findByCustomerId(@Param("customerId") Long customerId);
}
