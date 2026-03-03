package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictItemService;
import com.dfbs.app.application.dicttype.DictItemService.DictItemHasChildrenCannotBecomeChildException;
import com.dfbs.app.application.dicttype.DictItemService.DictItemParentInvalidException;
import com.dfbs.app.config.SuperAdminGuard;
import com.dfbs.app.infra.dto.ErrorResult;
import com.dfbs.app.interfaces.dicttype.dto.DictItemDto;
import com.dfbs.app.interfaces.dicttype.dto.UpdateDictItemRequest;
import com.dfbs.app.modules.dicttype.DictItemEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
@RequestMapping("/api/v1/admin/dictionary-items")
public class DictionaryItemAdminController {

    private static final String DICT_ITEM_PARENT_INVALID = "DICT_ITEM_PARENT_INVALID";
    private static final String DICT_ITEM_HAS_CHILDREN_CANNOT_BECOME_CHILD = "DICT_ITEM_HAS_CHILDREN_CANNOT_BECOME_CHILD";

    private final SuperAdminGuard superAdminGuard;
    private final DictItemService dictItemService;

    public DictionaryItemAdminController(SuperAdminGuard superAdminGuard, DictItemService dictItemService) {
        this.superAdminGuard = superAdminGuard;
        this.dictItemService = dictItemService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateDictItemRequest request) {
        superAdminGuard.requireSuperAdmin();
        try {
            DictItemEntity e = dictItemService.update(
                    id,
                    request.itemLabel(),
                    request.sortOrder(),
                    request.enabled(),
                    request.note(),
                    request.parentId()
            );
            return ResponseEntity.ok(DictItemDto.from(e));
        } catch (DictItemParentInvalidException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("父级无效（需为同类型根节点）", DICT_ITEM_PARENT_INVALID));
        } catch (DictItemHasChildrenCannotBecomeChildException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("该字典项已有子项，不能设为子项（仅支持一层级）", DICT_ITEM_HAS_CHILDREN_CANNOT_BECOME_CHILD));
        }
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<DictItemDto> enable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        DictItemEntity e = dictItemService.setEnabled(id, true);
        return ResponseEntity.ok(DictItemDto.from(e));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<DictItemDto> disable(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        DictItemEntity e = dictItemService.setEnabled(id, false);
        return ResponseEntity.ok(DictItemDto.from(e));
    }

    /** Soft-disable (enabled=false) so history/snapshot can still resolve via includeDisabled=true. No physical delete. */
    @RequestMapping(value = "/{id}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        dictItemService.setEnabled(id, false);
        return ResponseEntity.noContent().build();
    }
}
