package com.dfbs.app.application.notification;

import com.dfbs.app.modules.notification.NotificationEntity;
import com.dfbs.app.modules.notification.NotificationPriority;
import com.dfbs.app.modules.notification.NotificationRepo;
import com.dfbs.app.modules.notification.NotificationType;
import com.dfbs.app.modules.user.UserEntity;
import com.dfbs.app.modules.user.UserRepo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EntityManager entityManager;

    private Long userId;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setCanRequestPermission(false);
        user.setAuthorities("[]");
        user.setAllowNormalNotification(true);
        user = userRepo.save(user);
        userId = user.getId();
    }

    /**
     * Test 1 (Preference): User turns off Normal -> Send Normal (Not saved) -> Send Urgent (Saved).
     */
    @Test
    void test1_preference_normalOff_thenNormalNotSaved_urgentSaved() {
        notificationService.updatePreference(userId, false);

        NotificationEntity normalResult = notificationService.send(userId, "Normal", "Content",
                NotificationType.SYSTEM, null, NotificationPriority.NORMAL, false);
        assertThat(normalResult).isNull();

        NotificationEntity urgentResult = notificationService.send(userId, "Urgent", "Content",
                NotificationType.SYSTEM, null, NotificationPriority.URGENT, false);
        assertThat(urgentResult).isNotNull();
        assertThat(urgentResult.getTitle()).isEqualTo("Urgent");

        List<NotificationEntity> list = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPriority()).isEqualTo(NotificationPriority.URGENT);
    }

    /**
     * Test 2 (Life Cycle): Send -> Check Unread -> Mark Read -> Check Read.
     */
    @Test
    void test2_lifeCycle_sendThenUnread_thenMarkRead_thenRead() {
        NotificationEntity sent = notificationService.send(userId, "Title", "Content",
                NotificationType.QUOTE, 1L, NotificationPriority.NORMAL, false);
        assertThat(sent).isNotNull();
        assertThat(sent.getIsRead()).isFalse();

        List<NotificationEntity> unread = notificationService.listUnreadNotifications(userId);
        assertThat(unread).hasSize(1);
        assertThat(unread.get(0).getId()).isEqualTo(sent.getId());

        notificationService.markAsRead(sent.getId(), userId);
        List<NotificationEntity> unreadAfter = notificationService.listUnreadNotifications(userId);
        assertThat(unreadAfter).isEmpty();

        List<NotificationEntity> all = notificationService.listMyNotifications(userId);
        assertThat(all.get(0).getIsRead()).isTrue();
    }

    /**
     * Test 3 (Action Filter): Send with isActionRequired=true -> Search with filter -> Verify result.
     */
    @Test
    void test3_actionFilter_sendActionRequired_thenFilter() {
        notificationService.send(userId, "Action1", "Content", NotificationType.INVOICE, 10L,
                NotificationPriority.NORMAL, true);
        notificationService.send(userId, "NoAction", "Content", NotificationType.QUOTE, 20L,
                NotificationPriority.NORMAL, false);

        var pageAction = notificationService.listMyNotifications(userId, null, true,
                PageRequest.of(0, 20));
        assertThat(pageAction.getContent()).hasSize(1);
        assertThat(pageAction.getContent().get(0).getTitle()).isEqualTo("Action1");
        assertThat(pageAction.getContent().get(0).getIsActionRequired()).isTrue();

        var pageAll = notificationService.listMyNotifications(userId, null, null,
                PageRequest.of(0, 20));
        assertThat(pageAll.getContent()).hasSize(2);
    }

    /**
     * Test 4 (Cleanup): Insert old data -> Run cleanup -> Verify deletion.
     */
    @Test
    void test4_cleanup_oldNormalDeleted_oldUrgentDeleted() {
        NotificationEntity normalRecent = notificationService.send(userId, "Normal Recent", "C",
                NotificationType.SYSTEM, null, NotificationPriority.NORMAL, false);
        NotificationEntity normalOld = notificationService.send(userId, "Normal Old", "C",
                NotificationType.SYSTEM, null, NotificationPriority.NORMAL, false);
        NotificationEntity urgentOld = notificationService.send(userId, "Urgent Old", "C",
                NotificationType.SYSTEM, null, NotificationPriority.URGENT, false);

        // Backdate: set createdAt to past so cleanup will delete them
        normalOld.setCreatedAt(LocalDateTime.now().minusDays(200));
        notificationRepo.saveAndFlush(normalOld);
        urgentOld.setCreatedAt(LocalDateTime.now().minusDays(400));
        notificationRepo.saveAndFlush(urgentOld);

        notificationService.retentionCleanup();
        entityManager.flush();
        entityManager.clear();

        assertThat(notificationRepo.findById(normalRecent.getId())).isPresent();
        assertThat(notificationRepo.findById(normalOld.getId())).isEmpty();
        assertThat(notificationRepo.findById(urgentOld.getId())).isEmpty();
    }
}
