package com.dfbs.app.interfaces.importdata;

import com.dfbs.app.application.importdata.CustomerImportService;
import com.dfbs.app.application.importdata.dto.ImportActionReq;
import com.dfbs.app.application.importdata.dto.ImportResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/imports")
@Tag(name = "History Data Import", description = "Excel import for master data (customers, etc.)")
public class CustomerImportController {

    private final CustomerImportService customerImportService;

    public CustomerImportController(CustomerImportService customerImportService) {
        this.customerImportService = customerImportService;
    }

    @Operation(summary = "Import customers from Excel", description = "Parse Excel -> Validate -> Check duplicates -> Insert or report. Consumes multipart/form-data.")
    @PostMapping(value = "/customers", consumes = "multipart/form-data")
    public ImportResultDto importCustomers(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return customerImportService.importFromExcel(is);
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Resolve customer import conflicts", description = "Accepts list of actions (SKIP / UPDATE / REUSE). Stub: applies updates/skips.")
    @PostMapping("/customers/resolve")
    public ImportResultDto resolveCustomerConflicts(@RequestBody List<ImportActionReq> actions) {
        return customerImportService.resolve(actions);
    }
}
