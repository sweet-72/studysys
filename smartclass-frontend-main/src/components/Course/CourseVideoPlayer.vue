<template>
  <video
    v-if="renderMode === 'direct'"
    ref="videoRef"
    class="course-video-player"
    :src="directVideoUrl"
    controls
    playsinline
    preload="metadata"
    @loadedmetadata="$emit('loadedmetadata')"
    @ended="$emit('ended')"
    @timeupdate="$emit('timeupdate')"
    @pause="$emit('pause')"
    @error="$emit('error')"
  >
    当前浏览器不支持视频播放。
  </video>

  <iframe
    v-else-if="renderMode === 'bilibili'"
    class="course-video-iframe"
    :src="bilibiliEmbedUrl"
    title="课程视频播放器"
    allow="fullscreen; autoplay; encrypted-media; picture-in-picture"
    allowfullscreen
    referrerpolicy="no-referrer-when-downgrade"
  ></iframe>

  <div v-else-if="renderMode === 'external'" class="external-video-wrap">
    <div class="external-video-title">暂不支持站内播放</div>
    <div class="external-video-desc">该链接为第三方页面地址，可打开外部链接继续观看。</div>
    <van-button type="primary" size="small" @click="openExternalVideo">打开外部视频</van-button>
    <div class="external-video-link">{{ externalVideoUrl }}</div>
  </div>

  <div v-else class="video-empty-wrap">
    <van-empty description="当前小节暂无可播放视频" image-size="72" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getBrowserOriginBase } from '@/utils/api';

const props = defineProps<{
  videoUrl?: string;
}>();

defineEmits<{
  loadedmetadata: [];
  ended: [];
  timeupdate: [];
  pause: [];
  error: [];
}>();

const videoRef = ref<HTMLVideoElement | null>(null);

const toAbsoluteUrl = (url: string): string => {
  if (!url) {
    return '';
  }
  if (/^https?:\/\//i.test(url)) {
    return url;
  }
  if (url.startsWith('//')) {
    return `${window.location.protocol}${url}`;
  }
  const base = getBrowserOriginBase();
  try {
    return new URL(url, base.endsWith('/') ? base : `${base}/`).toString();
  } catch {
    return url;
  }
};

const isDirectVideoUrl = (url: string): boolean => {
  if (!url) {
    return false;
  }
  if (url.startsWith('/api/upload/course_video/')) {
    return true;
  }
  const pathname = (() => {
    try {
      return new URL(toAbsoluteUrl(url)).pathname;
    } catch {
      return url.split('?')[0].split('#')[0];
    }
  })();
  return /\.(mp4|webm|ogg|m3u8)$/i.test(pathname);
};

const parseBilibiliEmbedUrl = (url: string): string => {
  if (!url) {
    return '';
  }

  let parsed: URL;
  try {
    parsed = new URL(toAbsoluteUrl(url));
  } catch {
    return '';
  }

  const host = parsed.hostname.toLowerCase();
  if (!/(^|\.)bilibili\.com$/.test(host) && host !== 'b23.tv') {
    return '';
  }

  const text = decodeURIComponent(`${parsed.pathname}${parsed.search}`);
  const bvid = text.match(/(?:^|\/)(BV[0-9A-Za-z]+)/i)?.[1] || text.match(/[?&]bvid=(BV[0-9A-Za-z]+)/i)?.[1];
  if (bvid) {
    return `https://player.bilibili.com/player.html?bvid=${encodeURIComponent(bvid)}&page=1&high_quality=1&autoplay=0`;
  }

  const aid = text.match(/(?:^|\/)av(\d+)/i)?.[1] || text.match(/[?&]aid=(\d+)/i)?.[1];
  if (aid) {
    return `https://player.bilibili.com/player.html?aid=${encodeURIComponent(aid)}&page=1&high_quality=1&autoplay=0`;
  }

  return '';
};

const rawVideoUrl = computed(() => props.videoUrl?.trim() || '');
const directVideoUrl = computed(() => (isDirectVideoUrl(rawVideoUrl.value) ? toAbsoluteUrl(rawVideoUrl.value) : ''));
const bilibiliEmbedUrl = computed(() => parseBilibiliEmbedUrl(rawVideoUrl.value));
const externalVideoUrl = computed(() => {
  if (!rawVideoUrl.value || directVideoUrl.value || bilibiliEmbedUrl.value) {
    return '';
  }
  return /^https?:\/\//i.test(rawVideoUrl.value) ? toAbsoluteUrl(rawVideoUrl.value) : '';
});

const renderMode = computed<'direct' | 'bilibili' | 'external' | 'empty'>(() => {
  if (directVideoUrl.value) {
    return 'direct';
  }
  if (bilibiliEmbedUrl.value) {
    return 'bilibili';
  }
  if (externalVideoUrl.value) {
    return 'external';
  }
  return 'empty';
});

const openExternalVideo = () => {
  if (!externalVideoUrl.value) {
    return;
  }
  window.open(externalVideoUrl.value, '_blank', 'noopener,noreferrer');
};

defineExpose({
  videoElement: videoRef,
  renderMode,
});
</script>

<style scoped>
.course-video-player,
.course-video-iframe {
  display: block;
  width: 100%;
  height: min(430px, 48vh);
  border: 0;
  border-radius: 24px 24px 0 0;
}

.course-video-player {
  object-fit: cover;
  background: #151622;
}

.course-video-iframe {
  background: linear-gradient(135deg, rgba(255, 247, 237, 0.82), rgba(219, 234, 254, 0.82));
}

.external-video-wrap,
.video-empty-wrap {
  min-height: min(430px, 48vh);
  padding: 48px 20px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  background: linear-gradient(135deg, rgba(232, 224, 255, 0.5), rgba(219, 234, 254, 0.5));
}

.external-video-title {
  color: #25304d;
  font-size: 17px;
  font-weight: 900;
}

.external-video-desc,
.external-video-link {
  margin-top: 8px;
  color: #69738f;
  font-size: 13px;
  line-height: 1.6;
}

.external-video-link {
  max-width: min(92%, 680px);
  overflow-wrap: anywhere;
}

@media (max-width: 640px) {
  .course-video-player,
  .course-video-iframe,
  .external-video-wrap,
  .video-empty-wrap {
    height: 260px;
    min-height: 260px;
  }
}
</style>
