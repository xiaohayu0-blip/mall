const API_BASE = 'http://localhost:8080';

class Api {
    constructor() {
        this.token = localStorage.getItem('mall_token') || '';
        this.userName = localStorage.getItem('mall_user') || '';
    }

    get headers() {
        const h = { 'Content-Type': 'application/json' };
        if (this.token) h['token'] = this.token;
        return h;
    }

    setAuth(token, userName) {
        this.token = token;
        this.userName = userName;
        localStorage.setItem('mall_token', token);
        localStorage.setItem('mall_user', userName);
    }

    clearAuth() {
        this.token = '';
        this.userName = '';
        localStorage.removeItem('mall_token');
        localStorage.removeItem('mall_user');
    }

    get isLoggedIn() { return !!this.token; }

    async request(method, path, body) {
        const opts = { method, headers: this.headers };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(`${API_BASE}${path}`, opts);
        const json = await res.json();
        if (!json.success) throw new Error(json.message || '请求失败');
        return json.data;
    }

    async login(userName, password) {
        const url = `${API_BASE}/user/login?userName=${encodeURIComponent(userName)}&password=${encodeURIComponent(password)}`;
        const res = await fetch(url, { method: 'POST', headers: this.headers });
        const json = await res.json();
        if (!json.success) throw new Error(json.message || '登录失败');
        this.setAuth(json.data, userName);
        return json.data;
    }

    register(u, p) { return this.request('POST', '/user', { userName: u, password: p }); }

    // Commodity
    getProducts(page = 1, pageSize = 15, keyword = '', categoryId = '') {
        let url = `/commodity/page?page=${page}&pageSize=${pageSize}`;
        if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
        if (categoryId) url += `&categoryId=${categoryId}`;
        return this.request('GET', url);
    }

    createProduct(data) { return this.request('POST', '/commodity', data); }

    deleteProduct(id) { return this.request('DELETE', `/commodity/${id}`); }

    // Category
    getCategories() { return this.request('GET', '/category/list'); }

    // Tags
    getTags() { return this.request('GET', '/tags'); }

    getTagGroups() { return this.request('GET', '/tag-group'); }

    createTagGroup(name) { return this.request('POST', '/tag-group', { tagGroupName: name }); }

    createTag(data) { return this.request('POST', '/tag', data); }

    deleteTag(id) { return this.request('DELETE', `/tag/${id}`); }

    // Bind
    bindTags(commodityId, tagIds) {
        return this.request('POST', `/commodity/${commodityId}/tags/bind`, tagIds);
    }

    unbindTag(commodityId, tagId) {
        return this.request('DELETE', `/commodity/${commodityId}/tags/${tagId}/unbind`);
    }
}

const api = new Api();
