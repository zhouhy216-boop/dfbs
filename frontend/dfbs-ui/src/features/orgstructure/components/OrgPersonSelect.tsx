import { Select } from 'antd';
import { useEffect, useState } from 'react';
import { getPersonOptions, type PersonOption } from '@/features/orgstructure/services/orgStructure';

export interface OrgPersonSelectProps {
  value?: number[];
  onChange?: (value: number[]) => void;
  placeholder?: string;
  disabled?: boolean;
  style?: React.CSSProperties;
  /** Max tag count to show, rest as +N */
  maxTagCount?: number;
}

/**
 * Reusable multi-select people picker. Returns list of person ids.
 * Used for position bindings (0..N people per position).
 */
export function OrgPersonSelect({
  value,
  onChange,
  placeholder = '选择人员（可多选）',
  disabled = false,
  style,
  maxTagCount = 3,
}: OrgPersonSelectProps) {
  const [options, setOptions] = useState<PersonOption[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getPersonOptions()
      .then((data) => {
        if (!cancelled) setOptions(data);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  const selectOptions = options.map((o) => ({
    label: `${o.name} ${o.phone || ''}`.trim(),
    value: o.id,
  }));

  return (
    <Select
      mode="multiple"
      placeholder={placeholder}
      value={value ?? []}
      onChange={(ids) => onChange?.(ids ?? [])}
      loading={loading}
      style={{ minWidth: 200, ...style }}
      disabled={disabled}
      showSearch
      optionFilterProp="label"
      options={selectOptions}
      maxTagCount={maxTagCount}
      filterOption={(input, opt) =>
        (opt?.label ?? '').toString().toLowerCase().includes((input || '').toLowerCase())
      }
    />
  );
}
