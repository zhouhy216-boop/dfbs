package com.dfbs.app.application.permission;

import com.dfbs.app.modules.permission.PermissionRequestEntity;
import com.dfbs.app.modules.permission.PermissionRequestRepo;
import com.dfbs.app.modules.permission.RequestStatus;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PermissionRequestTest {

    @Autowired
    private PermissionRequestService permissionRequestService;

    @Autowired
    private PermissionRequestRepo permissionRequestRepo;

    @Autowired
    private UserRepo userRepo;

    private Long applicantId;
    private Long targetId;
    private Long otherUserId;
    private Long noFlagUserId;
    private static final Long ADMIN_ID = 1L;

    @BeforeEach
    void setUp() {
        UserEntity applicant = new UserEntity();
        applicant.setCanRequestPermission(true);
        applicant.setAuthorities("[\"ROLE_USER\"]");
        applicant = userRepo.save(applicant);
        applicantId = applicant.getId();

        UserEntity target = new UserEntity();
        target.setCanRequestPermission(false);
        target.setAuthorities("[\"ROLE_USER\"]");
        target = userRepo.save(target);
        targetId = target.getId();

        UserEntity other = new UserEntity();
        other.setCanRequestPermission(true);
        other.setAuthorities("[\"ROLE_USER\"]");
        other = userRepo.save(other);
        otherUserId = other.getId();

        UserEntity noFlag = new UserEntity();
        noFlag.setCanRequestPermission(false);
        noFlag.setAuthorities("[]");
        noFlag = userRepo.save(noFlag);
        noFlagUserId = noFlag.getId();
    }

    /**
     * Test 1 (Eligibility): User without flag tries to apply -> Fail.
     */
    @Test
    void test1_eligibility_userWithoutFlag_triesApply_fail() {
        PermissionRequestDto dto = new PermissionRequestDto(targetId, "需要管理员权限", "项目需要", "2024-06-01");
        assertThatThrownBy(() -> permissionRequestService.create(noFlagUserId, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("无权发起");
    }

    /**
     * Test 2 (Anti-Spam): User creates 1st -> Success. User creates 2nd immediately -> Fail.
     */
    @Test
    void test2_antiSpam_firstSuccess_secondFail() {
        PermissionRequestDto dto = new PermissionRequestDto(targetId, "申请权限", "原因", "2024-06-01");
        PermissionRequestEntity first = permissionRequestService.create(applicantId, dto);
        assertThat(first.getStatus()).isEqualTo(RequestStatus.PENDING);

        assertThatThrownBy(() -> permissionRequestService.create(applicantId, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("待处理或已退回");
    }

    /**
     * Test 3 (Flow): Apply -> Admin Return -> Resubmit -> Admin Approve (Verify User Authorities Updated & Snapshots saved).
     */
    @Test
    void test3_flow_apply_return_resubmit_approve_verifySnapshotsAndAuthorities() {
        PermissionRequestDto dto = new PermissionRequestDto(targetId, "申请权限", "原因", "2024-06-01");
        PermissionRequestEntity req = permissionRequestService.create(applicantId, dto);
        assertThat(req.getStatus()).isEqualTo(RequestStatus.PENDING);

        permissionRequestService.returnRequest(req.getId(), ADMIN_ID, "请补充说明");
        req = permissionRequestService.getRequest(req.getId());
        assertThat(req.getStatus()).isEqualTo(RequestStatus.RETURNED);

        permissionRequestService.resubmit(req.getId(), applicantId, "补充后的描述", "补充后的原因");
        req = permissionRequestService.getRequest(req.getId());
        assertThat(req.getStatus()).isEqualTo(RequestStatus.PENDING);

        permissionRequestService.approve(req.getId(), ADMIN_ID, "同意", List.of("ROLE_USER", "ROLE_ADMIN"));
        req = permissionRequestService.getRequest(req.getId());
        assertThat(req.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(req.getSnapshotBefore()).isNotNull();
        assertThat(req.getSnapshotAfter()).contains("ROLE_ADMIN");

        UserEntity targetUser = userRepo.findById(targetId).orElseThrow();
        assertThat(targetUser.getAuthorities()).contains("ROLE_ADMIN");
    }

    /**
     * Test 4 (Self vs Others): Apply for self vs Apply for another user -> Both valid.
     */
    @Test
    void test4_selfVsOthers_applyForSelf_andForOther_bothValid() {
        PermissionRequestDto forSelf = new PermissionRequestDto(applicantId, "为自己申请", "原因", "2024-06-01");
        PermissionRequestEntity req1 = permissionRequestService.create(applicantId, forSelf);
        assertThat(req1.getApplicantId()).isEqualTo(applicantId);
        assertThat(req1.getTargetUserId()).isEqualTo(applicantId);

        permissionRequestService.returnRequest(req1.getId(), ADMIN_ID, "退回");
        permissionRequestService.resubmit(req1.getId(), applicantId, "重提", "原因");
        permissionRequestService.approve(req1.getId(), ADMIN_ID, "同意", List.of("ROLE_USER", "ROLE_APPLICANT"));

        PermissionRequestDto forOther = new PermissionRequestDto(targetId, "为他人申请", "原因", "2024-06-01");
        PermissionRequestEntity req2 = permissionRequestService.create(otherUserId, forOther);
        assertThat(req2.getApplicantId()).isEqualTo(otherUserId);
        assertThat(req2.getTargetUserId()).isEqualTo(targetId);
    }
}
