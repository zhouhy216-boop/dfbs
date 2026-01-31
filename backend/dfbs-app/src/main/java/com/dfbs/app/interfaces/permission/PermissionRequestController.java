package com.dfbs.app.interfaces.permission;

import com.dfbs.app.application.permission.ApproveRequestDto;
import com.dfbs.app.application.permission.PermissionRequestDto;
import com.dfbs.app.application.permission.PermissionRequestService;
import com.dfbs.app.modules.permission.PermissionRequestEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission-requests")
public class PermissionRequestController {

    private final PermissionRequestService permissionRequestService;

    public PermissionRequestController(PermissionRequestService permissionRequestService) {
        this.permissionRequestService = permissionRequestService;
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionRequestEntity apply(@RequestBody PermissionRequestDto dto, @RequestParam Long applicantId) {
        return permissionRequestService.create(applicantId, dto);
    }

    @PostMapping("/{id}/resubmit")
    public PermissionRequestEntity resubmit(@PathVariable Long id,
                                            @RequestBody ResubmitRequest body,
                                            @RequestParam Long applicantId) {
        return permissionRequestService.resubmit(id, applicantId, body.newDescription(), body.newReason());
    }

    @PostMapping("/{id}/return")
    public PermissionRequestEntity returnRequest(@PathVariable Long id,
                                                 @RequestBody CommentRequest body,
                                                 @RequestParam Long adminId) {
        return permissionRequestService.returnRequest(id, adminId, body.comment());
    }

    @PostMapping("/{id}/reject")
    public PermissionRequestEntity reject(@PathVariable Long id,
                                           @RequestBody CommentRequest body,
                                           @RequestParam Long adminId) {
        return permissionRequestService.reject(id, adminId, body.comment());
    }

    @PostMapping("/{id}/approve")
    public PermissionRequestEntity approve(@PathVariable Long id,
                                           @RequestBody ApproveRequestDto body,
                                           @RequestParam Long adminId) {
        return permissionRequestService.approve(id, adminId, body.adminComment(), body.newAuthorities());
    }

    @GetMapping("/my-requests")
    public List<PermissionRequestEntity> myRequests(@RequestParam Long applicantId) {
        return permissionRequestService.getMyRequests(applicantId);
    }

    @GetMapping("/pending")
    public List<PermissionRequestEntity> pending() {
        return permissionRequestService.getPendingRequests();
    }

    public record ResubmitRequest(String newDescription, String newReason) {}
    public record CommentRequest(String comment) {}
}
