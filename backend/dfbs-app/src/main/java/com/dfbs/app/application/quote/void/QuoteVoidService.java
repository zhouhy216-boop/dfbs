package com.dfbs.app.application.quote.void_;

import com.dfbs.app.application.invoice.InvoiceApplicationService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.application.quote.payment.QuotePaymentService;
import com.dfbs.app.application.shipment.ShipmentService;
import com.dfbs.app.application.workorder.WorkOrderService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.DownstreamType;
import com.dfbs.app.modules.quote.enums.QuoteInvoiceStatus;
import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.enums.WorkflowAction;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationRepo;
import com.dfbs.app.modules.quote.void_.QuoteVoidRequestEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidRequestRepo;
import com.dfbs.app.modules.quote.void_.VoidRequestStage;
import com.dfbs.app.modules.quote.void_.VoidRequestStatus;
import com.dfbs.app.modules.quote.void_.VoidRequesterRole;
import com.dfbs.app.modules.quote.workflow.QuoteWorkflowHistoryEntity;
import com.dfbs.app.modules.quote.workflow.QuoteWorkflowHistoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuoteVoidService {

    private final QuoteRepo quoteRepo;
    private final QuoteVoidApplicationRepo applicationRepo;
    private final QuoteVoidRequestRepo voidRequestRepo;
    private final QuoteWorkflowHistoryRepo workflowHistoryRepo;
    private final QuoteService quoteService;
    private final QuotePaymentService paymentService;
    private final InvoiceApplicationService invoiceApplicationService;
    private final ShipmentService shipmentService;
    private final WorkOrderService workOrderService;

    public QuoteVoidService(QuoteRepo quoteRepo, QuoteVoidApplicationRepo applicationRepo,
                            QuoteVoidRequestRepo voidRequestRepo,
                            QuoteWorkflowHistoryRepo workflowHistoryRepo, QuoteService quoteService,
                            QuotePaymentService paymentService, InvoiceApplicationService invoiceApplicationService,
                            ShipmentService shipmentService, WorkOrderService workOrderService) {
        this.quoteRepo = quoteRepo;
        this.applicationRepo = applicationRepo;
        this.voidRequestRepo = voidRequestRepo;
        this.workflowHistoryRepo = workflowHistoryRepo;
        this.quoteService = quoteService;
        this.paymentService = paymentService;
        this.invoiceApplicationService = invoiceApplicationService;
        this.shipmentService = shipmentService;
        this.workOrderService = workOrderService;
    }

    /**
     * Strict control: true if quote has payment (PAID/PARTIAL) or has invoice application (not UNINVOICED).
     */
    public boolean isStrictControlScenario(QuoteEntity quote) {
        if (quote == null) return false;
        if (quote.getPaymentStatus() == QuotePaymentStatus.PAID || quote.getPaymentStatus() == QuotePaymentStatus.PARTIAL) {
            return true;
        }
        return quote.getInvoiceStatus() != null && quote.getInvoiceStatus() != QuoteInvoiceStatus.UNINVOICED;
    }

    private static boolean isPreFinanceConfirm(QuoteStatus status) {
        return status == QuoteStatus.DRAFT || status == QuoteStatus.RETURNED || status == QuoteStatus.APPROVAL_PENDING;
    }

    /**
     * New void flow: apply for void (creates request) or direct void (when privileged).
     * Returns empty when direct void was performed; otherwise the created request.
     */
    @Transactional
    public Optional<QuoteVoidRequestEntity> applyVoid(Long quoteId, String reason, Long operatorId, VoidRequesterRole role) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("作废原因不能为空");
        }
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));
        if (quote.getStatus() == QuoteStatus.CANCELLED) {
            throw new IllegalStateException("报价单已取消，无需申请作废");
        }

        // Only one active void request per quote
        voidRequestRepo.findFirstByQuoteIdAndStatusOrderByCreatedAtDesc(quoteId, VoidRequestStatus.PENDING)
                .ifPresent(r -> {
                    throw new IllegalStateException("报价单已有待审批的作废申请");
                });

        boolean strict = isStrictControlScenario(quote);
        boolean preFinance = isPreFinanceConfirm(quote.getStatus());

        // --- Pre–finance-confirm ---
        if (preFinance) {
            if (role == VoidRequesterRole.INITIATOR) {
                if (quote.getAssigneeId() != null && quote.getAssigneeId().equals(operatorId)) {
                    executeVoid(quote, operatorId);
                    return Optional.empty();
                }
                throw new IllegalStateException("只有报价单负责人可直接作废");
            }
            if (role == VoidRequesterRole.CUSTOMER_CONFIRMER) {
                QuoteVoidRequestEntity req = new QuoteVoidRequestEntity();
                req.setQuoteId(quoteId);
                req.setRequesterId(operatorId);
                req.setReason(reason);
                req.setCurrentStage(VoidRequestStage.INITIATOR);
                req.setStatus(VoidRequestStatus.PENDING);
                req.setPreviousStatus(quote.getStatus());
                req = voidRequestRepo.save(req);
                quote.setStatus(QuoteStatus.VOID_AUDIT_INITIATOR);
                quote.setVoidStatus(QuoteVoidStatus.APPLYING);
                quoteRepo.save(quote);
                return Optional.of(req);
            }
            throw new IllegalStateException("当前阶段仅负责人或客户确认人可操作作废");
        }

        // --- Post–finance-confirm: normal (not strict) ---
        if (!strict) {
            if (role == VoidRequesterRole.FINANCE) {
                executeVoid(quote, operatorId);
                return Optional.empty();
            }
            if (role == VoidRequesterRole.COLLECTOR) {
                if (quote.getCollectorId() == null || !quote.getCollectorId().equals(operatorId)) {
                    throw new IllegalStateException("只有收款人可以申请作废");
                }
                QuoteVoidRequestEntity req = new QuoteVoidRequestEntity();
                req.setQuoteId(quoteId);
                req.setRequesterId(operatorId);
                req.setReason(reason);
                req.setCurrentStage(VoidRequestStage.FINANCE);
                req.setStatus(VoidRequestStatus.PENDING);
                req.setPreviousStatus(quote.getStatus());
                req = voidRequestRepo.save(req);
                quote.setStatus(QuoteStatus.VOID_AUDIT_FINANCE);
                quote.setVoidStatus(QuoteVoidStatus.APPLYING);
                quoteRepo.save(quote);
                return Optional.of(req);
            }
            throw new IllegalStateException("当前阶段仅财务可直接作废或收款人可申请作废");
        }

        // --- Strict control: only Collector may apply ---
        if (role != VoidRequesterRole.COLLECTOR) {
            throw new IllegalStateException("已付款或已开票报价单仅支持收款人申请作废");
        }
        if (quote.getCollectorId() == null || !quote.getCollectorId().equals(operatorId)) {
            throw new IllegalStateException("只有收款人可以申请作废");
        }
        QuoteVoidRequestEntity req = new QuoteVoidRequestEntity();
        req.setQuoteId(quoteId);
        req.setRequesterId(operatorId);
        req.setReason(reason);
        req.setCurrentStage(VoidRequestStage.FINANCE);
        req.setStatus(VoidRequestStatus.PENDING);
        req.setPreviousStatus(quote.getStatus());
        req = voidRequestRepo.save(req);
        quote.setStatus(QuoteStatus.VOID_AUDIT_FINANCE);
        quote.setVoidStatus(QuoteVoidStatus.APPLYING);
        quoteRepo.save(quote);
        return Optional.of(req);
    }

    /**
     * Audit a void request: APPROVE (may advance stage or execute void) or REJECT (revert quote status).
     */
    @Transactional
    public QuoteVoidRequestEntity auditVoid(Long requestId, String result, Long auditorId, String reason) {
        QuoteVoidRequestEntity request = voidRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Void request not found: id=" + requestId));
        if (request.getStatus() != VoidRequestStatus.PENDING) {
            throw new IllegalStateException("该作废申请已审批，不能重复审批");
        }
        QuoteEntity quote = quoteRepo.findById(request.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("Quote not found"));

        if ("REJECT".equalsIgnoreCase(result)) {
            request.setStatus(VoidRequestStatus.REJECTED);
            request.setUpdatedAt(LocalDateTime.now());
            voidRequestRepo.save(request);
            quote.setStatus(request.getPreviousStatus());
            quote.setVoidStatus(QuoteVoidStatus.REJECTED);
            quoteRepo.save(quote);
            return request;
        }

        if (!"PASS".equalsIgnoreCase(result) && !"APPROVE".equalsIgnoreCase(result)) {
            throw new IllegalStateException("Invalid audit result: " + result);
        }

        if (request.getCurrentStage() == VoidRequestStage.INITIATOR) {
            executeVoid(quote, auditorId);
            request.setStatus(VoidRequestStatus.APPROVED);
            request.setUpdatedAt(LocalDateTime.now());
            voidRequestRepo.save(request);
            return request;
        }
        if (request.getCurrentStage() == VoidRequestStage.FINANCE) {
            if (isStrictControlScenario(quote)) {
                request.setCurrentStage(VoidRequestStage.LEADER);
                request.setUpdatedAt(LocalDateTime.now());
                voidRequestRepo.save(request);
                quote.setStatus(QuoteStatus.VOID_AUDIT_LEADER);
                quoteRepo.save(quote);
                quoteService.notifyLeadersOfVoidAfterPaid(quote, reason != null ? reason : request.getReason());
            } else {
                executeVoid(quote, auditorId);
                request.setStatus(VoidRequestStatus.APPROVED);
                request.setUpdatedAt(LocalDateTime.now());
                voidRequestRepo.save(request);
            }
            return request;
        }
        if (request.getCurrentStage() == VoidRequestStage.LEADER) {
            executeVoid(quote, auditorId);
            request.setStatus(VoidRequestStatus.APPROVED);
            request.setUpdatedAt(LocalDateTime.now());
            voidRequestRepo.save(request);
            return request;
        }
        return request;
    }

    /**
     * Execute void: set quote CANCELLED, cancel unconfirmed payments and pending invoice applications, notify.
     * @param operatorId User performing the void (required for workflow history; use 0L if system).
     */
    @Transactional
    public void executeVoid(QuoteEntity quote, Long operatorId) {
        Long opId = operatorId != null ? operatorId : 0L;
        QuoteStatus previousStatus = quote.getStatus();
        quote.setStatus(QuoteStatus.CANCELLED);
        quote.setVoidStatus(QuoteVoidStatus.VOIDED);
        quoteRepo.save(quote);

        paymentService.cancelUnconfirmed(quote.getId());
        invoiceApplicationService.cancelPending(quote.getId());

        if (quote.getDownstreamId() != null && quote.getDownstreamType() != null) {
            if (quote.getDownstreamType() == DownstreamType.SHIPMENT) {
                shipmentService.cancel(quote.getDownstreamId(), "Quote Voided");
            } else if (quote.getDownstreamType() == DownstreamType.WORK_ORDER) {
                workOrderService.cancel(quote.getDownstreamId(), "Quote Voided");
            }
        }

        recordVoidHistory(quote.getId(), opId, previousStatus, "作废");
        if (quote.getPaymentStatus() == QuotePaymentStatus.PAID || quote.getPaymentStatus() == QuotePaymentStatus.PARTIAL) {
            quoteService.notifyLeadersOfVoidAfterPaid(quote, "报价单已作废");
        }
    }

    /**
     * Direct void for privileged roles (Initiator in pre-finance, Finance in post-finance normal).
     */
    @Transactional
    public void directVoidByRole(Long quoteId, Long operatorId, String reason, VoidRequesterRole role) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("作废原因不能为空");
        }
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));
        if (quote.getStatus() == QuoteStatus.CANCELLED) {
            throw new IllegalStateException("报价单已取消");
        }
        boolean strict = isStrictControlScenario(quote);
        boolean preFinance = isPreFinanceConfirm(quote.getStatus());

        if (preFinance && role == VoidRequesterRole.INITIATOR) {
            if (quote.getAssigneeId() != null && quote.getAssigneeId().equals(operatorId)) {
                executeVoid(quote, operatorId);
                return;
            }
            throw new IllegalStateException("只有报价单负责人可直接作废");
        }
        if (!preFinance && !strict && role == VoidRequesterRole.FINANCE) {
            executeVoid(quote, operatorId);
            return;
        }
        throw new IllegalStateException("当前不允许直接作废，请走申请流程");
    }

    @Transactional(readOnly = true)
    public List<QuoteVoidRequestEntity> getVoidRequests(Long quoteId) {
        return voidRequestRepo.findByQuoteIdOrderByCreatedAtDesc(quoteId);
    }

    @Transactional(readOnly = true)
    public QuoteVoidRequestEntity getVoidRequest(Long requestId) {
        return voidRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Void request not found: id=" + requestId));
    }

    /**
     * Apply for voiding a quote (Collector).
     * 
     * @param quoteId Quote ID
     * @param applicantId Current user ID (must be collector)
     * @param applyReason Reason for voiding
     * @param attachmentUrls Attachment URLs
     */
    @Transactional
    public QuoteVoidApplicationEntity apply(Long quoteId, Long applicantId, String applyReason, String attachmentUrls) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        // Check: Current user == quote.collectorId
        if (quote.getCollectorId() == null || !quote.getCollectorId().equals(applicantId)) {
            throw new IllegalStateException("只有收款人可以申请作废报价单");
        }

        // Check: quote.voidStatus != APPLYING (Only 1 active application)
        if (quote.getVoidStatus() == QuoteVoidStatus.APPLYING) {
            throw new IllegalStateException("报价单已有待审批的作废申请");
        }

        // Check: quote.status != CANCELLED
        if (quote.getStatus() == QuoteStatus.CANCELLED) {
            throw new IllegalStateException("报价单已取消，无需申请作废");
        }

        // Create application
        QuoteVoidApplicationEntity application = new QuoteVoidApplicationEntity();
        application.setQuoteId(quoteId);
        application.setApplicantId(applicantId);
        application.setApplyReason(applyReason);
        application.setApplyTime(LocalDateTime.now());
        application.setAttachmentUrls(attachmentUrls);
        application = applicationRepo.save(application);

        // Set quote.voidStatus = APPLYING
        quote.setVoidStatus(QuoteVoidStatus.APPLYING);
        quoteRepo.save(quote);

        return application;
    }

    /**
     * Audit void application (Finance).
     * 
     * @param applicationId Application ID
     * @param auditorId Finance user ID
     * @param result PASS or REJECT
     * @param note Audit note
     */
    @Transactional
    public QuoteVoidApplicationEntity audit(Long applicationId, Long auditorId, String result, String note) {
        QuoteVoidApplicationEntity application = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Application not found: id=" + applicationId));

        if (application.getAuditResult() != null) {
            throw new IllegalStateException("申请已审批，不能重复审批");
        }

        QuoteEntity quote = quoteRepo.findById(application.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("Quote not found"));

        application.setAuditorId(auditorId);
        application.setAuditTime(LocalDateTime.now());
        application.setAuditResult(result);
        application.setAuditNote(note);
        application = applicationRepo.save(application);

        if ("PASS".equals(result)) {
            QuoteStatus previousStatus = quote.getStatus();
            quote.setVoidStatus(QuoteVoidStatus.VOIDED);
            quote.setStatus(QuoteStatus.CANCELLED);
            quoteRepo.save(quote);
            recordVoidHistory(quote.getId(), auditorId, previousStatus, note != null ? note : application.getApplyReason());
            if (quote.getPaymentStatus() == QuotePaymentStatus.PAID) {
                quoteService.notifyLeadersOfVoidAfterPaid(quote, note != null ? note : application.getApplyReason());
            }
        } else if ("REJECT".equals(result)) {
            // Set Application result=REJECT
            // Set quote.voidStatus = REJECTED
            quote.setVoidStatus(QuoteVoidStatus.REJECTED);
            quoteRepo.save(quote);
            // Trigger "Notify Collector" (Mock notification)
        } else {
            throw new IllegalStateException("Invalid audit result: " + result);
        }

        return application;
    }

    /**
     * Direct void by Finance (without application).
     * 
     * @param quoteId Quote ID
     * @param auditorId Finance user ID
     * @param reason Reason for direct void
     */
    @Transactional
    public QuoteVoidApplicationEntity directVoid(Long quoteId, Long auditorId, String reason) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        if (quote.getStatus() == QuoteStatus.CANCELLED) {
            throw new IllegalStateException("报价单已取消");
        }
        if (quote.getPaymentStatus() == QuotePaymentStatus.PAID && (reason == null || reason.isBlank())) {
            throw new IllegalStateException("已付清报价单作废必须填写原因");
        }

        // Create Application (auto-approved record)
        QuoteVoidApplicationEntity application = new QuoteVoidApplicationEntity();
        application.setQuoteId(quoteId);
        application.setApplicantId(auditorId);  // Finance is both applicant and auditor
        application.setApplyReason(reason);
        application.setApplyTime(LocalDateTime.now());
        application.setAuditorId(auditorId);
        application.setAuditTime(LocalDateTime.now());
        application.setAuditResult("PASS");
        application.setAuditNote("财务直接作废");
        application = applicationRepo.save(application);

        QuoteStatus previousStatus = quote.getStatus();
        quote.setVoidStatus(QuoteVoidStatus.VOIDED);
        quote.setStatus(QuoteStatus.CANCELLED);
        quoteRepo.save(quote);

        recordVoidHistory(quoteId, auditorId, previousStatus, reason);
        if (quote.getPaymentStatus() == QuotePaymentStatus.PAID && reason != null && !reason.isBlank()) {
            quoteService.notifyLeadersOfVoidAfterPaid(quote, reason);
        }

        return application;
    }

    private void recordVoidHistory(Long quoteId, Long operatorId, QuoteStatus previousStatus, String reason) {
        QuoteWorkflowHistoryEntity h = new QuoteWorkflowHistoryEntity();
        h.setQuoteId(quoteId);
        h.setOperatorId(operatorId);
        h.setAction(WorkflowAction.VOID);
        h.setPreviousStatus(previousStatus);
        h.setCurrentStatus(QuoteStatus.CANCELLED);
        h.setReason(reason);
        h.setCreatedAt(LocalDateTime.now());
        workflowHistoryRepo.save(h);
    }

    @Transactional(readOnly = true)
    public List<QuoteVoidApplicationEntity> getHistory(Long quoteId) {
        return applicationRepo.findByQuoteIdOrderByApplyTimeDesc(quoteId);
    }

    @Transactional(readOnly = true)
    public QuoteVoidApplicationEntity getApplication(Long applicationId) {
        return applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Application not found: id=" + applicationId));
    }
}
