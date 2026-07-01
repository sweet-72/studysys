# 系统技术路线

## 1. 后端技术栈

根据 `pom.xml`、`application.yml` 和源码结构，系统后端采用如下技术路线：

- **核心框架**：Spring Boot 2.7.2
- **开发语言与运行环境**：Java 17
- **数据访问层**：MyBatis Spring Boot Starter + MyBatis-Plus
- **缓存与会话**：Redis + Spring Session Data Redis
- **搜索能力**：Spring Data Elasticsearch
- **接口文档**：Knife4j OpenAPI2
- **AI 接口通信**：OkHttp
- **对象存储**：腾讯云 COS SDK
- **实时通信**：SSE + Netty WebSocket
- **微信接入**：wx-java-mp-spring-boot-starter
- **辅助工具**：Hutool、EasyExcel、FastJSON、Netty

整体上，这是一个典型的 Spring Boot 单体后端架构，在单体中承载多个业务域模块。

## 2. 数据库与缓存方案

### 2.1 数据库方案

系统数据库采用 MySQL，SQL 初始化脚本位于 `sql/smart_class.sql`。从脚本与实体映射可以看出，数据库包含以下几类核心表：

- 用户与权限：`user`、`teacher`
- 课程与学习：`course`、`chapter`、`section`、`question`、`homework_submission`、`user_progress`、`video_learning_record`、`user_course`
- AI 能力：`ai_avatar`、`ai_avatar_chat_history`、`subject_ai_binding`、`course_knowledge_sync`、`ai_learning_context_snapshot`
- 日常学习内容：`daily_article`、`daily_word`、`user_daily_word`、`user_word_book`
- 交互与社交：`post`、`post_comment`、`friend_request`、`friend_relationship`、`chat_message`、`chat_session`
- 平台管理：`announcement`、`user_announcement_reader`、`user_feedback`、`user_feedback_reply`
- 成长体系：`achievement`、`user_achievement`、`level_config`、`user_level`、`user_exp_record`

### 2.2 缓存与会话方案

Redis 在系统中承担多重职责：

- 存储 Spring Session，实现登录态持久化。
- 缓存课程目录、学习上下文、等级信息等热点数据。
- 维护聊天未读数、在线状态、离线消息和私聊会话索引。
- 作为课程知识同步的分布式锁载体，避免重复同步。
- 作为 JWT 黑名单存储，支持令牌注销。

因此，Redis 在本系统中不是可有可无的性能优化项，而是会话、实时消息和 AI 辅助链路的重要基础设施。

## 3. AI 能力接入方案

### 3.1 Dify 对话能力接入

系统通过 `DifyServiceImpl` 调用 Dify 对话接口，核心路径包括：

- `/chat-messages`：普通聊天与流式聊天
- `/chat-messages/{taskId}/stop`：停止流式响应
- `/chat-messages/summarize`：生成会话摘要
- `/conversations/{conversationId}`：删除 Dify 侧对话

系统不仅保存本地 `session_id`，还通过 `dify_conversation_id` 将本地会话与 Dify 会话绑定，实现多轮上下文连续。

### 3.2 Dify 工作流接入

课程场景学习助手并非简单调用聊天接口，而是通过 `DifyServiceImpl.handleWorkflowRunRequest` 和 `handleWorkflowStreamRequest` 调用 Dify 工作流接口 `/workflows/run`，并向工作流传入：

- 课程信息
- 章节与小节信息
- 学习进度
- 视频进度
- 近期作业情况
- 小节题目与错题摘要
- 学生当前学习目标

### 3.3 Dify 知识库接入

`DifyKnowledgeServiceImpl` 和 `CourseKnowledgeSyncServiceImpl` 负责将课程介绍、章节、小节、题目与资料同步到 Dify 数据集，支持文本同步和文件同步，并把同步状态记录在 `course_knowledge_sync` 表中。

## 4. 前后端交互方式

从控制器结构和跨域配置可以判断，该项目采用前后端分离模式，后端主要提供以下交互方式：

- **RESTful JSON 接口**：绝大多数业务接口均通过 Controller 提供。
- **SSE**：用于 AI 流式回复以及部分即时通知。
- **Netty WebSocket**：用于私聊、在线状态和更强实时性的通信场景。
- **文件上传接口**：通过 Multipart 上传头像、视频、文档和课程资料。
- **Postman 文档生成接口**：通过 `PostmanDocController` 动态导出接口文档。

需要特别说明的是：当前仓库主要是后端项目，未包含完整前端工程代码，因此论文中对前端页面实现的描述应以“接口驱动的前后端分离架构”表述为主。

## 5. 系统整体架构

系统采用单体应用中的分层架构，可概括为：

- **表示层**：Controller
- **业务层**：Service / ServiceImpl
- **数据访问层**：Mapper + MyBatis XML
- **模型层**：Entity、DTO、VO、Enum
- **基础设施层**：Redis、Elasticsearch、COS、Netty、Dify、JWT、SSE

## 6. 各层职责划分

### 6.1 Controller 层

- 暴露 REST 接口，例如 `CourseController`、`AiAvatarChatController`、`DailyArticleController`
- 统一返回 `BaseResponse<T>`
- 获取当前登录用户
- 在部分接口上使用 `@AuthCheck` 做角色级权限控制
- 对流式接口直接返回 `SseEmitter`

### 6.2 Service 层

- 负责课程学习规则校验
- 负责学习上下文组装
- 负责 AI 工作流与聊天请求编排
- 负责文件与知识库同步
- 负责用户成长、聊天未读和公告阅读等跨实体逻辑

### 6.3 Mapper 层

- 负责实体与数据库交互
- 提供 MyBatis-Plus 通用 CRUD
- 承载个别自定义 SQL 更新

### 6.4 Entity / DTO / VO 层

- **Entity**：映射数据库表
- **DTO**：封装请求参数
- **VO**：封装展示结果

## 7. 部署与运行方式

从 `Dockerfile` 和 `docker-compose.yml` 可知，系统支持容器化部署：

- 应用容器：Spring Boot 后端
- MySQL 容器：业务数据存储
- Redis 容器：缓存与会话
- Elasticsearch 容器：全文检索

同时，系统使用 `application.yml`、`application-test.yml`、`application-prod.yml` 实现环境隔离，支持本地测试和生产部署。

## 8. 技术路线总结

总体而言，本系统采用“Spring Boot 单体业务系统 + Redis 缓存会话 + Elasticsearch 检索 + COS 文件存储 + Dify AI 能力 + SSE/WebSocket 实时通信”的复合技术路线。该路线既能满足毕业设计“可落地、可运行”的要求，也具备一定的工程性和扩展性，适合在论文中作为“AI 赋能教学平台”的实现基础进行论述。
