<template>
  <div class="learning-space">
    <span class="decor decor-cloud decor-cloud-left"></span>
    <span class="decor decor-cloud decor-cloud-right"></span>
    <span class="decor decor-star star-one">✦</span>
    <span class="decor decor-star star-two">✧</span>
    <span class="decor decor-star star-three">✦</span>

    <section class="space-hero" :style="{ backgroundImage: `url(${backImg})` }">
      <div class="topbar">
        <button class="glass-icon" type="button" @click="router.back()">
          <van-icon name="arrow-left" />
        </button>
        <div class="topbar-title">
          <h1>猫猫鹅学习空间</h1>
          <p>今天也要加油呀！✨</p>
        </div>
      </div>

      <div class="hero-content">
        <img :src="catImg" alt="猫猫鹅" class="hero-cat" />
      </div>

      <section class="stats-grid" aria-label="学习概览">
        <article
          v-for="stat in statsCards"
          :key="stat.label"
          class="stat-widget"
          :class="stat.className"
        >
          <div class="stat-icon">
            <van-icon :name="stat.icon" />
          </div>
          <strong>{{ stat.value }}</strong>
          <span>{{ stat.label }}</span>
        </article>
      </section>
    </section>

    <main class="space-main">
      <section class="glass-panel workspace-panel">
        <div class="section-header">
          <div>
            <h2>我的课程表</h2>
            <p>课程和事件都在这里，一眼看清今天的学习节奏</p>
          </div>
          <div class="header-actions">
            <button class="section-btn" type="button" @click="openCourseForm">
              <van-icon name="plus" />
              新增课程
            </button>
          </div>
        </div>

        <div class="mobile-schedule">
          <div class="day-tabs">
            <button
              v-for="day in weekDays"
              :key="day.value"
              type="button"
              :class="{ active: selectedDay === day.value }"
              @click="selectedDay = day.value"
            >
              <span>{{ day.short }}</span>
              <small>{{ day.label }}</small>
            </button>
          </div>

          <div class="day-course-list">
            <article
              v-for="course in selectedDayCourses"
              :key="course.id"
              class="course-item-card"
              :class="`theme-${course.color}`"
            >
              <div class="course-icon">
                <van-icon :name="course.type === '考试' ? 'award-o' : course.type === '任务' ? 'todo-list-o' : 'bookmark-o'" />
              </div>
              <div class="course-info">
                <div class="course-title-row">
                  <h3>{{ course.name }}</h3>
                  <button class="more-dot" type="button" @click="deleteCourse(course.id)">
                    <van-icon name="delete-o" />
                  </button>
                </div>
                <div class="course-tags">
                  <span>{{ course.type }}</span>
                  <span>{{ course.startTime }}-{{ course.endTime }}</span>
                </div>
                <p>{{ course.location }}</p>
              </div>
            </article>
            <div v-if="selectedDayCourses.length === 0" class="empty-state">
              今天暂时没有课程安排
            </div>
          </div>
        </div>

        <div class="desktop-schedule">
          <div class="calendar-shell">
            <div class="calendar-week-header">
              <div class="week-spacer"></div>
              <div
                v-for="day in weekDays"
                :key="day.value"
                class="week-head"
                :class="{ today: day.value === todayDay }"
              >
                <strong>{{ day.label }}</strong>
                <span>{{ day.short }}</span>
              </div>
            </div>

            <div class="timeline-board">
              <div class="time-rail">
                <span
                  v-for="time in timeSlots"
                  :key="time"
                  :style="timeLabelStyle(time)"
                >
                  {{ time }}
                </span>
              </div>

              <div class="calendar-canvas">
                <span
                  v-for="time in timeSlots"
                  :key="`line-${time}`"
                  class="hour-line"
                  :style="timeLabelStyle(time)"
                ></span>
                <span
                  v-for="day in weekDays"
                  :key="`col-${day.value}`"
                  class="day-column"
                  :style="{ left: `${((day.value - 1) / 7) * 100}%` }"
                ></span>
                <article
                  v-for="course in desktopCourseBlocks"
                  :key="course.id"
                  class="calendar-course"
                  :class="`theme-${course.color}`"
                  :style="courseBlockStyle(course)"
                >
                  <strong>{{ course.name }}</strong>
                  <span>{{ course.location }}</span>
                  <em>{{ course.startTime }}-{{ course.endTime }}</em>
                </article>
              </div>
            </div>
          </div>
        </div>

        <div class="event-dock">
          <div class="event-dock-head">
            <div>
              <h3>我的事件</h3>
              <span>提醒、考试和纪念日</span>
            </div>
            <button class="section-btn event-add-btn" type="button" @click="openEventForm">
              <van-icon name="plus" />
              新增事件
            </button>
          </div>
        <div class="event-list">
          <article
            v-for="event in events"
            :key="event.id"
            class="event-card"
            :class="`theme-${event.color}`"
          >
            <div class="event-icon">
              <van-icon :name="eventIcon(event.type)" />
            </div>
            <div class="event-copy">
              <span>{{ event.type }}</span>
              <h3>{{ event.title }}</h3>
              <p>{{ event.date }}</p>
              <strong>{{ countdownText(event.date) }}</strong>
            </div>
            <button class="more-dot" type="button" @click="deleteEvent(event.id)">
              <van-icon name="delete-o" />
            </button>
          </article>
        </div>
        </div>
      </section>
    </main>

    <van-popup v-model:show="showCoursePopup" round position="bottom" class="space-popup">
      <form class="form-card" @submit.prevent="submitCourse">
        <div class="popup-title">
          <h3>新增课程</h3>
          <van-icon name="cross" @click="closeCourseForm" />
        </div>
        <label>
          课程名称
          <input v-model.trim="courseForm.name" placeholder="例如：高等数学" />
        </label>
        <div class="form-row">
          <label>
            星期几
            <select v-model.number="courseForm.day">
              <option :value="0">请选择</option>
              <option v-for="day in weekDays" :key="day.value" :value="day.value">{{ day.label }}</option>
            </select>
          </label>
          <label>
            类型
            <select v-model="courseForm.type">
              <option>课程</option>
              <option>考试</option>
              <option>任务</option>
            </select>
          </label>
        </div>
        <div class="form-row">
          <label>
            开始时间
            <input v-model="courseForm.startTime" type="time" />
          </label>
          <label>
            结束时间
            <input v-model="courseForm.endTime" type="time" />
          </label>
        </div>
        <label>
          地点
          <input v-model.trim="courseForm.location" placeholder="例如：教学楼 A201" />
        </label>
        <label>
          颜色主题
          <select v-model="courseForm.color">
            <option v-for="theme in colorThemes" :key="theme.value" :value="theme.value">{{ theme.label }}</option>
          </select>
        </label>
        <button class="submit-btn" type="submit">保存课程</button>
      </form>
    </van-popup>

    <van-popup v-model:show="showEventPopup" round position="bottom" class="space-popup">
      <form class="form-card" @submit.prevent="submitEvent">
        <div class="popup-title">
          <h3>新增事件</h3>
          <van-icon name="cross" @click="closeEventForm" />
        </div>
        <label>
          事件标题
          <input v-model.trim="eventForm.title" placeholder="例如：高数期中考试" />
        </label>
        <div class="form-row">
          <label>
            事件类型
            <select v-model="eventForm.type">
              <option>考试</option>
              <option>任务</option>
              <option>纪念日</option>
              <option>活动</option>
            </select>
          </label>
          <label>
            日期
            <input v-model="eventForm.date" type="date" />
          </label>
        </div>
        <label>
          备注
          <input v-model.trim="eventForm.note" placeholder="补充一点小提醒" />
        </label>
        <label>
          颜色主题
          <select v-model="eventForm.color">
            <option v-for="theme in colorThemes" :key="theme.value" :value="theme.value">{{ theme.label }}</option>
          </select>
        </label>
        <button class="submit-btn" type="submit">保存事件</button>
      </form>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import backImg from '@/assets/images/back.png';
