import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Select, message, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { orderApi } from '../api';

const { Text, Title } = Typography;

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING_PAYMENT: { label: '待支付', color: 'orange' },
  PAID: { label: '已支付', color: 'blue' },
  SHIPPED: { label: '已发货', color: 'purple' },
  COMPLETED: { label: '已完成', color: 'green' },
  CANCELLED: { label: '已取消', color: 'red' },
};

const MyOrders: React.FC = () => {
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const navigate = useNavigate();

  const loadOrders = async () => {
    setLoading(true);
    try {
      const res: any = await orderApi.myOrders({ page, pageSize: 10, status });
      setOrders(res.data?.records || []);
      setTotal(res.data?.total || 0);
    } catch { setOrders([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { setPage(1); }, [status]);

  useEffect(() => { loadOrders(); }, [page, status]);

  const cancelOrder = async (orderId: number) => {
    try {
      await orderApi.cancel(orderId);
      message.success('订单已取消');
      loadOrders();
    } catch (err: any) {
      message.error(err.message || '取消失败');
    }
  };

  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180,
    },
    {
      title: '总金额',
      dataIndex: 'totalAmountYuan',
      key: 'totalAmount',
      width: 100,
      render: (val: string) => <Text strong style={{ color: '#ff4d4f' }}>¥{val}</Text>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: string) => {
        const info = statusMap[s] || { label: s, color: 'default' };
        return <Tag color={info.color}>{info.label}</Tag>;
      },
    },
    {
      title: '收货人',
      dataIndex: 'receiverName',
      key: 'receiverName',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (t: number) => t ? new Date(t).toLocaleString('zh-CN') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: any) => (
        <>
          <Button type="link" onClick={() => navigate(`/orders/${record.id}`)}>详情</Button>
          {record.status === 'PENDING_PAYMENT' && (
            <>
              <Button type="link" style={{ color: '#52c41a' }}
                onClick={() => navigate(`/payment?orderNo=${record.orderNo}&amount=${record.totalAmountYuan}`)}>
                去支付
              </Button>
              <Button type="link" danger onClick={() => cancelOrder(record.id)}>取消</Button>
            </>
          )}
          {record.status === 'SHIPPED' && (
            <Button type="link" style={{ color: '#1890ff' }}
              onClick={async () => {
                try {
                  await orderApi.complete(record.id);
                  message.success('确认收货成功');
                  loadOrders();
                } catch (err: any) {
                  message.error(err.message || '操作失败');
                }
              }}>
              确认收货
            </Button>
          )}
        </>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4} style={{ margin: 0 }}>我的订单</Title>
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
          current: page,
          pageSize: 10,
          total,
          onChange: setPage,
          showTotal: (t) => `共 ${t} 条`,
        }}
      />
    </div>
  );
};

export default MyOrders;
