import React, { useState, useEffect } from 'react';
import { Card, Button, Form, Input, Select, Tag, message, Typography, Row, Col } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { tagApi } from '../../api';

const { Title } = Typography;

const AdminTags: React.FC = () => {
  const [tagGroups, setTagGroups] = useState<any[]>([]);
  const [tagsData, setTagsData] = useState<Record<string, any[]>>({});
  const [groupForm] = Form.useForm();
  const [tagForm] = Form.useForm();

  const loadData = async () => {
    try {
      const [groupsRes, tagsRes] = await Promise.all([
        tagApi.getGroups(),
        tagApi.getAll(),
      ]);
      setTagGroups(groupsRes.data || []);
      setTagsData(tagsRes.data || {});
    } catch { }
  };

  useEffect(() => { loadData(); }, []);

  const createGroup = async (values: any) => {
    try {
      await tagApi.createGroup(values.tagGroupName);
      message.success('标签组已创建');
      groupForm.resetFields();
      loadData();
    } catch (err: any) {
      message.error(err.message || '创建失败');
    }
  };

  const createTag = async (values: any) => {
    try {
      await tagApi.create({ tagName: values.tagName, tagGroupId: Number(values.tagGroupId) });
      message.success('标签已创建');
      tagForm.resetFields();
      loadData();
    } catch (err: any) {
      message.error(err.message || '创建失败');
    }
  };

  const deleteTag = async (id: number) => {
    try {
      await tagApi.delete(id);
      message.success('已删除');
      loadData();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    }
  };

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>标签管理</Title>
      <Row gutter={24}>
        <Col span={8}>
          <Card title="新建标签组" size="small" style={{ marginBottom: 16 }}>
            <Form form={groupForm} onFinish={createGroup} layout="inline">
              <Form.Item name="tagGroupName" rules={[{ required: true }]} style={{ flex: 1 }}>
                <Input placeholder="标签组名称" />
              </Form.Item>
              <Button type="primary" htmlType="submit" icon={<PlusOutlined />}>创建</Button>
            </Form>
          </Card>
          <Card title="新建标签" size="small">
            <Form form={tagForm} onFinish={createTag} layout="vertical">
              <Form.Item name="tagGroupId" label="所属标签组" rules={[{ required: true }]}>
                <Select
                  placeholder="选择标签组"
                  options={tagGroups.map((g: any) => ({ label: g.tagGroupName, value: g.tagGroupId }))}
                />
              </Form.Item>
              <Form.Item name="tagName" label="标签名称" rules={[{ required: true }]}>
                <Input placeholder="标签名称" />
              </Form.Item>
              <Button type="primary" htmlType="submit" icon={<PlusOutlined />} block>创建标签</Button>
            </Form>
          </Card>
        </Col>
        <Col span={16}>
          <Card title="全部标签">
            {Object.entries(tagsData).map(([groupName, tags]: [string, any]) => (
              <div key={groupName} style={{ marginBottom: 16 }}>
                <Title level={5} style={{ marginBottom: 8 }}>{typeof tags === 'object' && 'tagGroupName' in tags ? '' : groupName}</Title>
                <div>
                  {(tags as any[]).map((tag: any) => (
                    <Tag
                      key={tag.id}
                      closable
                      onClose={() => deleteTag(tag.id)}
                      style={{ marginBottom: 4, padding: '2px 8px', fontSize: 13 }}
                    >
                      {tag.tagName}
                    </Tag>
                  ))}
                </div>
              </div>
            ))}
            {Object.keys(tagsData).length === 0 && <span style={{ color: '#888' }}>暂无标签</span>}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AdminTags;
