import { Select } from 'antd';
import { useEffect, useState } from 'react';
import { getPersonOptions, type PersonOption } from '@/features/orgstructure/services/orgStructure';

export interface PersonSelectProps {
  value?: number | null;
  onChange?: (value: number | null, payload?: { name: string; phone: string; email?: string | null }) => void;
  placeholder?: string;
  allowClear?: boolean;
  style?: React.CSSProperties;
  disabled?: boolean;
}

/**
 * Reusable person selector. Returns id and optional payload (name, phone, email) for forms.
 * Requires org-structure read API (Super Admin in v1).
 */
export function PersonSelect({
  value,
  onChange,
  placeholder = '选择人员',
  allowClear = true,
  style,
  disabled = false,
}: PersonSelectProps) {
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

  const handleChange = (id: number | null) => {
    if (id == null) {
      onChange?.(null);
      return;
    }
    const opt = options.find((o) => o.id === id);
    onChange?.(id, opt ? { name: opt.name, phone: opt.phone, email: opt.email } : undefined);
  };

  return (
    <Select
      placeholder={placeholder}
      allowClear={allowClear}
      value={value === undefined || value === null ? undefined : value}
      onChange={handleChange}
      loading={loading}
      style={style}
      disabled={disabled}
      showSearch
      optionFilterProp="label"
      options={options.map((o) => ({
        label: `${o.name} ${o.phone}`,
        value: o.id,
      }))}
    />
  );
}
