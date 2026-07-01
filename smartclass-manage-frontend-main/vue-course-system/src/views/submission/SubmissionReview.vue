<template>
  <div class="submission-review-container">
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待批改" value="PENDING" />
            <el-option label="已批改" value="REVIEWED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 作业列表 -->
    <el-card class="table-card">
      <el-table
        :data="submissionList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="提交 ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="courseTitle" label="课程" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sectionTitle" label="小节" min-width="150" show-overflow-tooltip />
        <el-table-column prop="exerciseContent" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="studentAnswer" label="学生答案" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="得分" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.score !== undefined" :type="getScoreTagType(row.score)">
              {{ row.score }}
            </el-tag>
            <span v-else style="color: #999">未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
              {{ row.status === 'PENDING' ? '待批改' : '已批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleReview(row)"
              v-if="row.status === 'PENDING'"
            >
              批改
            </el-button>
            <el-button link type="info" @click="handleViewDetail(row)">查看</el-button>
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

    <!-- 批改对话框 -->
    <el-dialog
      v-model="reviewVisible"
      title="批改作业"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentSubmission" class="review-content">
        <!-- 作业信息 -->
        <el-descriptions title="作业信息" :column="2" border>
          <el-descriptions-item label="学生">{{ currentSubmission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ currentSubmission.courseTitle }}</el-descriptions-item>
          <el-descriptions-item label="小节">{{ currentSubmission.sectionTitle }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.submitTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 题目内容 -->
        <div class="question-section">
          <h4>题目内容</h4>
          <p>{{ currentSubmission.exerciseContent }}</p>
        </div>

        <!-- 学生答案 -->
        <div class="answer-section">
          <h4>学生答案</h4>
          <p>{{ currentSubmission.studentAnswer }}</p>
        </div>

        <!-- 批改表单 -->
        <el-form
          ref="reviewFormRef"
          :model="reviewForm"
          :rules="reviewRules"
          label-width="100px"
          style="margin-top: 20px"
        >
          <el-form-item label="得分" prop="score">
            <el-input-number
              v-model="reviewForm.score"
              :min="0"
              :max="100"
              :step="1"
              style="width: 150px"
            />
            <span style="margin-left: 10px; color: #999">满分 100 分</span>
          </el-form-item>

          <el-form-item label="评语" prop="comment">
            <el-input
              v-model="reviewForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入评语和建议"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewLoading">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useSubmissionStore } from '@/stores/submission'
import type { Submission } from '@/types/course'

const submissionStore = useSubmissionStore()

const loading = ref(false)
const reviewLoading = ref(false)
const reviewVisible = ref(false)
const currentSubmission = ref<Submission | null>(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: undefined as 'PENDING' | 'REVIEWED' | undefined
})

const submissionList = computed(() => submissionStore.submissionList)
const total = computed(() => submissionStore.total)

const reviewFormRef = ref<FormInstance>()
const reviewForm = reactive({
  submissionId: 0,
  score: 0,
  comment: ''
})

const reviewRules: FormRules = {
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    await submissionStore.fetchSubmissionList(queryParams)
  } catch (error) {
    console.error('获取作业列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置
function handleReset() {
  queryParams.page = 1
  queryParams.status = undefined
  fetchData()
}

// 查看批改
function handleReview(row: Submission) {
  currentSubmission.value = row
  reviewForm.submissionId = row.id
  reviewForm.score = 0
  reviewForm.comment = ''
  reviewVisible.value = true
}

// 查看详情
function handleViewDetail(row: Submission) {
  ElMessage.info('查看详细功能待实现')
}

// 提交批改
async function submitReview() {
  if (!reviewFormRef.value) return

  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return

    reviewLoading.value = true

    try {
      await submissionStore.submitReview({
        submissionId: reviewForm.submissionId,
        score: reviewForm.score,
        comment: reviewForm.comment
      })

      ElMessage.success('批改成功')
      reviewVisible.value = false
      fetchData()
    } catch (error) {
      console.error('批改失败:', error)
    } finally {
      reviewLoading.value = false
    }
  })
}

// 分数标签类型
function getScoreTagType(score: number) {
  if (score >= 90) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.submission-review-container {
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

.review-content {
  .question-section,
  .answer-section {
    margin-top: 20px;
    padding: 15px;
    background-color: #fafafa;
    border-radius: 4px;

    h4 {
      margin: 0 0 10px 0;
      color: #606266;
      font-size: 14px;
    }

    p {
      margin: 0;
      color: #303133;
      line-height: 1.6;
    }
  }

  .answer-section {
    background-color: #f0f9ff;
  }
}
</style>
