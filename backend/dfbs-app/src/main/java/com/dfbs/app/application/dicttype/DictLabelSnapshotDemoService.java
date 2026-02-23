package com.dfbs.app.application.dicttype;

import com.dfbs.app.interfaces.dicttype.dto.DictionaryItemsResponse;
import com.dfbs.app.modules.dicttype.DictLabelSnapshotDemoEntity;
import com.dfbs.app.modules.dicttype.DictLabelSnapshotDemoRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DictLabelSnapshotDemoService {

    private final DictionaryReadService dictionaryReadService;
    private final DictLabelSnapshotDemoRepo demoRepo;

    public DictLabelSnapshotDemoService(DictionaryReadService dictionaryReadService,
                                        DictLabelSnapshotDemoRepo demoRepo) {
        this.dictionaryReadService = dictionaryReadService;
        this.demoRepo = demoRepo;
    }

    /**
     * Resolve current label at save time (includeDisabled=true so we can resolve even if item is later disabled).
     * If type or item not found, use itemValue as fallback snapshot.
     */
    private String resolveLabelSnapshot(String typeCode, String itemValue) {
        try {
            DictionaryItemsResponse response = dictionaryReadService.getItemsByTypeCode(typeCode, true, null, null);
            return response.items().stream()
                    .filter(o -> itemValue.equals(o.value()))
                    .findFirst()
                    .map(o -> o.label())
                    .orElse(itemValue);
        } catch (DictTypeNotFoundException e) {
            return itemValue;
        }
    }

    @Transactional
    public DictLabelSnapshotDemoEntity create(String typeCode, String itemValue, String note) {
        String labelSnapshot = resolveLabelSnapshot(typeCode, itemValue);
        DictLabelSnapshotDemoEntity entity = new DictLabelSnapshotDemoEntity();
        entity.setTypeCode(typeCode);
        entity.setItemValue(itemValue);
        entity.setItemLabelSnapshot(labelSnapshot);
        entity.setNote(note);
        return demoRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<DictLabelSnapshotDemoEntity> list(int page, int size) {
        return demoRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
