package com.dfbs.app.interfaces.warehouse;

import com.dfbs.app.application.warehouse.WhReplenishService;
import com.dfbs.app.application.warehouse.dto.WhApproveReq;
import com.dfbs.app.application.warehouse.dto.WhReplenishReq;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.warehouse.WhReplenishRequestEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse/replenish")
public class WhReplenishController {

    private final WhReplenishService whReplenishService;
    private final CurrentUserIdResolver userIdResolver;

    public WhReplenishController(WhReplenishService whReplenishService, CurrentUserIdResolver userIdResolver) {
        this.whReplenishService = whReplenishService;
        this.userIdResolver = userIdResolver;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public WhReplenishRequestEntity create(@RequestBody WhReplenishReq req) {
        Long applicantId = userIdResolver.getCurrentUserId();
        return whReplenishService.create(req, applicantId);
    }

    @PostMapping("/approve-l1")
    public WhReplenishRequestEntity approveL1(@RequestBody WhApproveReq req) {
        Long userId = userIdResolver.getCurrentUserId();
        return whReplenishService.approveL1(userId, req);
    }

    @PostMapping("/approve-l2")
    public WhReplenishRequestEntity approveL2(@RequestBody WhApproveReq req) {
        Long userId = userIdResolver.getCurrentUserId();
        return whReplenishService.approveL2(userId, req);
    }

    @GetMapping("/my-pending")
    public List<WhReplenishRequestEntity> myPending() {
        return whReplenishService.listPending();
    }

    @GetMapping("/my-requests")
    public List<WhReplenishRequestEntity> myRequests() {
        Long userId = userIdResolver.getCurrentUserId();
        return whReplenishService.listMyRequests(userId);
    }
}
