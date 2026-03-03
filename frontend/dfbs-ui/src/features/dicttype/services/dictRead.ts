import request from '@/shared/utils/request';

const READ_BASE = '/v1/dictionaries';

/** Single dictionary item option (read-only contract; no internal id). */
export interface DictionaryItemOption {
  value: string;
  label: string;
  sortOrder: number;
  enabled: boolean;
  parentValue?: string | null;
  note?: string | null;
}

/** Response from GET /api/v1/dictionaries/{typeCode}/items */
export interface DictionaryItemsResponse {
  typeCode: string;
  items: DictionaryItemOption[];
}

export interface GetDictionaryItemsParams {
  /** Default false: exclude disabled (for dropdowns). Use true for history/snapshot label resolution. */
  includeDisabled?: boolean;
  /** Type D cascades (1-level): when set, returns only direct children of this parent (item_value). Omit to get all items (roots + children); filter roots client-side by parentValue == null/empty. */
  parentValue?: string;
  q?: string;
}

const DICT_TYPE_NOT_FOUND = 'DICT_TYPE_NOT_FOUND';

/**
 * Fetch dictionary items by typeCode (read-only API). No client caching; always fresh.
 * Throws with user-friendly Chinese: 字典类型不存在 for 404 DICT_TYPE_NOT_FOUND, else 加载失败，请重试.
 *
 * Type D cascades (1-level):
 * - Fetch all (roots + children): call without parentValue; filter roots client-side where item.parentValue is null/empty.
 * - Fetch children of a root: call with parentValue = root's value (e.g. parentValue: selectedRoot.value).
 * - Selection: use includeDisabled: false (default). History/snapshot: use includeDisabled: true.
 */
export async function getDictionaryItems(
  typeCode: string,
  params?: GetDictionaryItemsParams
): Promise<DictionaryItemsResponse> {
  try {
    const searchParams: Record<string, string | boolean | undefined> = {};
    if (params?.includeDisabled !== undefined) searchParams.includeDisabled = params.includeDisabled;
    if (params?.parentValue !== undefined && params.parentValue !== '') searchParams.parentValue = params.parentValue;
    if (params?.q !== undefined && params.q !== '') searchParams.q = params.q;

    const { data } = await request.get<DictionaryItemsResponse>(`${READ_BASE}/${encodeURIComponent(typeCode)}/items`, {
      params: searchParams,
    });
    if (!data) return { typeCode, items: [] };
    return data;
  } catch (e: unknown) {
    const err = e as { response?: { data?: { machineCode?: string }; status?: number } };
    const code = err.response?.data?.machineCode;
    if (code === DICT_TYPE_NOT_FOUND) {
      const userError = new Error('字典类型不存在') as Error & { userMessage?: string };
      userError.userMessage = '字典类型不存在';
      throw userError;
    }
    const userError = new Error('加载失败，请重试') as Error & { userMessage?: string };
    userError.userMessage = '加载失败，请重试';
    throw userError;
  }
}
