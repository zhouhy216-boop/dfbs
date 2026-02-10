package com.dfbs.app.interfaces.testdatacleaner;

import com.dfbs.app.application.testdatacleaner.TestDataCleanerPreviewService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerPreviewRequest;
import com.dfbs.app.interfaces.testdatacleaner.dto.TestDataCleanerPreviewResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/test-data-cleaner")
public class TestDataCleanerAdminController {

    private final SuperAdminGuard superAdminGuard;
    private final TestDataCleanerPreviewService previewService;

    public TestDataCleanerAdminController(SuperAdminGuard superAdminGuard, TestDataCleanerPreviewService previewService) {
        this.superAdminGuard = superAdminGuard;
        this.previewService = previewService;
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
}
