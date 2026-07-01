<template>
  <div class="exercise-editor">
    <div class="exercise-header">
      <span class="exercise-index">题目{{ index + 1 }}</span>
      <el-button link type="danger" size="small" @click="$emit('remove')">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>

    <el-form :model="exercise" label-width="100px" size="small">
      <!-- 题型选择 -->
      <el-form-item label="题型">
        <el-select v-model="exercise.type" @change="handleTypeChange" style="width: 200px">
          <el-option label="单选题" value="SINGLE_CHOICE" />
          <el-option label="多选题" value="MULTIPLE_CHOICE" />
          <el-option label="简答题" value="SHORT_ANSWER" />
        </el-select>
      </el-form-item>

      <!-- 题目内容 -->
      <el-form-item label="题目内容" required>
        <el-input
          v-model="exercise.questionContent"
          type="textarea"
          :rows="3"
          placeholder="请输入题目内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 选项（选择题） -->
      <template v-if="exercise.type === 'SINGLE_CHOICE' || exercise.type === 'MULTIPLE_CHOICE'">
        <el-form-item label="选项设置" required>
          <div class="options-list">
            <div
              v-for="(option, optIndex) in exercise.options"
              :key="option.key"
              class="option-item"
            >
              <div class="option-key">{{ option.key }}.</div>
              <el-input
                v-model="option.content"
                placeholder="请输入选项内容"
                style="flex: 1"
                maxlength="200"
              />
              <el-checkbox
                v-model="checkedAnswers[optIndex]"
                @change="handleAnswerChange"
                :label="option.key"
                style="margin-left: 10px"
              >
                正确
              </el-checkbox>
              <el-button
                link
                type="danger"
                size="small"
                @click="removeOption(optIndex)"
                v-if="exercise.options && exercise.options.length > 2"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>

            <el-button
              type="primary"
              size="small"
              @click="addOption"
              v-if="exercise.options && exercise.options.length < 4"
            >
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </div>
        </el-form-item>

        <!-- 正确答案显示 -->
        <el-form-item label="正确答案">
          <el-tag
            v-for="answer in correctAnswers"
            :key="answer"
            type="success"
            style="margin-right: 5px"
          >
            {{ answer }}
          </el-tag>
          <span v-if="!correctAnswers || correctAnswers.length === 0" style="color: #999">
            请选择正确选项
          </span>
        </el-form-item>
      </template>

      <!-- 简答题答案 -->
      <el-form-item
        v-else-if="exercise.type === 'SHORT_ANSWER'"
        label="参考答案"
        required
      >
        <el-input
          v-model="exercise.correctAnswer"
          type="textarea"
          :rows="3"
          placeholder="请输入参考答案"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import type { Exercise, ExerciseOption } from '@/types/course'

interface Props {
  modelValue: Exercise
  index: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: Exercise]
  remove: []
}>()

const exercise = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 已选中的答案
const checkedAnswers = ref<string[]>([])

// 正确答案显示
const correctAnswers = computed(() => {
  if (typeof exercise.value.correctAnswer === 'string') {
    return exercise.value.correctAnswer.split(',')
  }
  return exercise.value.correctAnswer || []
})

// 初始化选项
watch(
  () => exercise.value.type,
  (newType) => {
    if (newType === 'SINGLE_CHOICE' || newType === 'MULTIPLE_CHOICE') {
      if (!exercise.value.options || exercise.value.options.length === 0) {
        exercise.value.options = [
          { key: 'A', content: '' },
          { key: 'B', content: '' }
        ]
        exercise.value.correctAnswer = []
        checkedAnswers.value = []
      }
    } else {
      exercise.value.correctAnswer = ''
      exercise.value.options = undefined
    }
  },
  { immediate: true }
)

// 处理题型变化
function handleTypeChange() {
  if (exercise.value.type === 'SINGLE_CHOICE' || exercise.value.type === 'MULTIPLE_CHOICE') {
    if (!exercise.value.options) {
      exercise.value.options = [
        { key: 'A', content: '' },
        { key: 'B', content: '' }
      ]
    }
    exercise.value.correctAnswer = []
    checkedAnswers.value = []
  } else {
    exercise.value.correctAnswer = ''
    exercise.value.options = undefined
  }
}

// 添加选项
function addOption() {
  if (!exercise.value.options) {
    exercise.value.options = []
  }
  
  const keys = ['A', 'B', 'C', 'D', 'E', 'F']
  const nextIndex = exercise.value.options.length
  if (nextIndex < keys.length) {
    exercise.value.options.push({
      key: keys[nextIndex],
      content: ''
    })
  }
}

// 删除选项
function removeOption(index: number) {
  if (exercise.value.options) {
    exercise.value.options.splice(index, 1)
    // 重新计算选项 key
    const keys = ['A', 'B', 'C', 'D', 'E', 'F']
    exercise.value.options.forEach((opt, i) => {
      opt.key = keys[i]
    })
  }
}

// 答案变化
function handleAnswerChange() {
  if (exercise.value.type === 'SINGLE_CHOICE') {
    // 单选只能选一个
    exercise.value.correctAnswer = checkedAnswers.value[0] || ''
  } else {
    // 多选可以有多个
    exercise.value.correctAnswer = checkedAnswers.value
  }
}

// 监听外部答案变化
watch(
  () => exercise.value.correctAnswer,
  (newAnswer) => {
    if (Array.isArray(newAnswer)) {
      checkedAnswers.value = newAnswer
    } else if (typeof newAnswer === 'string') {
      checkedAnswers.value = newAnswer ? [newAnswer] : []
    }
  }
)
</script>

<style scoped lang="less">
.exercise-editor {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fafafa;

  .exercise-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .exercise-index {
      font-weight: bold;
      color: #606266;
      font-size: 14px;
    }
  }

  .options-list {
    width: 100%;

    .option-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .option-key {
        font-weight: bold;
        color: #606266;
        min-width: 25px;
      }
    }
  }
}
</style>
