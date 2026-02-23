package com.dfbs.app.interfaces.dicttype.dto;

import java.time.Instant;

public record DictSnapshotDemoRecordDto(
        Long id,
        String typeCode,
        String itemValue,
        String itemLabelSnapshot,
        String note,
        Instant createdAt
) {
    public static DictSnapshotDemoRecordDto from(com.dfbs.app.modules.dicttype.DictLabelSnapshotDemoEntity e) {
        return new DictSnapshotDemoRecordDto(
                e.getId(),
                e.getTypeCode(),
                e.getItemValue(),
                e.getItemLabelSnapshot(),
                e.getNote(),
                e.getCreatedAt()
        );
    }
}
