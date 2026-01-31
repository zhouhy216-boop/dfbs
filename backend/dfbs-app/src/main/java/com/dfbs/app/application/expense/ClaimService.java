package com.dfbs.app.application.expense;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.expense.ClaimEntity;
import com.dfbs.app.modules.expense.ClaimRepo;
import com.dfbs.app.modules.expense.ClaimStatus;
import com.dfbs.app.modules.expense.ExpenseEntity;
import com.dfbs.app.modules.expense.ExpenseRepo;
import com.dfbs.app.modules.expense.ExpenseStatus;
import com.dfbs.app.modules.quote.enums.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepo claimRepo;
    private final ExpenseRepo expenseRepo;
    private final CurrentUserIdResolver userIdResolver;

    public ClaimService(ClaimRepo claimRepo, ExpenseRepo expenseRepo, CurrentUserIdResolver userIdResolver) {
        this.claimRepo = claimRepo;
        this.expenseRepo = expenseRepo;
        this.userIdResolver = userIdResolver;
    }

    @Transactional
    public ClaimEntity create(List<Long> expenseIds) {
        Long currentUserId = userIdResolver.getCurrentUserId();
        if (expenseIds == null || expenseIds.isEmpty())
            throw new IllegalStateException("At least one expense is required");

        List<ExpenseEntity> expenses = new ArrayList<>();
        Currency firstCurrency = null;
        BigDecimal total = BigDecimal.ZERO;

        for (Long eid : expenseIds) {
            ExpenseEntity e = expenseRepo.findById(eid).orElseThrow(() -> new IllegalStateException("expense not found: id=" + eid));
            if (!e.getCreatedBy().equals(currentUserId))
                throw new IllegalStateException("Expense " + eid + " does not belong to current user");
            if (e.getStatus() != ExpenseStatus.DRAFT)
                throw new IllegalStateException("Expense " + eid + " must be DRAFT");
            if (firstCurrency == null) firstCurrency = e.getCurrency();
            else if (e.getCurrency() != firstCurrency)
                throw new IllegalStateException("All expenses must have the same currency");
            expenses.add(e);
            total = total.add(e.getAmount());
        }

        String claimNo = "CLM-" + System.currentTimeMillis();
        ClaimEntity claim = new ClaimEntity();
        claim.setClaimNo(claimNo);
        claim.setTitle("Claim " + claimNo);
        claim.setTotalAmount(total);
        claim.setCurrency(firstCurrency != null ? firstCurrency.name() : Currency.CNY.name());
        claim.setStatus(ClaimStatus.DRAFT);
        claim.setCreatedAt(OffsetDateTime.now());
        claim.setCreatedBy(currentUserId);
        claim = claimRepo.save(claim);

        for (ExpenseEntity e : expenses) {
            e.setClaim(claim);
            e.setStatus(ExpenseStatus.CLAIMED);
            expenseRepo.save(e);
        }

        return claimRepo.findById(claim.getId()).orElseThrow();
    }

    @Transactional
    public ClaimEntity submit(Long claimId) {
        ClaimEntity claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalStateException("claim not found: id=" + claimId));
        if (claim.getStatus() != ClaimStatus.DRAFT && claim.getStatus() != ClaimStatus.RETURNED)
            throw new IllegalStateException("Only DRAFT or RETURNED claim can be submitted");

        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!claim.getCreatedBy().equals(currentUserId))
            throw new IllegalStateException("Only creator can submit this claim");

        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmitterId(currentUserId);
        claim.setSubmitTime(OffsetDateTime.now());
        for (ExpenseEntity e : expenseRepo.findByClaimIdOrderByIdAsc(claimId)) {
            e.setStatus(ExpenseStatus.CLAIMED);
            expenseRepo.save(e);
        }
        return claimRepo.save(claim);
    }

    @Transactional
    public ClaimEntity returnClaim(Long claimId) {
        requireFinanceOrAdmin();
        ClaimEntity claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalStateException("claim not found: id=" + claimId));
        if (claim.getStatus() != ClaimStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED claim can be returned");

        claim.setStatus(ClaimStatus.RETURNED);
        for (ExpenseEntity e : expenseRepo.findByClaimIdOrderByIdAsc(claimId)) {
            e.setStatus(ExpenseStatus.DRAFT);
            expenseRepo.save(e);
        }
        return claimRepo.save(claim);
    }

    @Transactional
    public ClaimEntity reject(Long claimId) {
        requireFinanceOrAdmin();
        ClaimEntity claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalStateException("claim not found: id=" + claimId));
        if (claim.getStatus() != ClaimStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED claim can be rejected");

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setApproverId(userIdResolver.getCurrentUserId());
        claim.setApproveTime(OffsetDateTime.now());
        return claimRepo.save(claim);
    }

    @Transactional
    public ClaimEntity approve(Long claimId) {
        requireFinanceOrAdmin();
        ClaimEntity claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalStateException("claim not found: id=" + claimId));
        if (claim.getStatus() != ClaimStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED claim can be approved");

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApproverId(userIdResolver.getCurrentUserId());
        claim.setApproveTime(OffsetDateTime.now());
        return claimRepo.save(claim);
    }

    @Transactional
    public ClaimEntity pay(Long claimId) {
        requireFinanceOrAdmin();
        ClaimEntity claim = claimRepo.findById(claimId).orElseThrow(() -> new IllegalStateException("claim not found: id=" + claimId));
        if (claim.getStatus() != ClaimStatus.APPROVED)
            throw new IllegalStateException("Only APPROVED claim can be paid");

        claim.setStatus(ClaimStatus.PAID);
        return claimRepo.save(claim);
    }

    @Transactional(readOnly = true)
    public Page<ClaimEntity> search(ClaimSearchRequest request, Pageable pageable) {
        Long currentUserId = userIdResolver.getCurrentUserId();
        boolean seeAll = userIdResolver.isFinanceOrAdmin();

        Specification<ClaimEntity> spec = (root, query, cb) -> cb.conjunction();
        if (!seeAll) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("createdBy"), currentUserId));
        }
        if (request != null && request.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.getStatus()));
        }
        return claimRepo.findAll(spec, pageable);
    }

    private void requireFinanceOrAdmin() {
        if (!userIdResolver.isFinanceOrAdmin())
            throw new IllegalStateException("Finance or Admin permission required");
    }

    public static class ClaimSearchRequest {
        private ClaimStatus status;

        public ClaimStatus getStatus() { return status; }
        public void setStatus(ClaimStatus status) { this.status = status; }
    }
}
