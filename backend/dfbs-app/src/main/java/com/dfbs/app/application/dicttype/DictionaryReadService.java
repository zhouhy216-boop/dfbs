package com.dfbs.app.application.dicttype;

import com.dfbs.app.interfaces.dicttype.dto.DictionaryItemOptionDto;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryItemsResponse;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryTypeOptionDto;
import com.dfbs.app.interfaces.dicttype.dto.DictionaryTypesResponse;
import com.dfbs.app.modules.dicttype.DictItemEntity;
import com.dfbs.app.modules.dicttype.DictItemRepo;
import com.dfbs.app.modules.dicttype.DictTypeEntity;
import com.dfbs.app.modules.dicttype.DictTypeRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DictionaryReadService {

    private final DictTypeRepo typeRepo;
    private final DictItemRepo itemRepo;

    public DictionaryReadService(DictTypeRepo typeRepo, DictItemRepo itemRepo) {
        this.typeRepo = typeRepo;
        this.itemRepo = itemRepo;
    }

    /**
     * List items for a type by typeCode. No internal ids in response.
     * If parentValue is set, returns only children of the item with that item_value; otherwise all items for the type.
     */
    @Transactional(readOnly = true)
    public DictionaryItemsResponse getItemsByTypeCode(String typeCode, boolean includeDisabled,
                                                      String parentValue, String q) {
        DictTypeEntity type = typeRepo.findByTypeCode(typeCode)
                .orElseThrow(() -> new DictTypeNotFoundException("字典类型不存在"));
        Long typeId = type.getId();

        Long filterByParentId = null;
        if (parentValue != null && !parentValue.isBlank()) {
            var parentOpt = itemRepo.findByTypeIdAndItemValue(typeId, parentValue.trim());
            if (parentOpt.isEmpty()) {
                return new DictionaryItemsResponse(typeCode, List.of());
            }
            filterByParentId = parentOpt.get().getId();
        }
        final Long parentIdFilter = filterByParentId;
        final String searchQ = q;

        Specification<DictItemEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("typeId"), typeId));
            if (!includeDisabled) {
                predicates.add(cb.equal(root.get("enabled"), true));
            }
            if (searchQ != null && !searchQ.isBlank()) {
                String term = "%" + searchQ.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("itemValue")), term),
                        cb.like(cb.lower(root.get("itemLabel")), term)
                ));
            }
            if (parentIdFilter != null) {
                predicates.add(cb.equal(root.get("parentId"), parentIdFilter));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DictItemEntity> items = itemRepo.findAll(spec, Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));

        Set<Long> parentIds = items.stream().map(DictItemEntity::getParentId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> parentIdToValue = Map.of();
        if (!parentIds.isEmpty()) {
            parentIdToValue = itemRepo.findAllById(parentIds).stream()
                    .collect(Collectors.toMap(DictItemEntity::getId, DictItemEntity::getItemValue, (a, b) -> a));
        }

        Map<Long, String> finalParentIdToValue = parentIdToValue;
        List<DictionaryItemOptionDto> options = items.stream()
                .map(e -> {
                    String pv = e.getParentId() == null ? null : finalParentIdToValue.get(e.getParentId());
                    return new DictionaryItemOptionDto(
                            e.getItemValue(),
                            e.getItemLabel(),
                            e.getSortOrder() != null ? e.getSortOrder() : 0,
                            Boolean.TRUE.equals(e.getEnabled()),
                            pv,
                            e.getNote()
                    );
                })
                .toList();
        return new DictionaryItemsResponse(typeCode, options);
    }

    @Transactional(readOnly = true)
    public DictionaryTypesResponse getTypes(boolean includeDisabled, String q) {
        Specification<DictTypeEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!includeDisabled) {
                predicates.add(cb.equal(root.get("enabled"), true));
            }
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("typeCode")), term),
                        cb.like(cb.lower(root.get("typeName")), term)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<DictTypeEntity> types = typeRepo.findAll(spec, Sort.by("id").ascending());
        List<DictionaryTypeOptionDto> options = types.stream()
                .map(t -> new DictionaryTypeOptionDto(t.getTypeCode(), t.getTypeName(), Boolean.TRUE.equals(t.getEnabled())))
                .toList();
        return new DictionaryTypesResponse(options);
    }

    /**
     * Resolve label for a single value (includeDisabled=true). Does not throw; returns itemValue if type or item not found.
     */
    @Transactional(readOnly = true)
    public String resolveLabel(String typeCode, String itemValue) {
        if (typeCode == null || typeCode.isBlank() || itemValue == null || itemValue.isBlank()) {
            return itemValue != null ? itemValue : "";
        }
        try {
            DictionaryItemsResponse response = getItemsByTypeCode(typeCode, true, null, null);
            return response.items().stream()
                    .filter(o -> itemValue.equals(o.value()))
                    .findFirst()
                    .map(DictionaryItemOptionDto::label)
                    .orElse(itemValue);
        } catch (DictTypeNotFoundException e) {
            return itemValue;
        }
    }
}
