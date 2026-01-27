package com.dfbs.app.modules.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    Long countByUserIdAndIsReadFalse(Long userId);
}