import catImg from '@/assets/images/cat.png';

type CourseType = '课程' | '考试' | '任务';
type EventType = '考试' | '任务' | '纪念日' | '活动';
type ThemeName = 'purple' | 'blue' | 'green' | 'pink' | 'orange';

interface SpaceCourse {
  id: number;
  name: string;
  day: number;
  startTime: string;
  endTime: string;
  location: string;
  type: CourseType;
  color: ThemeName;
}

interface SpaceEvent {
  id: number;
  title: string;
  type: EventType;
  date: string;
  note: string;
  color: ThemeName;
}

const COURSES_KEY = 'smartclass_learning_space_courses';
const EVENTS_KEY = 'smartclass_learning_space_events';

const router = useRouter();
const todayDay = new Date().getDay() === 0 ? 7 : new Date().getDay();
const selectedDay = ref(2);
const showCoursePopup = ref(false);
const showEventPopup = ref(false);

const weekDays = [
  { value: 1, label: '周一', short: '一' },
  { value: 2, label: '周二', short: '二' },
  { value: 3, label: '周三', short: '三' },
  { value: 4, label: '周四', short: '四' },
  { value: 5, label: '周五', short: '五' },
  { value: 6, label: '周六', short: '六' },
  { value: 7, label: '周日', short: '日' },
];

