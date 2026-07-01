<template>
  <el-dialog
    v-model="visible"
    :title="courseData?.id ? '编辑课程' : '新建课程'"
    width="900px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="top"
    >
      <!-- 基本信息 -->
      <el-card class="section-card">
        <template #header>
          <span class="section-title">基本信息</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="课程标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入课程标题" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择课程类型" style="width: 100%">
                <el-option label="编程开发" value="PROGRAMMING" />
                <el-option label="产品设计" value="PRODUCT" />
                <el-option label="数据分析" value="DATA" />
                <el-option label="人工智能" value="AI" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="formData.difficulty" placeholder="请选择难度" style="width: 100%">
                <el-option label="初级" value="BEGINNER" />
                <el-option label="中级" value="INTERMEDIATE" />
                <el-option label="高级" value="ADVANCED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="讲师" prop="instructor">
              <el-input v-model="formData.instructor" placeholder="请输入讲师姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="课程描述" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="4"
                placeholder="请输入课程描述"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 章节小节编辑 -->
      <el-card class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">章节与小节</span>
            <el-button type="primary" @click="addChapter">
              <el-icon><Plus /></el-icon>
              添加章节
            </el-button>
          </div>
        </template>

        <div v-if="formData.chapters.length === 0" class="empty-chapter-tip">
          <el-empty description="还没有章节，请点击右上角添加章节" />
        </div>

        <div v-else class="chapter-list">
          <draggable
            v-model="formData.chapters"
            item-key="order"
            handle=".chapter-drag-handle"
            animation="300"
          >
            <template #item="{ element: chapter, index: chapterIndex }">
              <div class="chapter-item">
                <div class="chapter-header">
                  <div class="chapter-title-wrapper">
                    <el-icon class="chapter-drag-handle"><Rank /></el-icon>
                    <el-input
                      v-model="chapter.title"
                      placeholder="输入章节标题"
                      style="width: 300px"
                      maxlength="100"
                    />
                    <span class="chapter-order">第{{ chapterIndex + 1 }}章</span>
                  </div>
                  <div class="chapter-actions">
                    <el-button type="success" size="small" @click="addSection(chapter)">
                      <el-icon><Plus /></el-icon>
                      添加小节
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="removeChapter(chapterIndex)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除章节
                    </el-button>
                  </div>
                </div>

                <!-- 小节列表 -->
                <div class="section-list">
                  <div
                    v-for="(section, sectionIndex) in chapter.sections"
                    :key="section.order"
                    class="section-item"
                  >
                    <div class="section-content">
                      <div class="section-header-row">
                        <el-icon class="section-drag-handle"><Drag /></el-icon>
                        <el-input
                          v-model="section.title"
                          placeholder="输入小节标题"
                          style="width: 250px"
                          maxlength="100"
                        />
                        <span class="section-order">第{{ sectionIndex + 1 }}节</span>
                      </div>

                      <!-- 视频选择 -->
                      <div class="video-selector">
                        <el-radio-group v-model="section.videoType" size="small">
                          <el-radio-button value="URL">URL 视频</el-radio-button>
                          <el-radio-button value="LOCAL">本地视频</el-radio-button>
                        </el-radio-group>

                        <el-input
                          v-if="section.videoType === 'URL'"
                          v-model="section.videoUrl"
                          placeholder="请输入视频 URL 地址"
                          style="width: 400px; margin-left: 10px"
                        />

                        <el-upload
                          v-else
                          ref="uploadRef"
                          drag
                          :auto-upload="true"
                          :show-file-list="false"
                          accept="video/*"
                          :before-upload="handleBeforeUpload"
                          :http-request="(file: any) => handleVideoUpload(file, section)"
                          style="display: inline-block; margin-left: 10px"
                        >
                          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                          <div class="el-upload__text">
                            点击或拖拽视频到此处上传
                          </div>
                          <template #tip>
                            <div class="el-upload__tip">
                              支持 mp4/webm 等格式，不超过 500MB
                            </div>
                          </template>
                        </el-upload>

                        <!-- 上传进度 -->
                        <div v-if="section.uploading" class="upload-progress">
                          <el-progress
                            :percentage="section.uploadProgress || 0"
                            :status="section.uploadProgress === 100 ? 'success' : undefined"
                          />
                        </div>
                      </div>

                      <!-- 课后习题 -->
                      <div class="exercise-section">
                        <div class="exercise-header">
                          <span class="exercise-title">课后习题</span>
                          <el-button
                            link
                            type="primary"
                            size="small"
                            @click="addExercise(section)"
                          >
                            <el-icon><Plus /></el-icon>
                            添加习题
                          </el-button>
                        </div>

                        <div v-if="section.exercises && section.exercises.length > 0" class="exercise-list">
                          <div
                            v-for="(exercise, exIndex) in section.exercises"
                            :key="exIndex"
                            class="exercise-item"
                          >
                            <ExerciseEditor
                              v-model="section.exercises[exIndex]"
                              :index="exIndex"
                              @remove="removeExercise(section, exIndex)"
                            />
                          </div>
                        </div>
                      </div>
                    </div>

                    <div class="section-actions">
                      <el-button
                        link
                        type="danger"
                        size="small"
                        @click="removeSection(chapter, sectionIndex)"
                      >
                        <el-icon><Delete /></el-icon>
                        删除小节
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </el-card>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, defineModel } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete, Rank, Drag, UploadFilled } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import ExerciseEditor from './ExerciseEditor.vue'
import { uploadVideo } from '@/api/upload'
import type { Course, Chapter, Section, Exercise, VideoType } from '@/types/course'

