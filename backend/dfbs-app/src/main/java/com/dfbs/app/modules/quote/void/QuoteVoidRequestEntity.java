package com.dfbs.app.modules.quote.void_;

import com.dfbs.app.modules.quote.enums.QuoteStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_void_request")
@Data
public class QuoteVoidRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false)
    private Long quoteId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 32)
    private VoidRequestStage currentStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VoidRequestStatus status = VoidRequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private QuoteStatus previousStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public QuoteVoidRequestEntity() {}
}
