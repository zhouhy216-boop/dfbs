/**
 * Phase-1 统一表格：ProTable 包装，注入列设置/密度/刷新/持久化/斑马纹/空值/分页/结果数与刷新时间/恢复默认/列宽拖拽
 */
import { ProTable } from '@ant-design/pro-components';
import type { ActionType, ProColumns, ProTableProps } from '@ant-design/pro-components';
import { RollbackOutlined } from '@ant-design/icons';
import { Space, Tooltip } from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import dayjs from 'dayjs';
import {
  UNIFIED_DEFAULT_PAGE_SIZE,
  UNIFIED_PAGE_SIZE_OPTIONS,
  UNIFIED_SCROLL_Y,
} from './unifiedTableConstants';
import { unifiedTableLocale, UNIFIED_EMPTY_VALUE } from './unifiedTableLocale';
import { ResizableTitle } from './ResizableTitle';
import {
  useTableColumnsState,
  getStoredColumnWidths,
  setStoredColumnWidths,
  clearColumnWidthsStorage,
} from './useTableColumnsState';
import './unifiedTableStyles.css';

const TABLE_KEY_CUSTOMER = 'customer';
const TABLE_KEY_CONTRACT = 'master-data-contracts';
const TABLE_KEY_ACCOUNT_LIST = 'admin-account-permissions-accounts';
const TABLE_KEY_QUOTES = 'quotes';
const TABLE_KEY_SHIPMENTS = 'shipments';
const TABLE_KEY_AFTER_SALES = 'after-sales';
const TABLE_KEY_FINANCE = 'finance';
const TABLE_KEY_WAREHOUSE_INVENTORY = 'warehouse-inventory';
const TABLE_KEY_WAREHOUSE_REPLENISH_MY = 'warehouse-replenish-my';
const TABLE_KEY_WAREHOUSE_REPLENISH_PENDING = 'warehouse-replenish-pending';
const TABLE_KEY_PLATFORM_ORGS = 'platform-orgs';
const TABLE_KEY_PLATFORM_APPLICATIONS = 'platform-applications';
const TABLE_KEY_MASTERDATA_SIMCARDS = 'master-data-sim-cards';
const TABLE_KEY_MASTERDATA_SPAREPARTS = 'master-data-spare-parts';
const TABLE_KEY_MASTERDATA_MACHINES = 'master-data-machines';
const TABLE_KEY_MASTERDATA_MACHINEMODELS = 'master-data-machine-models';
const TABLE_KEY_MASTERDATA_MODELPARTLISTS = 'master-data-model-part-lists';
const TABLE_KEY_ADMIN_DICTIONARY_TYPES = 'admin-dictionary-types';
const TABLE_KEY_ADMIN_DICTIONARY_ITEMS = 'admin-dictionary-items';
const TABLE_KEY_SYSTEM_PLATFORM_CONFIG = 'system-platform-config';
const TABLE_KEY_WORKORDERS_PENDING = 'work-orders-pending';
const TABLE_KEY_WORKORDERS_READY = 'work-orders-ready';
const TABLE_KEY_WORKORDERS_MY = 'work-orders-my';
const TABLE_KEY_WORKORDERS_ALL = 'work-orders-all';
const TABLE_KEY_CONFIRMATION_CENTER = 'admin-confirmation-center';
const TABLE_KEY_MACHINE_DETAIL_HISTORY = 'master-data-machine-detail-history';
const TABLE_KEY_SIMCARD_DETAIL_HISTORY = 'master-data-simcard-detail-history';
const TABLE_KEY_MACHINEMODEL_DETAIL_BOM = 'master-data-machine-model-detail-bom';

export const UNIFIED_TABLE_KEYS = {
  CUSTOMER: TABLE_KEY_CUSTOMER,
  CONTRACT: TABLE_KEY_CONTRACT,
  ACCOUNT_LIST: TABLE_KEY_ACCOUNT_LIST,
  QUOTES: TABLE_KEY_QUOTES,
  SHIPMENTS: TABLE_KEY_SHIPMENTS,
  AFTER_SALES: TABLE_KEY_AFTER_SALES,
  FINANCE: TABLE_KEY_FINANCE,
  WAREHOUSE_INVENTORY: TABLE_KEY_WAREHOUSE_INVENTORY,
  WAREHOUSE_REPLENISH_MY: TABLE_KEY_WAREHOUSE_REPLENISH_MY,
  WAREHOUSE_REPLENISH_PENDING: TABLE_KEY_WAREHOUSE_REPLENISH_PENDING,
  PLATFORM_ORGS: TABLE_KEY_PLATFORM_ORGS,
  PLATFORM_APPLICATIONS: TABLE_KEY_PLATFORM_APPLICATIONS,
  MASTERDATA_SIMCARDS: TABLE_KEY_MASTERDATA_SIMCARDS,
  MASTERDATA_SPAREPARTS: TABLE_KEY_MASTERDATA_SPAREPARTS,
  MASTERDATA_MACHINES: TABLE_KEY_MASTERDATA_MACHINES,
  MASTERDATA_MACHINEMODELS: TABLE_KEY_MASTERDATA_MACHINEMODELS,
  MASTERDATA_MODELPARTLISTS: TABLE_KEY_MASTERDATA_MODELPARTLISTS,
  ADMIN_DICTIONARY_TYPES: TABLE_KEY_ADMIN_DICTIONARY_TYPES,
  ADMIN_DICTIONARY_ITEMS: TABLE_KEY_ADMIN_DICTIONARY_ITEMS,
  SYSTEM_PLATFORM_CONFIG: TABLE_KEY_SYSTEM_PLATFORM_CONFIG,
  WORKORDERS_PENDING: TABLE_KEY_WORKORDERS_PENDING,
  WORKORDERS_READY: TABLE_KEY_WORKORDERS_READY,
  WORKORDERS_MY: TABLE_KEY_WORKORDERS_MY,
  WORKORDERS_ALL: TABLE_KEY_WORKORDERS_ALL,
  CONFIRMATION_CENTER: TABLE_KEY_CONFIRMATION_CENTER,
  MACHINE_DETAIL_HISTORY: TABLE_KEY_MACHINE_DETAIL_HISTORY,
  SIMCARD_DETAIL_HISTORY: TABLE_KEY_SIMCARD_DETAIL_HISTORY,
  MACHINEMODEL_DETAIL_BOM: TABLE_KEY_MACHINEMODEL_DETAIL_BOM,
} as const;

