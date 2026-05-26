const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

let currentPage = 1;
let currentKeyword = '';
let currentCategory = '';
let isLoginMode = true;

// Toast
function toast(msg) {
    const el = $('#toast');
    el.textContent = msg;
    el.classList.add('show');
    setTimeout(() => el.classList.remove('show'), 2500);
}

// Auth
function checkAuth() {
    if (api.isLoggedIn) {
        $('#app-login').classList.add('hidden');
        $('#app-main').classList.remove('hidden');
        $('#user-name').textContent = api.userName;
        loadProducts();
    } else {
        $('#app-login').classList.remove('hidden');
        $('#app-main').classList.add('hidden');
    }
}

$('#auth-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const u = $('#form-username').value.trim();
    const p = $('#form-password').value.trim();
    try {
        if (isLoginMode) {
            await api.login(u, p);
        } else {
            await api.register(u, p);
            await api.login(u, p);
        }
        checkAuth();
    } catch (err) { toast(err.message); }
});

$('#switch-link').addEventListener('click', (e) => {
    e.preventDefault();
    isLoginMode = !isLoginMode;
    $('#form-submit').textContent = isLoginMode ? '登录' : '注册';
    $('#switch-link').textContent = isLoginMode ? '注册' : '登录';
});

$('#logout-btn').addEventListener('click', () => {
    api.clearAuth();
    checkAuth();
});

// Navigation
$$('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        $$('.nav-item').forEach(n => n.classList.remove('active'));
        item.classList.add('active');
        $$('.view').forEach(v => v.classList.remove('active'));
        $(`#view-${item.dataset.view}`).classList.add('active');
        if (item.dataset.view === 'products') loadProducts();
        if (item.dataset.view === 'tags') loadTagsView();
        if (item.dataset.view === 'bind') loadBindView();
    });
});

// === Products ===
async function loadProducts() {
    try {
        const data = await api.getProducts(currentPage, 15, currentKeyword, currentCategory);
        renderTable(data.records);
        renderPagination(data.totalPages, data.page);
        loadCategoryFilter();
    } catch { $('#products-table').innerHTML = '<p class="empty">加载失败</p>'; }
}

function renderTable(items) {
    if (!items || !items.length) {
        $('#products-table').innerHTML = '<p class="empty">暂无商品</p>';
        return;
    }
    const rows = items.map(p => {
        const tags = (p.tags || []).map(t => `<span class="tag-badge">${t.tagName}</span>`).join('');
        return `<tr>
            <td>${p.id}</td>
            <td><strong>${p.name}</strong></td>
            <td>&yen;${p.price}</td>
            <td>${p.categoryName || '-'}</td>
            <td>${p.stock ?? '-'}</td>
            <td class="td-tags">${tags || '-'}</td>
            <td><button class="btn-sm danger" onclick="delProduct(${p.id})">删除</button></td>
        </tr>`;
    }).join('');
    $('#products-table').innerHTML = `<table>
        <thead><tr><th>ID</th><th>名称</th><th>价格</th><th>分类</th><th>库存</th><th>标签</th><th>操作</th></tr></thead>
        <tbody>${rows}</tbody></table>`;
}

