import axios from 'axios';

const API_BASE = 'http://localhost:8081';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器：自动注入 Token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mall_token');
  if (token) {
    config.headers.token = token;
  }
  return config;
});

// 响应拦截器：统一处理错误
api.interceptors.response.use(
  (response) => {
    const data = response.data;
    if (data.success === false) {
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('mall_token');
      localStorage.removeItem('mall_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ========== 用户 ==========
export const userApi = {
  login: (userName: string, password: string) =>
    api.post(`/user/login?userName=${encodeURIComponent(userName)}&password=${encodeURIComponent(password)}`),
  register: (userName: string, password: string) =>
    api.post('/user', { userName, password }),
};

// ========== 商品 ==========
export const productApi = {
  list: (params: { page?: number; pageSize?: number; keyword?: string; categoryId?: number }) =>
    api.get('/commodity/page', { params }),
  getById: (id: number) => api.get(`/commodity/${id}`),
  create: (data: any) => api.post('/commodity', data),
  update: (id: number, data: any) => api.put(`/commodity/${id}`, null, { params: data }),
  delete: (id: number) => api.delete(`/commodity/${id}`),
  toggleStatus: (id: number, status: number) => api.put(`/commodity/${id}/status`, null, { params: { status } }),
  queryByTags: (tagIds: number[], page = 1, pageSize = 10) =>
    api.get('/commodities/by-tags', { params: { tagIds: tagIds.join(','), page, pageSize } }),
};

// ========== 分类 ==========
export const categoryApi = {
  list: () => api.get('/category/list'),
  create: (name: string) => api.post('/category', { name }),
  update: (id: number, data: any) => api.put(`/category/${id}`, null, { params: data }),
  delete: (id: number) => api.delete(`/category/${id}`),
};

// ========== 标签 ==========
export const tagApi = {
  getAll: () => api.get('/tags'),
  getGroups: () => api.get('/tag-group'),
  createGroup: (name: string) => api.post('/tag-group', { tagGroupName: name }),
  create: (data: any) => api.post('/tag', data),
  delete: (id: number) => api.delete(`/tag/${id}`),
  getByGroup: (groupId: number) => api.get(`/tags/group/${groupId}`),
};

// ========== 商品标签绑定 ==========
export const commodityTagApi = {
  bind: (commodityId: number, tagIds: number[]) =>
    api.post(`/commodity/${commodityId}/tags/bind`, tagIds),
  unbind: (commodityId: number, tagId: number) =>
    api.delete(`/commodity/${commodityId}/tags/${tagId}/unbind`),
};

// ========== 购物车 ==========
export const cartApi = {
  add: (commodityId: number, quantity = 1) =>
    api.post('/cart/add', null, { params: { commodityId, quantity } }),
  update: (commodityId: number, quantity: number) =>
    api.put('/cart/update', null, { params: { commodityId, quantity } }),
  remove: (commodityId: number) =>
    api.delete(`/cart/remove/${commodityId}`),
  list: () => api.get('/cart/list'),
  count: () => api.get('/cart/count'),
  clear: () => api.delete('/cart/clear'),
  selected: () => api.get('/cart/selected'),
};

// ========== 订单 ==========
export const orderApi = {
  create: (params: { receiverName: string; receiverPhone: string; receiverAddress: string; remark?: string }) =>
    api.post('/orders/create', null, { params }),
  myOrders: (params: { page?: number; pageSize?: number; status?: string }) =>
    api.get('/orders/my', { params }),
  detail: (orderId: number) => api.get(`/orders/${orderId}`),
  cancel: (orderId: number) => api.post(`/orders/${orderId}/cancel`),
  complete: (orderId: number) => api.post(`/orders/${orderId}/complete`),
  // Admin
  adminList: (params: { page?: number; pageSize?: number; status?: string }) =>
    api.get('/orders/admin/list', { params }),
  ship: (orderId: number) => api.post(`/orders/admin/${orderId}/ship`),
};

// ========== 支付 ==========
export const paymentApi = {
  mockPay: (orderNo: string) => api.post('/payment/mock', null, { params: { orderNo } }),
};

// ========== 点赞 ==========
export const likesApi = {
  add: (businessId: number, itemId: number) =>
    api.post('/likes', { businessId, itemId }),
  myLikes: (businessId: number) => api.get(`/likes/${businessId}`),
  count: (businessId: number, itemId: number) =>
    api.get('/likes/count', { params: { businessId, itemId } }),
};

export default api;
