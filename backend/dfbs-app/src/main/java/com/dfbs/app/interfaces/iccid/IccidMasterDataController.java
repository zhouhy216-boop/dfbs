package com.dfbs.app.interfaces.iccid;

import com.dfbs.app.application.iccid.IccidMasterDataService;
import com.dfbs.app.application.iccid.dto.IccidListDto;
import com.dfbs.app.modules.iccid.IccidEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/masterdata/iccid")
public class IccidMasterDataController {

    private final IccidMasterDataService service;

    public IccidMasterDataController(IccidMasterDataService service) {
        this.service = service;
    }

    @GetMapping
    public Page<IccidListDto> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isBound,
            Pageable pageable) {
        return service.searchIccids(keyword, isBound, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IccidEntity create(@RequestBody Map<String, String> body) {
        return service.create(
                body.get("iccidNo"),
                body.get("machineSn")
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(IllegalStateException ex) {
        // 404 only
    }
}
