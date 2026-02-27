package com.dfbs.app.application.perm;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.perm.PermAuditLogEntity;
import com.dfbs.app.modules.perm.PermAuditLogRepo;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Coarse-grained audit for RBAC changes. Best-effort insert (REQUIRES_NEW so failure does not roll back main action).
 */
@Service
public class PermAuditService {

    public static final String ACTION_ROLE_TEMPLATE_SAVE = "ROLE_TEMPLATE_SAVE";
    public static final String ACTION_ACCOUNT_OVERRIDE_SAVE = "ACCOUNT_OVERRIDE_SAVE";
    public static final String ACTION_MODULE_CREATE = "MODULE_CREATE";
    public static final String ACTION_MODULE_UPDATE = "MODULE_UPDATE";
    public static final String ACTION_MODULE_DELETE = "MODULE_DELETE";
    public static final String ACTION_MODULE_ACTIONS_SET = "MODULE_ACTIONS_SET";
    public static final String ACTION_TEST_KIT_RESET = "TEST_KIT_RESET";
    public static final String ACTION_VISION_SET = "VISION_SET";

    public static final String TARGET_ROLE = "ROLE";
    public static final String TARGET_USER = "USER";
    public static final String TARGET_MODULE = "MODULE";
    public static final String TARGET_SYSTEM = "SYSTEM";

    private final PermAuditLogRepo auditLogRepo;
    private final CurrentUserIdResolver userIdResolver;
    private final UserRepo userRepo;

    public PermAuditService(PermAuditLogRepo auditLogRepo, CurrentUserIdResolver userIdResolver, UserRepo userRepo) {
        this.auditLogRepo = auditLogRepo;
        this.userIdResolver = userIdResolver;
        this.userRepo = userRepo;
    }

    /** Best-effort: insert audit row. Uses REQUIRES_NEW so audit failure does not roll back main transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actionType, String targetType, Long targetId, String targetKey, String note) {
        try {
            Long actorId = userIdResolver.getCurrentUserId();
            String actorUsername = actorId != null ? userRepo.findById(actorId).map(u -> u.getUsername()).orElse(null) : null;
            PermAuditLogEntity e = new PermAuditLogEntity();
            e.setActorUserId(actorId);
            e.setActorUsername(actorUsername);
            e.setActionType(actionType);
            e.setTargetType(targetType);
            e.setTargetId(targetId);
            e.setTargetKey(targetKey);
            e.setNote(note != null && note.length() > 512 ? note.substring(0, 512) : note);
            auditLogRepo.save(e);
        } catch (Exception ignored) {
            // best-effort: do not fail main action
        }
    }

    public List<PermAuditLogEntity> getRecent(int limit, String actionType, String targetType, Long targetId) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return auditLogRepo.findRecent(actionType, targetType, targetId, PageRequest.of(0, safeLimit)).getContent();
    }
}
