/**
 * Phase-1 统一表格：中文文案（空状态、加载、分页等）
 */
import type { TableLocale } from 'antd/es/table/interface';

export const unifiedTableLocale: TableLocale = {
  emptyText: '暂无数据',
  triggerDesc: '点击降序',
  triggerAsc: '点击升序',
  cancelSort: '取消排序',
};

/** 空结果提示（无数据） */
export const UNIFIED_EMPTY_TEXT = '暂无数据';

/** 加载中提示 */
export const UNIFIED_LOADING_TEXT = '加载中...';

/** 空值占位渲染用 */
export const UNIFIED_EMPTY_VALUE = '—';
