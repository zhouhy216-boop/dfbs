package com.dfbs.app.application.expense;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseStatus;
import com.dfbs.app.modules.expense.ExpenseType;
import com.dfbs.app.modules.quote.enums.Currency;
import com.dfbs.app.modules.triprequest.TripRequestRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class ExpenseService {

    private final ExpenseRepo expenseRepo;
    private final CurrentUserIdResolver userIdResolver;
    private final TripRequestRepo tripRequestRepo;

    public ExpenseService(ExpenseRepo expenseRepo, CurrentUserIdResolver userIdResolver, TripRequestRepo tripRequestRepo) {
        this.expenseRepo = expenseRepo;
        this.userIdResolver = userIdResolver;
        this.tripRequestRepo = tripRequestRepo;
    }

    private void validateTripLinkForTravelExpense(ExpenseType expenseType, Long tripRequestId) {
        if (expenseType != ExpenseType.TRANSPORT && expenseType != ExpenseType.ACCOMMODATION) return;
        if (tripRequestId == null)
            throw new IllegalStateException("差旅费用必须关联出差申请");
        if (!tripRequestRepo.existsById(tripRequestId))
            throw new IllegalStateException("出差申请不存在: id=" + tripRequestId);
    }

    @Transactional
    public ExpenseEntity create(CreateExpenseCommand cmd) {
        if (cmd.getExpenseDate() == null) throw new IllegalStateException("expense_date is required");
        if (cmd.getAmount() == null || cmd.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException("amount is required and must be >= 0");
        validateTripLinkForTravelExpense(cmd.getExpenseType(), cmd.getTripRequestId());

        Long createdBy = userIdResolver.getCurrentUserId();
        ExpenseEntity e = new ExpenseEntity();
        e.setCreatedAt(OffsetDateTime.now());
        e.setCreatedBy(createdBy);
        e.setExpenseDate(cmd.getExpenseDate());
        e.setAmount(cmd.getAmount());
        e.setCurrency(cmd.getCurrency() != null ? cmd.getCurrency() : Currency.CNY);
        e.setExpenseType(cmd.getExpenseType());
        e.setDescription(cmd.getDescription());
        e.setStatus(ExpenseStatus.DRAFT);
        e.setQuoteId(cmd.getQuoteId());
        e.setWorkOrderId(cmd.getWorkOrderId());
        e.setInventoryOutboundId(cmd.getInventoryOutboundId());
        e.setTripRequestId(cmd.getTripRequestId());
        return expenseRepo.save(e);
    }

    @Transactional
    public ExpenseEntity update(Long id, UpdateExpenseCommand cmd) {
        ExpenseEntity e = expenseRepo.findById(id).orElseThrow(() -> new IllegalStateException("expense not found: id=" + id));
        if (e.getStatus() != ExpenseStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT expense can be updated");

        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!e.getCreatedBy().equals(currentUserId))
            throw new IllegalStateException("Only creator can update this expense");

        ExpenseType newType = cmd.getExpenseType() != null ? cmd.getExpenseType() : e.getExpenseType();
        Long newTripId = cmd.getTripRequestId() != null ? cmd.getTripRequestId() : e.getTripRequestId();
        validateTripLinkForTravelExpense(newType, newTripId);

        if (cmd.getExpenseDate() != null) e.setExpenseDate(cmd.getExpenseDate());
        if (cmd.getAmount() != null && cmd.getAmount().compareTo(BigDecimal.ZERO) >= 0) e.setAmount(cmd.getAmount());
        if (cmd.getCurrency() != null) e.setCurrency(cmd.getCurrency());
        if (cmd.getExpenseType() != null) e.setExpenseType(cmd.getExpenseType());
        if (cmd.getDescription() != null) e.setDescription(cmd.getDescription());
        if (cmd.getQuoteId() != null) e.setQuoteId(cmd.getQuoteId());
        if (cmd.getWorkOrderId() != null) e.setWorkOrderId(cmd.getWorkOrderId());
        if (cmd.getInventoryOutboundId() != null) e.setInventoryOutboundId(cmd.getInventoryOutboundId());
        if (cmd.getTripRequestId() != null) e.setTripRequestId(cmd.getTripRequestId());
        return expenseRepo.save(e);
    }

    @Transactional
    public ExpenseEntity voidExpense(Long id) {
        ExpenseEntity e = expenseRepo.findById(id).orElseThrow(() -> new IllegalStateException("expense not found: id=" + id));
        if (e.getStatus() != ExpenseStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT expense can be voided");

        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!e.getCreatedBy().equals(currentUserId))
            throw new IllegalStateException("Only creator can void this expense");

        e.setStatus(ExpenseStatus.VOID);
        return expenseRepo.save(e);
    }

    /** Void expense as part of Correction approval (no creator check). Only DRAFT. */
    @Transactional
    public ExpenseEntity voidExpenseForCorrection(Long id) {
        ExpenseEntity e = expenseRepo.findById(id).orElseThrow(() -> new IllegalStateException("expense not found: id=" + id));
        if (e.getStatus() != ExpenseStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT expense can be voided");
        e.setStatus(ExpenseStatus.VOID);
        return expenseRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseEntity> search(ExpenseSearchRequest request, Pageable pageable) {
        Long currentUserId = userIdResolver.getCurrentUserId();
        boolean seeAll = userIdResolver.isFinanceOrAdmin();

        Specification<ExpenseEntity> spec = (root, query, cb) -> cb.notEqual(root.get("status"), ExpenseStatus.VOID);
        if (!seeAll) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("createdBy"), currentUserId));
        }
        if (request != null && request.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.getStatus()));
        }
        if (request != null && request.getFromDate() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), request.getFromDate()));
        }
        if (request != null && request.getToDate() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), request.getToDate()));
        }
        return expenseRepo.findAll(spec, pageable);
    }

    public static class CreateExpenseCommand {
        private LocalDate expenseDate;
        private BigDecimal amount;
        private Currency currency;
        private ExpenseType expenseType;
        private String description;
        private Long quoteId, workOrderId, inventoryOutboundId, tripRequestId;

        public LocalDate getExpenseDate() { return expenseDate; }
        public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Currency getCurrency() { return currency; }
        public void setCurrency(Currency currency) { this.currency = currency; }
        public ExpenseType getExpenseType() { return expenseType; }
        public void setExpenseType(ExpenseType expenseType) { this.expenseType = expenseType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getQuoteId() { return quoteId; }
        public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
        public Long getWorkOrderId() { return workOrderId; }
        public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
        public Long getInventoryOutboundId() { return inventoryOutboundId; }
        public void setInventoryOutboundId(Long inventoryOutboundId) { this.inventoryOutboundId = inventoryOutboundId; }
        public Long getTripRequestId() { return tripRequestId; }
        public void setTripRequestId(Long tripRequestId) { this.tripRequestId = tripRequestId; }
    }

    public static class UpdateExpenseCommand {
        private LocalDate expenseDate;
        private BigDecimal amount;
        private Currency currency;
        private ExpenseType expenseType;
        private String description;
        private Long quoteId, workOrderId, inventoryOutboundId, tripRequestId;

        public LocalDate getExpenseDate() { return expenseDate; }
        public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Currency getCurrency() { return currency; }
        public void setCurrency(Currency currency) { this.currency = currency; }
        public ExpenseType getExpenseType() { return expenseType; }
        public void setExpenseType(ExpenseType expenseType) { this.expenseType = expenseType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getQuoteId() { return quoteId; }
        public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }
        public Long getWorkOrderId() { return workOrderId; }
        public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
        public Long getInventoryOutboundId() { return inventoryOutboundId; }
        public void setInventoryOutboundId(Long inventoryOutboundId) { this.inventoryOutboundId = inventoryOutboundId; }
        public Long getTripRequestId() { return tripRequestId; }
        public void setTripRequestId(Long tripRequestId) { this.tripRequestId = tripRequestId; }
    }

    public static class ExpenseSearchRequest {
        private ExpenseStatus status;
        private LocalDate fromDate, toDate;

        public ExpenseStatus getStatus() { return status; }
        public void setStatus(ExpenseStatus status) { this.status = status; }
        public LocalDate getFromDate() { return fromDate; }
        public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
        public LocalDate getToDate() { return toDate; }
        public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    }
}
