package com.dfbs.app.modules.repair;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RepairRecordSpecification {

    private RepairRecordSpecification() {}

    public static Specification<RepairRecordEntity> filter(
            String customerName,
            String machineNo,
            LocalDateTime repairDateFrom,
            LocalDateTime repairDateTo,
            WarrantyStatus warrantyStatus,
            String personInCharge,
            String oldWorkOrderNo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customerName != null && !customerName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("customerName")), "%" + customerName.toLowerCase() + "%"));
            }
            if (machineNo != null && !machineNo.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("machineNo")), "%" + machineNo.toLowerCase() + "%"));
            }
            if (repairDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("repairDate"), repairDateFrom));
            }
            if (repairDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("repairDate"), repairDateTo));
            }
            if (warrantyStatus != null) {
                predicates.add(cb.equal(root.get("warrantyStatus"), warrantyStatus));
            }
            if (personInCharge != null && !personInCharge.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("personInCharge")), "%" + personInCharge.toLowerCase() + "%"));
            }
            if (oldWorkOrderNo != null && !oldWorkOrderNo.isBlank()) {
                predicates.add(cb.equal(root.get("oldWorkOrderNo"), oldWorkOrderNo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
