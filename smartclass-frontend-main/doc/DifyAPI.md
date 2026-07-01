# Dify API 集成说明

本项目通过 Dify 平台实现 AI 智能对话功能（如“熊猫老师”）。本文档简要说明 Dify API 的核心接口及在前端中的使用方式。

## 基础配置

- **Base URL**: `http://localhost:8091/v1` (根据实际部署环境修改)
- **鉴权方式**: 在 HTTP Header 中携带 `Authorization: Bearer {API_KEY}`

## 核心接口

### 1. 发送对话消息 (`POST /chat-messages`)

用于创建会话或基于历史会话进行多轮对话。

**请求参数：**
- `query`: 用户输入内容。
- `user`: 用户唯一标识。
- `conversation_id`: （选填）会话 ID，用于延续之前的对话。
- `response_mode`: 建议使用 `streaming` 模式以实现流式输出。

**前端实现要点：**
前端使用 `@microsoft/fetch-event-source` 库处理 SSE 流式响应，实时渲染 AI 返回的文本块。

### 2. 获取会话历史消息 (`GET /messages`)

用于加载历史聊天记录，支持分页滚动加载。

**请求参数：**
- `conversation_id`: 会话 ID。
- `user`: 用户唯一标识。
- `first_id`: 当前页第一条消息的 ID（用于分页）。
- `limit`: 每次加载的消息数量，默认 20 条。

### 3. 停止响应 (`POST /chat-messages/:task_id/stop`)

在流式模式下，用户可以手动停止 AI 的输出。

## 前端集成实现

在 `src/api/aiAvatar.ts` 或相关服务文件中，我们封装了与 Dify API 的交互逻辑：

1. **流式处理**: 使用 `fetchEventSource` 监听 `message` 事件，实时更新 UI。
2. **上下文管理**: 将 `conversation_id` 存储在 Pinia Store 中，确保多轮对话的连贯性。
3. **错误处理**: 捕获 `error` 事件并向用户展示友好的提示信息。

    用户标识，由开发者定义规则，需保证用户标识在应用内唯一。

- - Name

    `last_id`

  - Type

    string

  - Description

    （选填）当前页最后面一条记录的 ID，默认 null

- - Name

    `limit`

  - Type

    int

  - Description

    （选填）一次请求返回多少条记录，默认 20 条，最大 100 条，最小 1 条。

- - Name

    `sort_by`

  - Type

    string

  - Description

    （选填）排序字段，默认 -updated_at(按更新时间倒序排列)可选值：created_at, -created_at, updated_at, -updated_at字段前面的符号代表顺序或倒序，-代表倒序

### Response

- ```
  data
  ```

  (array[object]) 会话列表

  - `id` (string) 会话 ID
  - `name` (string) 会话名称，默认为会话中用户最开始问题的截取。
  - `inputs` (object) 用户输入参数。
  - `status` (string) 会话状态
  - `introduction` (string) 开场白
  - `created_at` (timestamp) 创建时间
  - `updated_at` (timestamp) 更新时间

- `has_more` (bool)

- `limit` (int) 返回条数，若传入超过系统限制，返回系统限制数量

### Request

GET

/conversations

```
curl -X GET 'http://localhost:8091/v1/conversations?user=abc-123&last_id=&limit=20'\
--header 'Authorization: Bearer {api_key}'
```

CopyCopied!

### Response

```json
{
  "limit": 20,
  "has_more": false,
  "data": [
    {
      "id": "10799fb8-64f7-4296-bbf7-b42bfbe0ae54",
      "name": "New chat",
      "inputs": {
        "book": "book",
        "myName": "Lucy"
      },
      "status": "normal",
      "created_at": 1679667915,
      "updated_at": 1679667915
    },
    {
      "id": "hSIhXBhNe8X1d8Et"
      // ...
    }
  ]
}
```

CopyCopied!

---

DELETE/conversations/:conversation_id

## [删除会话](http://localhost:8091/app/demo/develop#delete)

删除会话。

### Path

- `conversation_id` (string) 会话 ID

### Request Body