export type UnifiedTableKey = typeof UNIFIED_TABLE_KEYS[keyof typeof UNIFIED_TABLE_KEYS];

const DEFAULT_OPTIONS = {
  reload: true,
  density: true,
  setting: true,
  fullScreen: true,
} as const;

export interface UnifiedProTableProps<T extends object = object, U = Record<string, unknown>, V = 'text'>
  extends Omit<ProTableProps<T, U, V>, 'options' | 'columnsState' | 'locale' | 'columnEmptyText' | 'pagination' | 'scroll' | 'rowClassName' | 'tableClassName' | 'toolBarRender' | 'optionsRender'> {
  /** 用于持久化列状态与恢复默认，必填 */
  tableKey: string;
  options?: ProTableProps<T, U, V>['options'];
  optionsRender?: ProTableProps<T, U, V>['optionsRender'];
  columnsState?: ProTableProps<T, U, V>['columnsState'];
  locale?: ProTableProps<T, U, V>['locale'];
  columnEmptyText?: ProTableProps<T, U, V>['columnEmptyText'];
  pagination?: ProTableProps<T, U, V>['pagination'];
  scroll?: ProTableProps<T, U, V>['scroll'];
  rowClassName?: ProTableProps<T, U, V>['rowClassName'];
  tableClassName?: string;
  toolBarRender?: ProTableProps<T, U, V>['toolBarRender'];
}

