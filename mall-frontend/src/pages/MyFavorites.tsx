import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Tag, Spin, message, Empty, Button, Typography } from 'antd';
import { ShoppingCartOutlined, HeartFilled, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { productApi, cartApi, likesApi } from '../api';

const { Title } = Typography;

const MyFavorites: React.FC = () => {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadFavorites();
  }, []);

  const loadFavorites = async () => {
    setLoading(true);
    try {
      // 获取点赞的商品 ID 列表（固定 businessId=1，代表商品模块）
      const res: any = await likesApi.myLikes(1);
      const likedIds: number[] = res.data || [];
      if (likedIds.length === 0) {
        setProducts([]);
        return;
      }
      // 逐个获取商品详情
      const details = await Promise.all(
        likedIds.map((id: number) =>
          productApi.getById(id).then((r: any) => r.data).catch(() => null)
        )
      );
      setProducts(details.filter(Boolean));
    } catch {
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (commodityId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await cartApi.add(commodityId);
      message.success('已加入购物车');
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>
          <HeartFilled style={{ color: '#ff4d4f', marginRight: 8 }} />
          我的收藏
        </Title>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')}>
          返回商城
        </Button>
      </div>

      <Spin spinning={loading}>
        {products.length === 0 ? (
          <Empty description="还没有收藏的商品，去逛逛吧~">
            <Button type="primary" onClick={() => navigate('/')}>去逛逛</Button>
          </Empty>
        ) : (
          <Row gutter={[16, 16]}>
            {products.map((p: any) => (
              <Col key={p.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  style={{ borderRadius: 8 }}
                  actions={[
                    <ShoppingCartOutlined key="cart" onClick={(e) => addToCart(p.id, e)} />,
                  ]}
                  onClick={() => navigate(`/product/${p.id}`)}
                >
                  <Card.Meta
                    title={
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span>{p.name}</span>
                        <span style={{ color: '#ff4d4f', fontSize: 18, fontWeight: 600 }}>¥{p.price}</span>
                      </div>
                    }
                    description={
                      <div>
                        <div style={{ color: '#888', marginBottom: 8 }}>
                          {p.categoryName || '未分类'} | 库存: {p.stock ?? 0}
                        </div>
                        <div>
                          {(p.tags || []).slice(0, 3).map((t: any) => (
                            <Tag key={t.tagId || t.id} color="blue" style={{ marginBottom: 2 }}>{t.tagName}</Tag>
                          ))}
                        </div>
                      </div>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>
    </div>
  );
};

export default MyFavorites;
