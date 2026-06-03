import React from 'react';
import { Layout, Menu, Badge, Dropdown, Avatar } from 'antd';
import {
  ShoppingCartOutlined, AppstoreOutlined, OrderedListOutlined,
  ShoppingOutlined, UserOutlined, LogoutOutlined, SettingOutlined,
  HeartOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { cartApi } from '../api';

const { Header, Sider, Content } = Layout;

const UserLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { userName, role, clearAuth } = useAuthStore();
  const [cartCount, setCartCount] = React.useState(0);

  React.useEffect(() => {
    cartApi.count().then((res: any) => setCartCount(res.data || 0)).catch(() => {});
  }, [location.pathname]);

  const menuItems = [
    { key: '/', icon: <AppstoreOutlined />, label: '商品列表' },
    { key: '/favorites', icon: <HeartOutlined />, label: '我的收藏' },
    { key: '/cart', icon: <Badge count={cartCount} size="small"><ShoppingCartOutlined style={{ fontSize: 16 }} /></Badge>, label: '购物车' },
    { key: '/orders', icon: <OrderedListOutlined />, label: '我的订单' },
  ];

  const logout = () => {
    clearAuth();
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="light" width={220} style={{ borderRight: '1px solid #f0f0f0' }}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 20, borderBottom: '1px solid #f0f0f0' }}>
          <ShoppingOutlined style={{ marginRight: 8 }} /> Mall
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', borderBottom: '1px solid #f0f0f0' }}>
          <Dropdown menu={{
            items: [
              { key: 'profile', icon: <UserOutlined />, label: userName },
              ...(role === 'ADMIN' ? [{ key: 'admin', icon: <SettingOutlined />, label: '管理后台' }] : []),
              { type: 'divider' },
              { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
            ],
            onClick: ({ key }) => {
              if (key === 'logout') logout();
              if (key === 'admin') navigate('/admin');
            },
          }}>
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} size="small" />
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

export default UserLayout;
