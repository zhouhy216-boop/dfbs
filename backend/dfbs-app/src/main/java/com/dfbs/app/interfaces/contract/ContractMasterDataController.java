package com.dfbs.app.interfaces.contract;

import com.dfbs.app.application.contract.ContractMasterDataService;
import com.dfbs.app.modules.contract.ContractEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/masterdata/contracts")
public class ContractMasterDataController {

    private final ContractMasterDataService service;

    public ContractMasterDataController(ContractMasterDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("contractNo"),
                body.get("customerCode")
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(IllegalStateException ex) {
        // 404 only
    }
}
