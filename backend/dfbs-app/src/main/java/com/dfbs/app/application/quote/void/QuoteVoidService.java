package com.dfbs.app.application.quote.void_;

import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.QuoteVoidStatus;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationEntity;
import com.dfbs.app.modules.quote.void_.QuoteVoidApplicationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteVoidService {

    private final QuoteRepo quoteRepo;
    private final QuoteVoidApplicationRepo applicationRepo;

    public QuoteVoidService(QuoteRepo quoteRepo, QuoteVoidApplicationRepo applicationRepo) {
        this.quoteRepo = quoteRepo;
        this.applicationRepo = applicationRepo;
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
            // Set Application result=PASS
            // Set quote.voidStatus = VOIDED
            quote.setVoidStatus(QuoteVoidStatus.VOIDED);
            // Set quote.status = CANCELLED
            quote.setStatus(QuoteStatus.CANCELLED);
            quoteRepo.save(quote);
            // Trigger "Notify Collector" (Mock notification - just log for now)
            // In production, this would send notification
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

        // Set quote.voidStatus = VOIDED
        quote.setVoidStatus(QuoteVoidStatus.VOIDED);
        // Set quote.status = CANCELLED
        quote.setStatus(QuoteStatus.CANCELLED);
        quoteRepo.save(quote);

        return application;
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
