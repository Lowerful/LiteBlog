# LiteBlog — 个人博客系统 · 教学项目设计文档

> 目标：通过一个体量适中、业务真实的完整项目，演示并实践标准的项目开发流程。
> 每一步迭代都对应真实工作中的一次"需求 → 设计 → 编码 → 验证"循环。

---

## 一、项目定位

**LiteBlog**：一个前后端分离的个人博客系统。

- 访客可以浏览文章、按标签筛选、查看评论
- 注册用户可以登录、发布/编辑/删除文章、发表评论、点赞
- 作者可以管理自己的文章、上传头像

选这个题材的原因：
1. 表关系丰富：用户—文章（一对多）、文章—标签（多对多）、文章—评论（一对多）、用户—点赞（去重）
2. 能自然覆盖几乎所有后端核心知识点：认证授权、事务、分页、缓存、文件上传、参数校验、异常处理
3. 体量可控：6 张表、十几个接口，既能做完整又不至于烂尾

---

## 二、技术栈

| 层次 | 技术 | 说明 |
|---|---|---|
| 语言 | Java 25 | 已有 |
| 框架 | Spring Boot 4.0.10 (Web MVC) | 已有 |
| ORM | MyBatis 4.0.1 | 已有，学习 SQL 与 ORM 的边界 |
| 数据库 | MySQL 8 | 已有驱动 |
| 认证 | JWT（jjwt）+ 拦截器 | 手写认证流程，理解原理而非黑盒 |
| 密码存储 | BCrypt (spring-security-crypto) | 只用加密模块，不引入整套 Security |
| 参数校验 | Jakarta Validation (hibernate-validator) | 声明式校验 |
| 分页 | PageHelper | MyBatis 生态标配 |
| 缓存 | Redis（第 6 步引入） | 阅读数计数、热点缓存 |
| 接口文档 | SpringDoc OpenAPI + Swagger UI | 告别手写接口文档 |
| 日志 | Logback（Spring Boot 默认）+ 自定义配置 | 分级日志、文件输出 |
| 测试 | JUnit 5 + MockMvc + Testcontainers | 单元测试 + 集成测试 |
| 构建 | Maven | 已有 |
| 前端 | Vue 3 + Vite + Axios + Element Plus（轻量使用） | 简易前端，重点在联调 |
| 部署 | Docker + Docker Compose | 一键拉起 MySQL/Redis/应用 |

---

## 三、架构设计

经典三层架构（Controller → Service → Mapper），这是国内 Java 后端的事实标准：

```
com.example.blog
├── BlogApplication.java        # 启动类
├── controller/                 # 接收请求、参数绑定、调 Service、组装响应（不写业务逻辑）
│   ├── AuthController          # 注册/登录
│   ├── UserController          # 用户信息/头像
│   ├── ArticleController       # 文章 CRUD
│   ├── CommentController       # 评论
│   └── TagController           # 标签
├── service/                    # 业务逻辑、事务边界（接口 + impl）
├── mapper/                     # MyBatis 数据访问接口（XML 写 SQL）
├── entity/                     # 与数据库表一一对应的持久化对象
├── dto/                        # 请求参数对象（request）与响应对象（response/VO）
├── common/                     # 统一返回结构 Result、分页对象、常量
├── exception/                  # 业务异常 + 全局异常处理器
├── config/                     # Web/拦截器/Redis/Swagger 等配置类
├── interceptor/                # JWT 登录拦截器
└── util/                       # JwtUtil、密码加密等工具类
```

**分层原则（贯穿全程的纪律）：**
- Controller 不碰数据库、不写 if 业务判断，只做"翻译"
- Service 是事务和业务规则的家；Service 之间尽量不互相调用形成环
- Entity 不直接出接口，出接口一律用 DTO/VO（隐藏密码字段、控制字段暴露）
- SQL 全部写在 mapper XML 里，SQL 注入只用 #{} 不用 ${}

### 请求处理流程（一个请求的一生）

```
浏览器 → Controller(参数校验) → Service(业务/事务) → Mapper(SQL) → MySQL
                          ↓ 业务失败                    ↓ 查询结果
                    抛 BusinessException ← 全局异常处理器 ← MyBatis 映射
                          ↓
              统一返回 Result{code, message, data} → JSON → 浏览器
```

---

## 四、数据库设计（6 张表）

```text
user（用户）
  id, username(唯一), password(BCrypt), nickname, avatar, created_at

article（文章）
  id, user_id(FK), title, summary, content(TEXT), status(draft/published),
  views, created_at, updated_at

tag（标签）
  id, name(唯一)

article_tag（文章-标签 多对多中间表）
  article_id(FK), tag_id(FK)   联合主键

comment（评论）
  id, article_id(FK), user_id(FK), content, created_at

article_like（点赞，唯一约束防重复）
  article_id(FK), user_id(FK), created_at   联合主键
```

设计要点（届时逐一讲解）：
- 主键用 `BIGINT AUTO_INCREMENT`，为什么不用 UUID
- 每张表的索引怎么建：外键列、唯一列、列表查询的排序列
- `created_at/updated_at` 统一由数据库维护（`DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE`）
- 点赞表用联合主键从数据库层面防重复，而不是只靠代码判断
- 字符集 `utf8mb4`（支持 emoji）

---

## 五、API 设计概览（RESTful）

