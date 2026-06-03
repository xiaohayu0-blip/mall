import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Table, Button, Spin, message, Typography } from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import { orderApi } from '../api';

const { Text, Title } = Typography;

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING_PAYMENT: { label: '待支付', color: 'orange' },
  PAID: { label: '已支付', color: 'blue' },
  SHIPPED: { label: '已发货', color: 'purple' },
  COMPLETED: { label: '已完成', color: 'green' },
  CANCELLED: { label: '已取消', color: 'red' },
};

const OrderDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      orderApi.detail(Number(id))
        .then((res: any) => setOrder(res.data))
        .catch(() => message.error('加载失败'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) return <Spin style={{ display: 'block', margin: '100px auto' }} />;
  if (!order) return <div style={{ textAlign: 'center', padding: 100 }}>订单不存在</div>;

  const statusInfo = statusMap[order.status] || { label: order.status, color: 'default' };
  const items = order.items || [];

  const itemColumns = [
    { title: '商品', dataIndex: 'commodityName', key: 'name' },
    {
      title: '单价',
      dataIndex: 'commodityPrice',
      key: 'price',
      render: (p: number) => `¥${(p / 100).toFixed(2)}`,
    },
    { title: '数量', dataIndex: 'quantity', key: 'qty' },
    {
      title: '小计',
      dataIndex: 'subtotal',
      key: 'subtotal',
      render: (s: number) => <Text strong style={{ color: '#ff4d4f' }}>¥{(s / 100).toFixed(2)}</Text>,
    },
  ];

  const cancelOrder = async () => {
    try {
      await orderApi.cancel(order.id);
      message.success('订单已取消');
      window.location.reload();
    } catch (err: any) {
      message.error(err.message || '取消失败');
    }
  };

  return (
    <Card style={{ maxWidth: 900, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>订单详情</Title>
        <Tag color={statusInfo.color} style={{ fontSize: 14, padding: '4px 12px' }}>{statusInfo.label}</Tag>
      </div>

      <Descriptions bordered column={2} size="small">
        <Descriptions.Item label="订单号">{order.orderNo}</Descriptions.Item>
        <Descriptions.Item label="总金额">
          <Text strong style={{ color: '#ff4d4f', fontSize: 18 }}>¥{order.totalAmountYuan}</Text>
        </Descriptions.Item>
        <Descriptions.Item label="收货人">{order.receiverName || '-'}</Descriptions.Item>
        <Descriptions.Item label="联系电话">{order.receiverPhone || '-'}</Descriptions.Item>
        <Descriptions.Item label="收货地址" span={2}>{order.receiverAddress || '-'}</Descriptions.Item>
        <Descriptions.Item label="备注">{order.remark || '-'}</Descriptions.Item>
        <Descriptions.Item label="创建时间">
          {order.createTime ? new Date(order.createTime).toLocaleString('zh-CN') : '-'}
        </Descriptions.Item>
        {order.paidTime && (
          <Descriptions.Item label="支付时间">
            {new Date(order.paidTime).toLocaleString('zh-CN')}
          </Descriptions.Item>
        )}
        {order.shippedTime && (
          <Descriptions.Item label="发货时间">
            {new Date(order.shippedTime).toLocaleString('zh-CN')}
          </Descriptions.Item>
        )}
        {order.cancelledTime && (
          <Descriptions.Item label="取消时间">
            {new Date(order.cancelledTime).toLocaleString('zh-CN')}
          </Descriptions.Item>
        )}
      </Descriptions>

      <Title level={5} style={{ marginTop: 24 }}>商品明细</Title>
      <Table dataSource={items} columns={itemColumns} rowKey="id" pagination={false} />

      <div style={{ marginTop: 24, display: 'flex', gap: 12 }}>
        <Button onClick={() => navigate('/orders')}>返回订单列表</Button>
        {order.status === 'PENDING_PAYMENT' && (
          <>
            <Button type="primary"
              onClick={() => navigate(`/payment?orderNo=${order.orderNo}&amount=${order.totalAmountYuan}`)}>
              去支付
            </Button>
            <Button danger onClick={cancelOrder}>取消订单</Button>
          </>
        )}
        {order.status === 'SHIPPED' && (
          <Button type="primary" onClick={async () => {
            try {
              await orderApi.complete(order.id);
              window.location.reload();
            } catch (err: any) { message.error(err.message); }
          }}>确认收货</Button>
        )}
      </div>
    </Card>
  );
};

export default OrderDetail;
