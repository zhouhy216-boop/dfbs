package com.dfbs.app.modules.notification;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // Target user

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32)
    private NotificationType type;

    @Column(name = "related_id")
    private Long relatedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 16, nullable = false)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_action_required", nullable = false)
    private Boolean isActionRequired = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public NotificationEntity() {}
}
