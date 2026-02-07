import { useCallback, useEffect, useRef, useState } from 'react';
import { AutoComplete, Tag } from 'antd';
import type { DefaultOptionType } from 'antd/es/select';
import request from '@/shared/utils/request';

export type EntityType = 'CUSTOMER' | 'MACHINE' | 'PART' | 'CONTRACT' | 'SIM' | 'MODEL';

export interface SmartSelectItem {
  id: number;
  displayName: string;
  uniqueKey: string;
  isTemp?: boolean;
}

/** Ref passed to parent: existing (id set) or free-text (id null, isTemp true). */
export interface SmartReferenceValue {
  id: number | null;
  name: string;
  isTemp: boolean;
}

export interface SmartReferenceSelectProps {
  entityType: EntityType;
  /** Current display string (e.g. form's customerName). */
  value?: string | null;
  /** When user selects existing or types new text. */
  onChange?: (ref: SmartReferenceValue) => void;
  /** e.g. modelId for PART */
  dependency?: number | null;
  placeholder?: string;
  disabled?: boolean;
  style?: React.CSSProperties;
  allowClear?: boolean;
}

const DEBOUNCE_MS = 300;

export default function SmartReferenceSelect({
  entityType,
  value,
  onChange,
  dependency,
  placeholder,
  disabled,
  style,
  allowClear = true,
}: SmartReferenceSelectProps) {
  const [options, setOptions] = useState<DefaultOptionType[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputValue, setInputValue] = useState<string>(value ?? '');
  const lastSelectedNameRef = useRef<string | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const search = useCallback(
    async (keyword: string) => {
      const k = keyword?.trim() ?? '';
      if (!k) {
        setOptions([]);
        return;
      }
      setLoading(true);
      try {
        const { data } = await request.get<SmartSelectItem[]>('/v1/smart-select/search', {
          params: { keyword: k, entityType },
        });
        const list = Array.isArray(data) ? data : [];
        setOptions(
          list.map((item) => ({
            label: (
              <span>
                {item.displayName}
                {item.isTemp && (
                  <Tag color="orange" style={{ marginLeft: 6 }}>
                    待确认
                  </Tag>
                )}
              </span>
            ),
            value: item.displayName,
            item,
          }))
        );
      } catch {
        setOptions([]);
      } finally {
        setLoading(false);
      }
    },
    [entityType]
  );

  const handleSelect = useCallback(
    (val: string, opt?: DefaultOptionType & { item?: SmartSelectItem }) => {
      const item = opt?.item ?? null;
      lastSelectedNameRef.current = val;
      setInputValue(val);
      onChange?.({ id: item?.id ?? null, name: val, isTemp: item?.isTemp ?? false });
    },
    [onChange]
  );

  const handleBlur = useCallback(() => {
    const v = (inputValue ?? '').trim();
    if (!v) {
      onChange?.({ id: null, name: '', isTemp: false });
      return;
    }
    if (v === lastSelectedNameRef.current) return;
    lastSelectedNameRef.current = v;
    onChange?.({ id: null, name: v, isTemp: true });
  }, [inputValue, onChange]);

  const partNeedsModel = entityType === 'PART' && (dependency == null || dependency === 0);
  const effectiveDisabled = disabled || partNeedsModel;

  useEffect(() => {
    setInputValue(value ?? '');
  }, [value]);

  useEffect(() => {
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, []);

  const handleSearch = useCallback(
    (keyword: string) => {
      setInputValue(keyword);
      if ((keyword ?? '').trim() === '') {
        lastSelectedNameRef.current = null;
        onChange?.({ id: null, name: '', isTemp: false });
      } else {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => search(keyword), DEBOUNCE_MS);
      }
    },
    [onChange, search]
  );

  const autoCompleteProps = {
    value: inputValue,
    onChange: handleSearch,
    onSelect: handleSelect,
    onBlur: handleBlur,
    placeholder: partNeedsModel ? '请先选择型号' : placeholder ?? '输入关键词搜索或直接输入新名称',
    options,
    loading,
    disabled: effectiveDisabled,
    style: { width: '100%', ...style },
    allowClear,
    notFoundContent: loading ? '搜索中...' : undefined,
    filterOption: false,
  };
  // AutoComplete types omit 'loading'; rc-select accepts it at runtime
  return <AutoComplete {...(autoCompleteProps as React.ComponentProps<typeof AutoComplete> & { loading?: boolean })} />;
}
