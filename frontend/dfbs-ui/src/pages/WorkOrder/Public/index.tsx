import { useState, useEffect } from 'react';
import { Card, Form, Input, Button, DatePicker, Select, Result, message } from 'antd';
import type { FormInstance } from 'antd/es/form';
import type { Dayjs } from 'dayjs';
import request from '@/shared/utils/request';
import SmartReferenceSelect from '@/shared/components/SmartReferenceSelect';

function CustomerSmartSelect({ form }: { form: FormInstance }) {
  const customerName = Form.useWatch('customerName', form);
  return (
    <SmartReferenceSelect
      entityType="CUSTOMER"
      value={customerName ?? ''}
      placeholder="输入客户名称或从下拉选择"
      onChange={(ref) => {
        form.setFieldValue('customerId', ref.id ?? null);
        form.setFieldValue('customerName', ref.name);
      }}
    />
  );
}

interface MachineModelOption {
  id: number;
  modelNo: string;
  modelName?: string;
}

interface SpringPage<T> {
  content?: T[];
  totalElements?: number;
}

export default function WorkOrderPublicPage() {
  const [form] = Form.useForm();
  const [submitted, setSubmitted] = useState(false);
  const [orderNo, setOrderNo] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [modelOptions, setModelOptions] = useState<{ label: string; value: number }[]>([]);

  useEffect(() => {
    request
      .get<SpringPage<MachineModelOption>>('/v1/masterdata/machine-models', {
        params: { page: 0, size: 200, sort: 'id,asc' },
      })
      .then(({ data }) => {
        const list = data?.content ?? [];
        setModelOptions(
          list.map((m) => ({
            label: `${m.modelNo}${m.modelName ? ' - ' + m.modelName : ''}`,
            value: m.id,
          }))
        );
      })
      .catch(() => setModelOptions([]));
  }, []);

  const onFinish = async (values: {
    customerId?: number | null;
    customerName?: string;
    contactPerson: string;
    contactPhone: string;
    serviceAddress: string;
    deviceModelId?: number;
    issueDescription?: string;
    appointmentTime?: Dayjs;
  }) => {
    setLoading(true);
    try {
      const { data } = await request.post<{ orderNo: string }>('/v1/public/work-orders/create', {
        customerId: values.customerId ?? undefined,
        customerName: values.customerName ?? undefined,
        contactPerson: values.contactPerson,
        contactPhone: values.contactPhone,
        serviceAddress: values.serviceAddress,
        deviceModelId: values.deviceModelId ?? undefined,
        issueDescription: values.issueDescription ?? undefined,
        appointmentTime: values.appointmentTime ? values.appointmentTime.toISOString() : undefined,
      });
      setOrderNo(data?.orderNo ?? '');
      setSubmitted(true);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } }; message?: string };
      message.error(err.response?.data?.message ?? err.message ?? '提交失败');
    } finally {
      setLoading(false);
    }
  };

  if (submitted) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5' }}>
        <Card style={{ maxWidth: 520 }}>
          <Result
            status="success"
            title="报修提交成功"
            subTitle={orderNo ? `工单号：${orderNo}` : undefined}
            extra={[
              <Button type="primary" key="again" onClick={() => { setSubmitted(false); setOrderNo(null); form.resetFields(); }}>
                再提交一单
              </Button>,
            ]}
          />
        </Card>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5', padding: 24 }}>
      <Card title="在线报修" style={{ maxWidth: 560, width: '100%' }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="customerId" hidden>
            <Input type="hidden" />
          </Form.Item>
          <Form.Item name="customerName" hidden rules={[{ required: true, message: '请输入或选择客户' }]}>
            <Input type="hidden" />
          </Form.Item>
          <Form.Item label="客户" required>
            <CustomerSmartSelect form={form} />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '请输入联系人' }]}>
            <Input placeholder="联系人" />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input placeholder="联系电话" />
          </Form.Item>
          <Form.Item name="serviceAddress" label="服务地址" rules={[{ required: true, message: '请输入服务地址' }]}>
            <Input.TextArea rows={2} placeholder="服务地址" />
          </Form.Item>
          <Form.Item name="deviceModelId" label="设备型号">
            <Select placeholder="请选择设备型号（选填）" allowClear options={modelOptions} showSearch optionFilterProp="label" />
          </Form.Item>
          <Form.Item name="issueDescription" label="故障描述">
            <Input.TextArea rows={4} placeholder="请描述故障现象（选填）" />
          </Form.Item>
          <Form.Item name="appointmentTime" label="期望上门时间">
            <DatePicker showTime style={{ width: '100%' }} placeholder="选填" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block size="large">
              提交报修
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
