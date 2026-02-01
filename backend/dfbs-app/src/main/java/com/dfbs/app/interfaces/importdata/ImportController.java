package com.dfbs.app.interfaces.importdata;

import com.dfbs.app.application.importdata.ImportServiceDelegate;
import com.dfbs.app.application.importdata.*;
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
@Tag(name = "History Data Import", description = "Excel import for all master data types")
public class ImportController {

    private final CustomerImportService customerImportService;
    private final ContractImportService contractImportService;
    private final MachineModelImportService machineModelImportService;
    private final SparePartImportService sparePartImportService;
    private final MachineImportService machineImportService;
    private final SimCardImportService simCardImportService;
    private final ModelPartListImportService modelPartListImportService;

    public ImportController(CustomerImportService customerImportService,
                            ContractImportService contractImportService,
                            MachineModelImportService machineModelImportService,
                            SparePartImportService sparePartImportService,
                            MachineImportService machineImportService,
                            SimCardImportService simCardImportService,
                            ModelPartListImportService modelPartListImportService) {
        this.customerImportService = customerImportService;
        this.contractImportService = contractImportService;
        this.machineModelImportService = machineModelImportService;
        this.sparePartImportService = sparePartImportService;
        this.machineImportService = machineImportService;
        this.simCardImportService = simCardImportService;
        this.modelPartListImportService = modelPartListImportService;
    }

    @Operation(summary = "Import customers from Excel")
    @PostMapping(value = "/customers", consumes = "multipart/form-data")
    public ImportResultDto importCustomers(@RequestParam("file") MultipartFile file) {
        return runImport(file, customerImportService);
    }

    @PostMapping("/customers/resolve")
    public ImportResultDto resolveCustomers(@RequestBody List<ImportActionReq> actions) {
        return customerImportService.resolve(actions);
    }

    @Operation(summary = "Import contracts from Excel")
    @PostMapping(value = "/contracts", consumes = "multipart/form-data")
    public ImportResultDto importContracts(@RequestParam("file") MultipartFile file) {
        return runImport(file, contractImportService);
    }

    @PostMapping("/contracts/resolve")
    public ImportResultDto resolveContracts(@RequestBody List<ImportActionReq> actions) {
        return contractImportService.resolve(actions);
    }

    @Operation(summary = "Import machine models from Excel")
    @PostMapping(value = "/models", consumes = "multipart/form-data")
    public ImportResultDto importModels(@RequestParam("file") MultipartFile file) {
        return runImport(file, machineModelImportService);
    }

    @PostMapping("/models/resolve")
    public ImportResultDto resolveModels(@RequestBody List<ImportActionReq> actions) {
        return machineModelImportService.resolve(actions);
    }

    @Operation(summary = "Import spare parts from Excel")
    @PostMapping(value = "/spare-parts", consumes = "multipart/form-data")
    public ImportResultDto importSpareParts(@RequestParam("file") MultipartFile file) {
        return runImport(file, sparePartImportService);
    }

    @PostMapping("/spare-parts/resolve")
    public ImportResultDto resolveSpareParts(@RequestBody List<ImportActionReq> actions) {
        return sparePartImportService.resolve(actions);
    }

    @Operation(summary = "Import machines from Excel")
    @PostMapping(value = "/machines", consumes = "multipart/form-data")
    public ImportResultDto importMachines(@RequestParam("file") MultipartFile file) {
        return runImport(file, machineImportService);
    }

    @PostMapping("/machines/resolve")
    public ImportResultDto resolveMachines(@RequestBody List<ImportActionReq> actions) {
        return machineImportService.resolve(actions);
    }

    @Operation(summary = "Import SIM cards from Excel")
    @PostMapping(value = "/sim-cards", consumes = "multipart/form-data")
    public ImportResultDto importSimCards(@RequestParam("file") MultipartFile file) {
        return runImport(file, simCardImportService);
    }

    @PostMapping("/sim-cards/resolve")
    public ImportResultDto resolveSimCards(@RequestBody List<ImportActionReq> actions) {
        return simCardImportService.resolve(actions);
    }

    @Operation(summary = "Import BOM / model part lists from Excel (flat: Model Name, Version, Part No, Qty)")
    @PostMapping(value = "/model-part-lists", consumes = "multipart/form-data")
    public ImportResultDto importModelPartLists(@RequestParam("file") MultipartFile file) {
        return runImport(file, modelPartListImportService);
    }

    @PostMapping("/model-part-lists/resolve")
    public ImportResultDto resolveModelPartLists(@RequestBody List<ImportActionReq> actions) {
        return modelPartListImportService.resolve(actions);
    }

    private static ImportResultDto runImport(MultipartFile file, ImportServiceDelegate service) {
        try (InputStream is = file.getInputStream()) {
            return service.importFromExcel(is);
        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage());
        }
    }
}
