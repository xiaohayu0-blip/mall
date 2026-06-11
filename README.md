# Mall 电商平台

基于 Spring Boot 的 B2C 电商平台，覆盖商品管理、购物车、订单、支付、标签分类等核心电商链路。集成 Redis 缓存、RabbitMQ 异步消息、布隆过滤器缓存穿透防护、分布式锁库存扣减等中间件能力，支持 Docker Compose 一键部署。

---

## 技术栈

**后端：** Java 17 · Spring Boot 4.x · Spring Data JPA · MySQL 8.4 · Redis · RabbitMQ · Redisson · Flyway · Springdoc OpenAPI

**前端：** React 19 · TypeScript · Vite · Ant Design · React Router

**部署：** Docker · Docker Compose · Nginx

## 功能结构

### 用户端
- 注册 / 登录（JWT 鉴权，密码加盐 MD5 加密）
- 商品浏览（标签筛选、全文搜索、分页查询）
- 购物车管理（Redis Hash 存储，商品选中/取消选中）
- 下单结算（分布式锁防超卖）
- 支付模拟（原子状态更新，防重复支付）
- 订单管理（查看、取消）
- 商品点赞（RabbitMQ 异步削峰）

### 管理端
- 商品管理（CRUD、绑定标签）
- 订单管理（发货、完成）
- 标签管理（标签组 + 标签树）
- 分类管理

## 架构亮点

### 缓存与性能
- 基于 **Redis Hash** 实现购物车，读写 O(1)
- 基于 **布隆过滤器** 拦截不存在商品/用户的查询请求，防止缓存穿透
- 商品热度数据缓存，减少数据库查询

### 并发安全
- 基于 **Redisson 分布式锁** 实现库存扣减，按 commodityId 粒度加锁，避免死锁
- **支付幂等性**：使用原子 SQL（`UPDATE ... WHERE status='PENDING_PAYMENT'`）防止重复支付

### 异步解耦
- 点赞操作通过 **RabbitMQ** 异步写入数据库，失败消息进入死信队列
- 下单与库存扣减通过分布式锁保证数据一致性

### 质量保障
- **Flyway** 数据库版本管理
- **GitHub Actions** CI：push/PR 自动 Maven 构建 + 运行测试
- 单元测试覆盖 JWT 工具类、用户服务、购物车服务

## 快速开始

### 前提条件

- Docker & Docker Compose
- 宿主机 MySQL 8.4（需提前创建 `mall` 数据库）

### 1. 配置环境变量

```bash
cd mall/
cp .env.example .env
```

编辑 `.env`，填写数据库密码和 JWT 密钥：

```properties
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=your-256-bit-secret-key
```

### 2. 一键启动

```bash
docker-compose up --build
```

启动后访问：
- 前台页面：**http://localhost**
- 管理后台：**http://localhost/admin**（需管理员账号登录）
- Swagger 文档：**http://localhost:8082/swagger-ui.html**

### 3. 创建管理员账号

注册普通用户后，在数据库中执行：

```sql
UPDATE users SET role = 'ADMIN' WHERE username = '你的用户名';
```

## API 概览

### 用户模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 用户登录 |

### 商品模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/commodity/{id}` | 商品详情 |
| GET | `/api/commodity/search` | 全文搜索 |
| GET | `/api/commodity/by-tags` | 按标签筛选 |
| POST | `/api/commodity` | 新增商品（管理员） |
| PUT | `/api/commodity/{id}` | 更新商品（管理员） |
| DELETE | `/api/commodity/{id}` | 删除商品（管理员） |

### 购物车
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart` | 获取购物车列表 |
| POST | `/api/cart/add` | 添加商品 |
| PUT | `/api/cart/update` | 更新数量 |
| DELETE | `/api/cart/remove` | 删除商品 |
| PUT | `/api/cart/toggle-select` | 切换选中状态 |

### 订单
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/order/create` | 创建订单 |
| GET | `/api/order/list` | 订单列表 |
| GET | `/api/order/{orderNo}` | 订单详情 |
| POST | `/api/order/{orderNo}/cancel` | 取消订单 |
| POST | `/api/payment/mock` | 模拟支付 |

### 点赞
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/likes` | 点赞/取消点赞 |
| GET | `/api/likes/count/{commodityId}` | 查看商品点赞数 |
| GET | `/api/likes/user/{userId}/liked` | 查看用户点赞列表 |

## 项目结构

```
mall/
├── src/main/java/com/gym/mall/
│   ├── config/             # Redis、RabbitMQ、布隆过滤器配置
│   ├── controller/         # REST 接口层
│   ├── consumer/           # RabbitMQ 消息消费者
│   ├── converter/          # DTO <-> Entity 转换器
│   ├── domain/
│   │   ├── dto/            # 数据传输对象
│   │   └── entity/         # JPA 实体
│   ├── exception/          # 全局异常处理
│   ├── interceptor/        # JWT 拦截器 + 权限注解
│   ├── Repository/         # Spring Data JPA 数据访问
│   ├── service/            # 业务逻辑层
│   ├── utils/              # JWT、雪花ID 工具类
│   └── validator/          # 统一响应封装
├── mall-frontend/          # React 前端项目
├── src/test/               # 单元测试 + 集成测试
├── Dockerfile              # 后端 Docker 镜像（多阶段构建）
├── docker-compose.yml      # 容器编排
└── .github/workflows/      # GitHub Actions CI
```

## 开发环境（非 Docker）

```bash
# 后端
cd mall/
mvn spring-boot:run

# 前端
cd mall/mall-frontend/
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`，API 请求自动代理到后端 `http://localhost:8082`。
