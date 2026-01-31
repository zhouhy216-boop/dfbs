package com.dfbs.app.application.permission;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.permission.PermissionRequestEntity;
import com.dfbs.app.modules.permission.PermissionRequestRepo;
import com.dfbs.app.modules.permission.RequestStatus;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionRequestService {

    private static final List<RequestStatus> PENDING_OR_RETURNED = List.of(RequestStatus.PENDING, RequestStatus.RETURNED);
    private static final long ADMIN_USER_ID = 1L;
    private static final long LEADER_USER_ID = 1L;

    private final PermissionRequestRepo permissionRequestRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public PermissionRequestService(PermissionRequestRepo permissionRequestRepo,
                                    UserRepo userRepo,
                                    NotificationService notificationService,
                                    ObjectMapper objectMapper) {
        this.permissionRequestRepo = permissionRequestRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Serialize user's current authorities to JSON string.
     */
    @Transactional(readOnly = true)
    public String getCurrentAuthoritiesJson(Long userId) {
        UserEntity user = userRepo.findById(userId).orElse(null);
        if (user == null || user.getAuthorities() == null || user.getAuthorities().isBlank()) {
            return "[]";
        }
        return user.getAuthorities();
    }

    /**
     * Create permission request. Check canRequestPermission and anti-spam (no PENDING/RETURNED).
     */
    @Transactional
    public PermissionRequestEntity create(Long applicantId, PermissionRequestDto dto) {
        UserEntity applicant = userRepo.findById(applicantId)
                .orElseThrow(() -> new IllegalStateException("User not found: id=" + applicantId));
        if (Boolean.FALSE.equals(applicant.getCanRequestPermission())) {
            throw new IllegalStateException("该用户无权发起权限申请");
        }
        if (permissionRequestRepo.existsByApplicantIdAndStatusIn(applicantId, PENDING_OR_RETURNED)) {
            throw new IllegalStateException("存在待处理或已退回的申请，请先处理后再提交");
        }

        requireNotNull(dto.targetUserId(), "targetUserId不能为空");
        requireNotBlank(dto.description(), "description不能为空");
        requireNotBlank(dto.reason(), "reason不能为空");
        requireNotBlank(dto.expectedTime(), "expectedTime不能为空");

        UserEntity target = userRepo.findById(dto.targetUserId())
                .orElseThrow(() -> new IllegalStateException("Target user not found: id=" + dto.targetUserId()));

        PermissionRequestEntity req = new PermissionRequestEntity();
        req.setApplicantId(applicantId);
        req.setTargetUserId(dto.targetUserId());
        req.setDescription(dto.description());
        req.setReason(dto.reason());
        req.setExpectedTime(dto.expectedTime());
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        req = permissionRequestRepo.save(req);

        notificationService.send(ADMIN_USER_ID, "新权限申请", "用户 " + applicantId + " 提交了权限申请，目标用户: " + target.getId(), "/permission-requests/" + req.getId());
        return req;
    }

    /**
     * Resubmit after return. Request must be RETURNED, applicant must match.
     */
    @Transactional
    public PermissionRequestEntity resubmit(Long requestId, Long applicantId, String newDescription, String newReason) {
        PermissionRequestEntity req = permissionRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Permission request not found: id=" + requestId));
        if (req.getStatus() != RequestStatus.RETURNED) {
            throw new IllegalStateException("只有已退回的申请可以重新提交");
        }
        if (!req.getApplicantId().equals(applicantId)) {
            throw new IllegalStateException("只能重新提交本人发起的申请");
        }
        requireNotBlank(newDescription, "description不能为空");
        requireNotBlank(newReason, "reason不能为空");

        req.setDescription(newDescription);
        req.setReason(newReason);
        req.setStatus(RequestStatus.PENDING);
        req.setUpdatedAt(LocalDateTime.now());
        req = permissionRequestRepo.save(req);

        notificationService.send(ADMIN_USER_ID, "权限申请已重新提交", "申请 " + requestId + " 已重新提交", "/permission-requests/" + req.getId());
        return req;
    }

    /**
     * Admin: return request. Set status RETURNED, notify applicant.
     */
    @Transactional
    public PermissionRequestEntity returnRequest(Long requestId, Long adminId, String comment) {
        PermissionRequestEntity req = permissionRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Permission request not found: id=" + requestId));
        req.setStatus(RequestStatus.RETURNED);
        req.setHandlerId(adminId);
        req.setHandleTime(LocalDateTime.now());
        req.setAdminComment(comment);
        req.setUpdatedAt(LocalDateTime.now());
        req = permissionRequestRepo.save(req);

        notificationService.send(req.getApplicantId(), "权限申请已退回", "申请 " + requestId + " 已被退回: " + (comment != null ? comment : ""), "/permission-requests/" + req.getId());
        return req;
    }

    /**
     * Admin: reject request. Set status REJECTED, notify applicant.
     */
    @Transactional
    public PermissionRequestEntity reject(Long requestId, Long adminId, String comment) {
        PermissionRequestEntity req = permissionRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Permission request not found: id=" + requestId));
        req.setStatus(RequestStatus.REJECTED);
        req.setHandlerId(adminId);
        req.setHandleTime(LocalDateTime.now());
        req.setAdminComment(comment);
        req.setUpdatedAt(LocalDateTime.now());
        req = permissionRequestRepo.save(req);

        notificationService.send(req.getApplicantId(), "权限申请已拒绝", "申请 " + requestId + " 已被拒绝: " + (comment != null ? comment : ""), "/permission-requests/" + req.getId());
        return req;
    }

    /**
     * Admin: approve. Snapshot before, update target user authorities, snapshot after, notify applicant and leaders.
     */
    @Transactional
    public PermissionRequestEntity approve(Long requestId, Long adminId, String comment, List<String> newAuthorities) {
        PermissionRequestEntity req = permissionRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Permission request not found: id=" + requestId));
        requireNotNull(newAuthorities, "newAuthorities不能为空");

        Long targetUserId = req.getTargetUserId();
        UserEntity target = userRepo.findById(targetUserId)
                .orElseThrow(() -> new IllegalStateException("Target user not found: id=" + targetUserId));

        String snapshotBefore = getCurrentAuthoritiesJson(targetUserId);
        req.setSnapshotBefore(snapshotBefore);

        String authoritiesJson = serializeAuthorities(newAuthorities);
        target.setAuthorities(authoritiesJson);
        userRepo.save(target);

        req.setSnapshotAfter(authoritiesJson);
        req.setStatus(RequestStatus.APPROVED);
        req.setHandlerId(adminId);
        req.setHandleTime(LocalDateTime.now());
        req.setAdminComment(comment);
        req.setUpdatedAt(LocalDateTime.now());
        req = permissionRequestRepo.save(req);

        notificationService.send(req.getApplicantId(), "权限申请已同意", "申请 " + requestId + " 已通过", "/permission-requests/" + req.getId());
        notificationService.send(LEADER_USER_ID, "权限申请已执行", "申请 " + requestId + " 已同意并更新目标用户权限", "/permission-requests/" + req.getId());
        return req;
    }

    @Transactional(readOnly = true)
    public PermissionRequestEntity getRequest(Long requestId) {
        return permissionRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Permission request not found: id=" + requestId));
    }

    @Transactional(readOnly = true)
    public List<PermissionRequestEntity> getMyRequests(Long applicantId) {
        return permissionRequestRepo.findByApplicantIdOrderByCreatedAtDesc(applicantId);
    }

    @Transactional(readOnly = true)
    public List<PermissionRequestEntity> getPendingRequests() {
        return permissionRequestRepo.findByStatusInOrderByCreatedAtDesc(List.of(RequestStatus.PENDING));
    }

    private String serializeAuthorities(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list != null ? list : new ArrayList<>());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize authorities", e);
        }
    }

    private static void requireNotNull(Object value, String message) {
        if (value == null) throw new IllegalStateException(message);
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalStateException(message);
    }
}
