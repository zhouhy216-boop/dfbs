package com.dfbs.app.interfaces.repair;

import com.dfbs.app.application.repair.ImportResult;
import com.dfbs.app.application.repair.RepairRecordFilterRequest;
import com.dfbs.app.application.repair.RepairRecordService;
import com.dfbs.app.modules.repair.RepairRecordEntity;
import com.dfbs.app.modules.repair.WarrantyStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/repair-records")
public class RepairRecordController {

    private final RepairRecordService repairRecordService;

    public RepairRecordController(RepairRecordService repairRecordService) {
        this.repairRecordService = repairRecordService;
    }

    @PostMapping("/import")
    public ImportResult importExcel(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false) Long operatorId) {
        try (InputStream is = file.getInputStream()) {
            return repairRecordService.importFromExcel(is, operatorId);
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    @GetMapping
    public Page<RepairRecordEntity> list(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String machineNo,
            @RequestParam(required = false) LocalDateTime repairDateFrom,
            @RequestParam(required = false) LocalDateTime repairDateTo,
            @RequestParam(required = false) WarrantyStatus warrantyStatus,
            @RequestParam(required = false) String personInCharge,
            @RequestParam(required = false) String oldWorkOrderNo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        RepairRecordFilterRequest filter = new RepairRecordFilterRequest(
                customerName, machineNo, repairDateFrom, repairDateTo,
                warrantyStatus, personInCharge, oldWorkOrderNo, page, size);
        return repairRecordService.search(filter);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] bytes = repairRecordService.generateTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "repair_record_template.xlsx");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
