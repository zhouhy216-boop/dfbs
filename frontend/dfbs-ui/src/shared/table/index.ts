/**
 * Phase-1 统一表格能力：共享配置、持久化、包装组件、工具
 */
export { UnifiedProTable, UNIFIED_TABLE_KEYS, type UnifiedProTableProps, type UnifiedTableKey } from './UnifiedProTable';
export { CopyableCell, type CopyableCellProps } from './CopyableCell';
export {
  useTableColumnsState,
  getTableStateStorageKey,
  clearTableStateStorage,
  type TableColumnsStateConfig,
} from './useTableColumnsState';
export {
  unifiedTableLocale,
  UNIFIED_EMPTY_TEXT,
  UNIFIED_LOADING_TEXT,
  UNIFIED_EMPTY_VALUE,
} from './unifiedTableLocale';
export {
  UNIFIED_PAGE_SIZE_OPTIONS,
  UNIFIED_DEFAULT_PAGE_SIZE,
  UNIFIED_SCROLL_Y,
} from './unifiedTableConstants';
