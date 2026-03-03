package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictTransitionService;
import com.dfbs.app.application.dicttype.DictionaryReadService;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryItemsResponse;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryTypesResponse;
import com.dfbs.app.interfaces.dicttype.dto.TransitionsReadResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dictionaries")
public class DictionaryReadController {

    /** Headers to prevent caching so business reads see admin changes immediately. */
    private static final HttpHeaders NO_CACHE_HEADERS = new HttpHeaders();
    static {
        NO_CACHE_HEADERS.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        NO_CACHE_HEADERS.setPragma("no-cache");
        NO_CACHE_HEADERS.setExpires(0L);
    }

    private final DictionaryReadService readService;
    private final DictTransitionService dictTransitionService;

    public DictionaryReadController(DictionaryReadService readService, DictTransitionService dictTransitionService) {
        this.readService = readService;
        this.dictTransitionService = dictTransitionService;
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
        return ResponseEntity.ok()
                .headers(NO_CACHE_HEADERS)
                .body(readService.getItemsByTypeCode(typeCode, includeDisabled, parentValue, q));
    }

    /** Type B: list allowed transitions (from→to) for a dict type. Read-only; no auth. */
    @GetMapping("/{typeCode}/transitions")
    public ResponseEntity<TransitionsReadResponse> getTransitions(
            @PathVariable String typeCode,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return ResponseEntity.ok()
                .headers(NO_CACHE_HEADERS)
                .body(dictTransitionService.listForRead(typeCode, includeDisabled));
    }
}
