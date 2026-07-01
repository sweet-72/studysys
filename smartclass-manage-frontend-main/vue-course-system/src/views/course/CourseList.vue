<template>
  <div class="course-list-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="课程名称">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入课程名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已下架" value="UNLISTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryParams.difficulty" placeholder="请选择难度" clearable>
            <el-option label="初级" value="BEGINNER" />
            <el-option label="中级" value="INTERMEDIATE" />
            <el-option label="高级" value="ADVANCED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleCreate" v-if="isAdmin">
            <el-icon><Plus /></el-icon>
            新建课程
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表表格 -->
    <el-card class="table-card">
      <el-table
        :data="courseList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="课程 ID" width="80" />
        <el-table-column prop="title" label="课程标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="课程类型" width="120" />
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)">
              {{ getDifficultyLabel(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="instructor" label="讲师" width="120" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)" v-if="isAdmin">编辑</el-button>
            <el-button
              link
              type="success"
              @click="handlePublish(row)"
              v-if="isAdmin && row.status === 'DRAFT'"
            >
              发布
            </el-button>
            <el-button
              link
              type="warning"
              @click="handleUnlist(row)"
              v-if="isAdmin && row.status === 'PUBLISHED'"
            >
              下架
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-if="isAdmin">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新建/编辑课程对话框 -->
    <CourseForm
      v-model:visible="formVisible"
      :course-data="currentCourse"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import CourseForm from './CourseForm.vue'
import { useCourseStore } from '@/stores/course'
import { useUserStore } from '@/stores/user'
import type { CourseQueryParams, Course, CourseStatus, CourseDifficulty } from '@/types/course'

const courseStore = useCourseStore()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const formVisible = ref(false)
const currentCourse = ref<Course | null>(null)

const queryParams = reactive<CourseQueryParams>({
  page: 1,
  size: 10,
  title: undefined,
  status: undefined,
  difficulty: undefined
})

const courseList = computed(() => courseStore.courseList)
const total = computed(() => courseStore.total)

// 获取课程列表
async function fetchData() {
  loading.value = true
  try {
    await courseStore.fetchCourseList(queryParams)
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  queryParams.page = 1
  fetchData()
}

// 重置
function handleReset() {
  queryParams.page = 1
  queryParams.title = undefined
  queryParams.status = undefined
  queryParams.difficulty = undefined
  fetchData()
}

// 新建课程
function handleCreate() {
  currentCourse.value = null
  formVisible.value = true
}

// 编辑课程
function handleEdit(row: Course) {
  currentCourse.value = { ...row }
  formVisible.value = true
}

// 查看课程
function handleView(row: Course) {
  ElMessage.info('查看功能待实现')
}

// 发布课程
async function handlePublish(row: Course) {
  try {
    await ElMessageBox.confirm(`确定要发布课程"${row.title}"吗？`, '确认发布', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用发布接口
    ElMessage.success('发布成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
    }
  }
}

// 下架课程
async function handleUnlist(row: Course) {
  try {
    await ElMessageBox.confirm(`确定要下架课程"${row.title}"吗？`, '确认下架', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用下架接口
    ElMessage.success('下架成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

// 删除课程
async function handleDelete(row: Course) {
  try {
    await ElMessageBox.confirm(`确定要删除课程"${row.title}"吗？此操作不可恢复！`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    await courseStore.removeCourse(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 表单提交成功回调
function handleFormSuccess() {
  formVisible.value = false
  ElMessage.success('操作成功')
  fetchData()
}

// 难度标签类型
function getDifficultyTagType(difficulty: CourseDifficulty) {
  const map = {
    BEGINNER: 'success',
    INTERMEDIATE: 'warning',
    ADVANCED: 'danger'
  }
  return map[difficulty] || 'info'
}

// 难度标签文本
function getDifficultyLabel(difficulty: CourseDifficulty) {
  const map = {
    BEGINNER: '初级',
    INTERMEDIATE: '中级',
    ADVANCED: '高级'
  }
  return map[difficulty] || difficulty
}

// 状态标签类型
function getStatusTagType(status: CourseStatus) {
  const map = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    UNLISTED: 'danger'
  }
  return map[status] || 'info'
}

// 状态标签文本
function getStatusLabel(status: CourseStatus) {
  const map = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    UNLISTED: '已下架'
  }
  return map[status] || status
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.course-list-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
