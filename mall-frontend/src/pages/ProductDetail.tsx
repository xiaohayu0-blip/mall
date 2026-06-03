import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Button, InputNumber, message, Spin } from 'antd';
import { ShoppingCartOutlined, ShoppingOutlined, HeartOutlined, HeartFilled } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi, cartApi, likesApi } from '../api';

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      setLoading(true);
      productApi.getById(Number(id))
        .then((res: any) => setProduct(res.data))
        .catch(() => message.error('加载失败'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  // 加载点赞状态
  useEffect(() => {
    if (!id) return;
    likesApi.myLikes(1).then((res: any) => {
      const likedIds: number[] = res.data || [];
      setLiked(likedIds.includes(Number(id)));
    }).catch(() => {});
    likesApi.count(1, Number(id)).then((res: any) => {
      setLikeCount(res.data || 0);
    }).catch(() => {});
  }, [id]);

  const addToCart = async () => {
    if (!id) return;
    try {
      await cartApi.add(Number(id), quantity);
      message.success('已加入购物车');
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const toggleLike = async () => {
    if (!id) return;
    try {
      await likesApi.add(1, Number(id));
      setLiked(!liked);
      setLikeCount(prev => liked ? prev - 1 : prev + 1);
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  if (loading) return <Spin style={{ display: 'block', margin: '100px auto' }} />;
  if (!product) return <div style={{ textAlign: 'center', padding: 100 }}>商品不存在</div>;

  return (
    <Card style={{ maxWidth: 800, margin: '0 auto' }}>
      <Descriptions title={product.name} bordered column={1}>
        <Descriptions.Item label="价格">
          <span style={{ color: '#ff4d4f', fontSize: 24, fontWeight: 600 }}>¥{product.price}</span>
        </Descriptions.Item>
        <Descriptions.Item label="分类">{product.categoryName || '未分类'}</Descriptions.Item>
        <Descriptions.Item label="库存">{product.stock ?? 0}</Descriptions.Item>
        <Descriptions.Item label="描述">{product.description || '暂无描述'}</Descriptions.Item>
        <Descriptions.Item label="标签">
          {(product.tags || []).map((t: any) => (
            <Tag key={t.tagId || t.id} color="blue">{t.tagName}</Tag>
          ))}
          {(!product.tags || product.tags.length === 0) && <span>-</span>}
        </Descriptions.Item>
        <Descriptions.Item label="数量">
          <InputNumber min={1} max={product.stock || 99} value={quantity} onChange={(v) => setQuantity(v || 1)} />
          <span style={{ marginLeft: 8, color: '#888' }}>库存 {product.stock} 件</span>
        </Descriptions.Item>
      </Descriptions>

      <div style={{ marginTop: 24, display: 'flex', gap: 12, alignItems: 'center' }}>
        <Button type="primary" icon={<ShoppingCartOutlined />} size="large" onClick={addToCart}>
          加入购物车
        </Button>
        <Button icon={<ShoppingOutlined />} size="large" onClick={() => { addToCart(); navigate('/cart'); }}>
          立即购买
        </Button>
        <Button
          icon={liked ? <HeartFilled /> : <HeartOutlined />}
          size="large"
          onClick={toggleLike}
          style={{ color: liked ? '#ff4d4f' : undefined }}
        >
          {liked ? '已收藏' : '收藏'} {likeCount > 0 && `(${likeCount})`}
        </Button>
      </div>
    </Card>
  );
};

export default ProductDetail;