- - Name

    `user`

  - Type

    string

  - Description

    用户标识，由开发者定义规则，需保证用户标识在应用内唯一。

### Response

- `result` (string) 固定返回 success

### Request

DELETE

/conversations/:conversation_id

```
curl -X DELETE 'http://localhost:8091/v1/conversations/:conversation_id' \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{
 "user": "abc-123"
}'
```

CopyCopied!

### Response

```json
{
  "result": "success"
}
```

CopyCopied!

---

POST/conversations/:conversation_id/name

## [会话重命名](http://localhost:8091/app/demo/develop#rename)

对会话进行重命名，会话名称用于显示在支持多会话的客户端上。

### Path

- `conversation_id` (string) 会话 ID

### Request Body

- - Name

    `name`

  - Type

    string

  - Description

    （选填）名称，若 `auto_generate` 为 `true` 时，该参数可不传。

- - Name

    `auto_generate`

  - Type

    bool

  - Description

    （选填）自动生成标题，默认 false。

- - Name

    `user`

  - Type

    string

  - Description

    用户标识，由开发者定义规则，需保证用户标识在应用内唯一。

### Response

- `id` (string) 会话 ID
- `name` (string) 会话名称
- `inputs` (object) 用户输入参数
- `status` (string) 会话状态
- `introduction` (string) 开场白
- `created_at` (timestamp) 创建时间
- `updated_at` (timestamp) 更新时间

### Request

POST

/conversations/:conversation_id/name

```
curl -X POST 'http://localhost:8091/v1/conversations/:conversation_id/name' \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{
 "name": "",
 "auto_generate": true,
 "user": "abc-123"
}'
```

CopyCopied!

### Response

```json
{
  "id": "34d511d5-56de-4f16-a997-57b379508443",
  "name": "hello",
  "inputs": {},
  "status": "normal",
  "introduction": "",
  "created_at": 1732731141,
  "updated_at": 1732734510
}
```

CopyCopied!

---

POST/audio-to-text

## [语音转文字](http://localhost:8091/app/demo/develop#audio)

### Request Body

该接口需使用 `multipart/form-data` 进行请求。

- - Name

    `file`

  - Type

    file

  - Description

    语音文件。 支持格式：`['mp3', 'mp4', 'mpeg', 'mpga', 'm4a', 'wav', 'webm']` 文件大小限制：15MB

- - Name

    `user`

  - Type

    string

  - Description

    用户标识，由开发者定义规则，需保证用户标识在应用内唯一。

### Response

- `text` (string) 输出文字

### Request

POST

/audio-to-text

```
curl -X POST 'http://localhost:8091/v1/audio-to-text' \
--header 'Authorization: Bearer {api_key}' \
--form 'file=@localfile;type=audio/[mp3|mp4|mpeg|mpga|m4a|wav|webm]
```

CopyCopied!

### Response

```json
{
  "text": "hello"
}
```

CopyCopied!

---

POST/text-to-audio

## [文字转语音](http://localhost:8091/app/demo/develop#audio)

文字转语音。

### Request Body

- - Name

    `message_id`

  - Type

    str

  - Description

    Dify 生成的文本消息，那么直接传递生成的message-id 即可，后台会通过 message_id 查找相应的内容直接合成语音信息。如果同时传 message_id 和 text，优先使用 message_id。

- - Name

    `text`

  - Type

    str

  - Description

    语音生成内容。如果没有传 message-id的话，则会使用这个字段的内容

- - Name

    `user`

  - Type

    string

  - Description

    用户标识，由开发者定义规则，需保证用户标识在应用内唯一。

### Request

POST

/text-to-audio

```
curl --location --request POST 'http://localhost:8091/v1/text-to-audio' \
--header 'Authorization: Bearer {api_key}' \
--form 'text=你好Dify;user=abc-123;message_id=5ad4cb98-f0c7-4085-b384-88c403be6290
```

CopyCopied!

### headers

```json
{
  "Content-Type": "audio/wav"
}
```

CopyCopied!

---

GET/info

## [获取应用基本信息](http://localhost:8091/app/demo/develop#info)

用于获取应用的基本信息

### Response

