import React, { useEffect, useMemo, useState } from 'react';
import {
  deleteCourseHomework,
  deleteCourseHomeworkCompat,
  getCourseHomeworkById,
  listCourseHomeworksByPage,
  resolveCourseHomeworkStatus,
} from '@/api/courseHomework';
import { Button, Empty, Popconfirm, Space, Table, Tag, message } from 'antd';
import { listChaptersUsingGet } from '@/services/backend/courseChapterController';
import { listSectionsByChapterIdUsingGet } from '@/services/backend/courseSectionController';
import HomeworkEditorModal from './HomeworkEditorModal';

const PAGE_SIZE_LIMIT = 20;

const HomeworkManager = ({ courseId }) => {
  const [loading, setLoading] = useState(false);
  const [sections, setSections] = useState([]);
  const [homeworkMap, setHomeworkMap] = useState({});
  const [expandedRowKeys, setExpandedRowKeys] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [currentSection, setCurrentSection] = useState(null);
  const [editingHomework, setEditingHomework] = useState(null);

  const hasSections = useMemo(() => sections.length > 0, [sections]);

  const loadSectionHomework = async (sectionId) => {
    const pageSize = PAGE_SIZE_LIMIT;
    let current = 1;
    let allRecords = [];
    let hasNext = true;

    while (hasNext) {
      const res = await listCourseHomeworksByPage({
        sectionId,
        current,
        pageSize,
      });
      const pageData = res.data || {};
      const records = pageData.records || [];
      const total = Number(pageData.total || 0);

      allRecords = [...allRecords, ...records];

      if (total > 0) {
        hasNext = allRecords.length < total;
      } else {
        hasNext = records.length === pageSize;
      }

      current += 1;
    }

    return allRecords;
  };

  const loadData = async () => {
    if (!courseId) {
      setSections([]);
      setHomeworkMap({});
      return;
    }

    setLoading(true);
    try {
      const chapterRes = await listChaptersUsingGet({ courseId });
      const chapterList = chapterRes.data || [];

      const sectionResults = await Promise.all(
        chapterList.map(async (chapter) => {
          const chapterId = chapter.id ?? chapter.chapterId;
          if (!chapterId) {
            return [];
          }
          const sectionRes = await listSectionsByChapterIdUsingGet({ chapterId });
          return (sectionRes.data || []).map((section) => ({
            key: String(section.id ?? section.sectionId),
            courseId,
            chapterId,
            chapterTitle: chapter.title,
            sectionId: section.id ?? section.sectionId,
            sectionTitle: section.title,
            sectionDescription: section.description,
          }));
        }),
      );

      const sectionRows = sectionResults.flat();
      setSections(sectionRows);

      const homeworkEntries = await Promise.all(
        sectionRows.map(async (section) => [section.sectionId, await loadSectionHomework(section.sectionId)]),
      );

      setHomeworkMap(Object.fromEntries(homeworkEntries));
    } catch (error) {
      message.error('加载作业配置失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [courseId]);

  const refreshSectionHomework = async (section) => {
    if (!section?.sectionId) {
      return;
    }

    try {
      const nextList = await loadSectionHomework(section.sectionId);
      setHomeworkMap((prev) => ({
        ...prev,
        [section.sectionId]: nextList,
      }));
    } catch (error) {
      message.error('刷新作业列表失败');
    }
  };

  const openCreateModal = (section) => {
    setCurrentSection(section);
    setEditingHomework(null);
    setModalOpen(true);
    setExpandedRowKeys((prev) =>
      prev.includes(String(section.sectionId)) ? prev : [...prev, String(section.sectionId)],
    );
  };

  const openEditModal = async (section, homework) => {
    setCurrentSection(section);
    setExpandedRowKeys((prev) =>
      prev.includes(String(section.sectionId)) ? prev : [...prev, String(section.sectionId)],
    );
    setLoading(true);
    try {
      const res = await getCourseHomeworkById(homework.id);
      setEditingHomework(res.data || homework);
      setModalOpen(true);
    } catch (error) {
      message.error(error?.message || '加载作业详情失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (section, homework) => {
    if (!homework?.id) {
      return;
    }
    try {
      await deleteCourseHomework(homework.id);
      message.success('作业删除成功');
      await refreshSectionHomework(section);
    } catch (error) {
      const status = error?.response?.status;
      if (status === 404 || status === 405) {
        try {
          await deleteCourseHomeworkCompat(homework.id);
          message.success('作业删除成功');
          await refreshSectionHomework(section);
          return;
        } catch (compatError) {
          message.error(compatError?.message || '作业删除失败');
          return;
        }
      }
      message.error(error?.message || '作业删除失败');
    }
  };

  return !hasSections ? (
    <Empty description={String.fromCharCode(0x6682, 0x65e0, 0x5c0f, 0x8282, 0xff0c, 0x8bf7, 0x5148, 0x521b, 0x5efa, 0x7ae0, 0x8282, 0x548c, 0x5c0f, 0x8282)} />
  ) : (
    <>
      <Table
        rowKey="key"
        loading={loading}
        dataSource={sections}
        pagination={false}
        expandable={{
          expandedRowKeys,
          onExpandedRowsChange: (keys) => setExpandedRowKeys(keys.map((key) => String(key))),
          expandedRowRender: (section) => {
            const homeworks = homeworkMap[section.sectionId] || [];
            return (
              <Table
                rowKey="id"
                dataSource={homeworks}
                pagination={false}
                locale={{ emptyText: String.fromCharCode(0x8be5, 0x5c0f, 0x8282, 0x6682, 0x65e0, 0x4f5c, 0x4e1a, 0xff0c, 0x70b9, 0x51fb, 0x53f3, 0x4fa7, 0x201c, 0x65b0, 0x589e, 0x4f5c, 0x4e1a, 0x201d, 0x5f00, 0x59cb, 0x914d, 0x7f6e) }}
                columns={[
                  { title: '作业ID', dataIndex: 'id', width: 100 },
                  { title: '作业标题', dataIndex: 'title', ellipsis: true },
                  {
                    title: '状态',
                    width: 100,
                    render: (_, record) => {
                      const status = resolveCourseHomeworkStatus(record.status);
                      return <Tag color={status.color}>{status.label}</Tag>;
                    },
                  },
                  { title: '分值', dataIndex: 'score', width: 80, render: (value) => value ?? '-' },
                  { title: '排序', dataIndex: 'sort', width: 80, render: (value) => value ?? '-' },
                  { title: '更新时间', dataIndex: 'updateTime', width: 180, render: (value) => value || '-' },
                  {
                    title: '操作',
                    width: 160,
                    render: (_, record) => (
                      <Space>
                        <Button type="link" onClick={() => openEditModal(section, record)}>
                          {'编辑'}
                        </Button>
                        <Popconfirm title={String.fromCharCode(0x786e, 0x8ba4, 0x5220, 0x9664, 0x8be5, 0x4f5c, 0x4e1a, 0x5417, 0xff1f)} onConfirm={() => handleDelete(section, record)}>
                          <Button type="link" danger>
                            {'删除'}
                          </Button>
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]}
              />
            );
          },
        }}
        columns={[
          { title: '章节', dataIndex: 'chapterTitle', width: 220 },
          { title: '小节', dataIndex: 'sectionTitle', width: 260 },
          {
            title: '小节说明',
            dataIndex: 'sectionDescription',
            ellipsis: true,
            render: (value) => value || '-',
          },
          {
            title: '作业数量',
            width: 100,
            render: (_, record) => (homeworkMap[record.sectionId] || []).length,
          },
          {
            title: '操作',
            width: 140,
            render: (_, record) => (
              <Button type="link" onClick={() => openCreateModal(record)}>
                {'新增作业'}
              </Button>
            ),
          },
        ]}
      />

      <HomeworkEditorModal
        open={modalOpen}
        courseId={courseId}
        sectionId={currentSection?.sectionId}
        sectionTitle={currentSection?.sectionTitle}
        homework={editingHomework}
        onCancel={() => {
          setModalOpen(false);
          setEditingHomework(null);
        }}
        onSuccess={async () => {
          setModalOpen(false);
          setEditingHomework(null);
          await refreshSectionHomework(currentSection);
        }}
      />
    </>
  );
};

export default HomeworkManager;
