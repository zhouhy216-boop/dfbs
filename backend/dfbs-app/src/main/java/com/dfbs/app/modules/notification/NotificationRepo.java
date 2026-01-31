package com.dfbs.app.modules.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    Long countByUserIdAndIsReadFalse(Long userId);

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId " +
           "AND (:isRead IS NULL OR n.isRead = :isRead) " +
           "AND (:isActionRequired IS NULL OR n.isActionRequired = :isActionRequired)")
    Page<NotificationEntity> findByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("isRead") Boolean isRead,
            @Param("isActionRequired") Boolean isActionRequired,
            Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.priority = :priority AND n.createdAt < :before")
    int deleteByPriorityAndCreatedAtBefore(
            @Param("priority") NotificationPriority priority,
            @Param("before") LocalDateTime before);
}
