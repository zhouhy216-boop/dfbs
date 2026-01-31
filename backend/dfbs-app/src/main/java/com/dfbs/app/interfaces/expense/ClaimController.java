package com.dfbs.app.interfaces.expense;

import com.dfbs.app.application.expense.ClaimService;
import com.dfbs.app.modules.expense.ClaimEntity;
import com.dfbs.app.modules.expense.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimEntity create(@RequestBody CreateClaimRequest req) {
        return claimService.create(req.expenseIds() != null ? req.expenseIds() : java.util.List.of());
    }

    @PostMapping("/{id}/submit")
    public ClaimEntity submit(@PathVariable Long id) {
        return claimService.submit(id);
    }

    @PostMapping("/{id}/return")
    public ClaimEntity returnClaim(@PathVariable Long id) {
        return claimService.returnClaim(id);
    }

    @PostMapping("/{id}/reject")
    public ClaimEntity reject(@PathVariable Long id) {
        return claimService.reject(id);
    }

    @PostMapping("/{id}/approve")
    public ClaimEntity approve(@PathVariable Long id) {
        return claimService.approve(id);
    }

    @PostMapping("/{id}/pay")
    public ClaimEntity pay(@PathVariable Long id) {
        return claimService.pay(id);
    }

    @GetMapping
    public ResponseEntity<Page<ClaimDto>> search(
            @RequestParam(required = false) ClaimStatus status,
            Pageable pageable) {
        var request = new ClaimService.ClaimSearchRequest();
        request.setStatus(status);
        Page<ClaimEntity> page = claimService.search(request, pageable);
        return ResponseEntity.ok(page.map(ClaimDto::from));
    }
}
