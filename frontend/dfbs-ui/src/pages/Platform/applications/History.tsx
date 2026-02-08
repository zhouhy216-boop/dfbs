import { Tabs } from 'antd';
import type { TabsProps } from 'antd';

/**
 * Applications history with tabs by application type.
 * Backend not implemented; placeholder tabs for Account Open / SIM Open, Reuse, Verification, SIM Activation.
 */
export default function ApplicationsHistory() {
  const items: TabsProps['items'] = [
    { key: 'account-open', label: '开户申请', children: <div style={{ padding: 24 }}>暂无已处理的开户申请记录。</div> },
    { key: 'sim-open', label: '网卡申请', children: <div style={{ padding: 24 }}>暂无已处理的网卡申请记录。</div> },
    { key: 'reuse', label: '缴费复用', children: <div style={{ padding: 24 }}>暂无记录。</div> },
    { key: 'verification', label: '申请核查', children: <div style={{ padding: 24 }}>暂无记录。</div> },
    { key: 'sim-activation', label: '网卡开通', children: <div style={{ padding: 24 }}>暂无记录。</div> },
  ];
  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 16 }}>申请历史</h2>
      <Tabs items={items} />
    </div>
  );
}
