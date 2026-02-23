package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictLabelSnapshotDemoService;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.interfaces.dicttype.dto.DictSnapshotDemoCreateRequest;
import com.dfbs.app.interfaces.dicttype.dto.DictSnapshotDemoRecordDto;
import com.dfbs.app.modules.dicttype.DictLabelSnapshotDemoEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dictionary-snapshot-demo")
public class DictionarySnapshotDemoController {

    private final SuperAdminGuard superAdminGuard;
    private final DictLabelSnapshotDemoService demoService;

    public DictionarySnapshotDemoController(SuperAdminGuard superAdminGuard,
                                            DictLabelSnapshotDemoService demoService) {
        this.superAdminGuard = superAdminGuard;
        this.demoService = demoService;
    }

    @PostMapping("/records")
    public ResponseEntity<DictSnapshotDemoRecordDto> create(@RequestBody DictSnapshotDemoCreateRequest request) {
        superAdminGuard.requireSuperAdmin();
        String typeCode = request.typeCode() != null ? request.typeCode().trim() : "";
        String itemValue = request.itemValue() != null ? request.itemValue().trim() : "";
        if (typeCode.isEmpty() || itemValue.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DictLabelSnapshotDemoEntity entity = demoService.create(typeCode, itemValue, request.note());
        return ResponseEntity.ok(DictSnapshotDemoRecordDto.from(entity));
    }

    @GetMapping("/records")
    public ResponseEntity<Page<DictSnapshotDemoRecordDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        superAdminGuard.requireSuperAdmin();
        Page<DictLabelSnapshotDemoEntity> data = demoService.list(page, size);
        return ResponseEntity.ok(data.map(DictSnapshotDemoRecordDto::from));
    }
}