const timeSlots = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'];
const calendarStartMinutes = 8 * 60;
const calendarEndMinutes = 18 * 60;
const calendarTotalMinutes = calendarEndMinutes - calendarStartMinutes;
const colorThemes: Array<{ value: ThemeName; label: string }> = [
  { value: 'purple', label: '紫' },
  { value: 'blue', label: '蓝' },
  { value: 'green', label: '绿' },
  { value: 'pink', label: '粉' },
  { value: 'orange', label: '橙' },
];

const defaultCourses: SpaceCourse[] = [
  { id: 1, name: '高等数学', day: 2, startTime: '08:00', endTime: '09:40', location: '教学楼 A201', type: '考试', color: 'purple' },
  { id: 2, name: '大学英语', day: 2, startTime: '10:00', endTime: '11:40', location: '教学楼 B305', type: '课程', color: 'blue' },
  { id: 3, name: '有机化学', day: 5, startTime: '14:00', endTime: '15:40', location: '教学楼 C101', type: '考试', color: 'green' },
  { id: 4, name: '天体物理导论', day: 2, startTime: '16:00', endTime: '17:40', location: '教学楼 D402', type: '课程', color: 'blue' },
];

const defaultEvents: SpaceEvent[] = [
  { id: 1, title: '高数期中考试', date: '2024-05-25', type: '考试', note: '', color: 'purple' },
  { id: 2, title: '英语演讲比赛', date: '2024-05-25', type: '活动', note: '', color: 'blue' },
  { id: 3, title: '物理实验报告', date: '2024-05-30', type: '任务', note: '', color: 'green' },
  { id: 4, title: '期末考试', date: '2024-06-20', type: '考试', note: '', color: 'orange' },
];

const courses = ref<SpaceCourse[]>([]);
const events = ref<SpaceEvent[]>([]);

const emptyCourseForm = (): SpaceCourse => ({
  id: 0,
  name: '',
  day: selectedDay.value,
  startTime: '',
  endTime: '',
  location: '',
  type: '课程',
  color: 'purple',
});

const emptyEventForm = (): SpaceEvent => ({
  id: 0,
  title: '',
  type: '考试',
  date: '',
  note: '',
  color: 'purple',
});

const courseForm = reactive<SpaceCourse>(emptyCourseForm());
const eventForm = reactive<SpaceEvent>(emptyEventForm());

const selectedDayCourses = computed(() =>
  courses.value
    .filter((course) => course.day === selectedDay.value)
    .sort((a, b) => a.startTime.localeCompare(b.startTime)),
);

const pendingEventsCount = computed(() =>
  events.value.filter((event) => getDaysLeft(event.date) >= 0).length,
);

