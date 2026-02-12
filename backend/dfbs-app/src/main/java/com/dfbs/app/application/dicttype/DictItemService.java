package com.dfbs.app.application.dicttype;

import com.dfbs.app.modules.dicttype.DictItemEntity;
import com.dfbs.app.modules.dicttype.DictItemRepo;
import com.dfbs.app.modules.dicttype.DictTypeRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DictItemService {

    private static final String DICT_ITEM_VALUE_EXISTS = "DICT_ITEM_VALUE_EXISTS";
    private static final String DICT_ITEM_PARENT_INVALID = "DICT_ITEM_PARENT_INVALID";
    private static final String DICT_TYPE_NOT_FOUND = "DICT_TYPE_NOT_FOUND";
    private static final int ITEM_VALUE_MAX = 64;
    private static final int ITEM_LABEL_MAX = 128;
    private static final int NOTE_MAX = 512;
    private static final Pattern ITEM_VALUE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final DictItemRepo itemRepo;
    private final DictTypeRepo typeRepo;

    public DictItemService(DictItemRepo itemRepo, DictTypeRepo typeRepo) {
        this.itemRepo = itemRepo;
        this.typeRepo = typeRepo;
    }

    public record ListResult(List<DictItemEntity> items, long total) {}

    /**
     * List items for a type. When parentId present: list children of that parent.
     * When parentId absent: list all items for the type.
     */
    public ListResult list(Long typeId, String q, Boolean enabled, Long parentId, int page, int pageSize) {
        if (!typeRepo.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在");
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, pageSize)),
                Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));
        Specification<DictItemEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("typeId"), typeId));
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("itemValue")), term.toLowerCase()),
                        cb.like(cb.lower(root.get("itemLabel")), term.toLowerCase())
                ));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            if (parentId != null) {
                predicates.add(cb.equal(root.get("parentId"), parentId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var springPage = itemRepo.findAll(spec, pageable);
        return new ListResult(springPage.getContent(), springPage.getTotalElements());
    }

    private void validateItemValue(String itemValue) {
        if (itemValue == null || itemValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item_value 不能为空");
        }
        if (itemValue.length() > ITEM_VALUE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item_value 长度不能超过 " + ITEM_VALUE_MAX);
        }
        if (!ITEM_VALUE_PATTERN.matcher(itemValue).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item_value 仅允许字母、数字、下划线、连字符");
        }
    }

    private void validateItemLabel(String itemLabel) {
        if (itemLabel == null || itemLabel.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item_label 不能为空");
        }
        if (itemLabel.length() > ITEM_LABEL_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "item_label 长度不能超过 " + ITEM_LABEL_MAX);
        }
    }

    private void validateNote(String note) {
        if (note != null && note.length() > NOTE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "note 长度不能超过 " + NOTE_MAX);
        }
    }

    private void validateParent(Long typeId, Long parentId) {
        if (parentId == null) return;
        DictItemEntity parent = itemRepo.findById(parentId).orElseThrow(() -> new DictItemParentInvalidException());
        if (!parent.getTypeId().equals(typeId)) {
            throw new DictItemParentInvalidException();
        }
        if (parent.getParentId() != null) {
            throw new DictItemParentInvalidException();
        }
    }

    @Transactional
    public DictItemEntity create(Long typeId, String itemValue, String itemLabel, Integer sortOrder,
                                 Boolean enabled, String note, Long parentId) {
        if (!typeRepo.existsById(typeId)) {
            throw new DictTypeNotFoundException();
        }
        validateItemValue(itemValue);
        validateItemLabel(itemLabel);
        validateNote(note);
        validateParent(typeId, parentId);
        if (itemRepo.existsByTypeIdAndItemValue(typeId, itemValue.trim())) {
            throw new DictItemValueExistsException();
        }
        DictItemEntity e = new DictItemEntity();
        e.setTypeId(typeId);
        e.setItemValue(itemValue.trim());
        e.setItemLabel(itemLabel.trim());
        e.setSortOrder(sortOrder != null ? sortOrder : 0);
        e.setEnabled(enabled != null ? enabled : true);
        e.setNote(note != null ? note.trim() : null);
        e.setParentId(parentId);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return itemRepo.save(e);
    }

    @Transactional
    public DictItemEntity update(Long id, String itemLabel, Integer sortOrder, Boolean enabled, String note, Long parentId) {
        DictItemEntity e = itemRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "字典项不存在"));
        if (itemLabel != null) {
            validateItemLabel(itemLabel);
            e.setItemLabel(itemLabel.trim());
        }
        if (sortOrder != null) {
            e.setSortOrder(sortOrder);
        }
        if (note != null) {
            validateNote(note);
            e.setNote(note.trim());
        }
        if (enabled != null) {
            e.setEnabled(enabled);
        }
        if (parentId != null) {
            validateParent(e.getTypeId(), parentId);
            e.setParentId(parentId);
        } else {
            e.setParentId(null);
        }
        e.setUpdatedAt(Instant.now());
        return itemRepo.save(e);
    }

    @Transactional
    public DictItemEntity setEnabled(Long id, boolean enabled) {
        DictItemEntity e = itemRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "字典项不存在"));
        e.setEnabled(enabled);
        e.setUpdatedAt(Instant.now());
        return itemRepo.save(e);
    }

    @SuppressWarnings("serial")
    public static class DictItemValueExistsException extends RuntimeException {}
    @SuppressWarnings("serial")
    public static class DictItemParentInvalidException extends RuntimeException {}
    @SuppressWarnings("serial")
    public static class DictTypeNotFoundException extends RuntimeException {}
}
