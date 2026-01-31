package com.dfbs.app.application.expense;

import com.dfbs.app.application.expense.dto.ExpenseDto;
import com.dfbs.app.modules.expense.ClaimStatus;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseStatus;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.QuoteEntity;
import com.dfbs.app.modules.quote.QuoteRepo;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestRepo;
import com.dfbs.app.modules.workorder.WorkOrderEntity;
import com.dfbs.app.modules.workorder.WorkOrderRepo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseStatsService {

    private final ExpenseRepo expenseRepo;
    private final QuoteRepo quoteRepo;
    private final WorkOrderRepo workOrderRepo;
    private final TripRequestRepo tripRequestRepo;
    private final ExchangeRateService exchangeRateService;

    public ExpenseStatsService(ExpenseRepo expenseRepo, QuoteRepo quoteRepo, WorkOrderRepo workOrderRepo,
                              TripRequestRepo tripRequestRepo, ExchangeRateService exchangeRateService) {
        this.expenseRepo = expenseRepo;
        this.quoteRepo = quoteRepo;
        this.workOrderRepo = workOrderRepo;
        this.tripRequestRepo = tripRequestRepo;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional(readOnly = true)
    public List<ExpenseStatsItemDto> getStats(ExpenseStatsRequest request) {
        List<ExpenseEntity> expenses = loadExpensesWithClaim(request);
        if (expenses.isEmpty()) return List.of();

        Set<Long> quoteIds = new HashSet<>();
        Set<Long> workOrderIds = new HashSet<>();
        Set<Long> tripIds = new HashSet<>();
        for (ExpenseEntity e : expenses) {
            if (e.getQuoteId() != null) quoteIds.add(e.getQuoteId());
            if (e.getWorkOrderId() != null) workOrderIds.add(e.getWorkOrderId());
            if (e.getTripRequestId() != null) tripIds.add(e.getTripRequestId());
        }

        Map<Long, WorkOrderEntity> workOrderMap = workOrderIds.isEmpty() ? Map.of() : workOrderRepo.findAllById(workOrderIds).stream().collect(Collectors.toMap(WorkOrderEntity::getId, w -> w));
        for (WorkOrderEntity wo : workOrderMap.values()) {
            if (wo.getQuoteId() != null) quoteIds.add(wo.getQuoteId());
        }
        Map<Long, QuoteEntity> quoteMap = quoteIds.isEmpty() ? Map.of() : quoteRepo.findAllById(quoteIds).stream().collect(Collectors.toMap(QuoteEntity::getId, q -> q));
        Map<Long, TripRequestEntity> tripMap = tripIds.isEmpty() ? Map.of() : tripRequestRepo.findAllById(tripIds).stream().collect(Collectors.toMap(TripRequestEntity::getId, t -> t));

        Map<String, ExpenseStatsItemDto> bucket = new LinkedHashMap<>();

        for (ExpenseEntity e : expenses) {
            if (e.getStatus() == ExpenseStatus.VOID) continue;

            String groupKey = resolveGroupKey(e, request.getGroupBy(), quoteMap, workOrderMap, tripMap);
            ExpenseStatsItemDto item = bucket.computeIfAbsent(groupKey, k -> {
                ExpenseStatsItemDto dto = new ExpenseStatsItemDto();
                dto.setGroupKey(k);
                return dto;
            });

            Currency cur = e.getCurrency() != null ? e.getCurrency() : Currency.CNY;
            BigDecimal amount = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;

            addToMap(item.getTotalAmount(), cur, amount);
            item.setTotalRmb(item.getTotalRmb().add(exchangeRateService.convertToRmb(amount, cur)));

            ClaimStatus claimStatus = e.getClaim() != null ? e.getClaim().getStatus() : null;
            if (claimStatus == ClaimStatus.SUBMITTED) addToMap(item.getSubmittedAmount(), cur, amount);
            if (claimStatus == ClaimStatus.APPROVED || claimStatus == ClaimStatus.PAID) addToMap(item.getApprovedAmount(), cur, amount);
            if (claimStatus == ClaimStatus.REJECTED) addToMap(item.getRejectedAmount(), cur, amount);

            if (request.getGroupBy() == GroupBy.TRIP && e.getTripRequestId() != null) {
                if (e.getExpenseType() == ExpenseType.TRANSPORT) item.setRealTransport(item.getRealTransport().add(amount));
                if (e.getExpenseType() == ExpenseType.ACCOMMODATION) item.setRealAccommodation(item.getRealAccommodation().add(amount));
            }
        }

        if (request.getGroupBy() == GroupBy.TRIP) {
            for (ExpenseStatsItemDto item : bucket.values()) {
                String key = item.getGroupKey();
                if (key.startsWith("Trip:")) {
                    Long tripId = Long.parseLong(key.substring(5));
                    TripRequestEntity trip = tripMap.get(tripId);
                    if (trip != null) {
                        item.setEstTransport(trip.getEstTransportCost() != null ? trip.getEstTransportCost() : BigDecimal.ZERO);
                        item.setEstAccommodation(trip.getEstAccommodationCost() != null ? trip.getEstAccommodationCost() : BigDecimal.ZERO);
                    }
                }
            }
        }

        return new ArrayList<>(bucket.values());
    }

    private static void addToMap(Map<Currency, BigDecimal> map, Currency cur, BigDecimal amount) {
        map.merge(cur, amount, BigDecimal::add);
    }

    private String resolveGroupKey(ExpenseEntity e, GroupBy groupBy, Map<Long, QuoteEntity> quoteMap,
                                  Map<Long, WorkOrderEntity> workOrderMap, Map<Long, TripRequestEntity> tripMap) {
        switch (groupBy) {
            case USER:
                return "User:" + e.getCreatedBy();
            case CUSTOMER: {
                String customer = resolveCustomerName(e, quoteMap, workOrderMap);
                return customer != null ? customer : "N/A";
            }
            case WORK_ORDER:
                return e.getWorkOrderId() != null ? "WO:" + e.getWorkOrderId() : "N/A";
            case TRIP:
                return e.getTripRequestId() != null ? "Trip:" + e.getTripRequestId() : "N/A";
            default:
                return "N/A";
        }
    }

    private String resolveCustomerName(ExpenseEntity e, Map<Long, QuoteEntity> quoteMap, Map<Long, WorkOrderEntity> workOrderMap) {
        if (e.getQuoteId() != null) {
            QuoteEntity q = quoteMap.get(e.getQuoteId());
            return q != null && q.getCustomerName() != null ? q.getCustomerName() : (q != null && q.getCustomerId() != null ? "C" + q.getCustomerId() : "N/A");
        }
        if (e.getWorkOrderId() != null) {
            WorkOrderEntity wo = workOrderMap.get(e.getWorkOrderId());
            if (wo != null && wo.getQuoteId() != null) {
                QuoteEntity q = quoteMap.get(wo.getQuoteId());
                return q != null && q.getCustomerName() != null ? q.getCustomerName() : (q != null && q.getCustomerId() != null ? "C" + q.getCustomerId() : "N/A");
            }
        }
        return "N/A";
    }

    private List<ExpenseEntity> loadExpensesWithClaim(ExpenseStatsRequest request) {
        Specification<ExpenseEntity> spec = (root, query, cb) -> {
            root.fetch("claim", JoinType.LEFT);
            query.distinct(true);
            var list = new ArrayList<jakarta.persistence.criteria.Predicate>();
            list.add(cb.notEqual(root.get("status"), ExpenseStatus.VOID));
            if (request.getStartDate() != null) list.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), request.getStartDate()));
            if (request.getEndDate() != null) list.add(cb.lessThanOrEqualTo(root.get("expenseDate"), request.getEndDate()));
            if (request.getExpenseType() != null) list.add(cb.equal(root.get("expenseType"), request.getExpenseType()));
            if (request.getCurrency() != null) list.add(cb.equal(root.get("currency"), request.getCurrency()));
            return cb.and(list.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return expenseRepo.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> getDetailExpenses(ExpenseStatsRequest request) {
        List<ExpenseEntity> expenses = loadExpensesWithClaim(request);
        return expenses.stream().map(ExpenseDto::from).toList();
    }
}
