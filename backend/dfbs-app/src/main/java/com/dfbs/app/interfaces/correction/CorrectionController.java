package com.dfbs.app.interfaces.correction;

import com.dfbs.app.application.correction.CorrectionService;
import com.dfbs.app.application.correction.CorrectionService.CreateCorrectionDto;
import com.dfbs.app.modules.correction.CorrectionEntity;
import com.dfbs.app.modules.correction.CorrectionTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Correction", description = "Data correction slip: create, submit, approve, reject")
@RestController
@RequestMapping("/api/v1/corrections")
public class CorrectionController {

    private final CorrectionService correctionService;

    public CorrectionController(CorrectionService correctionService) {
        this.correctionService = correctionService;
    }

    @Operation(summary = "Create correction draft", description = "Creates a draft correction record")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorrectionEntity create(@RequestBody CreateCorrectionRequest request) {
        LocalDate occurredDate = request.occurredDate() != null ? LocalDate.parse(request.occurredDate()) : LocalDate.now();
        return correctionService.createDraft(new CreateCorrectionDto(
                request.targetType(),
                request.targetId(),
                request.reason(),
                request.changesJson(),
                occurredDate
        ));
    }

    @PostMapping("/{id}/submit")
    public CorrectionEntity submit(@PathVariable Long id, @RequestBody SubmitCorrectionRequest request) {
        List<String> urls = request.attachmentUrls() != null ? request.attachmentUrls() : List.of();
        return correctionService.submit(id, urls);
    }

    @Operation(summary = "Approve and execute correction", description = "Requires APPROVE_EXECUTE_CORRECTION permission")
    @PostMapping("/{id}/approve")
    public CorrectionEntity approve(@PathVariable Long id) {
        return correctionService.approveAndExecute(id);
    }

    @PostMapping("/{id}/reject")
    public CorrectionEntity reject(@PathVariable Long id) {
        return correctionService.reject(id);
    }

    @GetMapping("/{id}")
    public CorrectionEntity get(@PathVariable Long id) {
        return correctionService.get(id);
    }

    public record CreateCorrectionRequest(CorrectionTargetType targetType, Long targetId, String reason, String changesJson, String occurredDate) {}
    public record SubmitCorrectionRequest(List<String> attachmentUrls) {}
}
