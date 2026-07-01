<template>
  <div class="today-goals">
    <div class="goals-header">
      <h3 class="section-title">今日学习目标</h3>
      <button class="add-goal-button" type="button" @click="showAddGoalPopup">
        <van-icon name="plus" size="20" />
      </button>
    </div>

    <div class="progress-row">
      <span>{{ completedGoals }}/{{ totalGoals }}</span>
      <span>{{ progress }}%</span>
    </div>
    <div class="progress-container">
      <van-progress
        :percentage="progress"
        :show-pivot="false"
        color="#1989fa"
        :stroke-width="8"
      />
    </div>

    <van-empty
      v-if="goals.length === 0"
      image-size="64"
      description="今天还没有学习目标，点击右上角 + 添加一个吧"
    />
    <div v-else class="goal-items">
      <div
        v-for="goal in goals"
        :key="goal.id"
        class="goal-item"
        :class="{ completed: goal.completed }"
      >
        <van-icon
          class="goal-check"
          :name="goal.completed ? 'checked' : 'circle'"
          @click="toggleGoalStatus(goal)"
        />
        <div class="goal-main">
          <div class="goal-title">{{ goal.title }}</div>
          <div class="goal-meta">
            <van-tag v-if="sourceLabel(goal.source)" plain :type="sourceTagType(goal.source)">
              {{ sourceLabel(goal.source) }}
            </van-tag>
            <span v-if="goal.targetValue > 1" class="goal-count">
              {{ goal.currentValue }}/{{ goal.targetValue }}{{ goal.unit || '' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <van-popup
    v-model:show="showPopup"
    position="bottom"
    round
    class="add-goal-popup"
  >
    <div class="popup-header">
      <div class="popup-title">添加学习目标</div>
      <van-icon name="cross" class="close-icon" @click="showPopup = false" />
    </div>
    <div class="popup-content">
      <van-field
        v-model="newGoalTitle"
        label="目标"
        placeholder="请输入学习目标"
        maxlength="50"
        show-word-limit
      />
      <van-field
        v-model.number="newGoalTarget"
        label="目标值"
        type="digit"
        placeholder="默认 1"
      />
      <van-field
        v-model="newGoalUnit"
        label="单位"
        placeholder="如：个 / 分钟 / 篇 / 节"
        maxlength="10"
      />
      <div class="popup-buttons">
        <van-button
          block
          type="primary"
          class="add-button"
          :disabled="!newGoalTitle.trim()"
          @click="addNewGoal"
        >
          添加
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue';

export interface Goal {
  id: number;
  title: string;
  completed: boolean;
  source: string;
  goalType?: string;
  targetValue: number;
  currentValue: number;
  unit?: string;
}

defineProps<{
  progress: number;
  completedGoals: number;
  totalGoals: number;
  goals: Goal[];
}>();

const emit = defineEmits<{
  (e: 'add-goal', payload: { title: string; targetValue: number; unit?: string }): void;
  (e: 'toggle-goal', goalId: number): void;
}>();

const showPopup = ref(false);
const newGoalTitle = ref('');
const newGoalTarget = ref<number | undefined>(1);
const newGoalUnit = ref('');

const showAddGoalPopup = () => {
  showPopup.value = true;
  newGoalTitle.value = '';
  newGoalTarget.value = 1;
  newGoalUnit.value = '';
};

const addNewGoal = () => {
  const title = newGoalTitle.value.trim();
  if (!title) {
    return;
  }
  emit('add-goal', {
    title,
    targetValue: newGoalTarget.value && newGoalTarget.value > 0 ? newGoalTarget.value : 1,
    unit: newGoalUnit.value.trim() || undefined,
  });
  showPopup.value = false;
};

const toggleGoalStatus = (goal: Goal) => {
  emit('toggle-goal', goal.id);
};

const sourceLabel = (source: string) => {
  if (source === 'CARRY_OVER') {
    return '昨日未完成';
  }
  if (source === 'CUSTOM') {
    return '自定义';
  }
  return '';
};

const sourceTagType = (source: string) => {
  if (source === 'CARRY_OVER') {
    return 'warning';
  }
  if (source === 'CUSTOM') {
    return 'success';
  }
  return 'primary';
};
</script>

<style scoped>
.today-goals {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(100, 101, 102, 0.08);
  padding: 16px;
}

.goals-header,
.progress-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goals-header {
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
  font-size: var(--font-size-md);
  color: #323233;
  margin: 0;
}

.progress-row {
  margin-bottom: 8px;
  color: #646566;
  font-size: 13px;
}

.progress-container {
  margin-bottom: 12px;
}

.goal-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  color: #323233;
  font-size: var(--font-size-base);
  line-height: 22px;
}

.goal-check {
  margin-top: 2px;
  font-size: 20px;
  color: #1989fa;
}

.goal-main {
  flex: 1;
  min-width: 0;
}

.goal-title {
  overflow-wrap: anywhere;
}

.goal-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 5px;
}

.goal-count {
  font-size: 12px;
  color: #969799;
}

.goal-item.completed .goal-title {
  color: #1989fa;
  text-decoration: line-through;
}

.add-goal-button {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 50%;
  background-color: #1989fa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.3);
  color: white;
}

.add-goal-popup {
  height: auto;
  max-height: 55%;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.popup-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: #323233;
}

.close-icon {
  font-size: var(--font-size-lg);
  color: #969799;
  cursor: pointer;
}

.popup-content {
  padding: 16px;
}

.popup-buttons {
  margin-top: 16px;
}

.add-button {
  height: 40px;
  border-radius: 8px;
}
</style>
