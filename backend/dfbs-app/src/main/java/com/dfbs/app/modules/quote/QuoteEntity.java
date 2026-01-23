package com.dfbs.app.modules.quote;

import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote")
@EntityListeners(AuditingEntityListener.class)
@Data
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_no", nullable = false, unique = true)
    private String quoteNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false)
    private QuoteSourceType sourceType;

    @Column(name = "source_ref_id")
    private String sourceRefId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    private String recipient;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public QuoteEntity() {}
}
