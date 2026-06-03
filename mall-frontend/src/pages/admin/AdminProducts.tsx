import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, Tag, Popconfirm, message, Switch, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { productApi, categoryApi, tagApi, commodityTagApi } from '../../api';

const { Title } = Typography;

const AdminProducts: React.FC = () => {
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [allTags, setAllTags] = useState<any[]>([]);
  const [currentTags, setCurrentTags] = useState<number[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const res: any = await productApi.list({ page, pageSize: 15 });
      setProducts(res.data?.records || []);
      setTotal(res.data?.total || 0);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    loadProducts();
    categoryApi.list().then((res: any) => setCategories(res.data || [])).catch(() => {});
    // 加载所有标签
    loadAllTags();
  }, [page]);

  const loadAllTags = async () => {
    try {
      const res: any = await tagApi.getAll();
      const tags: any[] = [];
      Object.values(res.data || {}).forEach((groupTags: any) => {
        tags.push(...groupTags);
      });
      setAllTags(tags);
    } catch {}
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    try {
      if (editingId) {
        await productApi.update(editingId, values);
        // 绑定标签
        await commodityTagApi.bind(editingId, currentTags);
        message.success('更新成功');
      } else {
        const res: any = await productApi.create(values);
        if (currentTags.length > 0) {
          await commodityTagApi.bind(res.data, currentTags);
        }
        message.success('添加成功');
      }
      setModalOpen(false);
      form.resetFields();
      setCurrentTags([]);
      setEditingId(null);
      loadProducts();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const handleEdit = (record: any) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    // 设置当前标签
    const tagIds = (record.tags || []).map((t: any) => t.tagId || t.id);
    setCurrentTags(tagIds);
    setModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await productApi.delete(id);
      message.success('删除成功');
      loadProducts();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    }
  };

  const toggleStatus = async (id: number, currentStatus: number) => {
    try {
      await productApi.toggleStatus(id, currentStatus === 1 ? 0 : 1);
      message.success(currentStatus === 1 ? '已下架' : '已上架');
      loadProducts();
    } catch (err: any) {
      message.error(err.message);
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '名称', dataIndex: 'name', key: 'name' },
    {
      title: '价格', dataIndex: 'price', key: 'price', width: 100,
      render: (p: string) => <span style={{ color: '#ff4d4f', fontWeight: 600 }}>¥{p}</span>,
    },
    {
      title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 100,
      render: (v: string) => v || '-',
    },
    { title: '库存', dataIndex: 'stock', key: 'stock', width: 80 },
    {
      title: '状态', dataIndex: 'status', key: 'status', width: 100,
      render: (s: number, record: any) => (
        <Switch
          checked={s === 1}
          checkedChildren="上架"
          unCheckedChildren="下架"
          onChange={() => toggleStatus(record.id, s)}
        />
      ),
    },
    {
      title: '标签', key: 'tags', width: 200,
      render: (_: any, record: any) => (
        (record.tags || []).map((t: any) => (
          <Tag key={t.tagName} color="blue">{t.tagName}</Tag>
        ))
      ),
    },
    {
      title: '操作', key: 'action', width: 120,
      render: (_: any, record: any) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>商品管理</Title>
        <Button type="primary" icon={<PlusOutlined />}
          onClick={() => { setEditingId(null); form.resetFields(); setModalOpen(true); }}>
          添加商品
        </Button>
      </div>

      <Table
        dataSource={products}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page, pageSize: 15, total,
          onChange: setPage, showTotal: (t) => `共 ${t} 条`,
        }}
      />

      <Modal
        title={editingId ? '编辑商品' : '添加商品'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => { setModalOpen(false); form.resetFields(); setEditingId(null); setCurrentTags([]); }}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="商品名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Space style={{ width: '100%' }} size={16}>
            <Form.Item name="price" label="价格" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input placeholder="例如: 99.99" />
            </Form.Item>
            <Form.Item name="stock" label="库存" rules={[{ required: true }]} style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </Space>
          <Form.Item name="categoryId" label="分类">
            <Select
              allowClear
              placeholder="请选择分类"
              options={categories.map((c: any) => ({ label: c.name, value: c.id }))}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item label="标签">
            <Select
              mode="multiple"
              placeholder="选择标签"
              value={currentTags}
              onChange={(val: number[]) => setCurrentTags(val)}
              options={allTags.map((t: any) => ({ label: t.tagName, value: t.id }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminProducts;
