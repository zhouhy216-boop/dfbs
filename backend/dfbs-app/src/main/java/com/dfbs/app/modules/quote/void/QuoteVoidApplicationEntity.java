package com.dfbs.app.modules.quote.void_;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_void_application")
@Data
public class QuoteVoidApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "apply_reason", length = 200, nullable = false)
    private String applyReason;

    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;

    @Column(name = "attachment_urls", length = 2000)
    private String attachmentUrls;  // JSON array or comma-separated URLs

    @Column(name = "auditor_id")
    private Long auditorId;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "audit_result", length = 32)
    private String auditResult;  // PASS or REJECT

    @Column(name = "audit_note", length = 1000)
    private String auditNote;

    public QuoteVoidApplicationEntity() {}
}
