import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Button, Typography, message, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { cartApi, orderApi } from '../api';

const { Text, Title } = Typography;

const Checkout: React.FC = () => {
  const [items, setItems] = useState<any[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    cartApi.selected().then((res: any) => {
      const data = res.data || [];
      if (data.length === 0) {
        message.warning('购物车为空，请先添加商品');
        navigate('/');
      }
      setItems(data);
    }).catch(() => navigate('/'));
  }, []);

  const totalAmount = items.reduce((sum, item) =>
    sum + (parseFloat(item.price || '0') * (item.quantity || 0)), 0
  );

  const onSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      const res: any = await orderApi.create({
        receiverName: values.name,
        receiverPhone: values.phone,
        receiverAddress: values.address,
        remark: values.remark,
      });
      const order = res.data;
      message.success('订单创建成功');
      navigate(`/payment?orderNo=${order.orderNo}&amount=${order.totalAmountYuan}`);
    } catch (err: any) {
      message.error(err.message || '下单失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Title level={4}>确认订单</Title>
      <Card title="收货信息" style={{ marginBottom: 16 }}>
        <Form form={form} layout="vertical" onFinish={onSubmit}>
          <Space style={{ width: '100%' }} size={16}>
            <Form.Item name="name" label="收货人" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input placeholder="请输入收货人" />
            </Form.Item>
            <Form.Item name="phone" label="联系电话" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input placeholder="请输入电话" />
            </Form.Item>
          </Space>
          <Form.Item name="address" label="收货地址" rules={[{ required: true }]}>
            <Input placeholder="请输入详细地址" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} placeholder="选填" />
          </Form.Item>
        </Form>
      </Card>

      <Card title="商品清单" style={{ marginBottom: 16 }}>
        {items.map((item) => (
          <div key={item.commodityId} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
            <div>
              <Text>{item.name}</Text>
              <Text type="secondary" style={{ marginLeft: 8 }}>x{item.quantity}</Text>
            </div>
            <Text style={{ color: '#ff4d4f' }}>
              ¥{(parseFloat(item.price) * item.quantity).toFixed(2)}
            </Text>
          </div>
        ))}
        <div style={{ textAlign: 'right', marginTop: 16 }}>
          <Text style={{ fontSize: 16 }}>合计：</Text>
          <Text strong style={{ color: '#ff4d4f', fontSize: 24 }}>¥{totalAmount.toFixed(2)}</Text>
        </div>
      </Card>

      <div style={{ textAlign: 'right' }}>
        <Button onClick={() => navigate('/cart')} style={{ marginRight: 12 }}>返回购物车</Button>
        <Button type="primary" size="large" loading={submitting}
          onClick={() => form.submit()}>
          提交订单
        </Button>
      </div>
    </div>
  );
};

export default Checkout;
