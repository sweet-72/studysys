# 作业与练习管理模块分析

## 1. 模块定位

从当前项目代码来看，作业与练习管理并不是一个完全独立的教学闭环模块，而是嵌入在课程学习流程中的一组数据采集与结果记录能力。

其核心作用主要包括：

- 为课程小节配置练习题
- 记录学生客观题作答结果
- 记录学生主观题提交内容
- 为教师提供作业审核与评分入口
- 为学习过程与课程学习助手提供部分过程数据

需要明确的是，当前代码更偏向“练习与作业数据的采集、查询和审核管理”，并未体现完整的教学反馈闭环。

## 2. 相关代码结构

结合当前代码，作业与练习相关能力主要分布在以下对象中：

- `CourseController`：提供题目新增、删除、分页查看、作业批改等课程侧管理接口
- `CourseLearningController`：在学生学习流程中处理题目作答与主观题提交
- `HomeworkSubmissionController`：提供作业提交、个人作业列表、未批改作业列表等接口
- `CourseHomeworkSubmissionController`：提供教师/管理员侧作业审核分页与详情接口
- `ExerciseController`：提供按小节获取练习题与提交练习的简化入口
- `HomeworkSubmissionServiceImpl`：负责主观题提交、审核、分页查询与数据转换
- `CourseServiceImpl`：负责客观题提交、自动判题与小节完成判定

从结构上看，练习与作业能力分别附着在课程管理、课程学习和作业提交三个子链路上，而不是单一控制器独占。

## 3. 核心实体与数据结构

### 3.1 题目实体

`Question` 是练习与作业配置的基础实体，核心字段包括：

- `sectionId`：所属小节
- `type`：题目类型
- `content`：题目内容
- `options`：选项
- `answer`：标准答案
- `explanation`：答案解析
- `score`：分值
- `difficulty`：难度
- `sortOrder`：排序

从字段设计看，当前题目既可用于客观题自动判分，也可作为主观题作业题目使用。

### 3.2 客观题作答记录

`UserAnswer` 用于记录学生对题目的客观作答结果，主要字段包括：

- `userId`
- `questionId`
- `userAnswer`
- `isCorrect`
- `score`
- `timeSpent`

这类数据主要用于自动判题和学习过程中的答题结果沉淀。

### 3.3 主观题提交记录

`HomeworkSubmission` 用于记录主观题作业提交信息，主要字段包括：

- `userId`
- `sectionId`
- `questionId`
- `answer`
- `status`
- `score`
- `feedback`
- `gradedBy`
- `gradedTime`

这说明当前系统对主观题采用“提交记录 + 批改状态”模式进行管理。

## 4. 练习题管理能力

从 `CourseController` 可以看出，教师或管理员可以对课程小节下的题目进行管理，当前已实现的能力包括：

- `/course/homework/add`：新增题目
- `/course/homework/delete/{questionId}`：删除题目
- `/course/homework/list/page/vo`：按小节分页获取题目列表
- `/course/{sectionId}/questions` 或 `/course/section/questions`：获取小节题目

这些接口的实现说明，当前代码中“作业/练习”的基础管理对象实际上是 `Question`。

也就是说，系统当前首先完成的是题目级资源配置能力。

## 5. 学生作答流程

在学生端，当前代码区分了客观题与主观题两种处理方式。

### 5.1 客观题

当学生通过 `CourseLearningController.submitAnswer()` 提交客观题时，系统会：

1. 校验题目是否属于当前小节
2. 校验用户是否允许学习该小节
3. 调用 `CourseServiceImpl.submitAnswer()`
4. 自动判定答案是否正确
5. 写入 `UserAnswer`
6. 返回正确性、得分、标准答案与解析

这说明客观题链路已经具备“提交即判分”的能力。

### 5.2 主观题

当学生提交主观题时，系统不会自动评分，而是：

1. 组装 `HomeworkSubmitRequest`
2. 调用 `HomeworkSubmissionServiceImpl.submitHomework()`
3. 写入或更新 `HomeworkSubmission`
4. 默认设置为待批改状态
5. 返回提交成功及待审核信息

