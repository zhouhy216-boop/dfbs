# DICT-260211-001-02.c reorder fix (DICT-260211-001-02.c-fix1)

## What was wrong

- When 父级 filter = "全部", the list showed **all** nodes (roots + children) because the backend did not filter by `parent_id IS NULL` when no `parentId` was sent.
- Reorder then sent `orderedItemIds` from that mixed list; the backend requires all ids to share the same parent, so it returned 400 and the UI showed "排序保存失败，请重试".

## Fix

- **Backend:** List endpoint accepts `rootsOnly=true`. When set, only items with `parentId` null (root nodes) are returned.
- **Frontend:** When 父级 = "全部", request list with `rootsOnly: true`. Table data source is thus roots-only. Reorder uses the same displayed list and sends `parentId: null` for roots scope.

## After fix (UI-only acceptance)

- 全部只显示根节点；根节点/子项均可上移下移并保存。
