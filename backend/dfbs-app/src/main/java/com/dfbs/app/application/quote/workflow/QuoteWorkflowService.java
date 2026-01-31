package com.dfbs.app.application.quote.workflow;

import com.dfbs.app.application.notification.NotificationService;
import com.dfbs.app.application.quote.QuoteService;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.WorkflowAction;
import com.dfbs.app.modules.quote.workflow.QuoteWorkflowHistoryEntity;
import com.dfbs.app.modules.quote.workflow.QuoteWorkflowHistoryRepo;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryEntity;
import com.dfbs.app.modules.quote.payment.QuoteCollectorHistoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteWorkflowService {

    private static final long MOCK_FINANCE_NOTIFY_USER_ID = 1L;

    private final QuoteRepo quoteRepo;
    private final QuoteWorkflowHistoryRepo workflowHistoryRepo;
    private final QuoteCollectorHistoryRepo collectorHistoryRepo;
    private final NotificationService notificationService;
    private final QuoteService quoteService;

    public QuoteWorkflowService(QuoteRepo quoteRepo, QuoteWorkflowHistoryRepo workflowHistoryRepo,
                               QuoteCollectorHistoryRepo collectorHistoryRepo,
                               NotificationService notificationService, QuoteService quoteService) {
        this.quoteRepo = quoteRepo;
        this.workflowHistoryRepo = workflowHistoryRepo;
        this.collectorHistoryRepo = collectorHistoryRepo;
        this.notificationService = notificationService;
        this.quoteService = quoteService;
    }

    /**
     * Customer Confirmer submits quote to Finance.
     * DRAFT or RETURNED -> APPROVAL_PENDING.
     */
    @Transactional
    public QuoteEntity submit(Long quoteId, Long customerConfirmerId) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.RETURNED) {
            throw new IllegalStateException("Only DRAFT or RETURNED quote can be submitted to Finance");
        }

        QuoteStatus previous = quote.getStatus();
        quote.setStatus(QuoteStatus.APPROVAL_PENDING);
        quote.setCustomerConfirmerId(customerConfirmerId);
        if (quote.getFirstSubmissionTime() == null) {
            quote.setFirstSubmissionTime(LocalDateTime.now());
        }
        quote = quoteRepo.save(quote);

        recordHistory(quoteId, customerConfirmerId, WorkflowAction.SUBMIT, previous, QuoteStatus.APPROVAL_PENDING, null);
        notifyFinance(quote, "报价单已提交财务审核");
        return quote;
    }

    /**
     * Finance audits: PASS (-> CONFIRMED, optional assign collector) or REJECT (-> RETURNED).
     */
    @Transactional
    public QuoteEntity financeAudit(Long quoteId, String result, Long newCollectorId, Long auditorId, String reason) {
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        if (quote.getStatus() != QuoteStatus.APPROVAL_PENDING) {
            throw new IllegalStateException("Only APPROVAL_PENDING quote can be audited by Finance");
        }

        QuoteStatus previous = quote.getStatus();

        if ("REJECT".equals(result)) {
            quote.setStatus(QuoteStatus.RETURNED);
            quote = quoteRepo.save(quote);
            recordHistory(quoteId, auditorId, WorkflowAction.REJECT, previous, QuoteStatus.RETURNED, reason);
            if (quote.getCustomerConfirmerId() != null) {
                notifyUser(quote.getCustomerConfirmerId(), quote, "报价单已被财务退回", reason);
            }
            return quote;
        }

        if (!"PASS".equals(result)) {
            throw new IllegalStateException("Invalid audit result: " + result);
        }

        quote.setStatus(QuoteStatus.CONFIRMED);
        if (newCollectorId != null) {
            Long fromUserId = quote.getCollectorId();
            quote.setCollectorId(newCollectorId);
            QuoteCollectorHistoryEntity history = new QuoteCollectorHistoryEntity();
            history.setQuoteId(quoteId);
            history.setFromUserId(fromUserId);
            history.setToUserId(newCollectorId);
            history.setChangedBy(auditorId);
            history.setChangedAt(LocalDateTime.now());
            collectorHistoryRepo.save(history);
        }
        quote = quoteRepo.save(quote);

        recordHistory(quoteId, auditorId, WorkflowAction.APPROVE, previous, QuoteStatus.CONFIRMED, reason);
        quoteService.triggerPostConfirmNotifications(quote);
        if (quote.getCollectorId() != null) {
            notifyUser(quote.getCollectorId(), quote, "报价单已通过财务审核，请安排收款", null);
        }
        return quote;
    }

    /**
     * Assign collector (when quote is CONFIRMED). Delegates to QuoteService.changeCollector.
     */
    @Transactional
    public QuoteEntity assignCollector(Long quoteId, Long newCollectorId, Long operatorId) {
        return quoteService.changeCollector(quoteId, newCollectorId, operatorId);
    }

    /**
     * Fallback: move quote to logical previous step (e.g. APPROVAL_PENDING -> DRAFT, CONFIRMED -> APPROVAL_PENDING).
     * Records workflow history with action FALLBACK and the given reason. Re-submission is allowed from the fallback state.
     */
    @Transactional
    public QuoteEntity fallback(Long quoteId, Long operatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Fallback reason is mandatory");
        }
        QuoteEntity quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalStateException("Quote not found: id=" + quoteId));

        QuoteStatus current = quote.getStatus();
        QuoteStatus previousStep;
        switch (current) {
            case APPROVAL_PENDING:
                previousStep = QuoteStatus.DRAFT;
                break;
            case CONFIRMED:
                previousStep = QuoteStatus.APPROVAL_PENDING;
                break;
            case RETURNED:
                previousStep = QuoteStatus.DRAFT;
                break;
            default:
                throw new IllegalStateException("Fallback not allowed from status: " + current);
        }

        quote.setStatus(previousStep);
        quote = quoteRepo.save(quote);
        recordHistory(quoteId, operatorId, WorkflowAction.FALLBACK, current, previousStep, reason);
        return quote;
    }

    private void recordHistory(Long quoteId, Long operatorId, WorkflowAction action,
                               QuoteStatus previousStatus, QuoteStatus currentStatus, String reason) {
        QuoteWorkflowHistoryEntity h = new QuoteWorkflowHistoryEntity();
        h.setQuoteId(quoteId);
        h.setOperatorId(operatorId);
        h.setAction(action);
        h.setPreviousStatus(previousStatus);
        h.setCurrentStatus(currentStatus);
        h.setReason(reason);
        h.setCreatedAt(LocalDateTime.now());
        workflowHistoryRepo.save(h);
    }

    private void notifyFinance(QuoteEntity quote, String content) {
        String title = "报价单待审核: " + quote.getQuoteNo();
        String targetUrl = "/quotes/" + quote.getId();
        notificationService.send(MOCK_FINANCE_NOTIFY_USER_ID, title, content, targetUrl);
    }

    private void notifyUser(Long userId, QuoteEntity quote, String titleSuffix, String detail) {
        String title = quote.getQuoteNo() + " - " + titleSuffix;
        String content = detail != null ? detail : ("报价单号: " + quote.getQuoteNo());
        String targetUrl = "/quotes/" + quote.getId();
        notificationService.send(userId, title, content, targetUrl);
    }

    @Transactional(readOnly = true)
    public List<QuoteWorkflowHistoryEntity> getHistory(Long quoteId) {
        return workflowHistoryRepo.findByQuoteIdOrderByCreatedAtDesc(quoteId);
    }
}