因此，当前主观题链路的重点是提交记录保存，而不是即时反馈。

## 6. 教师审核与批改能力

教师和管理员侧当前已实现以下能力：

- 查看未批改作业列表
- 分页查看作业审核列表
- 查看单条提交详情
- 对提交进行评分与填写评语

具体对应的接口包括：

- `/homework/ungraded/list`
- `/homework/grade`
- `/course/homework/submission/list/page/vo`
- `/course/homework/submission/get/vo`
- `/course/homework/submission/review`
- `/course/review`

从 `HomeworkSubmissionServiceImpl` 的实现看，教师批改后会更新 `status`、`score`、`feedback`、`gradedBy` 和 `gradedTime` 等字段。

## 7. 学生侧查询能力

当前代码中，学生侧已实现以下查询能力：

- `/homework/exercise/{sectionId}`：查看某小节题目及个人提交情况
- `/homework/my/list`：分页查看个人作业提交列表

此外，`HomeworkSubmissionVO` 中已经包含 `status`、`score`、`feedback`、`gradedByName` 等字段，说明学生侧查询结果中可以携带批改后数据。

但需要明确的是，当前代码中未体现“教师批改结果主动下发学生端”的能力，也未体现作业批改后的消息通知机制。

换言之，当前以数据查询为主，反馈机制仍待扩展。

## 8. 与学习过程管理的关系

作业与练习管理模块并不是独立存在，而是直接参与学习过程推进。

从 `CourseServiceImpl` 可见：

- 客观题作答结果会参与小节完成判定
- 主观题提交行为也会触发 `tryCompleteSection(...)`

这说明当前系统中，练习与作业数据不仅用于结果记录，也参与学习状态流转。

因此，该模块与学习过程管理之间是“结果数据 + 状态推进”的关系，而不是纯展示关系。

## 9. 与 AI 调用链路的关系

当前代码中，作业与练习数据并非完全独立于 AI。

在 `LearningContextAssemblerImpl` 中，系统会调用：

- `homeworkSubmissionService.getSectionExercises(...)`
- `homeworkSubmissionService.getMyHomeworkList(...)`

并将结果加工为 `recentHomeworkSummary`，同时结合题目与错题数据生成 `recentQuestionSummary` 和 `wrongQuestionSummary`。

因此，可以明确写出：

- 当前作业与练习数据已部分接入课程学习助手的 AI 上下文
- 当前接入方式以“摘要信息”形式为主
- 当前代码中未体现教师评语、批改详情被深度注入 AI 上下文

也就是说，当前 AI 使用的是作业与练习的概览性数据，而不是完整的批改反馈链路。

## 10. 当前实现边界

严格基于当前代码，可以明确以下边界：

- 已实现题目配置、客观题自动判分、主观题提交、教师审核、学生查询
- 已实现作业和题目摘要进入课程学习助手上下文
- 当前代码中未体现教师批改结果主动下发学生端
- 当前代码中未体现作业批改后的消息通知机制
- 当前代码中未体现完整的教学反馈闭环

因此，本模块当前更适合被描述为“作业与练习数据管理模块”，而不是“完整作业教学闭环模块”。

## 11. 论文写作建议

在论文写作中，本专题建议重点突出以下关键词：

- 题目配置
- 客观题自动判分
- 主观题提交与审核
- 作业数据沉淀
- 作业摘要接入 AI 上下文

同时需要明确说明：当前系统以数据采集和审核管理为主，反馈下发机制尚未实现。

## 12. 模块分析结论

综合来看，当前项目中的作业与练习管理模块已经实现了题目配置、学生作答、主观题提交、教师审核和结果查询等基础能力，并且部分作业与练习摘要数据已进入课程学习助手的 AI 上下文链路。

但严格基于现有代码，当前实现仍以数据采集与审核管理为主，尚未体现教师批改结果主动下发学生端，也未形成完整的教学反馈闭环。因此，在论文中更适合将其概括为：**系统面向课程学习过程构建的作业与练习数据管理模块，用于支撑题目管理、学生提交、教师审核以及学习过程与 AI 辅助所需的基础数据沉淀。**
