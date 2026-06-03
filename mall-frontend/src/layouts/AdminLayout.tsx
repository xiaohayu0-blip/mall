import React from 'react';
import { Layout, Menu, Dropdown, Avatar } from 'antd';
import {
  ShopOutlined, OrderedListOutlined, TagsOutlined, FolderOutlined,
  UserOutlined, LogoutOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';

const { Header, Sider, Content } = Layout;

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { userName, clearAuth } = useAuthStore();

  const menuItems = [
    { key: '/admin/products', icon: <ShopOutlined />, label: '商品管理' },
    { key: '/admin/categories', icon: <FolderOutlined />, label: '分类管理' },
    { key: '/admin/orders', icon: <OrderedListOutlined />, label: '订单管理' },
    { key: '/admin/tags', icon: <TagsOutlined />, label: '标签管理' },
  ];

  const logout = () => {
    clearAuth();
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={220}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 700, fontSize: 18 }}>
          Mall 管理后台
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', borderBottom: '1px solid #f0f0f0' }}>
          <span style={{ marginRight: 16, color: '#888' }}>管理员</span>
          <Dropdown menu={{
            items: [
              { key: 'profile', icon: <UserOutlined />, label: userName },
              { type: 'divider' },
              { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
            ],
            onClick: ({ key }) => key === 'logout' && logout(),
          }}>
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} size="small" style={{ backgroundColor: '#1890ff' }} />
              <span>{userName}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ padding: 24, background: '#f5f5f5' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