interface Props {
  courseData?: Course | null
}

const props = withDefaults(defineProps<Props>(), {
  courseData: null
})

const visible = defineModel<boolean>('visible', { required: true })

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 空的小节模板
function createEmptySection(): Section {
  return {
    title: '',
    videoType: 'URL',
    videoUrl: '',
    order: 0,
    exercises: []
  }
}

// 空的章节模板
function createEmptyChapter(): Chapter {
  return {
    title: '',
    order: 0,
    sections: [createEmptySection()]
  }
}

// 表单数据
const formData = ref<Course>({
  title: '',
  type: 'PROGRAMMING',
  difficulty: 'BEGINNER',
  status: 'DRAFT',
  instructor: '',
  description: '',
  chapters: []
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  instructor: [
    { required: true, message: '请输入讲师姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 监听课程数据变化
watch(
  () => props.courseData,
  (newData) => {
    if (newData) {
      formData.value = JSON.parse(JSON.stringify(newData))
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// 重置表单
function resetForm() {
  formData.value = {
    title: '',
    type: 'PROGRAMMING',
    difficulty: 'BEGINNER',
    status: 'DRAFT',
    instructor: '',
    description: '',
    chapters: []
  }
  formRef.value?.clearValidate()
}

// 关闭对话框
function handleClosed() {
  resetForm()
}

// 取消
function handleCancel() {
  visible.value = false
}

// 添加章节
function addChapter() {
  formData.value.chapters.push(createEmptyChapter())
}

// 删除章节
function removeChapter(index: number) {
  formData.value.chapters.splice(index, 1)
}

// 添加小节
function addSection(chapter: Chapter) {
  chapter.sections.push(createEmptySection())
}

// 删除小节
function removeSection(chapter: Chapter, index: number) {
  chapter.sections.splice(index, 1)
}

// 添加习题
function addExercise(section: Section) {
  if (!section.exercises) {
    section.exercises = []
  }
  section.exercises.push({
    questionContent: '',
    type: 'SINGLE_CHOICE',
    correctAnswer: ''
  })
}

// 删除习题
function removeExercise(section: Section, index: number) {
  if (section.exercises) {
    section.exercises.splice(index, 1)
  }
}

// 上传前校验
function handleBeforeUpload(file: File) {
  const isVideo = file.type.startsWith('video/')
  const isLt500MB = file.size / 1024 / 1024 < 500

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt500MB) {
    ElMessage.error('视频大小不能超过 500MB!')
    return false
  }
  return true
}

// 处理视频上传
async function handleVideoUpload(file: any, section: Section) {
  section.uploading = true
  section.uploadProgress = 0

  try {
    const res = await uploadVideo(file.file, (percent) => {
      section.uploadProgress = percent
    })

    section.videoFileId = res.data.fileId
    section.uploading = false
    ElMessage.success('上传成功')
  } catch (error) {
    section.uploading = false
    ElMessage.error('上传失败')
    throw error
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 校验章节和小节
    if (!validateChapters()) {
      return
    }

    submitLoading.value = true

    try {
      // TODO: 调用保存接口
      // if (formData.value.id) {
      //   await updateCourse(formData.value.id, formData.value)
      // } else {
      //   await createCourse(formData.value)
      // }

      ElMessage.success('保存成功')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 校验章节和小节
function validateChapters(): boolean {
  if (formData.value.chapters.length === 0) {
    ElMessage.warning('请至少添加一个章节')
    return false
  }

  for (let i = 0; i < formData.value.chapters.length; i++) {
    const chapter = formData.value.chapters[i]
    
    if (!chapter.title.trim()) {
      ElMessage.warning(`第${i + 1}章的章节标题不能为空`)
      return false
    }

    if (chapter.sections.length === 0) {
      ElMessage.warning(`第${i + 1}章至少需要一个小节`)
      return false
    }

    for (let j = 0; j < chapter.sections.length; j++) {
      const section = chapter.sections[j]

      if (!section.title.trim()) {
        ElMessage.warning(`第${i + 1}章第${j + 1}节的小节标题不能为空`)
        return false
      }

      if (section.videoType === 'URL' && !section.videoUrl?.trim()) {
        ElMessage.warning(`第${i + 1}章第${j + 1}节的视频 URL 不能为空`)
        return false
      }

      if (section.videoType === 'LOCAL' && !section.videoFileId) {
        ElMessage.warning(`第${i + 1}章第${j + 1}节请先上传本地视频`)
        return false
      }
    }
  }

  return true
}

const emit = defineEmits<{
  success: []
}>()
</script>

<style scoped lang="less">
.section-card {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .section-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.empty-chapter-tip {
  padding: 40px 0;
}

.chapter-list {
  .chapter-item {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    margin-bottom: 20px;
    background-color: #fafafa;

    .chapter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background-color: #f5f7fa;
      border-bottom: 1px solid #dcdfe6;

      .chapter-title-wrapper {
        display: flex;
        align-items: center;
        gap: 10px;

        .chapter-drag-handle {
          cursor: move;
          font-size: 18px;
          color: #909399;
        }

        .chapter-order {
          color: #909399;
          font-size: 14px;
        }
      }

      .chapter-actions {
        display: flex;
        gap: 10px;
      }
    }

    .section-list {
      padding: 15px;

      .section-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 15px;
        margin-bottom: 15px;
        background-color: #fff;
        border: 1px solid #e4e7ed;
        border-radius: 4px;

        .section-content {
          flex: 1;

          .section-header-row {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 15px;

            .section-drag-handle {
              cursor: move;
              font-size: 16px;
              color: #909399;
            }

            .section-order {
              color: #909399;
              font-size: 13px;
            }
          }

          .video-selector {
            margin-bottom: 15px;

            .upload-progress {
              margin-top: 10px;
              width: 400px;
            }
          }

          .exercise-section {
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px dashed #dcdfe6;

            .exercise-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 10px;

              .exercise-title {
                font-weight: bold;
                color: #606266;
              }
            }

            .exercise-item {
              margin-bottom: 10px;
            }
          }
        }

        .section-actions {
          margin-left: 15px;
        }
      }
    }
  }
}
</style>