const statsCards = computed(() => [
  {
    label: '今日学习时长',
    value: '2h 35min',
    icon: 'clock-o',
    className: 'primary',
  },
  {
    label: '待完成任务',
    value: `${pendingEventsCount.value}项`,
    icon: 'todo-list-o',
    className: 'purple',
  },
  {
    label: '本周课程数',
    value: `${courses.value.length}门`,
    icon: 'bookmark-o',
    className: 'blue',
  },
  {
    label: '连续学习',
    value: '7天',
    icon: 'fire-o',
    className: 'pink',
  },
]);

const desktopCourseBlocks = computed(() =>
  courses.value
    .filter((course) => course.day >= 1 && course.day <= 7)
    .sort((a, b) => a.day - b.day || a.startTime.localeCompare(b.startTime)),
);

const safeRead = <T,>(key: string, fallback: T): T => {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch (error) {
    return fallback;
  }
};

const safeWrite = (key: string, value: unknown) => {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    showToast('本地保存失败，请检查浏览器设置');
  }
};

const getDaysLeft = (dateText: string): number => {
  if (!dateText) return 0;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(dateText);
  target.setHours(0, 0, 0, 0);
  return Math.ceil((target.getTime() - today.getTime()) / 86400000);
};

const countdownText = (dateText: string): string => {
  const days = getDaysLeft(dateText);
  if (days < 0) return '已结束';
  if (days === 0) return '就是今天';
  return `还有 ${days} 天`;
};

const toMinutes = (timeText: string): number => {
  const [hour = '0', minute = '0'] = timeText.split(':');
  return Number(hour) * 60 + Number(minute);
};

const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max);

const timeLabelStyle = (timeText: string): Record<string, string> => {
  const offset = clamp(toMinutes(timeText) - calendarStartMinutes, 0, calendarTotalMinutes);
  return {
    top: `${(offset / calendarTotalMinutes) * 100}%`,
  };
};

const courseBlockStyle = (course: SpaceCourse): Record<string, string> => {
  const start = clamp(toMinutes(course.startTime) - calendarStartMinutes, 0, calendarTotalMinutes);
  const end = clamp(toMinutes(course.endTime) - calendarStartMinutes, start + 30, calendarTotalMinutes);
  const left = ((course.day - 1) / 7) * 100;
  const width = 100 / 7;
  const overlapOffset = (course.id % 3) * 4;

  return {
    top: `${(start / calendarTotalMinutes) * 100}%`,
    left: `calc(${left}% + 10px + ${overlapOffset}px)`,
    width: `calc(${width}% - 20px)`,
    minHeight: '48px',
    height: `${Math.max(8, ((end - start) / calendarTotalMinutes) * 100)}%`,
  };
};

const eventIcon = (type: EventType) => {
  const iconMap: Record<EventType, string> = {
    考试: 'award-o',
    任务: 'todo-list-o',
    纪念日: 'like-o',
    活动: 'flag-o',
  };
  return iconMap[type];
};

const resetCourseForm = () => {
  Object.assign(courseForm, emptyCourseForm());
};

const resetEventForm = () => {
  Object.assign(eventForm, emptyEventForm());
};

const openCourseForm = () => {
  resetCourseForm();
  showCoursePopup.value = true;
};

const closeCourseForm = () => {
  showCoursePopup.value = false;
  resetCourseForm();
};

const openEventForm = () => {
  resetEventForm();
  showEventPopup.value = true;
};

const closeEventForm = () => {
  showEventPopup.value = false;
  resetEventForm();
};

const submitCourse = () => {
  if (!courseForm.name) return showToast('课程名称不能为空');
  if (!courseForm.day) return showToast('请选择星期');
  if (!courseForm.startTime || !courseForm.endTime) return showToast('请填写开始和结束时间');

  const nextCourse = { ...courseForm, id: Date.now() };
  courses.value = [...courses.value, nextCourse];
  safeWrite(COURSES_KEY, courses.value);
  closeCourseForm();
  showToast('课程已添加');
};

