package com.dfbs.app.modules.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRequestRepo extends JpaRepository<PermissionRequestEntity, Long> {

    List<PermissionRequestEntity> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<PermissionRequestEntity> findByStatusInOrderByCreatedAtDesc(java.util.List<RequestStatus> statuses);

    boolean existsByApplicantIdAndStatusIn(Long applicantId, List<RequestStatus> statuses);
}
