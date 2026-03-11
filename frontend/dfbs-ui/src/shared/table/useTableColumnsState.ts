/**
 * Phase-1 统一表格：列状态持久化（按 tableKey 存 localStorage），支持恢复默认
 */
import { useCallback, useMemo } from 'react';

const STORAGE_PREFIX = 'dfbs_table_';

export function getTableStateStorageKey(tableKey: string): string {
  return `${STORAGE_PREFIX}${tableKey}`;
}

/** 清除指定表的持久化列状态 */
export function clearTableStateStorage(tableKey: string): void {
  try {
    localStorage.removeItem(getTableStateStorageKey(tableKey));
  } catch {
    // ignore
  }
}

const WIDTHS_SUFFIX = '_widths';

export function getColumnWidthsStorageKey(tableKey: string): string {
  return getTableStateStorageKey(tableKey) + WIDTHS_SUFFIX;
}

export function getStoredColumnWidths(tableKey: string): Record<string, number> {
  try {
    const raw = localStorage.getItem(getColumnWidthsStorageKey(tableKey));
    if (!raw) return {};
    const parsed = JSON.parse(raw) as unknown;
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) return parsed as Record<string, number>;
  } catch {
    // ignore
  }
  return {};
}

export function setStoredColumnWidths(tableKey: string, widths: Record<string, number>): void {
  try {
    localStorage.setItem(getColumnWidthsStorageKey(tableKey), JSON.stringify(widths));
  } catch {
    // ignore
  }
}

/** 清除指定表的持久化列宽（恢复默认时调用） */
export function clearColumnWidthsStorage(tableKey: string): void {
  try {
    localStorage.removeItem(getColumnWidthsStorageKey(tableKey));
  } catch {
    // ignore
  }
}

/** ProTable columnsState 所需的最小类型（持久化） */
export interface TableColumnsStateConfig {
  persistenceKey: string;
  persistenceType: 'localStorage' | 'sessionStorage';
}

/**
 * 返回 ProTable columnsState 配置与恢复默认方法
 * 持久化范围：当前页面 tableKey，仅列显示/顺序等，存 localStorage
 */
export function useTableColumnsState(tableKey: string): {
  columnsState: TableColumnsStateConfig;
  clearPersistedState: () => void;
} {
  const storageKey = useMemo(() => getTableStateStorageKey(tableKey), [tableKey]);

  const columnsState: TableColumnsStateConfig = useMemo(
    () => ({
      persistenceKey: storageKey,
      persistenceType: 'localStorage',
    }),
    [storageKey],
  );

  const clearPersistedState = useCallback(() => {
    clearTableStateStorage(tableKey);
  }, [tableKey]);

  return { columnsState, clearPersistedState };
}