const submitEvent = () => {
  if (!eventForm.title) return showToast('事件标题不能为空');
  if (!eventForm.date) return showToast('请选择事件日期');

  const nextEvent = { ...eventForm, id: Date.now() };
  events.value = [...events.value, nextEvent];
  safeWrite(EVENTS_KEY, events.value);
  closeEventForm();
  showToast('事件已添加');
};

const deleteCourse = (id: number) => {
  courses.value = courses.value.filter((course) => course.id !== id);
  safeWrite(COURSES_KEY, courses.value);
};

const deleteEvent = (id: number) => {
  events.value = events.value.filter((event) => event.id !== id);
  safeWrite(EVENTS_KEY, events.value);
};

onMounted(() => {
  courses.value = safeRead(COURSES_KEY, defaultCourses);
  events.value = safeRead(EVENTS_KEY, defaultEvents);
});
</script>

<style scoped>
.learning-space {
  position: relative;
  min-height: 100vh;
  padding: 22px 24px 88px;
  overflow-x: hidden;
  color: #1e293b;
  background:
    radial-gradient(circle at 14% 9%, rgba(196, 181, 253, 0.32), transparent 28%),
    radial-gradient(circle at 82% 12%, rgba(147, 197, 253, 0.28), transparent 26%),
    radial-gradient(circle at 52% 78%, rgba(251, 207, 232, 0.32), transparent 28%),
    linear-gradient(135deg, #f8fafc 0%, #eef2ff 48%, #fdf2f8 100%);
}

.learning-space::before {
  position: fixed;
  inset: 0;
  pointer-events: none;
  content: '';
  background-image:
    radial-gradient(rgba(99, 102, 241, 0.11) 0.7px, transparent 0.7px),
    radial-gradient(rgba(255, 255, 255, 0.62) 0.8px, transparent 0.8px);
  background-position: 0 0, 12px 14px;
  background-size: 28px 28px;
  opacity: 0.24;
}

.decor {
  position: fixed;
  z-index: 0;
  pointer-events: none;
}

.decor-cloud {
  width: 190px;
  height: 72px;
  background: rgba(255, 255, 255, 0.42);
  border-radius: 999px;
  filter: blur(18px);
}

.decor-cloud-left {
  top: 170px;
  left: -70px;
}

.decor-cloud-right {
  right: -90px;
  bottom: 210px;
}

.decor-star {
  color: rgba(139, 92, 246, 0.18);
  font-size: 42px;
}

.star-one {
  top: 112px;
  left: 8%;
}

.star-two {
  top: 72px;
  right: 15%;
  color: rgba(96, 165, 250, 0.2);
}

.star-three {
  right: 7%;
  bottom: 110px;
  color: rgba(244, 114, 182, 0.16);
}

.space-hero,
.glass-panel,
.stats-grid,
.course-item-card,
.event-card,
.calendar-shell,
.event-dock {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 18px 44px rgba(99, 102, 241, 0.1);
  backdrop-filter: blur(20px);
}

.space-hero {
  position: relative;
  z-index: 1;
  max-width: 1100px;
  min-height: 198px;
  padding: 26px 30px 24px;
  margin: 0 auto -8px;
  overflow: hidden;
  background-repeat: no-repeat;
  background-position: center 38%;
  background-size: cover;
  border-radius: 28px;
  animation: pageIn 0.45s ease both;
}

.topbar,
.section-header,
.popup-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.topbar {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 18px;
  align-items: start;
}

.topbar-title h1 {
  margin: 0;
  color: #0f172a;
  font-size: 28px !important;
  font-weight: 900 !important;
  line-height: 1.12;
}

.topbar-title p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 15px !important;
  font-weight: 700;
}

.glass-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  color: #1e293b;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.44);
  border-radius: 50%;
  box-shadow: 0 10px 26px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(20px);
}

.hero-content {
  position: absolute;
  top: 54px;
  left: 34%;
  bottom: -42px;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  pointer-events: none;
}

.hero-cat {
  width: 226px;
  max-width: 21vw;
  filter: drop-shadow(0 18px 32px rgba(99, 102, 241, 0.16));
  transform: translateX(-50%) translateY(10px);
}

