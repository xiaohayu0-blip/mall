import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Select, message, Typography, Space } from 'antd';
import { CarOutlined } from '@ant-design/icons';
import { orderApi } from '../../api';

const { Title } = Typography;

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING_PAYMENT: { label: '待支付', color: 'orange' },
  PAID: { label: '已支付', color: 'blue' },
  SHIPPED: { label: '已发货', color: 'purple' },
  COMPLETED: { label: '已完成', color: 'green' },
  CANCELLED: { label: '已取消', color: 'red' },
};

const AdminOrders: React.FC = () => {
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  const loadOrders = async () => {
    setLoading(true);
    try {
      const res: any = await orderApi.adminList({ page, pageSize: 15, status });
      setOrders(res.data?.records || []);
      setTotal(res.data?.total || 0);
    } catch { setOrders([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { setPage(1); }, [status]);
  useEffect(() => { loadOrders(); }, [page, status]);

  const handleShip = async (orderId: number) => {
    try {
      await orderApi.ship(orderId);
      message.success('发货成功');
      loadOrders();
    } catch (err: any) {
      message.error(err.message || '发货失败');
    }
  };

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    {
      title: '用户', dataIndex: 'userName', key: 'userName', width: 100,
    },
    {
      title: '金额', dataIndex: 'totalAmountYuan', key: 'totalAmount', width: 100,
      render: (v: string) => <span style={{ color: '#ff4d4f', fontWeight: 600 }}>¥{v}</span>,
    },
    {
      title: '状态', dataIndex: 'status', key: 'status', width: 100,
      render: (s: string) => {
        const info = statusMap[s] || { label: s, color: 'default' };
        return <Tag color={info.color}>{info.label}</Tag>;
      },
    },
    { title: '收货人', dataIndex: 'receiverName', key: 'receiverName', width: 100 },
    { title: '电话', dataIndex: 'receiverPhone', key: 'receiverPhone', width: 120 },
    { title: '地址', dataIndex: 'receiverAddress', key: 'receiverAddress', ellipsis: true },
    {
      title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170,
      render: (t: number) => t ? new Date(t).toLocaleString('zh-CN') : '-',
    },
    {
      title: '操作', key: 'action', width: 120,
      render: (_: any, record: any) => (
        <Space>
          {record.status === 'PAID' && (
            <Button type="primary" size="small" icon={<CarOutlined />} onClick={() => handleShip(record.id)}>
              发货
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>订单管理</Title>
        <Select
          style={{ width: 150 }}
          placeholder="全部状态"
          allowClear
          value={status}
          onChange={(val) => setStatus(val)}
          options={Object.entries(statusMap).map(([key, v]) => ({ label: v.label, value: key }))}
        />
      </div>
      <Table
        dataSource={orders}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page, pageSize: 15, total,
          onChange: setPage, showTotal: (t) => `共 ${t} 条`,
        }}
      />
    </div>
  );
};

export default AdminOrders;
