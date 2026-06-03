import React, { useState } from 'react';
import { Card, Button, Result, Typography, message } from 'antd';
import { CheckCircleOutlined, LoadingOutlined } from '@ant-design/icons';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { paymentApi } from '../api';

const { Text, Title } = Typography;

const Payment: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [paying, setPaying] = useState(false);
  const [paid, setPaid] = useState(false);
  const navigate = useNavigate();

  const orderNo = searchParams.get('orderNo') || '';
  const amount = searchParams.get('amount') || '0.00';

  const handlePay = async () => {
    setPaying(true);
    try {
      await paymentApi.mockPay(orderNo);
      setPaid(true);
      message.success('支付成功！');
    } catch (err: any) {
      message.error(err.message || '支付失败');
    } finally {
      setPaying(false);
    }
  };

  return (
    <div style={{ maxWidth: 500, margin: '40px auto' }}>
      <Card>
        {paid ? (
          <Result
            status="success"
            title="支付成功"
            subTitle={`订单 ${orderNo} 已支付成功，金额 ¥${amount}`}
            extra={[
              <Button type="primary" key="orders" onClick={() => navigate('/orders')}>
                查看订单
              </Button>,
              <Button key="home" onClick={() => navigate('/')}>
                继续购物
              </Button>,
            ]}
          />
        ) : (
          <div style={{ textAlign: 'center' }}>
            <Title level={4}>订单确认</Title>
            <div style={{ margin: '24px 0' }}>
              <Text type="secondary">订单号：</Text>
              <Text>{orderNo}</Text>
            </div>
            <div style={{ margin: '16px 0' }}>
              <Text style={{ fontSize: 16 }}>应付金额：</Text>
              <Text strong style={{ color: '#ff4d4f', fontSize: 32 }}>¥{amount}</Text>
            </div>
            <Button
              type="primary"
              size="large"
              style={{ height: 48, width: 200, marginTop: 16 }}
              onClick={handlePay}
              loading={paying}
              icon={paying ? <LoadingOutlined /> : <CheckCircleOutlined />}
            >
              {paying ? '支付中...' : '模拟支付'}
            </Button>
            <div style={{ marginTop: 12 }}>
              <Text type="secondary" style={{ fontSize: 12 }}>
                * 此为模拟支付，点击即成功
              </Text>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
};

export default Payment;
