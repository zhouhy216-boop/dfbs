package com.dfbs.app.interfaces.dicttype;

import com.dfbs.app.application.dicttype.DictItemService;
import com.dfbs.app.application.dicttype.DictItemService.DictItemDeleteNotAllowedHasChildrenException;
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
    private static final String DICT_ITEM_DELETE_NOT_ALLOWED_HAS_CHILDREN = "DICT_ITEM_DELETE_NOT_ALLOWED_HAS_CHILDREN";

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

    @RequestMapping(value = "/{id}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        superAdminGuard.requireSuperAdmin();
        try {
            dictItemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DictItemDeleteNotAllowedHasChildrenException ex) {
            return ResponseEntity.badRequest().body(ErrorResult.of("该字典项存在子项，无法删除", DICT_ITEM_DELETE_NOT_ALLOWED_HAS_CHILDREN));
        }
    }
}
