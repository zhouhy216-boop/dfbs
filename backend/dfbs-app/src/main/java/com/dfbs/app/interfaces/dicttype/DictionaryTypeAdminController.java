package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictItemService;
import com.dfbs.app.application.dicttype.DictItemService.DictItemParentInvalidException;
import com.dfbs.app.application.dicttype.DictItemService.DictItemValueExistsException;
import com.dfbs.app.application.dicttype.DictItemService.DictTypeNotFoundException;
import com.dfbs.app.application.dicttype.DictTransitionService;
import com.dfbs.app.application.dicttype.DictTransitionService.DictTransitionException;
import com.dfbs.app.application.dicttype.DictTypeService;
import com.dfbs.app.application.dicttype.DictTypeService.DictTypeCodeExistsException;
import com.dfbs.app.application.dicttype.DictTypeService.DictTypeDeleteNotAllowedUsedException;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.interfaces.dicttype.dto.CreateDictItemRequest;
import com.dfbs.app.interfaces.dicttype.dto.CreateDictTypeRequest;
import com.dfbs.app.interfaces.dicttype.dto.ReorderDictItemsRequest;
import com.dfbs.app.interfaces.dicttype.dto.DictItemDto;
import com.dfbs.app.interfaces.dicttype.dto.DictItemListResponse;
import com.dfbs.app.interfaces.dicttype.dto.DictTypeItemDto;
import com.dfbs.app.interfaces.dicttype.dto.DictTypeListResponse;
import com.dfbs.app.interfaces.dicttype.dto.TransitionEdgeRequest;
import com.dfbs.app.interfaces.dicttype.dto.TransitionListResponse;
import com.dfbs.app.interfaces.dicttype.dto.UpsertTransitionsRequest;
import com.dfbs.app.interfaces.dicttype.dto.UpdateDictTypeRequest;
import com.dfbs.app.modules.dicttype.DictItemEntity;
import com.dfbs.app.modules.dicttype.DictTypeEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
@RequestMapping("/api/v1/admin/dictionary-types")
public class DictionaryTypeAdminController {

    private static final String DICT_TYPE_CODE_EXISTS = "DICT_TYPE_CODE_EXISTS";
    private static final String DICT_TYPE_DELETE_NOT_ALLOWED_USED = "DICT_TYPE_DELETE_NOT_ALLOWED_USED";
    private static final String DICT_ITEM_VALUE_EXISTS = "DICT_ITEM_VALUE_EXISTS";
    private static final String DICT_ITEM_PARENT_INVALID = "DICT_ITEM_PARENT_INVALID";
    private static final String DICT_TYPE_NOT_FOUND = "DICT_TYPE_NOT_FOUND";

    private final SuperAdminGuard superAdminGuard;
    private final DictTypeService dictTypeService;
    private final DictItemService dictItemService;
    private final DictTransitionService dictTransitionService;

    public DictionaryTypeAdminController(SuperAdminGuard superAdminGuard, DictTypeService dictTypeService, DictItemService dictItemService, DictTransitionService dictTransitionService) {
        this.superAdminGuard = superAdminGuard;
        this.dictTypeService = dictTypeService;
        this.dictItemService = dictItemService;
        this.dictTransitionService = dictTransitionService;
    }

    @GetMapping
    public DictTypeListResponse list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        superAdminGuard.requireSuperAdmin();
        var result = dictTypeService.list(q, enabled, page, pageSize);
        List<DictTypeItemDto> items = result.items().stream().map(DictTypeItemDto::from).toList();
        return new DictTypeListResponse(items, result.total());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateDictTypeRequest request) {
        superAdminGuard.requireSuperAdmin();
        try {
            DictTypeEntity e = dictTypeService.create(
                    request.typeCode(),
                    request.typeName(),
                    request.description(),
                    request.type(),
                    request.enabled()
            );
            return ResponseEntity.ok(DictTypeItemDto.from(e));
        } catch (DictTypeCodeExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("字典类型编码已存在", DICT_TYPE_CODE_EXISTS));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateDictTypeRequest request) {
        superAdminGuard.requireSuperAdmin();
        DictTypeEntity e = dictTypeService.update(id, request.typeName(), request.description(), request.type(), request.enabled());
        return ResponseEntity.ok(DictTypeItemDto.from(e));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<DictTypeItemDto> enable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        DictTypeEntity e = dictTypeService.setEnabled(id, true);
        return ResponseEntity.ok(DictTypeItemDto.from(e));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<DictTypeItemDto> disable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        DictTypeEntity e = dictTypeService.setEnabled(id, false);
        return ResponseEntity.ok(DictTypeItemDto.from(e));
    }

    @RequestMapping(value = "/{id}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        try {
            dictTypeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DictTypeDeleteNotAllowedUsedException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("该类型下存在字典项，无法删除", DICT_TYPE_DELETE_NOT_ALLOWED_USED));
        }
    }

    @GetMapping("/{typeId}/items")
    public DictItemListResponse listItems(
            @PathVariable Long typeId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean rootsOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        superAdminGuard.requireSuperAdmin();
        var result = dictItemService.list(typeId, q, enabled, parentId, rootsOnly, page, pageSize);
        List<DictItemDto> items = result.items().stream().map(DictItemDto::from).toList();
        return new DictItemListResponse(items, result.total());
    }

    @PostMapping("/{typeId}/items")
    public ResponseEntity<?> createItem(@PathVariable Long typeId, @RequestBody CreateDictItemRequest request) {
        superAdminGuard.requireSuperAdmin();
        try {
            DictItemEntity e = dictItemService.create(
                    typeId,
                    request.itemValue(),
                    request.itemLabel(),
                    request.sortOrder(),
                    request.enabled(),
                    request.note(),
                    request.parentId()
            );
            return ResponseEntity.ok(DictItemDto.from(e));
        } catch (DictTypeNotFoundException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("字典类型不存在", DICT_TYPE_NOT_FOUND));
        } catch (DictItemValueExistsException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("字典项值已存在", DICT_ITEM_VALUE_EXISTS));
        } catch (DictItemParentInvalidException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("父级无效（需为同类型根节点）", DICT_ITEM_PARENT_INVALID));
        }
    }

    @PatchMapping("/{typeId}/items/reorder")
    public ResponseEntity<DictItemListResponse> reorderItems(
            @PathVariable Long typeId,
            @RequestBody ReorderDictItemsRequest request) {
        superAdminGuard.requireSuperAdmin();
        var entities = dictItemService.reorder(typeId, request.parentId(), request.orderedItemIds() != null ? request.orderedItemIds() : List.of());
        var items = entities.stream().map(DictItemDto::from).toList();
        return ResponseEntity.ok(new DictItemListResponse(items, items.size()));
    }

    /** Type B: list allowed transitions (from -> to) for a dict type. */
    @GetMapping("/{typeId}/transitions")
    public TransitionListResponse listTransitions(@PathVariable Long typeId) {
        superAdminGuard.requireSuperAdmin();
        return dictTransitionService.list(typeId);
    }

    /** Type B: batch upsert transitions (fromValue -> toValue, enabled). Self-loop disallowed. */
    @PostMapping("/{typeId}/transitions")
    public ResponseEntity<?> upsertTransitions(@PathVariable Long typeId, @RequestBody UpsertTransitionsRequest request) {
        superAdminGuard.requireSuperAdmin();
        try {
            var list = request != null && request.transitions() != null ? request.transitions() : List.<TransitionEdgeRequest>of();
            return ResponseEntity.ok(dictTransitionService.upsert(typeId, list));
        } catch (DictTransitionException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of(ex.getMessage(), ex.getMachineCode()));
        }
    }
}
