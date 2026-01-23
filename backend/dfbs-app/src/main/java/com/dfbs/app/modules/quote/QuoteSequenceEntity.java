package com.dfbs.app.modules.quote;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "quote_number_sequence",
    uniqueConstraints = @UniqueConstraint(columnNames = { "user_initials", "year_month" })
)
@Data
public class QuoteSequenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_initials", nullable = false)
    private String userInitials;

    @Column(name = "year_month", nullable = false)
    private String yearMonth;

    @Column(name = "current_seq", nullable = false)
    private Integer currentSeq;

    public QuoteSequenceEntity() {}
}
