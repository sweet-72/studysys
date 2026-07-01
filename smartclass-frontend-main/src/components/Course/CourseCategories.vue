<template>
  <div class="course-categories-container">
    <div class="course-categories">
      <div
        v-for="category in categories"
        :key="category.id"
        :class="['category-item', { active: activeCategory === category.id }]"
        @click="emit('select', category)"
      >
        <span v-if="isTextIcon(category.icon)" class="category-text-icon">{{ category.icon }}</span>
        <svg v-else class="icon svg-icon category-icon" aria-hidden="true">
          <use :xlink:href="'#icon-' + getIconName(category.icon)"></use>
        </svg>
        <span>{{ category.name }}</span>
      </div>
    </div>
    <div class="swipe-hint">
      <van-icon name="arrow" class="swipe-icon" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Category {
  id: number;
  name: string;
  icon: string;
  path: string;
}

defineProps<{
  categories: Category[];
  activeCategory: number;
}>();

const emit = defineEmits<{
  (e: 'select', category: Category): void;
}>();

const getIconName = (iconName: string) => {
  const iconMap: Record<string, string> = {
    star: 'tuijian',
  };

  return iconMap[iconName] || iconName;
};

const isTextIcon = (iconName: string) => {
  return !!iconName && !/^[a-zA-Z0-9_-]+$/.test(iconName);
};
</script>

<style scoped>
.course-categories-container {
  position: relative;
  margin-bottom: 0;
  margin-top: 0;
  padding: 4px 0 8px;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  overflow: hidden;
}

.course-categories {
  display: flex;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding: 2px 42px 6px 0;
}

.course-categories::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  min-height: 38px;
  padding: 0 14px;
  margin-right: 10px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'Noto Sans SC', sans-serif;
  color: #64748b;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 999px;
  box-shadow: 0 8px 20px rgba(80, 100, 120, 0.05);
  backdrop-filter: blur(12px);
  cursor: pointer;
  transition: transform 0.22s ease, background 0.22s ease, color 0.22s ease, box-shadow 0.22s ease;
}

.category-item.active {
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 12px 24px rgba(80, 100, 120, 0.08);
  transform: translateY(-1px);
}

.category-item.active .category-icon {
  color: #4f46e5;
  fill: #4f46e5;
}

.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}

.category-icon {
  margin-right: 5px;
  font-size: 16px;
}

.category-text-icon {
  margin-right: 5px;
  font-size: 16px;
  line-height: 1;
}

.swipe-hint {
  position: absolute;
  top: 0;
  right: 0;
  height: 100%;
  width: 40px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(248, 250, 252, 0.55));
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 10px;
  pointer-events: none;
}

.swipe-icon {
  color: #94a3b8;
  animation: swipeHint 1.5s infinite ease-in-out;
}

@keyframes swipeHint {
  0%,
  100% {
    transform: translateX(-2px);
    opacity: 0.5;
  }
  50% {
    transform: translateX(2px);
    opacity: 1;
  }
}
</style>

<style scoped>
.course-categories-container {
  position: relative;
  z-index: 3;
  margin: 0 auto;
  padding: 4px 0 0;
  border-radius: 20px;
}

.course-categories {
  padding: 4px 40px 8px 4px;
  gap: 8px;
}

.category-item {
  position: relative;
  min-height: 38px;
  margin-right: 0;
  padding: 0 14px;
  color: #69738f;
  background: rgba(255, 255, 255, 0.46);
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 999px;
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(16px);
  transition:
    transform 0.22s ease,
    color 0.22s ease,
    box-shadow 0.22s ease,
    background 0.22s ease;
}

.category-item::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  padding: 1px;
  opacity: 0;
  background: linear-gradient(135deg, #b8a7ff, #9bd7ff);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  transition: opacity 0.22s ease;
}

.category-item.active {
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 16px 30px rgba(99, 102, 241, 0.14);
  transform: translateY(-3px);
}

.category-item.active::before {
  opacity: 1;
}

.category-icon {
  color: currentColor;
  font-size: 16px;
}

.swipe-hint {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(249, 247, 255, 0.78));
}
</style>
