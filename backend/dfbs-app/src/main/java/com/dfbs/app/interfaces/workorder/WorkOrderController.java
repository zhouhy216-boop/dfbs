package com.dfbs.app.interfaces.workorder;

import com.dfbs.app.application.perm.PermEnforcementService;
import com.dfbs.app.application.workorder.WorkOrderService;
import com.dfbs.app.application.workorder.dto.WorkOrderAcceptReq;
import com.dfbs.app.application.workorder.dto.WorkOrderCreateReq;
import com.dfbs.app.application.workorder.dto.WorkOrderDetailDto;
import com.dfbs.app.application.workorder.dto.WorkOrderDispatchReq;
import com.dfbs.app.application.workorder.dto.WorkOrderListDto;
import com.dfbs.app.application.workorder.dto.WorkOrderPartReq;
import com.dfbs.app.application.workorder.dto.WorkOrderRecordReq;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderPartEntity;
import com.dfbs.app.modules.workorder.WorkOrderRecordEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final CurrentUserIdResolver userIdResolver;
    private final PermEnforcementService permEnforcement;

    public WorkOrderController(WorkOrderService workOrderService,
                               CurrentUserIdResolver userIdResolver,
                               PermEnforcementService permEnforcement) {
        this.workOrderService = workOrderService;
        this.userIdResolver = userIdResolver;
        this.permEnforcement = permEnforcement;
    }

    @PostMapping("/create-from-quote")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderEntity createFromQuote(@RequestBody CreateFromQuoteRequest req) {
        permEnforcement.requirePermission("work_order:CREATE");
        return workOrderService.createPlaceholder(req.quoteId(), req.initiatorId());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderEntity createInternal(@RequestBody WorkOrderCreateReq req) {
        permEnforcement.requirePermission("work_order:CREATE");
        return workOrderService.createInternal(req);
    }

    @PostMapping("/reject")
    public WorkOrderEntity reject(@RequestBody RejectBody body) {
        permEnforcement.requirePermission("work_order:REJECT");
        return workOrderService.reject(body.id(), body.reason());
    }

    @PostMapping("/accept-by-dispatcher")
    public WorkOrderEntity acceptByDispatcher(@RequestBody WorkOrderAcceptReq req) {
        permEnforcement.requirePermission("work_order:ASSIGN");
        return workOrderService.acceptByDispatcher(req);
    }

    @PostMapping("/dispatch")
    public WorkOrderEntity dispatch(@RequestBody WorkOrderDispatchReq req) {
        permEnforcement.requirePermission("work_order:ASSIGN");
        return workOrderService.dispatch(req.getWorkOrderId(), req.getServiceManagerId());
    }

    @PostMapping("/accept")
    public WorkOrderEntity accept(@RequestBody AcceptBody body) {
        permEnforcement.requirePermission("work_order:SUBMIT");
        Long currentUserId = userIdResolver.getCurrentUserId();
        return workOrderService.accept(body.id(), currentUserId);
    }

    @PostMapping("/record")
    public WorkOrderRecordEntity addRecord(@RequestBody RecordBody body) {
        permEnforcement.requirePermission("work_order:EDIT");
        return workOrderService.addProcessRecord(body.id(), new WorkOrderRecordReq(body.description(), body.attachmentUrl()));
    }

    @PostMapping("/parts/add")
    public WorkOrderPartEntity addPart(@RequestBody PartAddBody body) {
        permEnforcement.requirePermission("work_order:EDIT");
        return workOrderService.addPartUsage(body.id(), new WorkOrderPartReq(body.partNo(), body.quantity()));
    }

    @PostMapping("/parts/consume")
    public WorkOrderPartEntity consumePart(@RequestBody ConsumePartBody body) {
        permEnforcement.requirePermission("work_order:EDIT");
        return workOrderService.consumePart(body.woPartId());
    }

    @PostMapping("/sign")
    public WorkOrderEntity submitForSignature(@RequestBody IdBody body) {
        permEnforcement.requirePermission("work_order:APPROVE");
        return workOrderService.submitForSignature(body.id());
    }

    @PostMapping("/complete")
    public WorkOrderEntity complete(@RequestBody CompleteBody body) {
        permEnforcement.requirePermission("work_order:CLOSE");
        return workOrderService.complete(body.id(), body.signatureUrl());
    }

    @GetMapping("/pool")
    public List<WorkOrderListDto> pool() {
        permEnforcement.requirePermission("work_order:VIEW");
        return workOrderService.getPool();
    }

    @GetMapping("/my-orders")
    public List<WorkOrderListDto> myOrders() {
        permEnforcement.requirePermission("work_order:VIEW");
        Long currentUserId = userIdResolver.getCurrentUserId();
        return workOrderService.getMyOrders(currentUserId);
    }

    @GetMapping("/{id}")
    public WorkOrderDetailDto getDetail(@PathVariable Long id) {
        permEnforcement.requirePermission("work_order:VIEW");
        return workOrderService.getDetail(id);
    }

    public record CreateFromQuoteRequest(Long quoteId, Long initiatorId) {}
    public record RejectBody(Long id, String reason) {}
    public record AcceptBody(Long id) {}
    public record RecordBody(Long id, String description, String attachmentUrl) {}
    public record PartAddBody(Long id, String partNo, Integer quantity) {}
    public record ConsumePartBody(Long woPartId) {}
    public record IdBody(Long id) {}
    public record CompleteBody(Long id, String signatureUrl) {}
}
