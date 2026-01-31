package com.dfbs.app.interfaces.triprequest;

import com.dfbs.app.application.triprequest.TripRequestService;
import com.dfbs.app.modules.triprequest.TripRequestEntity;
import com.dfbs.app.modules.triprequest.TripRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trip-requests")
public class TripRequestController {

    private final TripRequestService tripRequestService;

    public TripRequestController(TripRequestService tripRequestService) {
        this.tripRequestService = tripRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripRequestEntity create(@RequestBody CreateTripRequestRequest req) {
        var cmd = new TripRequestService.CreateTripRequestCommand();
        cmd.setCity(req.city());
        cmd.setStartDate(req.startDate());
        cmd.setEndDate(req.endDate());
        cmd.setPurpose(req.purpose());
        cmd.setEstTransportCost(req.estTransportCost());
        cmd.setEstAccommodationCost(req.estAccommodationCost());
        cmd.setCurrency(req.currency());
        cmd.setWorkOrderId(req.workOrderId());
        cmd.setIndependentReason(req.independentReason());
        return tripRequestService.create(cmd);
    }

    @PutMapping("/{id}")
    public TripRequestEntity update(@PathVariable Long id, @RequestBody UpdateTripRequestRequest req) {
        var cmd = new TripRequestService.UpdateTripRequestCommand();
        cmd.setCity(req.city());
        cmd.setStartDate(req.startDate());
        cmd.setEndDate(req.endDate());
        cmd.setPurpose(req.purpose());
        cmd.setEstTransportCost(req.estTransportCost());
        cmd.setEstAccommodationCost(req.estAccommodationCost());
        cmd.setCurrency(req.currency());
        cmd.setWorkOrderId(req.workOrderId());
        cmd.setIndependentReason(req.independentReason());
        return tripRequestService.update(id, cmd);
    }

    @PostMapping("/{id}/submit")
    public TripRequestEntity submit(@PathVariable Long id) {
        return tripRequestService.submit(id);
    }

    @PostMapping("/{id}/withdraw")
    public TripRequestEntity withdraw(@PathVariable Long id) {
        return tripRequestService.withdraw(id);
    }

    @PostMapping("/{id}/leader-approve")
    public TripRequestEntity leaderApprove(@PathVariable Long id) {
        return tripRequestService.leaderApprove(id);
    }

    @PostMapping("/{id}/finance-approve")
    public TripRequestEntity financeApprove(@PathVariable Long id) {
        return tripRequestService.financeApprove(id);
    }

    @PostMapping("/{id}/return")
    public TripRequestEntity returnRequest(@PathVariable Long id) {
        return tripRequestService.returnRequest(id);
    }

    @PostMapping("/{id}/reject")
    public TripRequestEntity reject(@PathVariable Long id) {
        return tripRequestService.reject(id);
    }

    @PostMapping("/{id}/cancel-request")
    public TripRequestEntity cancelRequest(@PathVariable Long id, @RequestBody(required = false) CancelRequestRequest req) {
        return tripRequestService.requestCancel(id, req != null ? req.reason() : null);
    }

    @PostMapping("/{id}/cancel-approve")
    public TripRequestEntity cancelApprove(@PathVariable Long id) {
        return tripRequestService.leaderCancelApprove(id);
    }

    @PostMapping("/{id}/cancel-reject")
    public TripRequestEntity cancelReject(@PathVariable Long id) {
        return tripRequestService.leaderCancelReject(id);
    }

    @GetMapping
    public ResponseEntity<Page<TripRequestDto>> search(
            @RequestParam(required = false) TripRequestStatus status,
            @RequestParam(required = false) java.time.LocalDate fromDate,
            @RequestParam(required = false) java.time.LocalDate toDate,
            Pageable pageable) {
        var request = new TripRequestService.TripRequestSearchRequest();
        request.setStatus(status);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        Page<TripRequestEntity> page = tripRequestService.search(request, pageable);
        return ResponseEntity.ok(page.map(TripRequestDto::from));
    }
}