- `name` (string) 应用名称
- `description` (string) 应用描述
- `tags` (array[string]) 应用标签

### Request

GET

/info

```
curl -X GET 'http://localhost:8091/v1/info' \
-H 'Authorization: Bearer {api_key}'
```

CopyCopied!

### Response

```json
{
  "name": "My App",
  "description": "This is my app.",
  "tags": ["tag1", "tag2"]
}
```

CopyCopied!

---

GET/parameters

## [获取应用参数](http://localhost:8091/app/demo/develop#parameters)

用于进入页面一开始，获取功能开关、输入参数名称、类型及默认值等使用。

### Response

- `opening_statement` (string) 开场白

- `suggested_questions` (array[string]) 开场推荐问题列表

- ```
  suggested_questions_after_answer
  ```

  (object) 启用回答后给出推荐问题。

  - `enabled` (bool) 是否开启

- ```
  speech_to_text
  ```

  (object) 语音转文本

  - `enabled` (bool) 是否开启

- ```
  retriever_resource
  ```

  (object) 引用和归属

  - `enabled` (bool) 是否开启

- ```
  annotation_reply
  ```

  (object) 标记回复

  - `enabled` (bool) 是否开启

- ```
  user_input_form
  ```

  (array[object]) 用户输入表单配置

  - ```
    text-input
    ```

    (object) 文本输入控件

    - `label` (string) 控件展示标签名
    - `variable` (string) 控件 ID
    - `required` (bool) 是否必填
    - `default` (string) 默认值

  - ```
    paragraph
    ```

    (object) 段落文本输入控件

    - `label` (string) 控件展示标签名
    - `variable` (string) 控件 ID
    - `required` (bool) 是否必填
    - `default` (string) 默认值

  - ```
    select
    ```

    (object) 下拉控件

    - `label` (string) 控件展示标签名
    - `variable` (string) 控件 ID
    - `required` (bool) 是否必填
    - `default` (string) 默认值
    - `options` (array[string]) 选项值

- ```
  file_upload
  ```

  (object) 文件上传配置

  - ```
    image
    ```

    (object) 图片设置 当前仅支持图片类型：

    ```
    png
    ```

    ,

    ```
    jpg
    ```

    ,

    ```
    jpeg
    ```

    ,

    ```
    webp
    ```

    ,

    ```
    gif
    ```

    - `enabled` (bool) 是否开启
    - `number_limits` (int) 图片数量限制，默认 3
    - `transfer_methods` (array[string]) 传递方式列表，remote_url , local_file，必选一个

- ```
  system_parameters
  ```

  (object) 系统参数

  - `file_size_limit` (int) 文档上传大小限制 (MB)
  - `image_file_size_limit` (int) 图片文件上传大小限制（MB）
  - `audio_file_size_limit` (int) 音频文件上传大小限制 (MB)
  - `video_file_size_limit` (int) 视频文件上传大小限制 (MB)

### Request

GET

/parameters

```
 curl -X GET 'http://localhost:8091/v1/parameters'\
--header 'Authorization: Bearer {api_key}'
```

CopyCopied!

### Response

```json
{
  "introduction": "nice to meet you",
  "user_input_form": [
    {
      "text-input": {
        "label": "a",
        "variable": "a",
        "required": true,
        "max_length": 48,
        "default": ""
      }
    },
    {
      // ...
    }
  ],
  "file_upload": {
    "image": {
      "enabled": true,
      "number_limits": 3,
      "transfer_methods": ["remote_url", "local_file"]
    }
  },
  "system_parameters": {
    "file_size_limit": 15,
    "image_file_size_limit": 10,
    "audio_file_size_limit": 50,
    "video_file_size_limit": 100
  }
}
```

CopyCopied!

---

GET/meta

## [获取应用Meta信息](http://localhost:8091/app/demo/develop#meta)

用于获取工具icon

### Response

- ```
  tool_icons
  ```

  (object[string]) 工具图标

  - ```
    工具名称
    ```

    (string)

    - ```
      icon
      ```

      (object|string)

      - (object) 图标
        - `background` (string) hex格式的背景色
        - `content`(string) emoji
      - (string) 图标URL
