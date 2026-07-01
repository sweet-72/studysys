// 课程相关类型定义

/**
 * 课程难度枚举
 */
export type CourseDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

/**
 * 课程状态枚举
 */
export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'UNLISTED'

/**
 * 视频类型枚举
 */
export type VideoType = 'URL' | 'LOCAL'

/**
 * 习题类型枚举
 */
export type ExerciseType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'SHORT_ANSWER'

/**
 * 用户角色枚举
 */
export type UserRole = 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'

/**
 * 选项接口
 */
export interface ExerciseOption {
  key: string // 选项标识 A/B/C/D
  content: string // 选项内容
}

/**
 * 习题接口
 */
export interface Exercise {
  id?: number // 习题 ID，新建时没有
  questionContent: string // 题目内容
  type: ExerciseType // 题型
  options?: ExerciseOption[] // 选项（选择题需要）
  correctAnswer: string | string[] // 正确答案
  sectionId?: number // 所属小节 ID
}

/**
 * 小节接口
 */
export interface Section {
  id?: number
  title: string // 小节标题
  videoType?: VideoType // 视频类型
  videoUrl?: string // 视频 URL
  videoFileId?: number // 本地视频文件 ID
  order: number // 排序
  exercises?: Exercise[] // 课后习题
}

/**
 * 章节接口
 */
export interface Chapter {
  id?: number
  title: string // 章节标题
  order: number // 排序
  sections: Section[] // 包含的小节
}

/**
 * 课程接口
 */
export interface Course {
  id?: number
  title: string // 课程标题
  type: string // 课程类型
  difficulty: CourseDifficulty // 难度
  status: CourseStatus // 状态
  instructor: string // 讲师
  rating?: number // 评分
  description?: string // 课程描述
  chapters: Chapter[] // 章节列表
  createTime?: string
  updateTime?: string
}

/**
 * 课程查询参数
 */
export interface CourseQueryParams {
  page: number
  size: number
  title?: string // 课程名搜索
  status?: CourseStatus // 状态筛选
  difficulty?: CourseDifficulty // 难度筛选
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
}

/**
 * 学生作业提交
 */
export interface Submission {
  id: number
  studentId: number
  studentName: string
  courseId: number
  courseTitle: string
  sectionId: number
  sectionTitle: string
  exerciseId: number
  exerciseContent: string
  studentAnswer: string // 学生答案
  score?: number // 得分
  comment?: string // 评语
  status: 'PENDING' | 'REVIEWED' // 批改状态
  submitTime: string
}

/**
 * 作业批改参数
 */
export interface ReviewParams {
  submissionId: number
  score: number
  comment: string
}
