package com.dfbs.app.interfaces.contract;

import com.dfbs.app.application.contract.ContractMasterDataService;
import com.dfbs.app.modules.contract.ContractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ContractMasterDataController {

    private final ContractMasterDataService service;

    public ContractMasterDataController(ContractMasterDataService service) {
        this.service = service;
    }

    @PostMapping("/api/masterdata/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("contractNo"),
                body.get("customerCode")
        );
    }

    // ===== Search =====
    @GetMapping("/api/v1/contracts")
    public ResponseEntity<Page<ContractDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<ContractEntity> page = service.search(keyword, pageable);
        Page<ContractDto> dtoPage = page.map(ContractDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(IllegalStateException ex) {
        // 404 only
    }
}
