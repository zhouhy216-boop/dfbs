package com.dfbs.app.modules.statement;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "account_statement_item")
@Data
public class AccountStatementItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statement_id", nullable = false)
    private Long statementId;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "quote_no", nullable = false, length = 64)
    private String quoteNo;

    @Column(name = "quote_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal quoteTotal = BigDecimal.ZERO;

    @Column(name = "quote_paid", nullable = false, precision = 19, scale = 4)
    private BigDecimal quotePaid = BigDecimal.ZERO;

    @Column(name = "quote_unpaid", nullable = false, precision = 19, scale = 4)
    private BigDecimal quoteUnpaid = BigDecimal.ZERO;

    public AccountStatementItemEntity() {}
}
