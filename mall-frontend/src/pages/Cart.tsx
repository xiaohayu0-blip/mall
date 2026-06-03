import React, { useState, useEffect } from 'react';
import { Table, Button, InputNumber, message, Empty, Popconfirm, Typography, Card } from 'antd';
import { DeleteOutlined, ShoppingOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { cartApi } from '../api';

const { Text, Title } = Typography;

const Cart: React.FC = () => {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadCart = async () => {
    setLoading(true);
    try {
      const res: any = await cartApi.list();
      setItems(res.data || []);
    } catch { setItems([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { loadCart(); }, []);

  const updateQty = async (commodityId: number, quantity: number) => {
    try {
      await cartApi.update(commodityId, quantity);
      loadCart();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const removeItem = async (commodityId: number) => {
    try {
      await cartApi.remove(commodityId);
      message.success('已删除');
      loadCart();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const totalAmount = items.reduce((sum: number, item: any) => {
    return sum + (parseFloat(item.price || '0') * (item.quantity || 0));
  }, 0);

  const columns = [
    {
      title: '商品',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: any) => (
        <a onClick={() => navigate(`/product/${record.commodityId}`)}>{name}</a>
      ),
    },
    {
      title: '单价',
      dataIndex: 'price',
      key: 'price',
      width: 120,
      render: (price: string) => <Text strong style={{ color: '#ff4d4f' }}>¥{price}</Text>,
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 160,
      render: (qty: number, record: any) => (
        <InputNumber
          min={1}
          max={record.stock || 99}
          value={qty}
          onChange={(v) => v && updateQty(record.commodityId, v)}
        />
      ),
    },
    {
      title: '小计',
      key: 'subtotal',
      width: 120,
      render: (_: any, record: any) => (
        <Text strong style={{ color: '#ff4d4f' }}>
          ¥{(parseFloat(record.price || '0') * (record.quantity || 0)).toFixed(2)}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_: any, record: any) => (
        <Popconfirm title="确定删除？" onConfirm={() => removeItem(record.commodityId)}>
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  if (items.length === 0) {
    return (
      <Card>
        <Empty description="购物车是空的" />
        <Button type="primary" onClick={() => navigate('/')} icon={<ArrowLeftOutlined />}>
          去逛逛
        </Button>
      </Card>
    );
  }

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>购物车 ({items.length})</Title>
      <Table
        dataSource={items}
        columns={columns}
        rowKey="commodityId"
        loading={loading}
        pagination={false}
        footer={() => (
          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 24 }}>
            <div>
              合计：<Text strong style={{ color: '#ff4d4f', fontSize: 24 }}>¥{totalAmount.toFixed(2)}</Text>
            </div>
            <Button type="primary" size="large" icon={<ShoppingOutlined />}
              onClick={() => navigate('/checkout')}>
              去结算
            </Button>
          </div>
        )}
      />
    </div>
  );
};

export default Cart;
