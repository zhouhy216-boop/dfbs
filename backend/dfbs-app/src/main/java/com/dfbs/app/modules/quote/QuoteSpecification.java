package com.dfbs.app.modules.quote;

import com.dfbs.app.modules.quote.enums.QuotePaymentStatus;
import com.dfbs.app.modules.quote.enums.QuoteStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class QuoteSpecification {

    private QuoteSpecification() {}

    /**
     * Pending payment list: collectorId == current user, status CONFIRMED, paymentStatus != PAID.
     * Optional: customerName (recipient like), createTime range, paymentStatus exact.
     */
    public static Specification<QuoteEntity> myPendingPaymentQuotes(
            Long collectorId,
            String customerName,
            LocalDateTime createTimeFrom,
            LocalDateTime createTimeTo,
            QuotePaymentStatus paymentStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("collectorId"), collectorId));
            predicates.add(root.get("status").in(QuoteStatus.CONFIRMED));
            predicates.add(cb.notEqual(root.get("paymentStatus"), QuotePaymentStatus.PAID));

            if (customerName != null && !customerName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("recipient")), "%" + customerName.toLowerCase() + "%"));
            }
            if (createTimeFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createTimeFrom));
            }
            if (createTimeTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createTimeTo));
            }
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
