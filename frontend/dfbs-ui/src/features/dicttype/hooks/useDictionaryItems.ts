import { useCallback, useState } from 'react';
import {
  getDictionaryItems,
  type DictionaryItemsResponse,
  type GetDictionaryItemsParams,
} from '@/features/dicttype/services/dictRead';

export interface UseDictionaryItemsResult {
  loading: boolean;
  error: string | null;
  items: DictionaryItemsResponse['items'];
  typeCode: string | null;
  reload: () => Promise<void>;
}

/**
 * Fetch dictionary items by typeCode. No client caching; reload() always fetches fresh.
 * Error is user-friendly Chinese: 字典类型不存在 or 加载失败，请重试.
 */
export function useDictionaryItems(
  typeCode: string,
  params?: GetDictionaryItemsParams
): UseDictionaryItemsResult {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<DictionaryItemsResponse | null>(null);

  const reload = useCallback(async () => {
    if (!typeCode?.trim()) {
      setData(null);
      setError(null);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await getDictionaryItems(typeCode.trim(), params);
      setData(res);
    } catch (e: unknown) {
      const err = e as Error & { userMessage?: string };
      setError(err?.userMessage ?? '加载失败，请重试');
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [typeCode, params?.includeDisabled, params?.parentValue, params?.q]);

  return {
    loading,
    error,
    items: data?.items ?? [],
    typeCode: data?.typeCode ?? null,
    reload,
  };
}
