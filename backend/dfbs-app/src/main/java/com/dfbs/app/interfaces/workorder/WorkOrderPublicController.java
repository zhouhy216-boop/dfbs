package com.dfbs.app.interfaces.workorder;

import com.dfbs.app.application.workorder.WorkOrderService;
import com.dfbs.app.application.workorder.dto.WorkOrderCreateReq;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public API for external customers to submit work orders. No authentication required.
 */
@RestController
@RequestMapping("/api/v1/public/work-orders")
public class WorkOrderPublicController {

    private final WorkOrderService workOrderService;

    public WorkOrderPublicController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/create")
    public ResponseEntity<WorkOrderEntity> create(@RequestBody WorkOrderCreateReq req) {
        WorkOrderEntity created = workOrderService.createPublic(req);
        return ResponseEntity.ok(created);
    }
}
