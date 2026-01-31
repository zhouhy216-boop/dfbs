package com.dfbs.app.interfaces.notification;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.notification.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /**
     * List notifications for the current user with optional filters. Supports pagination.
     * Params: isRead (optional), isActionRequired (optional), page (default 0), size (default 20).
     */
    @GetMapping
    public Page<NotificationEntity> listNotifications(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Boolean isActionRequired,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return service.listMyNotifications(userId, isRead, isActionRequired, pageable);
    }

    @GetMapping("/my")
    public java.util.List<NotificationEntity> listMyNotifications(@RequestParam Long userId) {
        return service.listMyNotifications(userId);
    }

    @GetMapping("/my/unread")
    public java.util.List<NotificationEntity> listUnreadNotifications(@RequestParam Long userId) {
        return service.listUnreadNotifications(userId);
    }

    @GetMapping("/my/unread-count")
    public UnreadCountResponse getUnreadCount(@RequestParam Long userId) {
        Long count = service.getUnreadCount(userId);
        return new UnreadCountResponse(count);
    }

    @PostMapping("/{id}/read")
    public NotificationEntity markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        return service.markAsRead(id, userId);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(@RequestParam Long userId) {
        service.markAllAsRead(userId);
    }

    @PutMapping("/preference")
    public void updatePreference(@RequestParam Long userId, @RequestParam boolean allowNormal) {
        service.updatePreference(userId, allowNormal);
    }

    public record UnreadCountResponse(Long count) {}
}
