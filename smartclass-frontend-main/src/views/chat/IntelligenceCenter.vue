<template>
  <div class="intelligence-center has-tabbar">
    <search-bar
      v-model="searchText"
      placeholder="搜索智慧体"
      :disable-redirect="true"
      @search="onSearch"
    />

    <div class="center-content">
      <intelligence-center-content
        :keyword="activeKeyword"
        @select="handleAssistantSelect"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import SearchBar from '../../components/Common/SearchBar.vue';
import { IntelligenceCenterContent } from '../../components/Chat';

const router = useRouter();
const searchText = ref('');
const activeKeyword = ref('');

const onSearch = (text: string) => {
  activeKeyword.value = text.trim();
};

watch(searchText, (value) => {
  if (!value.trim()) {
    activeKeyword.value = '';
  }
});

const handleAssistantSelect = (assistantId: number) => {
  router.push(`/chat/detail/${assistantId}`);
};
</script>

<style scoped>
.intelligence-center {
  padding: 16px;
  padding-bottom: 66px;
  background-color: #f2f7fd;
  min-height: 100vh;
}

.search-bar {
  margin-bottom: 16px;
}

.center-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 4px;
  box-sizing: border-box;
}
</style>
