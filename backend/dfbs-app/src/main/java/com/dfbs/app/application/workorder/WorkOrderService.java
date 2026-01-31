package com.dfbs.app.application.workorder;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderRepo;
import com.dfbs.app.modules.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WorkOrderService {

    private final QuoteRepo quoteRepo;
    private final WorkOrderRepo workOrderRepo;
    private final NotificationService notificationService;

    public WorkOrderService(QuoteRepo quoteRepo, WorkOrderRepo workOrderRepo,
                            NotificationService notificationService) {
        this.quoteRepo = quoteRepo;
        this.workOrderRepo = workOrderRepo;
        this.notificationService = notificationService;
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

        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setQuoteId(quoteId);
        workOrder.setInitiatorId(initiatorId);
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setSummary("Created from Quote #" + quote.getQuoteNo());
        workOrder.setCreatedAt(LocalDateTime.now());
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
}
