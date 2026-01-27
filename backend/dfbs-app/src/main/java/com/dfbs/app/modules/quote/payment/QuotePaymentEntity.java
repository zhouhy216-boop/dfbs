package com.dfbs.app.modules.quote.payment;

import com.dfbs.app.modules.quote.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_payment")
@Data
public class QuotePaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "method_id", nullable = false)
    private Long methodId;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.SUBMITTED;

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "confirmer_id")
    private Long confirmerId;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirm_note", length = 1000)
    private String confirmNote;

    @Column(length = 1000)
    private String remark;  // for overpayment

    @Column(name = "attachment_urls", length = 2000)
    private String attachmentUrls;  // JSON array or comma-separated URLs

    public QuotePaymentEntity() {}
}
