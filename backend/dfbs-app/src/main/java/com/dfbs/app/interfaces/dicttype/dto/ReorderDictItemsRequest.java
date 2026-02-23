package com.dfbs.app.interfaces.dicttype.dto;

import java.util.List;

/** Request body for PATCH /api/v1/admin/dictionary-types/{typeId}/items/reorder */
public record ReorderDictItemsRequest(Long parentId, List<Long> orderedItemIds) {}
