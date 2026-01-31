package com.dfbs.app.modules.quote.workflow;

import com.dfbs.app.modules.quote.enums.QuoteStatus;
import com.dfbs.app.modules.quote.enums.WorkflowAction;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_workflow_history")
@Data
public class QuoteWorkflowHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkflowAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private QuoteStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", length = 32)
    private QuoteStatus currentStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public QuoteWorkflowHistoryEntity() {}
}
