package com.dfbs.app.modules.statement;

import com.dfbs.app.modules.quote.enums.Currency;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_statement")
@Data
public class AccountStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statement_no", nullable = false, unique = true, length = 64)
    private String statementNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", length = 256)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Currency currency;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StatementStatus status = StatementStatus.PENDING;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "statementId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountStatementItemEntity> items = new ArrayList<>();

    public AccountStatementEntity() {}
}
