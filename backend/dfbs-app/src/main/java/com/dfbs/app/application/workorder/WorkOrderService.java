package com.dfbs.app.application.workorder;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.warehouse.WhCoreService;
import com.dfbs.app.application.warehouse.dto.WhOutboundReq;
import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.warehouse.OutboundRefType;
import com.dfbs.app.modules.warehouse.WarehouseType;
import com.dfbs.app.modules.warehouse.WhWarehouseEntity;
import com.dfbs.app.modules.warehouse.WhWarehouseRepo;
import com.dfbs.app.application.workorder.dto.WorkOrderCreateReq;
import com.dfbs.app.application.workorder.dto.WorkOrderDetailDto;
import com.dfbs.app.application.workorder.dto.WorkOrderPartReq;
import com.dfbs.app.application.workorder.dto.WorkOrderRecordReq;
import com.dfbs.app.modules.workorder.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WorkOrderService {

    private final QuoteRepo quoteRepo;
    private final WorkOrderRepo workOrderRepo;
    private final WorkOrderRecordRepo workOrderRecordRepo;
    private final WorkOrderPartRepo workOrderPartRepo;
    private final NotificationService notificationService;
    private final WhCoreService whCoreService;
    private final WhWarehouseRepo whWarehouseRepo;
    private final CurrentUserIdResolver userIdResolver;

    public WorkOrderService(QuoteRepo quoteRepo, WorkOrderRepo workOrderRepo,
                            WorkOrderRecordRepo workOrderRecordRepo, WorkOrderPartRepo workOrderPartRepo,
                            NotificationService notificationService, WhCoreService whCoreService,
                            WhWarehouseRepo whWarehouseRepo, CurrentUserIdResolver userIdResolver) {
        this.quoteRepo = quoteRepo;
        this.workOrderRepo = workOrderRepo;
        this.workOrderRecordRepo = workOrderRecordRepo;
        this.workOrderPartRepo = workOrderPartRepo;
        this.notificationService = notificationService;
        this.whCoreService = whCoreService;
        this.whWarehouseRepo = whWarehouseRepo;
        this.userIdResolver = userIdResolver;
    }

    /**
     * Create a placeholder work order from a CONFIRMED quote. Enforces one quote -> one downstream doc.
     */
    @Transactional
    public WorkOrderEntity createPlaceholder(Long quoteId, Long initiatorId) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        if (quote.getStatus() != QuoteStatus.CONFIRMED) {
            throw new IllegalStateException("只能对已确认的报价单发起工单");
        }
        if (quote.getAssigneeId() == null || !quote.getAssigneeId().equals(initiatorId)) {
            throw new IllegalStateException("只有报价单负责人可发起工单");
        }
        if (quote.getSourceType() == QuoteSourceType.WORK_ORDER) {
            throw new IllegalStateException("来源为工单的报价单不能再次发起工单，避免循环");
        }
        if (quote.getDownstreamId() != null) {
            throw new IllegalStateException("已发起下游单据，无法重复");
        }

        String orderNo = "WO-QUOTE-" + quoteId;
        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setOrderNo(orderNo);
        workOrder.setType(WorkOrderType.REPAIR);
        workOrder.setStatus(WorkOrderStatus.PENDING);
        workOrder.setCustomerName(quote.getCustomerName() != null ? quote.getCustomerName() : "-");
        workOrder.setContactPerson(quote.getRecipient() != null ? quote.getRecipient() : "-");
        workOrder.setContactPhone(quote.getPhone() != null ? quote.getPhone() : "-");
        workOrder.setServiceAddress(quote.getAddress() != null ? quote.getAddress() : "-");
        workOrder.setIssueDescription("Created from Quote #" + quote.getQuoteNo());
        workOrder.setQuoteId(quoteId);
        workOrder.setInitiatorId(initiatorId);
        workOrder = workOrderRepo.save(workOrder);

        quote.setDownstreamType(DownstreamType.WORK_ORDER);
        quote.setDownstreamId(workOrder.getId());
        quoteRepo.save(quote);

        String title = "新工单: 报价单 " + quote.getQuoteNo();
        String content = "从报价单 " + quote.getQuoteNo() + " 发起的工单已创建";
        String targetUrl = "/work-orders/" + workOrder.getId();
        notificationService.send(initiatorId, title, content, targetUrl);

        return workOrder;
    }

    /**
     * Cancel a work order (e.g. when quote is voided).
     */
    @Transactional
    public WorkOrderEntity cancel(Long workOrderId, String reason) {
        WorkOrderEntity workOrder = workOrderRepo.findById(workOrderId)
                .orElseThrow(() -> new IllegalStateException("Work order not found: id=" + workOrderId));
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            return workOrder;
        }
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        return workOrderRepo.save(workOrder);
    }

    // ---------- Public / dispatch / accept / records / parts ----------

    /**
     * Create a work order from public request (no quote). Generate orderNo = "WO-" + timestamp.
     */
    @Transactional
    public WorkOrderEntity createPublic(WorkOrderCreateReq req) {
        String orderNo = "WO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        WorkOrderEntity wo = new WorkOrderEntity();
        wo.setOrderNo(orderNo);
        wo.setType(WorkOrderType.REPAIR);
        wo.setStatus(WorkOrderStatus.PENDING);
        wo.setCustomerName(requireNonBlank(req.getCustomerName(), "customerName"));
        wo.setContactPerson(requireNonBlank(req.getContactPerson(), "contactPerson"));
        wo.setContactPhone(requireNonBlank(req.getContactPhone(), "contactPhone"));
        wo.setServiceAddress(requireNonBlank(req.getServiceAddress(), "serviceAddress"));
        wo.setDeviceModelId(req.getDeviceModelId());
        wo.setIssueDescription(req.getIssueDescription());
        wo.setAppointmentTime(req.getAppointmentTime());
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    /**
     * Create a work order internally (e.g. from phone call). Same as public create but set initiatorId to current user.
     */
    @Transactional
    public WorkOrderEntity createInternal(WorkOrderCreateReq req) {
        WorkOrderEntity wo = createPublic(req);
        wo.setInitiatorId(userIdResolver.getCurrentUserId());
        return workOrderRepo.save(wo);
    }

    /**
     * Reject a PENDING work order with a reason. Sets cancellationReason and status = CANCELLED.
     */
    @Transactional
    public WorkOrderEntity reject(Long id, String reason) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (wo.getStatus() != WorkOrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING work orders can be rejected; current=" + wo.getStatus());
        }
        wo.setCancellationReason(reason != null ? reason : "");
        wo.setStatus(WorkOrderStatus.CANCELLED);
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    /**
     * Dispatch work order to a service manager. Status must be PENDING.
     */
    @Transactional
    public WorkOrderEntity dispatch(Long id, Long serviceManagerId) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (wo.getStatus() != WorkOrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING work orders can be dispatched; current=" + wo.getStatus());
        }
        wo.setServiceManagerId(serviceManagerId);
        wo.setDispatcherId(userIdResolver.getCurrentUserId());
        wo.setStatus(WorkOrderStatus.DISPATCHED);
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    /**
     * Service manager accepts the work order. Status must be DISPATCHED; current user must be the assigned service manager.
     */
    @Transactional
    public WorkOrderEntity accept(Long id, Long serviceManagerId) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (wo.getStatus() != WorkOrderStatus.DISPATCHED) {
            throw new IllegalStateException("Only DISPATCHED work orders can be accepted; current=" + wo.getStatus());
        }
        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!java.util.Objects.equals(wo.getServiceManagerId(), currentUserId) || !java.util.Objects.equals(serviceManagerId, currentUserId)) {
            throw new IllegalStateException("Only the assigned service manager can accept this work order");
        }
        wo.setStatus(WorkOrderStatus.ACCEPTED);
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    /**
     * Add a process record. If work order status is ACCEPTED, auto-update to PROCESSING.
     */
    @Transactional
    public WorkOrderRecordEntity addProcessRecord(Long id, WorkOrderRecordReq req) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        WorkOrderRecordEntity rec = new WorkOrderRecordEntity();
        rec.setWorkOrderId(id);
        rec.setDescription(req.getDescription());
        rec.setAttachmentUrl(req.getAttachmentUrl());
        setAudit(rec);
        rec = workOrderRecordRepo.save(rec);
        if (wo.getStatus() == WorkOrderStatus.ACCEPTED) {
            wo.setStatus(WorkOrderStatus.PROCESSING);
            setAudit(wo);
            workOrderRepo.save(wo);
        }
        return rec;
    }

    /**
     * Add planned part usage (PENDING). Does not deduct stock.
     */
    @Transactional
    public WorkOrderPartEntity addPartUsage(Long id, WorkOrderPartReq req) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (req.getPartNo() == null || req.getPartNo().isBlank()) {
            throw new IllegalArgumentException("partNo is required");
        }
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        WorkOrderPartEntity part = new WorkOrderPartEntity();
        part.setWorkOrderId(id);
        part.setPartNo(req.getPartNo());
        part.setQuantity(req.getQuantity());
        part.setUsageStatus(PartUsageStatus.PENDING);
        setAudit(part);
        return workOrderPartRepo.save(part);
    }

    /**
     * Consume part: outbound from service manager's satellite warehouse and mark part usage as OUTBOUNDED.
     */
    @Transactional
    public WorkOrderPartEntity consumePart(Long woPartId) {
        WorkOrderPartEntity part = workOrderPartRepo.findById(woPartId)
                .orElseThrow(() -> new IllegalArgumentException("Work order part not found: id=" + woPartId));
        if (part.getUsageStatus() != PartUsageStatus.PENDING) {
            throw new IllegalStateException("Only PENDING part usage can be consumed; current=" + part.getUsageStatus());
        }
        WorkOrderEntity wo = workOrderRepo.findById(part.getWorkOrderId())
                .orElseThrow(() -> new IllegalStateException("Work order not found"));
        Long serviceManagerId = wo.getServiceManagerId();
        if (serviceManagerId == null) {
            throw new IllegalStateException("Work order has no assigned service manager; cannot determine warehouse");
        }
        WhWarehouseEntity satellite = whWarehouseRepo.findFirstByManagerIdAndTypeAndIsActiveTrue(serviceManagerId, WarehouseType.SATELLITE)
                .orElseThrow(() -> new IllegalStateException("No active satellite warehouse found for service manager id=" + serviceManagerId));
        WhOutboundReq outReq = new WhOutboundReq(
                satellite.getId(),
                part.getPartNo(),
                part.getQuantity(),
                OutboundRefType.WORK_ORDER,
                wo.getOrderNo(),
                null
        );
        whCoreService.outbound(outReq);
        part.setUsageStatus(PartUsageStatus.OUTBOUNDED);
        part.setWarehouseId(satellite.getId());
        setAudit(part);
        return workOrderPartRepo.save(part);
    }

    /**
     * Pool: work orders waiting for dispatch (PENDING).
     */
    public java.util.List<WorkOrderEntity> getPool() {
        return workOrderRepo.findByStatus(WorkOrderStatus.PENDING);
    }

    /**
     * My orders: work orders assigned to the given service manager.
     */
    public java.util.List<WorkOrderEntity> getMyOrders(Long serviceManagerId) {
        return workOrderRepo.findByServiceManagerId(serviceManagerId);
    }

    /**
     * Get full detail: work order + records + parts.
     */
    public WorkOrderDetailDto getDetail(Long id) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        var records = workOrderRecordRepo.findByWorkOrderIdOrderByCreatedAtAsc(id);
        var parts = workOrderPartRepo.findByWorkOrderId(id);
        return new WorkOrderDetailDto(wo, records, parts);
    }

    /**
     * Submit work order for customer signature. Status must be PROCESSING (or PENDING_SIGN).
     */
    @Transactional
    public WorkOrderEntity submitForSignature(Long id) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (wo.getStatus() != WorkOrderStatus.PROCESSING && wo.getStatus() != WorkOrderStatus.PENDING_SIGN) {
            throw new IllegalStateException("Only PROCESSING work orders can be submitted for signature; current=" + wo.getStatus());
        }
        wo.setStatus(WorkOrderStatus.PENDING_SIGN);
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    /**
     * Complete work order with customer signature URL.
     */
    @Transactional
    public WorkOrderEntity complete(Long id, String signatureUrl) {
        WorkOrderEntity wo = workOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: id=" + id));
        if (wo.getStatus() != WorkOrderStatus.PENDING_SIGN && wo.getStatus() != WorkOrderStatus.PROCESSING) {
            throw new IllegalStateException("Work order must be PENDING_SIGN or PROCESSING to complete; current=" + wo.getStatus());
        }
        wo.setCustomerSignatureUrl(signatureUrl);
        wo.setStatus(WorkOrderStatus.COMPLETED);
        setAudit(wo);
        return workOrderRepo.save(wo);
    }

    private void setAudit(WorkOrderEntity e) {
        String uid = userIdResolver.getCurrentUserId().toString();
        e.setUpdatedBy(uid);
        if (e.getCreatedBy() == null) e.setCreatedBy(uid);
    }

    private void setAudit(WorkOrderRecordEntity e) {
        String uid = userIdResolver.getCurrentUserId().toString();
        e.setCreatedBy(uid);
        e.setUpdatedBy(uid);
    }

    private void setAudit(WorkOrderPartEntity e) {
        String uid = userIdResolver.getCurrentUserId().toString();
        e.setCreatedBy(uid);
        e.setUpdatedBy(uid);
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
