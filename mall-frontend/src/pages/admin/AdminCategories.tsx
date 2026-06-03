import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Space, Popconfirm, message, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { categoryApi } from '../../api';

const { Title } = Typography;

const AdminCategories: React.FC = () => {
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();

  const loadCategories = async () => {
    setLoading(true);
    try {
      const res: any = await categoryApi.list();
      setCategories(res.data || []);
    } catch { setCategories([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { loadCategories(); }, []);

  const handleSubmit = async () => {
    const values = await form.validateFields();
    try {
      if (editingId) {
        await categoryApi.update(editingId, { name: values.name, description: values.description });
        message.success('更新成功');
      } else {
        await categoryApi.create(values.name);
        message.success('添加成功');
      }
      setModalOpen(false);
      form.resetFields();
      setEditingId(null);
      loadCategories();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    }
  };

  const handleEdit = (record: any) => {
    setEditingId(record.id);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await categoryApi.delete(id);
      message.success('删除成功');
      loadCategories();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '分类名称', dataIndex: 'name', key: 'name' },
    { title: '描述', dataIndex: 'description', key: 'description', render: (v: string) => v || '-' },
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
        <Title level={4} style={{ margin: 0 }}>分类管理</Title>
        <Button type="primary" icon={<PlusOutlined />}
          onClick={() => { setEditingId(null); form.resetFields(); setModalOpen(true); }}>
          添加分类
        </Button>
      </div>
      <Table dataSource={categories} columns={columns} rowKey="id" loading={loading} pagination={false} />
      <Modal
        title={editingId ? '编辑分类' : '添加分类'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => { setModalOpen(false); form.resetFields(); setEditingId(null); }}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="分类名称" rules={[{ required: true }]}>
            <Input placeholder="请输入分类名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="选填" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCategories;