.space-main {
  position: relative;
  z-index: 1;
  display: grid;
  max-width: 1100px;
  gap: 14px;
  margin: 0 auto;
  animation: pageIn 0.5s ease 0.04s both;
}

.stats-grid {
  position: absolute;
  top: 62px;
  right: 30px;
  z-index: 3;
  display: grid;
  width: min(560px, 52%);
  grid-template-columns: 1.22fr repeat(3, 1fr);
  gap: 0;
  padding: 12px 14px;
  border-radius: 22px;
}

.stat-widget {
  display: grid;
  grid-template-columns: 38px 1fr;
  gap: 3px 10px;
  align-items: center;
  min-height: 70px;
  padding: 10px 14px;
  border-radius: 16px;
  animation: cardIn 0.45s ease both;
  transition: background 0.3s ease, box-shadow 0.3s ease, transform 0.3s ease;
}

.stat-widget + .stat-widget {
  border-left: 1px solid rgba(148, 163, 184, 0.18);
}

.stat-widget:nth-child(2) {
  animation-delay: 0.04s;
}

.stat-widget:nth-child(3) {
  animation-delay: 0.08s;
}

.stat-widget:nth-child(4) {
  animation-delay: 0.12s;
}

.stat-icon {
  grid-row: span 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: #6366f1;
  background: rgba(255, 255, 255, 0.58);
  border-radius: 16px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.stat-widget strong {
  display: inline-block;
  color: transparent;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.08;
  background: linear-gradient(135deg, #4f46e5, #60a5fa 58%, #c084fc);
  background-clip: text;
  -webkit-background-clip: text;
}

.stat-widget.primary strong {
  font-size: 30px;
}

.stat-widget span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.glass-panel {
  padding: 24px;
  overflow: hidden;
  border-radius: 28px;
}

.workspace-panel {
  position: relative;
}

.workspace-panel::before {
  position: absolute;
  top: -80px;
  right: 18%;
  width: 260px;
  height: 260px;
  content: '';
  background: radial-gradient(circle, rgba(196, 181, 253, 0.24), transparent 66%);
  filter: blur(8px);
}

.section-header {
  position: relative;
  z-index: 1;
  gap: 16px;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 24px !important;
  font-weight: 900;
}

.section-header p {
  margin: 6px 0 0;
  color: #94a3b8;
  font-size: 13px !important;
}

.header-actions {
  display: flex;
  flex-shrink: 0;
  gap: 10px;
}

.event-add-btn {
  flex-shrink: 0;
}

.section-btn,
.ghost-section-btn,
.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 42px;
  padding: 0 17px;
  font-weight: 800;
  border: 0;
  border-radius: 999px;
}

.section-btn,
.submit-btn {
  color: #fff;
  background: linear-gradient(135deg, #60a5fa, #6366f1 56%, #a78bfa);
  box-shadow: 0 14px 30px rgba(99, 102, 241, 0.24);
}

.ghost-section-btn {
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(18px);
}

.mobile-schedule {
  display: none;
}

.desktop-schedule,
.calendar-shell,
.event-dock {
  position: relative;
  z-index: 1;
}

.calendar-shell {
  padding: 18px 18px 22px;
  border-radius: 24px;
}

.calendar-week-header {
  display: grid;
  grid-template-columns: 70px repeat(7, minmax(0, 1fr));
  gap: 0;
  margin-bottom: 10px;
}

.week-head {
  display: grid;
  gap: 2px;
  justify-items: center;
  padding: 9px 4px;
  color: #64748b;
  border-radius: 16px;
}

.week-head.today {
  color: #4f46e5;
  background: rgba(238, 242, 255, 0.72);
}

.week-head strong {
  font-size: 13px;
}

.week-head span {
  color: #94a3b8;
  font-size: 11px;
}

.timeline-board {
  display: grid;
  grid-template-columns: 70px 1fr;
  min-height: 520px;
}

.time-rail,
.calendar-canvas {
  position: relative;
}

.time-rail span {
  position: absolute;
  right: 16px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  transform: translateY(-50%);
}

.calendar-canvas {
  overflow: hidden;
  border-radius: 20px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.46)),
    rgba(248, 250, 252, 0.34);
}

