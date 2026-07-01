import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Divider, Form, Input, InputNumber, Space, Typography } from 'antd';
import type { FormInstance } from 'antd/es/form';
import type { CourseUpsertPayload } from '@/api/course-admin';
import SectionVideoInput from './SectionVideoInput';

const { Text } = Typography;

interface ChapterSectionEditorProps {
  form: FormInstance<CourseUpsertPayload>;
  disabled?: boolean;
  courseId?: number;
}

const ChapterSectionEditor: React.FC<ChapterSectionEditorProps> = ({ form, disabled, courseId }) => {
  return (
    <Form.List
      name="chapters"
      rules={[
        {
          validator: async (_, chapters) => {
            if (!Array.isArray(chapters) || chapters.length === 0) {
              throw new Error('章节不能为空，请至少添加一个章节');
            }
          },
        },
      ]}
    >
      {(chapterFields, chapterOperators, chapterMeta) => (
        <Space direction="vertical" style={{ width: '100%' }} size={16}>
          {chapterFields.map((chapterField, chapterIndex) => (
            <Card
              key={chapterField.key}
              title={`章节 ${chapterIndex + 1}`}
              extra={
                <Button
                  danger
                  type="text"
                  icon={<DeleteOutlined />}
                  onClick={() => chapterOperators.remove(chapterField.name)}
                  disabled={disabled}
                >
                  删除章节
                </Button>
              }
            >
              <Form.Item name={[chapterField.name, 'chapterId']} hidden>
                <Input />
              </Form.Item>

              <Space style={{ width: '100%' }} size={12} align="start" wrap>
                <Form.Item
                  label="章节标题"
                  name={[chapterField.name, 'title']}
                  rules={[{ required: true, message: '章节标题不能为空' }]}
                  style={{ minWidth: 320, flex: 1 }}
                >
                  <Input placeholder="请输入章节标题" disabled={disabled} />
                </Form.Item>
                <Form.Item label="章节排序" name={[chapterField.name, 'sort']} initialValue={chapterIndex + 1}>
                  <InputNumber min={1} precision={0} disabled={disabled} style={{ width: 140 }} />
                </Form.Item>
              </Space>

              <Form.Item label="章节描述" name={[chapterField.name, 'description']}>
                <Input.TextArea rows={2} placeholder="请输入章节描述（选填）" disabled={disabled} />
              </Form.Item>

              <Divider style={{ margin: '12px 0' }}>小节管理</Divider>

              <Form.List
                name={[chapterField.name, 'sections']}
                rules={[
                  {
                    validator: async (_, sections) => {
                      if (!Array.isArray(sections) || sections.length === 0) {
                        throw new Error('小节不能为空，请至少添加一个小节');
                      }
                    },
                  },
                ]}
              >
                {(sectionFields, sectionOperators, sectionMeta) => (
                  <Space direction="vertical" style={{ width: '100%' }} size={12}>
                    {sectionFields.map((sectionField, sectionIndex) => (
                      <Card
                        key={sectionField.key}
                        size="small"
                        type="inner"
                        title={`小节 ${chapterIndex + 1}.${sectionIndex + 1}`}
                        extra={
                          <Button
                            danger
                            type="text"
                            icon={<DeleteOutlined />}
                            onClick={() => sectionOperators.remove(sectionField.name)}
                            disabled={disabled}
                          >
                            删除小节
                          </Button>
                        }
                      >
                        <Form.Item name={[sectionField.name, 'sectionId']} hidden>
                          <Input />
                        </Form.Item>

                        <Space style={{ width: '100%' }} size={12} align="start" wrap>
                          <Form.Item
                            label="小节标题"
                            name={[sectionField.name, 'title']}
                            rules={[{ required: true, message: '小节标题不能为空' }]}
                            style={{ minWidth: 320, flex: 1 }}
                          >
                            <Input placeholder="请输入小节标题" disabled={disabled} />
                          </Form.Item>
                          <Form.Item
                            label="小节排序"
                            name={[sectionField.name, 'sort']}
                            initialValue={sectionIndex + 1}
                          >
                            <InputNumber min={1} precision={0} disabled={disabled} style={{ width: 140 }} />
                          </Form.Item>
                          <Form.Item label="时长(秒)" name={[sectionField.name, 'duration']}>
                            <InputNumber min={0} precision={0} disabled={disabled} style={{ width: 140 }} />
                          </Form.Item>
                        </Space>

                        <Form.Item label="小节描述" name={[sectionField.name, 'description']}>
                          <Input.TextArea rows={2} placeholder="请输入小节描述（选填）" disabled={disabled} />
                        </Form.Item>

                        <SectionVideoInput
                          form={form}
                          videoUrlName={['chapters', chapterField.name, 'sections', sectionField.name, 'videoUrl']}
                          localVideoPathName={[
                            'chapters',
                            chapterField.name,
                            'sections',
                            sectionField.name,
                            'localVideoPath',
                          ]}
                          courseId={courseId}
                          chapterId={form.getFieldValue(['chapters', chapterField.name, 'chapterId'])}
                          sectionId={form.getFieldValue([
                            'chapters',
                            chapterField.name,
                            'sections',
                            sectionField.name,
                            'sectionId',
                          ])}
                          disabled={disabled}
                        />

                        <Form.Item
                          name={[sectionField.name, 'videoResourceCheck']}
                          hidden
                          rules={[
                            {
                              validator: async () => {
                                const currentSection = form.getFieldValue([
                                  'chapters',
                                  chapterField.name,
                                  'sections',
                                  sectionField.name,
                                ]);
                                if (currentSection?.videoUrl || currentSection?.localVideoPath) {
                                  return;
                                }
                                throw new Error('每个小节必须填写 videoUrl 或 localVideoPath 至少一个');
                              },
                            },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Card>
                    ))}

                    <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      onClick={() => sectionOperators.add({ title: '', sort: sectionFields.length + 1 })}
                      disabled={disabled}
                      block
                    >
                      新增小节
                    </Button>

                    <Form.ErrorList errors={sectionMeta.errors} />
                  </Space>
                )}
              </Form.List>
            </Card>
          ))}

          <Button
            type="dashed"
            icon={<PlusOutlined />}
            onClick={() => chapterOperators.add({ title: '', sort: chapterFields.length + 1, sections: [] })}
            disabled={disabled}
            block
          >
            新增章节
          </Button>

          <Form.ErrorList errors={chapterMeta.errors} />
          <Text type="secondary">校验要求：讲师必填、章节不能为空、小节不能为空、每个小节必须有视频 URL 或本地视频路径。</Text>
        </Space>
      )}
    </Form.List>
  );
};

export default ChapterSectionEditor;
