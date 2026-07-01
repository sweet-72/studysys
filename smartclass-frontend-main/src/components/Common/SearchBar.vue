<template>
  <div class="search-bar">
    <van-search
      v-model="searchValue"
      :placeholder="placeholder"
      shape="round"
      :clearable="true"
      input-align="center"
      @search="onSearch"
      :readonly="!disableRedirect"
      @click="!disableRedirect && goToSearchPage()"
    >
      <template #left-icon>
        <van-icon name="search" size="18" class="search-icon" />
      </template>
      <template #right-icon>
        <slot name="right-icon"></slot>
      </template>
    </van-search>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';

const props = defineProps<{
  placeholder?: string;
  modelValue?: string;
  disableRedirect?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'search', text: string): void;
}>();

const router = useRouter();
const searchValue = ref(props.modelValue || '');

watch(searchValue, (newVal) => {
  emit('update:modelValue', newVal);
});

const onSearch = (): void => {
  const keyword = searchValue.value.trim();
  emit('search', keyword);

  if (!props.disableRedirect) {
    goToSearchPage();
  }
};

const goToSearchPage = (): void => {
  const keyword = searchValue.value.trim();
  if (!keyword) {
    showToast('请输入搜索内容');
    return;
  }

  router.push({
    path: '/search',
    query: { keyword },
  });
};
</script>

<style scoped>
.search-bar {
  position: sticky;
  top: 0;
  z-index: 999;
  background: transparent;
  padding: 8px 0 12px;
  margin: 0 0 8px;
}

:deep(.van-search) {
  padding: 0;
  background: transparent;
}

:deep(.van-search__content) {
  height: 42px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 22px;
  box-shadow: none;
  backdrop-filter: blur(12px);
}

:deep(.van-search__field) {
  height: 42px;
}

:deep(.van-field__control) {
  color: #fff;
}

:deep(.van-field__control::placeholder) {
  color: rgba(255, 255, 255, 0.72);
}

:deep(.search-icon) {
  color: rgba(255, 255, 255, 0.92);
  font-weight: bold;
  transition: all 0.3s ease;
}

:deep(.van-search__content:hover .search-icon) {
  transform: scale(1.1);
}

</style>
