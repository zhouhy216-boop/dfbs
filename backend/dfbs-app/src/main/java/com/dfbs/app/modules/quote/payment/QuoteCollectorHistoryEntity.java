package com.dfbs.app.modules.quote.payment;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_collector_history")
@Data
public class QuoteCollectorHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public QuoteCollectorHistoryEntity() {}
}
