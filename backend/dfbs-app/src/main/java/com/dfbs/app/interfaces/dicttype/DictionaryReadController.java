package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictionaryReadService;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryItemsResponse;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryTypesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dictionaries")
public class DictionaryReadController {

    private final DictionaryReadService readService;

    public DictionaryReadController(DictionaryReadService readService) {
        this.readService = readService;
    }

    /**
     * List dictionary types (enabled-only by default). Read-only; no auth required.
     */
    @GetMapping("/types")
    public ResponseEntity<DictionaryTypesResponse> getTypes(
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(readService.getTypes(includeDisabled, q));
    }

    /**
     * List items for a type by typeCode (enabled-only by default). Read-only; no auth required.
     */
    @GetMapping("/{typeCode}/items")
    public ResponseEntity<DictionaryItemsResponse> getItems(
            @PathVariable String typeCode,
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestParam(required = false) String parentValue,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(readService.getItemsByTypeCode(typeCode, includeDisabled, parentValue, q));
    }
}
