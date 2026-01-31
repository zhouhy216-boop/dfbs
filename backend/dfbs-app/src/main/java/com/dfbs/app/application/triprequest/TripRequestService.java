package com.dfbs.app.application.triprequest;

import com.dfbs.app.config.CurrentUserIdResolver;
import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestRepo;
import com.dfbs.app.modules.triprequest.TripRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class TripRequestService {

    private final TripRequestRepo tripRequestRepo;
    private final CurrentUserIdResolver userIdResolver;

    public TripRequestService(TripRequestRepo tripRequestRepo, CurrentUserIdResolver userIdResolver) {
        this.tripRequestRepo = tripRequestRepo;
        this.userIdResolver = userIdResolver;
    }

    @Transactional
    public TripRequestEntity create(CreateTripRequestCommand cmd) {
        validateCreateUpdate(cmd);
        Long createdBy = userIdResolver.getCurrentUserId();
        TripRequestEntity e = new TripRequestEntity();
        e.setCreatedAt(OffsetDateTime.now());
        e.setCreatedBy(createdBy);
        e.setStatus(TripRequestStatus.DRAFT);
        mapCommandToEntity(cmd, e);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity update(Long id, UpdateTripRequestCommand cmd) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.DRAFT && e.getStatus() != TripRequestStatus.RETURNED)
            throw new IllegalStateException("Only DRAFT or RETURNED trip request can be updated");
        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!e.getCreatedBy().equals(currentUserId))
            throw new IllegalStateException("Only creator can update this trip request");
        validateCreateUpdate(cmd);
        mapCommandToEntity(cmd, e);
        return tripRequestRepo.save(e);
    }

    private void validateCreateUpdate(CreateTripRequestCommand cmd) {
        if (!StringUtils.hasText(cmd.getCity())) throw new IllegalStateException("city is required");
        if (cmd.getStartDate() == null) throw new IllegalStateException("start_date is required");
        if (cmd.getEndDate() == null) throw new IllegalStateException("end_date is required");
        if (!StringUtils.hasText(cmd.getPurpose())) throw new IllegalStateException("purpose is required");
        if (cmd.getWorkOrderId() == null && !StringUtils.hasText(cmd.getIndependentReason()))
            throw new IllegalStateException("independent_reason is required when work_order_id is not set");
    }

    private void mapCommandToEntity(CreateTripRequestCommand cmd, TripRequestEntity e) {
        e.setCity(cmd.getCity());
        e.setStartDate(cmd.getStartDate());
        e.setEndDate(cmd.getEndDate());
        e.setPurpose(cmd.getPurpose());
        e.setEstTransportCost(cmd.getEstTransportCost() != null ? cmd.getEstTransportCost() : BigDecimal.ZERO);
        e.setEstAccommodationCost(cmd.getEstAccommodationCost() != null ? cmd.getEstAccommodationCost() : BigDecimal.ZERO);
        e.setCurrency(cmd.getCurrency() != null ? cmd.getCurrency() : "CNY");
        e.setWorkOrderId(cmd.getWorkOrderId());
        e.setIndependentReason(cmd.getIndependentReason());
    }

    @Transactional
    public TripRequestEntity submit(Long id) {
        TripRequestEntity e = getAndCheckCreator(id);
        if (e.getStatus() != TripRequestStatus.DRAFT && e.getStatus() != TripRequestStatus.RETURNED)
            throw new IllegalStateException("Only DRAFT or RETURNED can be submitted");
        e.setStatus(TripRequestStatus.SUBMITTED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity withdraw(Long id) {
        TripRequestEntity e = getAndCheckCreator(id);
        if (e.getStatus() != TripRequestStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED can be withdrawn");
        e.setStatus(TripRequestStatus.DRAFT);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity leaderApprove(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.SUBMITTED)
            throw new IllegalStateException("Only SUBMITTED can be leader-approved");
        Long approverId = userIdResolver.getCurrentUserId();
        e.setApproverLeaderId(approverId);
        e.setApproveLeaderTime(OffsetDateTime.now());
        e.setStatus(TripRequestStatus.LEADER_APPROVED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity financeApprove(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.LEADER_APPROVED)
            throw new IllegalStateException("Only LEADER_APPROVED can be finance-approved");
        Long approverId = userIdResolver.getCurrentUserId();
        e.setApproverFinanceId(approverId);
        e.setApproveFinanceTime(OffsetDateTime.now());
        e.setStatus(TripRequestStatus.FINANCE_APPROVED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity returnRequest(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.SUBMITTED && e.getStatus() != TripRequestStatus.LEADER_APPROVED)
            throw new IllegalStateException("Only SUBMITTED or LEADER_APPROVED can be returned");
        e.setStatus(TripRequestStatus.RETURNED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity reject(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.SUBMITTED && e.getStatus() != TripRequestStatus.LEADER_APPROVED)
            throw new IllegalStateException("Only SUBMITTED or LEADER_APPROVED can be rejected");
        e.setStatus(TripRequestStatus.REJECTED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity requestCancel(Long id, String reason) {
        TripRequestEntity e = getAndCheckCreator(id);
        if (e.getStatus() != TripRequestStatus.FINANCE_APPROVED)
            throw new IllegalStateException("Only FINANCE_APPROVED can request cancellation");
        e.setStatus(TripRequestStatus.CANCEL_REQUESTED);
        e.setCancellationReason(reason);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity leaderCancelApprove(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.CANCEL_REQUESTED)
            throw new IllegalStateException("Only CANCEL_REQUESTED can be cancel-approved");
        e.setStatus(TripRequestStatus.CANCELLED);
        return tripRequestRepo.save(e);
    }

    @Transactional
    public TripRequestEntity leaderCancelReject(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        if (e.getStatus() != TripRequestStatus.CANCEL_REQUESTED)
            throw new IllegalStateException("Only CANCEL_REQUESTED can be cancel-rejected");
        e.setStatus(TripRequestStatus.FINANCE_APPROVED);
        e.setCancellationReason(null);
        return tripRequestRepo.save(e);
    }

    private TripRequestEntity getAndCheckCreator(Long id) {
        TripRequestEntity e = tripRequestRepo.findById(id).orElseThrow(() -> new IllegalStateException("trip request not found: id=" + id));
        Long currentUserId = userIdResolver.getCurrentUserId();
        if (!e.getCreatedBy().equals(currentUserId))
            throw new IllegalStateException("Only creator can perform this action");
        return e;
    }

    @Transactional(readOnly = true)
    public Page<TripRequestEntity> search(TripRequestSearchRequest request, Pageable pageable) {
        Long currentUserId = userIdResolver.getCurrentUserId();
        Specification<TripRequestEntity> spec = (root, query, cb) -> cb.equal(root.get("createdBy"), currentUserId);
        if (request != null && request.getStatus() != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.getStatus()));
        if (request != null && request.getFromDate() != null)
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), request.getFromDate()));
        if (request != null && request.getToDate() != null)
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), request.getToDate()));
        return tripRequestRepo.findAll(spec, pageable);
    }

    public static class CreateTripRequestCommand {
        private String city;
        private LocalDate startDate, endDate;
        private String purpose;
        private BigDecimal estTransportCost, estAccommodationCost;
        private String currency;
        private Long workOrderId;
        private String independentReason;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public BigDecimal getEstTransportCost() { return estTransportCost; }
        public void setEstTransportCost(BigDecimal estTransportCost) { this.estTransportCost = estTransportCost; }
        public BigDecimal getEstAccommodationCost() { return estAccommodationCost; }
        public void setEstAccommodationCost(BigDecimal estAccommodationCost) { this.estAccommodationCost = estAccommodationCost; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Long getWorkOrderId() { return workOrderId; }
        public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
        public String getIndependentReason() { return independentReason; }
        public void setIndependentReason(String independentReason) { this.independentReason = independentReason; }
    }

    public static class UpdateTripRequestCommand extends CreateTripRequestCommand {}

    public static class TripRequestSearchRequest {
        private TripRequestStatus status;
        private LocalDate fromDate, toDate;

        public TripRequestStatus getStatus() { return status; }
        public void setStatus(TripRequestStatus status) { this.status = status; }
        public LocalDate getFromDate() { return fromDate; }
        public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
        public LocalDate getToDate() { return toDate; }
        public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    }
}
