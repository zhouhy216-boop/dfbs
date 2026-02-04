package com.dfbs.app.interfaces.smartselect;

import com.dfbs.app.application.smartselect.SmartSelectService;
import com.dfbs.app.application.smartselect.TempDataService;
import com.dfbs.app.application.smartselect.dto.GetOrCreateTempRequest;
import com.dfbs.app.application.smartselect.dto.GetOrCreateTempResult;
import com.dfbs.app.application.smartselect.dto.SmartSelectItemDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/smart-select")
public class SmartSelectController {

    private final SmartSelectService smartSelectService;
    private final TempDataService tempDataService;

    public SmartSelectController(SmartSelectService smartSelectService, TempDataService tempDataService) {
        this.smartSelectService = smartSelectService;
        this.tempDataService = tempDataService;
    }

    @GetMapping("/search")
    public List<SmartSelectItemDto> search(@RequestParam String keyword, @RequestParam String entityType) {
        return smartSelectService.search(keyword, entityType);
    }

    @PostMapping("/get-or-create-temp")
    public GetOrCreateTempResult getOrCreateTemp(@RequestBody GetOrCreateTempRequest req) {
        return tempDataService.getOrCreateTemp(req);
    }
}
