package com.dfbs.app.application.dicttype;

import com.dfbs.app.interfaces.dicttype.dto.TransitionEdgeDto;
import com.dfbs.app.interfaces.dicttype.dto.TransitionEdgeReadDto;
import com.dfbs.app.interfaces.dicttype.dto.TransitionEdgeRequest;
import com.dfbs.app.interfaces.dicttype.dto.TransitionListResponse;
import com.dfbs.app.interfaces.dicttype.dto.TransitionsReadResponse;
import com.dfbs.app.modules.dicttype.DictItemEntity;
import com.dfbs.app.modules.dicttype.DictTransitionEntity;
import com.dfbs.app.modules.dicttype.DictTransitionRepo;
import com.dfbs.app.modules.dicttype.DictItemRepo;
import com.dfbs.app.modules.dicttype.DictTypeRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictTransitionService {

    private static final String DICT_TRANSITION_SELF_LOOP = "DICT_TRANSITION_SELF_LOOP";
    private static final String DICT_TRANSITION_ITEM_NOT_FOUND = "DICT_TRANSITION_ITEM_NOT_FOUND";
    private static final String DICT_TYPE_NOT_FOUND = "DICT_TYPE_NOT_FOUND";

    private final DictTransitionRepo transitionRepo;
    private final DictItemRepo itemRepo;
    private final DictTypeRepo typeRepo;

    public DictTransitionService(DictTransitionRepo transitionRepo, DictItemRepo itemRepo, DictTypeRepo typeRepo) {
        this.transitionRepo = transitionRepo;
        this.itemRepo = itemRepo;
        this.typeRepo = typeRepo;
    }

    @Transactional(readOnly = true)
    public TransitionListResponse list(Long typeId) {
        if (!typeRepo.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在");
        }
        List<DictTransitionEntity> list = transitionRepo.findByTypeIdOrderById(typeId);
        if (list.isEmpty()) {
            return new TransitionListResponse(List.of());
        }
        List<Long> fromIds = list.stream().map(DictTransitionEntity::getFromItemId).distinct().toList();
        List<Long> toIds = list.stream().map(DictTransitionEntity::getToItemId).distinct().toList();
        List<Long> allIds = new ArrayList<>(fromIds);
        toIds.forEach(id -> { if (!allIds.contains(id)) allIds.add(id); });
        Map<Long, DictItemEntity> itemMap = itemRepo.findAllById(allIds).stream().collect(Collectors.toMap(DictItemEntity::getId, e -> e));
        List<TransitionEdgeDto> dtos = list.stream()
                .map(t -> {
                    DictItemEntity from = itemMap.get(t.getFromItemId());
                    DictItemEntity to = itemMap.get(t.getToItemId());
                    return new TransitionEdgeDto(
                            t.getId(),
                            from != null ? from.getItemValue() : null,
                            from != null ? from.getItemLabel() : null,
                            to != null ? to.getItemValue() : null,
                            to != null ? to.getItemLabel() : null,
                            t.getEnabled()
                    );
                })
                .toList();
        return new TransitionListResponse(dtos);
    }

    @Transactional
    public TransitionListResponse upsert(Long typeId, List<TransitionEdgeRequest> transitions) {
        if (!typeRepo.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在");
        }
        if (transitions == null) {
            return list(typeId);
        }
        for (TransitionEdgeRequest req : transitions) {
            if (req == null || req.fromValue() == null || req.toValue() == null) continue;
            String fromVal = req.fromValue().trim();
            String toVal = req.toValue().trim();
            if (fromVal.isEmpty() || toVal.isEmpty()) continue;
            if (fromVal.equals(toVal)) {
                throw new DictTransitionException("不允许自环（from 与 to 相同）", DICT_TRANSITION_SELF_LOOP);
            }
            DictItemEntity fromItem = itemRepo.findByTypeIdAndItemValue(typeId, fromVal)
                    .orElseThrow(() -> new DictTransitionException("from 项不存在: " + fromVal, DICT_TRANSITION_ITEM_NOT_FOUND));
            DictItemEntity toItem = itemRepo.findByTypeIdAndItemValue(typeId, toVal)
                    .orElseThrow(() -> new DictTransitionException("to 项不存在: " + toVal, DICT_TRANSITION_ITEM_NOT_FOUND));
            boolean enabled = req.enabled() != null ? req.enabled() : true;
            transitionRepo.findByTypeIdAndFromItemIdAndToItemId(typeId, fromItem.getId(), toItem.getId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setEnabled(enabled);
                                existing.setUpdatedAt(Instant.now());
                                transitionRepo.save(existing);
                            },
                            () -> {
                                DictTransitionEntity e = new DictTransitionEntity();
                                e.setTypeId(typeId);
                                e.setFromItemId(fromItem.getId());
                                e.setToItemId(toItem.getId());
                                e.setEnabled(enabled);
                                e.setCreatedAt(Instant.now());
                                e.setUpdatedAt(Instant.now());
                                transitionRepo.save(e);
                            }
                    );
        }
        return list(typeId);
    }

    /** Business read: list allowed transitions by typeCode; 404 if type not found. */
    @Transactional(readOnly = true)
    public TransitionsReadResponse listForRead(String typeCode, boolean includeDisabled) {
        Long typeId = typeRepo.findByTypeCode(typeCode)
                .orElseThrow(() -> new DictTypeNotFoundException("字典类型不存在"))
                .getId();
        List<DictTransitionEntity> list = includeDisabled
                ? transitionRepo.findByTypeIdOrderById(typeId)
                : transitionRepo.findByTypeIdAndEnabledOrderById(typeId, true);
        if (list.isEmpty()) {
            return new TransitionsReadResponse(typeCode, List.of());
        }
        List<Long> allIds = list.stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getFromItemId(), t.getToItemId()))
                .distinct()
                .toList();
        Map<Long, DictItemEntity> itemMap = itemRepo.findAllById(allIds).stream().collect(Collectors.toMap(DictItemEntity::getId, e -> e));
        List<TransitionEdgeReadDto> edges = list.stream()
                .map(t -> {
                    DictItemEntity from = itemMap.get(t.getFromItemId());
                    DictItemEntity to = itemMap.get(t.getToItemId());
                    return new TransitionEdgeReadDto(
                            from != null ? from.getItemValue() : null,
                            to != null ? to.getItemValue() : null,
                            t.getEnabled(),
                            from != null ? from.getItemLabel() : null,
                            to != null ? to.getItemLabel() : null
                    );
                })
                .toList();
        return new TransitionsReadResponse(typeCode, edges);
    }

    public static class DictTransitionException extends RuntimeException {
        public DictTransitionException(String message, String machineCode) {
            super(message);
            this.machineCode = machineCode;
        }
        private final String machineCode;
        public String getMachineCode() { return machineCode; }
    }
}
