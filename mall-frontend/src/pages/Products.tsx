import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Input, Select, Tag, Spin, message, Empty } from 'antd';
import { ShoppingCartOutlined, HeartOutlined, HeartFilled } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { productApi, categoryApi, cartApi, likesApi, tagApi } from '../api';

const Products: React.FC = () => {
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [allTags, setAllTags] = useState<any[]>([]);
  const [selectedTagId, setSelectedTagId] = useState<number | undefined>();
  const [likedSet, setLikedSet] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const navigate = useNavigate();

  const loadProducts = async () => {
    setLoading(true);
    try {
      if (selectedTagId) {
        const res: any = await productApi.queryByTags([selectedTagId], 1, 20);
        setProducts(res.data?.records || []);
      } else {
        const res: any = await productApi.list({ page: 1, pageSize: 20, keyword, categoryId });
        setProducts(res.data?.records || []);
      }
    } catch { setProducts([]); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    categoryApi.list().then((res: any) => setCategories(res.data || [])).catch(() => {});
    // 加载所有标签
    tagApi.getAll().then((res: any) => {
      const tags: any[] = [];
      Object.values(res.data || {}).forEach((groupTags: any) => {
        tags.push(...groupTags);
      });
      setAllTags(tags);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    loadProducts();
  }, [keyword, categoryId, selectedTagId]);

  // 加载已点赞的商品
  useEffect(() => {
    likesApi.myLikes(1).then((res: any) => {
      if (res.data) setLikedSet(new Set(res.data));
    }).catch(() => {});
  }, []);

  const addToCart = async (commodityId: number) => {
    try {
      await cartApi.add(commodityId);
      message.success('已加入购物车');
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const toggleLike = async (commodityId: number) => {
    try {
      await likesApi.add(1, commodityId);
      setLikedSet(prev => {
        const next = new Set(prev);
        if (next.has(commodityId)) next.delete(commodityId);
        else next.add(commodityId);
        return next;
      });
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        <Input.Search
          placeholder="搜索商品"
          style={{ width: 300 }}
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onSearch={(val) => setKeyword(val)}
          allowClear
        />
        <Select
          placeholder="全部分类"
          style={{ width: 160 }}
          allowClear
          value={categoryId}
          onChange={(val) => setCategoryId(val)}
          options={categories.map((c: any) => ({ label: c.name, value: c.id }))}
        />
        <Select
          placeholder="标签筛选"
          style={{ width: 160 }}
          allowClear
          value={selectedTagId}
          onChange={(val) => setSelectedTagId(val)}
          options={allTags.map((t: any) => ({ label: t.tagName, value: t.id }))}
        />
      </div>

      <Spin spinning={loading}>
        {products.length === 0 ? (
          <Empty description="暂无商品" />
        ) : (
          <Row gutter={[16, 16]}>
            {products.map((p: any) => (
              <Col key={p.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  style={{ borderRadius: 8 }}
                  actions={[
                    likedSet.has(p.id)
                      ? <HeartFilled key="like" style={{ color: '#ff4d4f' }}
                          onClick={(e) => { e.stopPropagation(); toggleLike(p.id); }} />
                      : <HeartOutlined key="like"
                          onClick={(e) => { e.stopPropagation(); toggleLike(p.id); }} />,
                    <ShoppingCartOutlined key="cart" onClick={(e) => { e.stopPropagation(); addToCart(p.id); }} />,
                  ]}
                  onClick={() => navigate(`/product/${p.id}`)}
                >
                  <Card.Meta
                    title={<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>{p.name}</span>
                      <span style={{ color: '#ff4d4f', fontSize: 18, fontWeight: 600 }}>¥{p.price}</span>
                    </div>}
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

export default Products;