export function UnifiedProTable<T extends object, U = Record<string, unknown>, V = 'text'>(
  props: UnifiedProTableProps<T, U, V>,
) {
  const {
    tableKey,
    options = DEFAULT_OPTIONS,
    optionsRender: optionsRenderProp,
    columnsState: columnsStateProp,
    locale: localeProp,
    columnEmptyText = UNIFIED_EMPTY_VALUE,
    pagination: paginationProp,
    scroll: scrollProp,
    rowClassName: rowClassNameProp,
    tableClassName: tableClassNameProp,
    toolBarRender: toolBarRenderProp,
    request: requestProp,
    actionRef: actionRefProp,
    ...rest
  } = props;

  const { columnsState, clearPersistedState } = useTableColumnsState(tableKey);
  const [lastTotal, setLastTotal] = useState<number | null>(null);
  const [lastRefreshAt, setLastRefreshAt] = useState<number | null>(null);
  const [restoreKey, setRestoreKey] = useState(0);
  const [columnWidths, setColumnWidths] = useState<Record<string, number>>(() => getStoredColumnWidths(tableKey));

  useEffect(() => {
    setColumnWidths(getStoredColumnWidths(tableKey));
  }, [tableKey]);

  const request = useCallback(
    async (params: U & { pageSize?: number; current?: number; keyword?: string }, sort: Record<string, 'ascend' | 'descend' | null>, filter: Record<string, (string | number)[] | null>) => {
      if (!requestProp) return { data: [], total: 0, success: true };
      const result = await requestProp(params, sort, filter);
      if (result && typeof result.total === 'number') setLastTotal(result.total);
      setLastRefreshAt(Date.now());
      return result;
    },
    [requestProp],
  );

  const handleRestoreDefault = useCallback(() => {
    clearPersistedState();
    clearColumnWidthsStorage(tableKey);
    setColumnWidths({});
    setRestoreKey((k) => k + 1);
    const ref = actionRefProp as React.RefObject<{ reload?: () => void }> | undefined;
    setTimeout(() => ref?.current?.reload?.(), 0);
  }, [clearPersistedState, actionRefProp, tableKey]);

  const handleResize = useCallback(
    (colKey: string, width: number) => {
      setColumnWidths((prev) => {
        const next = { ...prev, [colKey]: width };
        setStoredColumnWidths(tableKey, next);
        return next;
      });
    },
    [tableKey],
  );

  const DEFAULT_COL_WIDTH = 120;
  const baseColumns = rest.columns ?? [];
  const mergedColumns = useMemo(() => {
    return baseColumns.map((col: ProColumns<T, V>, index: number) => {
      const key = String(col.key ?? col.dataIndex ?? index);
      const defW = typeof col.width === 'number' ? col.width : DEFAULT_COL_WIDTH;
      const currentW = columnWidths[key] ?? defW;
      return {
        ...col,
        width: currentW,
        onHeaderCell: () => ({
          width: currentW,
          onResize: (_e: React.SyntheticEvent, data: { size: { width: number } }) => handleResize(key, data.size.width),
        }),
      };
    });
  }, [baseColumns, columnWidths, handleResize]);

  const mergedColumnsState = columnsStateProp ?? columnsState;
  const mergedScroll = { x: 'max-content' as const, y: UNIFIED_SCROLL_Y, ...scrollProp };
  const mergedPagination =
    paginationProp === false
      ? false
      : {
          defaultPageSize: UNIFIED_DEFAULT_PAGE_SIZE,
          pageSizeOptions: UNIFIED_PAGE_SIZE_OPTIONS,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`,
          ...(typeof paginationProp === 'object' ? paginationProp : {}),
        };

  const zebraRowClassName = useCallback(
    (record: T, index?: number) => {
      const zebra = typeof index === 'number' && index % 2 === 1 ? ' unified-table-row-zebra' : '';
      const custom =
        typeof rowClassNameProp === 'function'
          ? (rowClassNameProp as (r: T, i?: number) => string)(record, index) ?? ''
          : (rowClassNameProp ?? '') as string;
      const out = `${String(custom)}${zebra}`.trim();
      return out || '';
    },
    [rowClassNameProp],
  );

  const restoreDefaultControl = useMemo(
    () => (
      <div key="restore-default" className="ant-pro-table-list-toolbar-setting-item">
        <Tooltip title="恢复默认视图">
          <span
            style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center' }}
            onClick={handleRestoreDefault}
            role="button"
            aria-label="恢复默认视图"
          >
            <RollbackOutlined style={{ fontSize: 14 }} />
          </span>
        </Tooltip>
      </div>
    ),
    [handleRestoreDefault],
  );

  const optionsRender = useCallback(
    (toolbarProps: Parameters<NonNullable<ProTableProps<T, U, V>['optionsRender']>>[0], defaultDom: React.ReactNode[]) => {
      const next = [...(Array.isArray(defaultDom) ? defaultDom : []), restoreDefaultControl];
      return typeof optionsRenderProp === 'function' ? optionsRenderProp(toolbarProps, next) : next;
    },
    [restoreDefaultControl, optionsRenderProp],
  );

  const toolBarRender = useCallback(
    (action: ActionType | undefined, rows: { selectedRowKeys?: (string | number)[]; selectedRows?: T[] }) => {
      const summary =
        lastTotal !== null || lastRefreshAt !== null ? (
          <Space size="middle" style={{ marginRight: 8 }} align="center" key="summary">
            {lastTotal !== null && <span style={{ fontSize: 12, color: '#666' }}>共 {lastTotal} 条</span>}
            {lastRefreshAt !== null && (
              <span style={{ fontSize: 12, color: '#999' }}>
                更新于 {dayjs(lastRefreshAt).format('HH:mm:ss')}
              </span>
            )}
          </Space>
        ) : null;
      const original =
        typeof toolBarRenderProp === 'function'
          ? (toolBarRenderProp as (a: ActionType | undefined, r: typeof rows) => React.ReactNode[])(action, rows)
          : toolBarRenderProp === false
            ? []
            : undefined;
      const origArr = Array.isArray(original) ? original : original ? [original] : [];
      return summary ? [summary, ...origArr] : origArr;
    },
    [lastTotal, lastRefreshAt, toolBarRenderProp],
  );

  const tableLocale = { ...unifiedTableLocale, ...localeProp };
  const tableComponents = useMemo(
    () => ({
      ...(typeof (rest as { components?: object }).components === 'object' ? (rest as { components: object }).components : {}),
      header: {
        ...(typeof (rest as { components?: { header?: object } }).components?.header === 'object'
          ? (rest as { components: { header: object } }).components.header
          : {}),
        cell: ResizableTitle,
      },
    }),
    [rest],
  );
  const tableProps: ProTableProps<T, U, V> = {
    ...rest,
    columns: mergedColumns,
    components: tableComponents,
    actionRef: actionRefProp,
    request: requestProp ? request : undefined,
    options: options === false ? false : { ...DEFAULT_OPTIONS, ...options },
    optionsRender,
    columnsState: mergedColumnsState,
    locale: tableLocale,
    columnEmptyText,
    pagination: mergedPagination,
    scroll: mergedScroll,
    rowClassName: zebraRowClassName,
    tableClassName: `unified-table${tableClassNameProp ? ` ${tableClassNameProp}` : ''}`,
    toolBarRender,
  };
  return (
    <ProTable key={restoreKey} {...(tableProps as ProTableProps<T, Record<string, unknown>, V>)} />
  );
}
