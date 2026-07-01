<template>
  <div class="homework-panel">
    <div class="panel-title">当前小节课后习题</div>

    <van-empty
      v-if="questions.length === 0"
      description="当前小节暂无习题"
      image-size="80"
    />

    <div
      v-for="question in questions"
      :key="question.id"
      class="question-card"
    >
      <div class="question-title">{{ question.title }}</div>

      <van-radio-group
        v-if="question.options && question.options.length > 0"
        :model-value="String(answers[question.id] ?? '')"
        @update:model-value="(value) => onAnswerChange(question.id, value)"
      >
        <van-cell-group inset>
          <van-cell
            v-for="option in question.options"
            :key="option.value"
            clickable
            @click="onAnswerChange(question.id, option.value)"
          >
            <template #title>
              <span>{{ option.label }}. {{ option.value }}</span>
            </template>
            <template #right-icon>
              <van-radio :name="option.value" />
            </template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>

      <van-field
        v-else
        :model-value="String(answers[question.id] ?? '')"
        type="textarea"
        rows="3"
        autosize
        placeholder="请输入你的答案"
        @update:model-value="(value) => onAnswerChange(question.id, value)"
      />

      <div class="question-actions">
        <van-button
          size="small"
          type="primary"
          :loading="submittingQuestionId === question.id"
          @click="emit('submit', question)"
        >
          提交答案
        </van-button>
      </div>

      <div v-if="results[question.id]" class="result-box">
        {{ results[question.id] }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CourseQuestion } from '@/api/course';

const props = defineProps<{
  questions: CourseQuestion[];
  answers: Record<number, string>;
  submittingQuestionId?: number | null;
  results: Record<number, string>;
}>();

const emit = defineEmits<{
  (e: 'answer-change', payload: { questionId: number; answer: string }): void;
  (e: 'submit', question: CourseQuestion): void;
}>();

const onAnswerChange = (questionId: number, value: string | number) => {
  emit('answer-change', { questionId, answer: String(value) });
};
</script>

<style scoped>
.homework-panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #edf0f4;
  padding: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 10px;
}

.question-card {
  border: 1px solid #edf0f4;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
  background: #fbfdff;
}

.question-card:last-child {
  margin-bottom: 0;
}

.question-title {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 10px;
  font-weight: 500;
}

.question-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.result-box {
  margin-top: 8px;
  border-radius: 8px;
  padding: 8px 10px;
  background: #eef6ff;
  color: #1d4ed8;
  font-size: 12px;
}
</style>
