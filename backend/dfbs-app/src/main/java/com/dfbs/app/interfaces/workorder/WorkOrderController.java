package com.dfbs.app.interfaces.workorder;

import com.dfbs.app.application.workorder.WorkOrderService;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/create-from-quote")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderEntity createFromQuote(@RequestBody CreateFromQuoteRequest req) {
        return workOrderService.createPlaceholder(req.quoteId(), req.initiatorId());
    }

    public record CreateFromQuoteRequest(Long quoteId, Long initiatorId) {}
}
