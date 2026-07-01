import type { CourseItem, CourseStatus, CourseUpsertPayload } from '@/api/course-admin';
import { Tag } from 'antd';

export const DEFAULT_COURSE_FORM: CourseUpsertPayload = {
  title: '',
  aiKnowleage: '',
  category: '',
  difficulty: '',
  status: 0,
  teacherId: 0,
  teacherName: '',
  description: '',
  chapters: [],
};

export const getCourseId = (item?: Partial<CourseItem> & Record<string, any>): number | undefined => {
  if (!item) {
    return undefined;
  }
  const raw = item.courseId ?? item.id;
  if (raw === undefined || raw === null) {
    return undefined;
  }
  const value = Number(raw);
  return Number.isNaN(value) ? undefined : value;
};

export const toCourseFormValues = (item?: Partial<CourseItem> & Record<string, any>): CourseUpsertPayload => {
  const chapters = Array.isArray(item?.chapters) ? item?.chapters : [];
  return {
    courseId: getCourseId(item),
    title: item?.title ?? '',
    aiKnowleage: item?.aiKnowleage ?? '',
    category: item?.category ?? '',
    categoryId: item?.categoryId,
    difficulty: item?.difficulty ?? '',
    status: (item?.status as CourseStatus) ?? 0,
    teacherId: Number(item?.teacherId ?? 0),
    teacherName: item?.teacherName ?? '',
    description: item?.description ?? '',
    chapters: chapters.map((chapter: any, chapterIndex: number) => ({
      chapterId: chapter.chapterId ?? chapter.id,
      title: chapter.title ?? '',
      description: chapter.description ?? '',
      sort: chapter.sort ?? chapterIndex + 1,
      sections: Array.isArray(chapter.sections)
        ? chapter.sections.map((section: any, sectionIndex: number) => ({
            sectionId: section.sectionId ?? section.id,
            title: section.title ?? '',
            description: section.description ?? '',
            sort: section.sort ?? sectionIndex + 1,
            duration: section.duration,
            videoUrl: section.videoUrl ?? '',
            localVideoPath: section.localVideoPath ?? section.videoPath ?? '',
            assignmentId: section.assignmentId,
            assignmentTitle: section.assignmentTitle,
            assignmentStatus: section.assignmentStatus,
          }))
        : [],
    })),
  };
};

export const validateCoursePayload = (values: CourseUpsertPayload): string | undefined => {
  if (!values.teacherId) {
    return '创建/更新课程时必须选择讲师';
  }

  if (!values.aiKnowleage?.trim()) {
    return 'AI 学习助手知识内容不能为空';
  }

  if (!Array.isArray(values.chapters) || values.chapters.length === 0) {
    return '章节不能为空，请至少添加一个章节';
  }

  for (let chapterIndex = 0; chapterIndex < values.chapters.length; chapterIndex += 1) {
    const chapter = values.chapters[chapterIndex];
    if (!Array.isArray(chapter.sections) || chapter.sections.length === 0) {
      return `第 ${chapterIndex + 1} 个章节的小节不能为空`;
    }

    for (let sectionIndex = 0; sectionIndex < chapter.sections.length; sectionIndex += 1) {
      const section = chapter.sections[sectionIndex];
      if (!section.videoUrl && !section.localVideoPath) {
        return `第 ${chapterIndex + 1} 章第 ${sectionIndex + 1} 节必须填写 videoUrl 或 localVideoPath`;
      }
    }
  }

  return undefined;
};

export const renderStatusTag = (status?: CourseStatus) => {
  const value = String(status ?? '0');
  if (value === '1' || value.toUpperCase() === 'PUBLISHED') {
    return <Tag color="success">已上架</Tag>;
  }
  if (value === '2' || value.toUpperCase() === 'OFFLINE') {
    return <Tag color="warning">已下架</Tag>;
  }
  return <Tag>草稿</Tag>;
};

export const toggleStatusValue = (status?: CourseStatus): CourseStatus => {
  const value = String(status ?? '0');
  if (value === '1' || value.toUpperCase() === 'PUBLISHED') {
    return 2;
  }
  return 1;
};
