package com.dfbs.app.interfaces.notification;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.notification.NotificationEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public List<NotificationEntity> listMyNotifications(@RequestParam Long userId) {
        return service.listMyNotifications(userId);
    }

    @GetMapping("/my/unread")
    public List<NotificationEntity> listUnreadNotifications(@RequestParam Long userId) {
        return service.listUnreadNotifications(userId);
    }

    @GetMapping("/my/unread-count")
    public UnreadCountResponse getUnreadCount(@RequestParam Long userId) {
        Long count = service.getUnreadCount(userId);
        return new UnreadCountResponse(count);
    }

    @PostMapping("/{id}/read")
    public NotificationEntity markAsRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }

    public record UnreadCountResponse(Long count) {}
}