| 模块 | 接口 | 方法 | 说明 |
|---|---|---|---|
| 认证 | /api/auth/register | POST | 注册 |
| 认证 | /api/auth/login | POST | 登录，返回 JWT |
| 用户 | /api/users/me | GET | 当前用户信息 |
| 用户 | /api/users/me/avatar | POST | 上传头像 |
| 文章 | /api/articles | GET | 分页列表（keyword/tag/作者筛选） |
| 文章 | /api/articles/{id} | GET | 详情（阅读数 +1） |
| 文章 | /api/articles | POST | 发布（需登录） |
| 文章 | /api/articles/{id} | PUT | 编辑（仅作者） |
| 文章 | /api/articles/{id} | DELETE | 删除（仅作者） |
| 评论 | /api/articles/{id}/comments | GET/POST | 列表/发表 |
| 点赞 | /api/articles/{id}/like | POST/DELETE | 点赞/取消 |
| 标签 | /api/tags | GET | 标签列表 |

统一返回结构：

```json
{ "code": 0, "message": "ok", "data": { ... } }
```

错误码约定：0 成功；4xx 客户端问题（401 未登录、403 无权限、404 不存在、400 参数错误）；500 服务器异常。

---

## 六、迭代路线图（从零到完整）

> 每一步 = 一个小迭代：先讲"为什么"，再写代码，再验证，最后 git commit 留存。
> 顺序刻意安排为"先骨架、再纵切、后横切"：先搭好骨架让请求能跑通，再按业务模块纵切实现，最后加认证之外的横切能力。

### 第 0 步 · 需求与设计（本文档）
产出：需求清单、架构、数据库设计、API 设计。真实项目中这一步决定了后面返工多少。

### 第 1 步 · 工程骨架与规范
- 规划包结构，把包名从 demo 重构为 blog
- 统一返回结构 `Result<T>` + 错误码枚举
- 全局异常处理器（@RestControllerAdvice）+ 自定义 BusinessException
- Jakarta Validation 参数校验
- ⚠️ 顺带学：Git 提交规范（feat/fix/docs 前缀）、为什么统一返回结构

### 第 2 步 · 数据库落库
- 建库建表 SQL（schema.sql）、初始化脚本管理
- MyBatis 接入：配置数据源、写第一个 mapper（user 表）
- ⚠️ 顺带学：HikariCP 连接池、#{} 与 ${} 的区别（SQL 注入）、日志里看真实 SQL

### 第 3 步 · 用户模块：注册与登录（项目第一个完整纵切）
- 注册：用户名唯一校验、BCrypt 密码加密、事务
- 登录：密码校验、签发 JWT（jjwt）
- JWT 拦截器：白名单、ThreadLocal 存当前用户
- 接口：/api/auth/register、/api/auth/login、/api/users/me
- ⚠️ 顺带学：为什么密码不能明文/MD5、JWT 原理与无状态认证、拦截器 vs 过滤器

### 第 4 步 · 文章模块（核心 CRUD + 分页）
- 发布/编辑/删除文章，"仅作者可改"的权限校验（403 场景）
- 分页列表（PageHelper）：keyword 模糊搜索、按标签/作者筛选
- 文章详情：阅读数自增（UPDATE ... SET views = views + 1，为什么不用先查再改）
- ⚠️ 顺带学：RESTful 动词语义、401/403/404 的区分、深分页问题初识

### 第 5 步 · 评论与标签（关联关系与事务）
- 评论：一对多查询、发表评论、删除自己的评论
- 标签：多对多（article_tag）、发布文章时打标签、按标签聚合查询
- ⚠️ 顺带学：事务边界放 Service 层的原因、@Transactional 失效场景、N+1 查询问题

### 第 6 步 · 点赞与 Redis 缓存
- 点赞/取消：联合主键防重、点赞数统计
- 引入 Redis：文章阅读数先写 Redis 再异步落库、热点文章列表缓存
- ⚠️ 顺带学：缓存与数据库一致性、缓存穿透/击穿/雪崩（概念 + 本项目的简单对策）

### 第 7 步 · 文件上传
- 头像上传：大小/类型限制、存储路径规划、静态资源映射
- ⚠️ 顺带学：multipart 原理、文件名安全（重命名）、生产环境为什么用对象存储

### 第 8 步 · 接口文档与测试
- SpringDoc 接入，给所有接口补文档注解
- Service 层单元测试、MockMvc 接口测试、Testcontainers 起真实 MySQL 跑集成测试
- ⚠️ 顺带学：测试金字塔、mock 的适用边界

### 第 9 步 · 简易前端（Vue 3）
- Vite 初始化 frontend/ 工程，Axios 封装（拦截器带 token、统一错误提示）
- 页面：登录注册页、文章列表页、文章详情+评论、写作页
- 跨域配置（CORS）、前后端联调排错
- ⚠️ 顺带学：跨域是什么、token 存哪里（localStorage vs Cookie 的取舍）

### 第 10 步 · 部署与收尾
- logback 日志配置：分级、滚动文件输出
- 多环境配置（application-dev/prod.yaml）、打包
- Dockerfile + docker-compose（MySQL + Redis + 应用一键启动）
- 项目复盘：回顾全流程、遗留优化项（如消息队列、单元化部署可以怎么演进）

---

## 七、贯穿全程的工程习惯（每次迭代刻意练习）

1. **Git 纪律**：每完成一个小迭代就 commit，message 遵循约定式提交
2. **先设计后编码**：动手前先说清楚"改哪些文件、为什么"
3. **验证闭环**：每步做完必须用 Swagger/curl/前端实际验证通过，再进下一步
4. **问题清单**：遇到"为什么这么做"的问题随时记，阶段末统一复盘
5. **代码即文档**：注释写"为什么"而不是"是什么"

---

## 八、进度记录

| 日期 | 步骤 | 状态 |
|---|---|---|
| 2026-11-22 | 第 0 步 设计文档 | ✅ 完成 |
| 2026-11-22 | 第 1 步 工程骨架与规范 | ✅ 完成（编译+单测通过，git 首笔提交；启动自检与数据库配置一起在第 2 步做） |