.hour-line {
  position: absolute;
  right: 0;
  left: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(148, 163, 184, 0.08), rgba(148, 163, 184, 0.2), rgba(148, 163, 184, 0.08));
}

.day-column {
  position: absolute;
  top: 0;
  bottom: 0;
  width: calc(100% / 7);
  border-left: 1px solid rgba(148, 163, 184, 0.08);
}

.calendar-course {
  position: absolute;
  z-index: 2;
  box-sizing: border-box;
  display: grid;
  align-content: center;
  gap: 3px;
  padding: 10px 12px;
  overflow: hidden;
  color: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 16px;
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(14px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.calendar-course strong {
  overflow: hidden;
  font-size: 13px;
  font-weight: 900;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.calendar-course span,
.calendar-course em {
  overflow: hidden;
  color: #64748b;
  font-size: 11px;
  font-style: normal;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-dock {
  margin-top: 18px;
  padding: 18px;
  border-radius: 24px;
}

.event-dock-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.event-dock-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px !important;
  font-weight: 900;
}

.event-dock-head span {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.event-list {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
}

.event-list::-webkit-scrollbar {
  display: none;
}

.event-card {
  position: relative;
  display: flex;
  flex: 0 0 210px;
  gap: 12px;
  min-height: 112px;
  padding: 15px;
  border-radius: 20px;
  scroll-snap-align: start;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.event-icon,
.course-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: #6366f1;
  background: rgba(255, 255, 255, 0.64);
  border-radius: 16px;
}

.event-copy {
  min-width: 0;
}

.event-copy span,
.course-tags span {
  display: inline-flex;
  padding: 4px 8px;
  color: #6366f1;
  font-size: 12px;
  font-weight: 800;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 999px;
}

.event-copy h3,
.course-info h3 {
  margin: 8px 0 4px;
  color: #1e293b;
  font-size: 16px !important;
  font-weight: 900;
}

.event-copy p,
.course-info p {
  margin: 0 0 7px;
  color: #64748b;
}

.event-copy strong {
  color: transparent;
  font-weight: 900;
  background: linear-gradient(135deg, #4f46e5, #60a5fa);
  background-clip: text;
  -webkit-background-clip: text;
}

.more-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.58);
  border: 0;
  border-radius: 50%;
}

.event-card .more-dot {
  position: absolute;
  top: 10px;
  right: 10px;
}

.theme-purple {
  background: linear-gradient(135deg, rgba(238, 242, 255, 0.92), rgba(196, 181, 253, 0.58));
}

.theme-blue {
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.94), rgba(147, 197, 253, 0.56));
}

.theme-green {
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.94), rgba(167, 243, 208, 0.58));
}

.theme-pink {
  background: linear-gradient(135deg, rgba(253, 242, 248, 0.94), rgba(251, 207, 232, 0.58));
}

.theme-orange {
  background: linear-gradient(135deg, rgba(255, 247, 237, 0.94), rgba(254, 215, 170, 0.58));
}

.space-popup {
  max-height: 86vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(20px);
}

.form-card {
  display: grid;
  gap: 14px;
  padding: 20px 18px 28px;
}

.popup-title h3 {
  margin: 0;
  font-size: 20px !important;
}

.form-card label {
  display: grid;
  gap: 7px;
  color: #475569;
  font-size: 13px;
  font-weight: 800;
}

.form-card input,
.form-card select {
  box-sizing: border-box;
  width: 100%;
  height: 42px;
  padding: 0 12px;
  color: #1e293b;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.86);
  border-radius: 14px;
  outline: none;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}

.empty-state {
  padding: 28px 0;
  color: #94a3b8;
  text-align: center;
}

