# LiteBlog

一个前后端分离的个人博客系统，同时作为**教学项目**：完整演示从需求设计到部署上线的标准开发流程。

支持访客浏览/搜索文章，注册用户发布文章、打标签、评论、点赞。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 4.0 · Java 25 · MyBatis · MySQL 8 |
| 认证 | JWT + BCrypt（手写拦截器，不引入整套 Security） |
| 基础设施 | Redis · PageHelper · SpringDoc |
| 前端 | Vue 3 · Vite · Axios（第 9 步引入） |
| 工程 | Maven · JUnit 5 · Docker |

## 目录结构

```
src/main/java/com/example/blog
├── common/       统一返回结构、错误码
├── exception/    业务异常、全局异常处理器
├── controller/   接口层
├── service/      业务逻辑与事务
├── mapper/       数据访问（MyBatis）
├── entity/       数据库实体
├── dto/          请求/响应对象
├── config/       配置类
└── interceptor/  登录拦截器
```

## 文档

- [项目设计文档](docs/PROJECT_PLAN.md)：架构、数据库设计、API 设计、迭代路线图

## 本地运行

> 当前进度：第 1 步（工程骨架）已完成，数据源尚未配置，应用暂不可启动。

```bash
# 编译 + 跑测试
mvn test

# 启动（第 2 步完成数据源配置后可运行）
mvn spring-boot:run
```

启动后访问健康检查：<http://localhost:8080/api/health>

## 分支与提交约定

- 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/)：`feat` / `fix` / `docs` / `refactor` / `test` / `chore`
- `main` 保持随时可运行，功能开发在 `feat/*` 分支进行
