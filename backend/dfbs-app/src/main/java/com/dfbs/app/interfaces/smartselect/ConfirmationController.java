package com.dfbs.app.interfaces.smartselect;

import com.dfbs.app.application.smartselect.ConfirmationService;
import com.dfbs.app.application.smartselect.dto.TempPoolConfirmRequest;
import com.dfbs.app.application.smartselect.dto.TempPoolItemDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Confirmation Center: list temp pool, confirm (set is_temp=false), reject (mark only; MVP: edit-to-correct).
 */
@RestController
@RequestMapping("/api/v1/temp-pool")
public class ConfirmationController {

    private final ConfirmationService confirmationService;

    public ConfirmationController(ConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @GetMapping
    public Map<String, List<TempPoolItemDto>> list() {
        return confirmationService.listAllTemp();
    }

    @PostMapping("/confirm")
    public TempPoolItemDto confirm(@RequestBody TempPoolConfirmRequest req) {
        return confirmationService.confirm(req);
    }

    /**
     * MVP: Reject = mark record so it can be hidden from pool (soft reject). Admin should "Confirm" with corrected data rather than delete.
     */
    @PostMapping("/reject")
    public void reject(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        Long id = idObj instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(idObj));
        String entityType = (String) body.get("entityType");
        confirmationService.reject(id, entityType);
    }
}
