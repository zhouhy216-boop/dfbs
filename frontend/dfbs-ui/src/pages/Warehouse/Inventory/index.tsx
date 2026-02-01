import { useRef, useState, useEffect } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormDigit,
  ProFormSelect,
  ProFormTextArea,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import request from '@/utils/request';

/** 库存行（与 WhInventoryEntity 对应） */
interface WhInventoryRow {
  id: number;
  warehouseId: number;
  partNo: string;
  quantity: number;
  safetyThreshold: number;
  updatedAt?: string;
}

/** 仓库选项（与 WhWarehouseEntity 对应） */
interface WhWarehouseOption {
  id: number;
  name: string;
  type: string;
}

const REF_TYPE_OPTIONS = [
  { label: '工单', value: 'WORK_ORDER' },
  { label: '报价单', value: 'QUOTE' },
];

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string }; status?: number }; message?: string };
  const msg = err.response?.data?.message ?? err.message ?? '操作失败';
  message.error(msg);
}

export default function WarehouseInventory() {
  const actionRef = useRef<ActionType>(null);
  const [warehouses, setWarehouses] = useState<WhWarehouseOption[]>([]);
  const [warehouseMap, setWarehouseMap] = useState<Record<number, string>>({});

  useEffect(() => {
    request.get<WhWarehouseOption[]>('/v1/warehouse/warehouses').then(({ data }) => {
      setWarehouses(data ?? []);
      const map: Record<number, string> = {};
      (data ?? []).forEach((w) => {
        map[w.id] = w.name;
      });
      setWarehouseMap(map);
    }).catch(() => setWarehouses([]));
  }, []);

  const columns: ProColumns<WhInventoryRow>[] = [
    { title: '配件编号', dataIndex: 'partNo', width: 140, ellipsis: true },
    { title: '数量', dataIndex: 'quantity', width: 100, search: false },
    {
      title: '仓库',
      dataIndex: 'warehouseId',
      width: 160,
      valueType: 'select',
      fieldProps: { options: warehouses.map((w) => ({ label: w.name, value: w.id })) },
      render: (_, row) => warehouseMap[row.warehouseId] ?? `ID:${row.warehouseId}`,
    },
    { title: '安全阈值', dataIndex: 'safetyThreshold', width: 100, search: false },
    { title: '更新时间', dataIndex: 'updatedAt', valueType: 'dateTime', width: 180, search: false },
  ];

  return (
    <div style={{ padding: 24 }}>
      <ProTable<WhInventoryRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          try {
            const { data } = await request.get<WhInventoryRow[]>('/v1/warehouse/inventory', {
              params: {
                warehouseId: params.warehouseId ?? undefined,
                partNo: params.partNo ?? undefined,
              },
            });
            const list = Array.isArray(data) ? data : [];
            return { data: list, total: list.length, success: true };
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="库存管理"
        toolBarRender={() => [
          <ModalForm<{ warehouseId: number; partNo: string; quantity: number; remark?: string }>
            key="inbound"
            title="入库"
            trigger={<Button type="primary">入库 (Inbound)</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/warehouse/inbound', {
                  warehouseId: values.warehouseId,
                  partNo: values.partNo,
                  quantity: values.quantity,
                  remark: values.remark,
                });
                message.success('入库成功');
                actionRef.current?.reload();
                return true;
              } catch (err) {
                showError(err);
                return false;
              }
            }}
          >
            <ProFormSelect
              name="warehouseId"
              label="仓库"
              rules={[{ required: true, message: '请选择仓库' }]}
              request={async () => warehouses.map((w) => ({ label: w.name, value: w.id }))}
              placeholder="请选择仓库（仅大库可入库）"
            />
            <ProFormText name="partNo" label="配件编号" rules={[{ required: true }]} placeholder="配件编号" />
            <ProFormDigit name="quantity" label="数量" min={1} rules={[{ required: true }]} />
            <ProFormTextArea name="remark" label="备注" />
          </ModalForm>,
          <ModalForm<{
            warehouseId: number;
            partNo: string;
            quantity: number;
            refType: string;
            refNo: string;
            remark?: string;
          }>
            key="outbound"
            title="出库"
            trigger={<Button>出库 (Outbound)</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/warehouse/outbound', {
                  warehouseId: values.warehouseId,
                  partNo: values.partNo,
                  quantity: values.quantity,
                  refType: values.refType,
                  refNo: values.refNo,
                  remark: values.remark,
                });
                message.success('出库成功');
                actionRef.current?.reload();
                return true;
              } catch (err) {
                showError(err);
                return false;
              }
            }}
          >
            <ProFormSelect
              name="warehouseId"
              label="仓库"
              rules={[{ required: true, message: '请选择仓库' }]}
              request={async () => warehouses.map((w) => ({ label: w.name, value: w.id }))}
              placeholder="请选择仓库"
            />
            <ProFormText name="partNo" label="配件编号" rules={[{ required: true }]} placeholder="配件编号" />
            <ProFormDigit name="quantity" label="数量" min={1} rules={[{ required: true }]} />
            <ProFormSelect
              name="refType"
              label="关联类型"
              rules={[{ required: true, message: '请选择工单或报价单' }]}
              options={REF_TYPE_OPTIONS}
              placeholder="工单/报价单"
            />
            <ProFormText name="refNo" label="单号" rules={[{ required: true, message: '请输入工单号或报价单号' }]} placeholder="工单号或报价单号" />
            <ProFormTextArea name="remark" label="备注" />
          </ModalForm>,
        ]}
      />
    </div>
  );
}