@media (hover: hover) {
  .stat-widget:hover {
    background: rgba(255, 255, 255, 0.38);
    box-shadow: 0 14px 32px rgba(99, 102, 241, 0.12);
    transform: translateY(-4px) scale(1.02);
  }

  .calendar-course:hover,
  .event-card:hover,
  .course-item-card:hover {
    box-shadow: 0 22px 50px rgba(99, 102, 241, 0.16);
    transform: translateY(-4px) scale(1.02);
  }
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 768px) {
  .learning-space {
    padding: 14px 14px 74px;
  }

  .space-hero {
    min-height: 340px;
    padding: 16px;
    margin: 0 auto -4px;
    border-radius: 26px;
    background-position: top center;
  }

  .topbar {
    grid-template-columns: 48px 1fr;
    gap: 12px;
  }

  .glass-icon {
    width: 48px;
    height: 48px;
  }

  .topbar-title h1 {
    font-size: 24px !important;
  }

  .topbar-title p {
    margin-top: 6px;
    font-size: 14px !important;
  }

  .hero-content {
    top: 140px;
    right: -18px;
    bottom: auto;
    left: auto;
  }

  .hero-cat {
    width: 238px;
    max-width: 58vw;
    transform: none;
  }

  .stats-grid {
    top: 122px;
    right: auto;
    left: 16px;
    width: min(220px, 55vw);
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0;
    padding: 10px;
    border-radius: 22px;
  }

  .stat-widget {
    grid-template-columns: 28px 1fr;
    min-height: 66px;
    padding: 8px;
    border-radius: 16px;
  }

  .stat-widget + .stat-widget {
    border-left: 0;
  }

  .stat-widget:nth-child(2n) {
    border-left: 1px solid rgba(148, 163, 184, 0.16);
  }

  .stat-widget:nth-child(n + 3) {
    border-top: 1px solid rgba(148, 163, 184, 0.16);
  }

  .stat-icon {
    width: 28px;
    height: 28px;
    border-radius: 11px;
  }

  .stat-widget strong,
  .stat-widget.primary strong {
    font-size: 18px;
  }

  .stat-widget span {
    font-size: 10px;
  }

  .space-main {
    gap: 12px;
  }

  .glass-panel {
    padding: 16px;
    border-radius: 24px;
  }

  .section-header {
    align-items: flex-start;
    gap: 12px;
  }

  .section-header h2 {
    font-size: 20px !important;
  }

  .section-header p {
    display: none;
  }

  .header-actions {
    flex-direction: column-reverse;
    gap: 8px;
  }

  .section-btn,
  .ghost-section-btn {
    height: 34px;
    padding: 0 12px;
    font-size: 12px;
  }

  .desktop-schedule {
    display: none;
  }

  .mobile-schedule {
    display: block;
  }

  .day-tabs {
    display: flex;
    gap: 8px;
    padding-bottom: 12px;
    overflow-x: auto;
    scrollbar-width: none;
  }

  .day-tabs::-webkit-scrollbar {
    display: none;
  }

  .day-tabs button {
    flex: 0 0 58px;
    height: 38px;
    color: #64748b;
    background: rgba(255, 255, 255, 0.58);
    border: 1px solid rgba(255, 255, 255, 0.38);
    border-radius: 999px;
    backdrop-filter: blur(14px);
  }

  .day-tabs button.active {
    color: #fff;
    background: linear-gradient(135deg, #60a5fa, #6366f1, #a78bfa);
  }

  .day-tabs span {
    display: none;
  }

  .day-tabs small {
    display: block;
    font-size: 13px;
    font-weight: 800;
  }

  .day-course-list {
    display: grid;
    gap: 12px;
    margin-top: 8px;
  }

  .course-item-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 12px;
    border-radius: 20px;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
  }

  .course-icon {
    width: 56px;
    height: 56px;
    border-radius: 18px;
    font-size: 26px;
  }

  .course-title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 10px;
  }

  .course-info {
    flex: 1;
    min-width: 0;
  }

  .course-info h3 {
    margin-top: 0;
  }

  .course-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;
  }

  .event-dock {
    margin-top: 14px;
    padding: 14px;
  }

  .event-card {
    flex-basis: 72%;
    min-width: 210px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
