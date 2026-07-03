# SmartClass 智慧学习系统

SmartClass 是一个面向在线学习、AI 辅导与学习社区的前后端分离毕业设计项目。系统由 Spring Boot 后端、Vue3 用户端、React/Ant Design Pro 管理端、MySQL 数据库以及 Redis、Elasticsearch、Dify、对象存储等能力组成，覆盖课程学习、每日学习、AI 学习助手、社区互动、好友聊天、学习成长、后台运营管理等场景。

## 项目介绍

SmartClass 的定位是“课程学习 + AI 辅导 + 学习社区”的综合学习平台。用户端提供首页推荐、AI 学习空间、课程中心、学习圈子、个人中心、每日单词、每日美文、好友聊天、AI 智能体对话等功能；管理端提供用户、教师、课程、分类、章节、小节、作业、AI 智能体、每日单词、美文、帖子、反馈、数据看板等后台管理能力。

项目特色包括：

- AI 学习助手：基于 AI Avatar 与 Dify 工作流提供通用智能体聊天、课程学习助手与流式问答。
- 每日学习：每日单词、每日美文、收藏、生词本、学习记录与目标管理。
- 课程学习：课程分类、推荐课程、章节小节、视频学习、作业提交、课程收藏、评价与进度记录。
- 社区交流：学习圈子支持帖子、搜索、点赞、收藏、评论与回复。
- 成长体系：等级、经验、成就、学习天数、连续打卡、每日目标等学习激励能力。
- 管理运营：后台可维护课程、教师、用户、AI 智能体、每日内容、帖子与用户反馈。

## 技术架构

### 后端

- Java 17
- Spring Boot 2.7.2
- MyBatis / MyBatis-Plus
- Spring Session Redis
- Spring Security Crypto
- Knife4j / Swagger OpenAPI
- Tencent COS SDK
- EasyExcel
- Netty WebSocket
- OkHttp、FastJSON、Hutool

### 用户端

- Vue 3
- Vue Router 4
- Pinia
- Vant 4
- Axios
- Rsbuild
- Markdown 渲染、KaTeX、Shiki、QRCode、jsQR
- Capacitor Android 适配

### 管理端

- React 18
- Umi Max
- Ant Design 5
- Ant Design Pro Components
- ECharts
- OpenAPI TypeScript 代码生成

### 基础设施

- MySQL：核心业务数据存储。
- Redis：登录态、Session 与缓存能力。
- Elasticsearch：项目已接入 Spring Data Elasticsearch，用于搜索能力扩展。
- Dify：AI 聊天、课程助手与知识库问答工作流。
- JWT / Token：前端请求支持 Bearer Token 形态，后端结合登录态与权限校验使用。
- OSS / COS：文件、头像、视频、资料等对象上传。

## 项目结构

```text
studysys/
├── README.md
├── learning-system/                 # Spring Boot 后端
│   ├── src/main/java/com/example/smartclass/
│   ├── src/main/resources/
│   ├── sql/
│   └── doc/
├── smartclass-frontend-main/         # Vue3 用户端
│   ├── src/pages/
│   ├── src/components/
│   ├── src/api/
│   └── src/router/
└── smartclass-manage-frontend-main/  # React 管理端
    ├── src/pages/
    ├── src/services/
    ├── src/api/
    └── config/routes.ts
```

完整目录说明见 [learning-system/doc/11-项目目录说明.md](learning-system/doc/11-项目目录说明.md)。

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8.x
- Redis 6.x+
- Elasticsearch 7.x/8.x 可用实例
- Dify 服务，默认配置地址为 `http://localhost:8091/v1`
- 腾讯云 COS 或兼容对象存储服务

## 项目启动

### 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE smart_class DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 按使用场景导入 SQL：

```bash
mysql -uroot -p smart_class < learning-system/sql/smart_class_clean.sql
```

开发演示需要较完整业务数据时可导入 `smart_class.sql`。

### 后端启动

后端配置文件位于 `learning-system/src/main/resources/application.yml`，默认端口为 `8101`，接口上下文为 `/api`。

```bash
cd learning-system
mvn spring-boot:run
```

启动后后端地址为：

```text
http://localhost:8101/api
```

### 用户端启动

用户端开发服务默认端口为 `8001`，`/api` 代理到 `http://localhost:8101`。

```bash
cd smartclass-frontend-main
npm install
npm run dev
```

### 管理端启动

管理端开发服务默认使用 `8000` 端口。

```bash
cd smartclass-manage-frontend-main
npm install
npm run start:dev
```

### 开发环境数据重置

如果只需要清理业务过程数据并恢复基础账号，可使用：

```bash
mysql -uroot -p smart_class < learning-system/sql/clear_business_data.sql
```

默认管理员：

- 账号：`mudong`
- 密码：`mudong520`

该账号用于开发环境登录管理端和验证后台权限。

## SQL 说明

- `smart_class.sql`：完整数据库初始化脚本，包含表结构、索引、基础数据和较完整的开发演示业务数据，适合联调和功能展示。
- `smart_class_clean.sql`：干净初始化脚本，包含最新表结构和必要基础数据，适合重新搭建环境。
- `clear_business_data.sql`：开发环境业务数据清理脚本，用于清空学习、互动、记录类数据并恢复基础用户和管理账号。

## 系统功能

- 用户模块：注册、账号登录、手机号登录、微信登录入口、个人资料、头像、搜索用户。
- 认证模块：登录态获取、退出登录、用户权限、管理端访问控制。
- 首页模块：课程推荐、热门课程、AI 助手、每日单词、每日美文、公告与快捷入口。
- AI 模块：AI Avatar 管理、用户智能体、会话、历史消息、流式回复、课程助手、语音识别。
- 课程模块：课程、分类、教师、章节、小节、视频、资料、作业、评价、收藏、浏览记录、进度。
- 每日学习模块：每日单词、美文、点赞、收藏、生词本、学习记录。
- 社区模块：帖子、评论、回复、点赞、收藏、搜索、个人主页。
- 好友聊天模块：好友申请、好友关系、私聊会话、消息已读、未读统计。
- 成长模块：经验、等级、成就、每日目标、学习统计。
- 管理模块：用户、教师、班级、课程、AI 智能体、每日内容、帖子、反馈、数据看板。

## 项目亮点

- AI Avatar 支持多智能体角色、对话历史、用户收藏与使用。
- Dify 工作流支持普通 AI 对话与课程上下文问答。
- 课程学习链路包含开始学习、完成小节、作业提交、视频进度保存、学习记录生成。
- 用户端首页聚合课程、AI、每日内容与公告，形成学习入口。
- 管理端基于 Umi Max + Ant Design Pro，适合后台数据运营。
- SQL 脚本区分完整演示数据、干净初始化和业务数据重置，便于毕业设计演示。

## 文档

项目文档已按当前代码重新整理，入口见：

- [文档总览](learning-system/doc/README.md)
- [项目概述](learning-system/doc/01-项目概述.md)
- [系统架构设计](learning-system/doc/02-系统架构设计.md)
- [数据库设计](learning-system/doc/03-数据库设计.md)
- [后端设计](learning-system/doc/04-后端设计.md)
- [用户端设计](learning-system/doc/05-用户端设计.md)
- [管理端设计](learning-system/doc/06-管理端设计.md)
- [AI模块设计](learning-system/doc/07-AI模块设计.md)
- [接口说明](learning-system/doc/08-接口说明.md)
- [业务流程说明](learning-system/doc/12-业务流程说明.md)
- [系统功能模块说明](learning-system/doc/15-系统功能模块说明.md)