function renderPagination(total, current) {
    if (total <= 1) { $('#pagination').innerHTML = ''; return; }
    let html = '';
    for (let i = 1; i <= total; i++) {
        html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="goPage(${i})">${i}</button>`;
    }
    $('#pagination').innerHTML = html;
}

function goPage(p) { currentPage = p; loadProducts(); }

async function loadCategoryFilter() {
    try {
        const cats = await api.getCategories();
        const sel = $('#filter-category');
        if (sel.options.length <= 1) {
            cats.forEach(c => sel.add(new Option(c.name, c.id)));
        }
    } catch {}
}

$('#search-input').addEventListener('input', debounce((e) => {
    currentKeyword = e.target.value.trim();
    currentPage = 1;
    loadProducts();
}, 400));

$('#filter-category').addEventListener('change', (e) => {
    currentCategory = e.target.value;
    currentPage = 1;
    loadProducts();
});

function debounce(fn, ms) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

// Add product modal
$('#btn-add-product').addEventListener('click', async () => {
    $('#modal-overlay').classList.remove('hidden');
    const sel = $('#p-category');
    if (sel.options.length === 0) {
        try {
            const cats = await api.getCategories();
            cats.forEach(c => sel.add(new Option(c.name, c.id)));
        } catch {}
    }
});

$('#modal-close').addEventListener('click', () => $('#modal-overlay').classList.add('hidden'));
$('#modal-overlay').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) $('#modal-overlay').classList.add('hidden');
});

$('#product-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await api.createProduct({
            name: $('#p-name').value.trim(),
            price: $('#p-price').value.trim(),
            categoryId: Number($('#p-category').value),
            stock: Number($('#p-stock').value),
            description: $('#p-desc').value.trim()
        });
        toast('商品添加成功');
        $('#product-form').reset();
        $('#modal-overlay').classList.add('hidden');
        loadProducts();
    } catch (err) { toast(err.message); }
});

async function delProduct(id) {
    if (!confirm('确认删除？')) return;
    try {
        await api.deleteProduct(id);
        toast('已删除');
        loadProducts();
    } catch (err) { toast(err.message); }
}

// === Tags View ===
async function loadTagsView() {
    loadTagGroups();
    loadTagsOverview();
}

async function loadTagGroups() {
    try {
        const groups = await api.getTagGroups();
        const sel = $('#t-group');
        sel.innerHTML = '';
        groups.forEach(g => sel.add(new Option(g.tagGroupName, g.tagGroupId)));
    } catch {}
}

async function loadTagsOverview() {
    try {
        const data = await api.getTags();
        let html = '';
        for (const [group, tags] of Object.entries(data)) {
            const groupName = typeof group === 'object' ? group.tagGroupName : group;
            const items = tags.map(t => `<span class="tag-badge">${t.tagName}</span>`).join('');
            html += `<div class="tag-group-section">
                <div class="tag-group-label">${groupName}</div>
                <div class="tag-group-items">${items}</div>
            </div>`;
        }
        $('#tags-overview').innerHTML = html || '<p class="empty">暂无标签</p>';
    } catch { $('#tags-overview').innerHTML = '<p class="empty">加载失败</p>'; }
}

$('#tag-group-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await api.createTagGroup($('#tg-name').value.trim());
        toast('标签组已创建');
        $('#tg-name').value = '';
        loadTagGroups();
        loadTagsOverview();
    } catch (err) { toast(err.message); }
});

$('#tag-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await api.createTag({
            tagName: $('#t-name').value.trim(),
            tagGroupId: Number($('#t-group').value)
        });
        toast('标签已创建');
        $('#t-name').value = '';
        loadTagsOverview();
    } catch (err) { toast(err.message); }
});

// === Bind View ===
let selectedTags = new Set();

async function loadBindView() {
    selectedTags.clear();
    try {
        const [products, tagsData] = await Promise.all([
            api.getProducts(1, 100),
            api.getTags()
        ]);
        const sel = $('#b-commodity');
        sel.innerHTML = '';
        products.records.forEach(p => sel.add(new Option(p.name, p.id)));

        let chips = '';
        for (const [group, tags] of Object.entries(tagsData)) {
            tags.forEach(t => {
                chips += `<span class="chip" data-id="${t.id}" onclick="toggleChip(this)">${t.tagName}</span>`;
            });
        }
        $('#b-tags').innerHTML = chips || '<p class="empty">暂无标签</p>';
    } catch {}
}

function toggleChip(el) {
    const id = Number(el.dataset.id);
    if (selectedTags.has(id)) {
        selectedTags.delete(id);
        el.classList.remove('selected');
    } else {
        selectedTags.add(id);
        el.classList.add('selected');
    }
}

$('#bind-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const commodityId = $('#b-commodity').value;
    if (!commodityId) { toast('请选择商品'); return; }
    if (selectedTags.size === 0) { toast('请选择标签'); return; }
    try {
        const result = await api.bindTags(commodityId, [...selectedTags]);
        toast('绑定成功');
        const bound = (result.tags || []).map(t => t.tagName).join('、');
        $('#bind-result').textContent = `已绑定标签：${bound}`;
    } catch (err) { toast(err.message); }
});

// Init
checkAuth();
