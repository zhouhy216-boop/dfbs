package com.dfbs.app.interfaces.testdatacleaner;

import com.dfbs.app.application.testdatacleaner.TestDataCleanerExecuteService;
import com.dfbs.app.application.testdatacleaner.TestDataCleanerPreviewService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerExecuteRequest;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerExecuteResponse;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerPreviewRequest;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerPreviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/test-data-cleaner")
public class TestDataCleanerAdminController {

    private static final String RESET_CONFIRM_REQUIRED = "RESET_CONFIRM_REQUIRED";
    private static final String ATTACHMENTS_NOT_SUPPORTED_YET = "ATTACHMENTS_NOT_SUPPORTED_YET";

    private final SuperAdminGuard superAdminGuard;
    private final TestDataCleanerPreviewService previewService;
    private final TestDataCleanerExecuteService executeService;

    public TestDataCleanerAdminController(
            SuperAdminGuard superAdminGuard,
            TestDataCleanerPreviewService previewService,
            TestDataCleanerExecuteService executeService) {
        this.superAdminGuard = superAdminGuard;
        this.previewService = previewService;
        this.executeService = executeService;
    }

    /**
     * Preview estimated delete counts by module and safety flag (read-only). Super-admin only.
     */
    @PostMapping("/preview")
    public TestDataCleanerPreviewResponse preview(@RequestBody TestDataCleanerPreviewRequest request) {
        superAdminGuard.requireSuperAdmin();
        List<String> moduleIds = request.moduleIds() != null ? request.moduleIds() : List.of();
        var result = previewService.preview(moduleIds);
        List<TestDataCleanerPreviewResponse.ModuleCountItemDto> items = result.items().stream()
                .map(i -> new TestDataCleanerPreviewResponse.ModuleCountItemDto(i.moduleId(), i.count()))
                .toList();
        return new TestDataCleanerPreviewResponse(
                items,
                result.totalCount(),
                result.requiresResetConfirm(),
                result.requiresResetReasons(),
                result.invalidModuleIds()
        );
    }

    /**
     * Execute best-effort cleanup for selected modules. Super-admin only.
     * When requiresResetConfirm would be true, confirmText must be exactly "RESET". includeAttachments true returns 400.
     */
    @PostMapping("/execute")
    public ResponseEntity<?> execute(@RequestBody TestDataCleanerExecuteRequest request) {
        superAdminGuard.requireSuperAdmin();
        List<String> moduleIds = request.moduleIds() != null ? request.moduleIds() : List.of();
        var preview = previewService.preview(moduleIds);

        if (Boolean.TRUE.equals(request.includeAttachments())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResult.of("Attachments cleanup not supported yet", ATTACHMENTS_NOT_SUPPORTED_YET));
        }
        if (preview.requiresResetConfirm()) {
            if (!"RESET".equals(request.confirmText())) {
                return ResponseEntity.badRequest()
                        .body(ErrorResult.of("RESET confirmation required", RESET_CONFIRM_REQUIRED));
            }
        }

        TestDataCleanerExecuteResponse response = executeService.execute(moduleIds);
        return ResponseEntity.ok(response);
    }
}
