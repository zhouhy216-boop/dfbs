package com.dfbs.app.application.notification;

import com.dfbs.app.modules.notification.NotificationEntity;
import com.dfbs.app.modules.notification.NotificationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepo repo;

    public NotificationService(NotificationRepo repo) {
        this.repo = repo;
    }

    /**
     * Send notification to a user.
     * 
     * @param userId Target user ID
     * @param title Notification title
     * @param content Notification content
     * @param targetUrl Target URL (frontend route)
     * @return Created notification
     */
    @Transactional
    public NotificationEntity send(Long userId, String title, String content, String targetUrl) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetUrl(targetUrl);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return repo.save(notification);
    }

    /**
     * List all notifications for a user (ordered by created_at DESC).
     * 
     * @param userId User ID
     * @return List of notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationEntity> listMyNotifications(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * List unread notifications for a user.
     * 
     * @param userId User ID
     * @return List of unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationEntity> listUnreadNotifications(Long userId) {
        return repo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark notification as read.
     * 
     * @param notificationId Notification ID
     * @return Updated notification
     */
    @Transactional
    public NotificationEntity markAsRead(Long notificationId) {
        NotificationEntity notification = repo.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification not found: id=" + notificationId));
        notification.setIsRead(true);
        return repo.save(notification);
    }

    /**
     * Get unread count for a user.
     * 
     * @param userId User ID
     * @return Unread count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return repo.countByUserIdAndIsReadFalse(userId);
    }
}
