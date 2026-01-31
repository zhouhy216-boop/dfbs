package com.dfbs.app.application.notification;

import com.dfbs.app.modules.notification.NotificationEntity;
import com.dfbs.app.modules.notification.NotificationPriority;
import com.dfbs.app.modules.notification.NotificationRepo;
import com.dfbs.app.modules.notification.NotificationType;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private static final int NORMAL_RETENTION_DAYS = 180;
    private static final int URGENT_RETENTION_DAYS = 365;

    private final NotificationRepo repo;
    private final UserRepo userRepo;

    public NotificationService(NotificationRepo repo, UserRepo userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    /**
     * Send notification to a user. Respects user preference: if priority is NORMAL and
     * user has allowNormalNotification = false, the notification is not saved.
     */
    @Transactional
    public NotificationEntity send(Long userId, String title, String content, NotificationType type,
                                   Long relatedId, NotificationPriority priority, boolean isActionRequired) {
        if (priority == NotificationPriority.NORMAL) {
            UserEntity user = userRepo.findById(userId).orElse(null);
            if (user != null && Boolean.FALSE.equals(user.getAllowNormalNotification())) {
                return null; // Do not save
            }
        }
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRelatedId(relatedId);
        n.setPriority(priority);
        n.setIsRead(false);
        n.setIsActionRequired(isActionRequired);
        n.setCreatedAt(LocalDateTime.now());
        return repo.save(n);
    }

    /**
     * Backward-compatible send (no type/priority/action). Uses SYSTEM, NORMAL, not action required.
     * Optional targetUrl can be set for frontend route.
     */
    @Transactional
    public NotificationEntity send(Long userId, String title, String content, String targetUrl) {
        NotificationEntity n = send(userId, title, content, NotificationType.SYSTEM, null, NotificationPriority.NORMAL, false);
        if (n != null && targetUrl != null) {
            n.setTargetUrl(targetUrl);
            repo.save(n);
        }
        return n;
    }

    /**
     * Send the same notification to multiple recipients (e.g. CC list).
     */
    @Transactional
    public List<NotificationEntity> sendBatch(List<Long> userIds, String title, String content,
                                              NotificationType type, Long relatedId,
                                              NotificationPriority priority, boolean isActionRequired) {
        List<NotificationEntity> result = new ArrayList<>();
        for (Long userId : userIds) {
            NotificationEntity saved = send(userId, title, content, type, relatedId, priority, isActionRequired);
            if (saved != null) {
                result.add(saved);
            }
        }
        return result;
    }

    /**
     * List notifications for a user with optional filters (isRead, isActionRequired), paged.
     */
    @Transactional(readOnly = true)
    public Page<NotificationEntity> listMyNotifications(Long userId, Boolean isRead, Boolean isActionRequired,
                                                         Pageable pageable) {
        return repo.findByUserIdAndFilters(userId, isRead, isActionRequired, pageable);
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> listMyNotifications(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> listUnreadNotifications(Long userId) {
        return repo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark one notification as read. Only if it belongs to the given user.
     */
    @Transactional
    public NotificationEntity markAsRead(Long id, Long userId) {
        if (!repo.existsByIdAndUserId(id, userId)) {
            throw new IllegalStateException("Notification not found or not owned: id=" + id);
        }
        NotificationEntity n = repo.findById(id).orElseThrow();
        n.setIsRead(true);
        return repo.save(n);
    }

    /**
     * Mark all notifications for the user as read.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        repo.markAllAsReadByUserId(userId);
    }

    /**
     * Update user's preference for normal notifications.
     */
    @Transactional
    public void updatePreference(Long userId, boolean allowNormal) {
        UserEntity user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("User not found: id=" + userId));
        user.setAllowNormalNotification(allowNormal);
        userRepo.save(user);
    }

    /**
     * Retention cleanup: delete NORMAL older than 180 days, URGENT older than 365 days. Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void retentionCleanup() {
        LocalDateTime normalBefore = LocalDateTime.now().minusDays(NORMAL_RETENTION_DAYS);
        LocalDateTime urgentBefore = LocalDateTime.now().minusDays(URGENT_RETENTION_DAYS);
        repo.deleteByPriorityAndCreatedAtBefore(NotificationPriority.NORMAL, normalBefore);
        repo.deleteByPriorityAndCreatedAtBefore(NotificationPriority.URGENT, urgentBefore);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return repo.countByUserIdAndIsReadFalse(userId);
    }
}
