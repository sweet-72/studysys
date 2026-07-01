import React, { useState, useEffect } from 'react';
import { Card, Row, Col, DatePicker, Radio, Typography, Statistic, Divider, Spin, message } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import ReactECharts from 'echarts-for-react';
import { RobotOutlined, LineChartOutlined, BarChartOutlined, PieChartOutlined, UserOutlined, MessageOutlined, ClockCircleOutlined } from '@ant-design/icons';
import moment from 'moment';
import './index.less';
import {
  getAiAvatarOverview,
  getAiAvatarUsageTrend,
  getAiAvatarUserDistribution,
  getAiAvatarRatingDistribution,
  getAiAvatarConversationDuration,
} from '@/api/aiAvatarStats';

const { RangePicker } = DatePicker;
const { Title, Paragraph } = Typography;

/**
 * AI分身使用统计页面
 *
 * @constructor
 */
const AiAvatarStatistics: React.FC = () => {
  // 时间范围
  const [timeRange, setTimeRange] = useState<string>('week');
  // 图表类型
  const [chartType, setChartType] = useState<string>('usage');
  // 加载状态
  const [loading, setLoading] = useState<boolean>(false);
  
  // 统计数据
  const [overviewData, setOverviewData] = useState<{
    totalUsers: number;
    totalConversations: number;
    avgConversationDuration: number;
    avgRating: number;
  } | null>(null);
  
  // 图表数据
  const [usageTrendData, setUsageTrendData] = useState<any[]>([]);
  const [userDistributionData, setUserDistributionData] = useState<any[]>([]);
  const [ratingDistributionData, setRatingDistributionData] = useState<any[]>([]);
  const [conversationDurationData, setConversationDurationData] = useState<any[]>([]);

  // 获取统计总览数据
  const fetchOverview = async () => {
    try {
      const res = await getAiAvatarOverview();
      if (res.code === 0 && res.data) {
        setOverviewData({
          totalUsers: Number(res.data.totalUsers) || 0,
          totalConversations: Number(res.data.totalConversations) || 0,
          avgConversationDuration: Number(res.data.avgConversationDuration) || 0,
          avgRating: Number(res.data.avgRating) || 0,
        });
      } else {
        message.error(res.message || '获取统计数据失败');
      }
    } catch (error) {
      console.error('获取统计总览失败:', error);
      message.error('获取统计数据失败，请稍后重试');
    }
  };

  // 获取使用趋势数据
  const fetchUsageTrend = async (days: number) => {
    try {
      const res = await getAiAvatarUsageTrend(days);
      if (res.code === 0 && res.data) {
        // 解析嵌套的 avatarUsage 结构
        const flatData: any[] = [];
        (res.data as any).forEach((item: any) => {
          if (item.avatarUsage) {
            Object.entries(item.avatarUsage).forEach(([avatarName, count]) => {
              flatData.push({
                date: item.date,
                avatarName,
                usageCount: Number(count) || 0,
              });
            });
          }
        });
        setUsageTrendData(flatData);
      }
    } catch (error) {
      console.error('获取使用趋势失败:', error);
    }
  };

  // 获取用户分布数据
  const fetchUserDistribution = async () => {
    try {
      const res = await getAiAvatarUserDistribution();
      if (res.code === 0 && res.data) {
        setUserDistributionData(res.data);
      }
    } catch (error) {
      console.error('获取用户分布失败:', error);
    }
  };

  // 获取评分分布数据
  const fetchRatingDistribution = async () => {
    try {
      const res = await getAiAvatarRatingDistribution();
      if (res.code === 0 && res.data) {
        setRatingDistributionData(res.data);
      }
    } catch (error) {
      console.error('获取评分分布失败:', error);
    }
  };

  // 获取对话时长数据
  const fetchConversationDuration = async (days: number) => {
    try {
      const res = await getAiAvatarConversationDuration(days);
      if (res.code === 0 && res.data) {
        // 解析嵌套的 avatarDurations 结构
        const flatData: any[] = [];
        (res.data as any).forEach((item: any) => {
          if (item.avatarDurations && Array.isArray(item.avatarDurations)) {
            item.avatarDurations.forEach((dur: any) => {
              flatData.push({
                date: item.date,
                avatarName: dur.avatarName,
                avgDuration: Number(dur.avgDuration) || 0,
              });
            });
          }
        });
        setConversationDurationData(flatData);
      }
    } catch (error) {
      console.error('获取对话时长失败:', error);
    }
  };

  // 获取所有数据
  const fetchAllData = async () => {
    setLoading(true);
    const days = timeRange === 'week' ? 7 : (timeRange === 'month' ? 30 : 365);
    await Promise.all([
      fetchOverview(),
      fetchUsageTrend(days),
      fetchUserDistribution(),
      fetchRatingDistribution(),
      fetchConversationDuration(days),
    ]);
    setLoading(false);
  };

  useEffect(() => {
    fetchAllData();
  }, [timeRange]);

  // 生成使用趋势图表配置
  const generateUsageTrendOption = () => {
    if (!usageTrendData || usageTrendData.length === 0) {
      return {
        title: { text: '暂无数据', left: 'center', top: 'center' },
      };
    }

    // 按智能体分组数据
    const avatarMap = new Map<string, Map<string, number>>();
    const dateSet = new Set<string>();

    usageTrendData.forEach(item => {
      if (!item.avatarName || !item.date) return;
      if (!avatarMap.has(item.avatarName)) {
        avatarMap.set(item.avatarName, new Map());
      }
      avatarMap.get(item.avatarName)!.set(item.date, item.usageCount || 0);
      dateSet.add(item.date);
    });

    const dates = Array.from(dateSet).sort();
    const avatars = Array.from(avatarMap.keys()).filter(name => name); // 过滤空值

    const series = avatars.map((avatar, index) => {
      const dataMap = avatarMap.get(avatar)!;
      const data = dates.map(date => dataMap.get(date) || 0);
      
      // 使用不同的颜色
      const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'];
      
      return {
        name: avatar,
        type: 'line',
        data,
        smooth: true,
        itemStyle: {
          color: colors[index % colors.length],
        },
        lineStyle: {
          color: colors[index % colors.length],
          width: 2,
        },
      };
    });

    return {
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: avatars,
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
      },
      yAxis: {
        type: 'value',
        name: '使用次数',
      },
      series,
    };
  };

  // 生成用户分布图表配置
  const generateUserDistributionOption = () => {
    if (!userDistributionData || userDistributionData.length === 0) {
      return {
        title: { text: '暂无数据', left: 'center', top: 'center' },
      };
    }

    const roleNames: Record<string, string> = {
      'ADMIN': '管理员',
      'TEACHER': '教师',
      'STUDENT': '学生',
      'USER': '普通用户',
    };

    const data = userDistributionData.map(item => {
      // 优先使用 userType，如果不存在则尝试 userRole
      const roleKey = item.userType || item.userRole || '';
      // 如果 roleKey 已经是中文，或者字典中没有匹配项，则直接使用 roleKey
      const name = roleNames[roleKey] || roleKey || '未知角色';
      return {
        value: item.count || 0,
        name,
      };
    });

    return {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        left: 10,
        data: data.map(item => item.name),
      },
      series: [
        {
          name: '用户分布',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '16',
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data,
        },
      ],
    };
  };

  // 生成评分分布图表配置
  const generateRatingDistributionOption = () => {
    if (!ratingDistributionData || ratingDistributionData.length === 0) {
      return {
        title: { text: '暂无数据', left: 'center', top: 'center' },
      };
    }

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      legend: {
        data: ['平均评分', '使用次数'],
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ratingDistributionData.map(item => item.avatarName || '未知'),
      },
      yAxis: [
        {
          type: 'value',
          name: '平均评分',
          min: 0,
          max: 5,
          position: 'left',
        },
        {
          type: 'value',
          name: '使用次数',
          position: 'right',
        },
      ],
      series: [
        {
          name: '平均评分',
          type: 'bar',
          data: ratingDistributionData.map(item => Number(item.avgRating) || 0),
          itemStyle: {
            color: '#5470c6',
          },
          yAxisIndex: 0,
        },
        {
          name: '使用次数',
          type: 'line',
          data: ratingDistributionData.map(item => item.usageCount || 0),
          itemStyle: {
            color: '#91cc75',
          },
          lineStyle: {
            color: '#91cc75',
            width: 2,
          },
          yAxisIndex: 1,
        },
      ],
    };
  };

  // 生成对话时长图表配置
  const generateConversationDurationOption = () => {
    if (!conversationDurationData || conversationDurationData.length === 0) {
      return {
        title: { text: '暂无数据', left: 'center', top: 'center' },
      };
    }

    // 按智能体和日期分组
    const avatarMap = new Map<string, Map<string, number>>();
    const dateSet = new Set<string>();

    conversationDurationData.forEach(item => {
      if (!item.avatarName || !item.date) return;
      if (!avatarMap.has(item.avatarName)) {
        avatarMap.set(item.avatarName, new Map());
      }
      avatarMap.get(item.avatarName)!.set(item.date, item.avgDuration || 0);
      dateSet.add(item.date);
    });

    const dates = Array.from(dateSet).sort();
    const avatars = Array.from(avatarMap.keys()).filter(name => name); // 过滤空值

    const series = avatars.map((avatar, index) => {
      const dataMap = avatarMap.get(avatar)!;
      const data = dates.map(date => dataMap.get(date) || 0);
      
      const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'];
      
      return {
        name: avatar,
        type: 'line',
        data,
        smooth: true,
        itemStyle: {
          color: colors[index % colors.length],
        },
        lineStyle: {
          color: colors[index % colors.length],
          width: 2,
        },
        areaStyle: {
          color: colors[index % colors.length],
          opacity: 0.1,
        },
      };
    });

    return {
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: avatars,
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
      },
      yAxis: {
        type: 'value',
        name: '平均时长(分钟)',
      },
      series,
    };
  };

  // 渲染图表
  const renderChart = () => {
    let option;
    switch (chartType) {
      case 'usage':
        option = generateUsageTrendOption();
        break;
      case 'userDistribution':
        option = generateUserDistributionOption();
        break;
      case 'feedback':
        option = generateRatingDistributionOption();
        break;
      case 'duration':
        option = generateConversationDurationOption();
        break;
      default:
        option = generateUsageTrendOption();
    }

    return (
      <ReactECharts
        option={option}
        style={{ height: 400 }}
        key={`${chartType}-${timeRange}`}
        notMerge={true}
        lazyUpdate={true}
      />
    );
  };

  // 总统计数据
  const totalStats = [
    { title: '总用户数', value: overviewData?.totalUsers || 0, icon: <UserOutlined /> },
    { title: '总对话数', value: overviewData?.totalConversations || 0, icon: <MessageOutlined /> },
    { 
      title: '平均对话时长', 
      value: `${(overviewData?.avgConversationDuration || 0).toFixed(1)}分钟`, 
      icon: <ClockCircleOutlined /> 
    },
    { 
      title: '平均评分', 
      value: (overviewData?.avgRating || 0).toFixed(1), 
      icon: <PieChartOutlined /> 
    },
  ];

  return (
    <PageContainer
      header={{
        title: (
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <RobotOutlined style={{ fontSize: 24, marginRight: 8, color: '#1890ff' }} />
            <span>AI分身使用统计</span>
          </div>
        ),
      }}
    >
      <Spin spinning={loading}>
        <div className="ai-avatar-statistics">
          <Card>
            <Row gutter={[16, 16]} className="stat-summary">
              {totalStats.map((stat, index) => (
                <Col xs={24} sm={12} md={6} key={index}>
                  <Card variant="borderless">
                    <Statistic 
                      title={
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                          <span style={{ marginRight: 8, color: '#1890ff' }}>{stat.icon}</span>
                          {stat.title}
                        </div>
                      }
                      value={stat.value}
                    />
                  </Card>
                </Col>
              ))}
            </Row>

            <Divider />

            <div className="chart-controls">
              <div className="control-group">
                <span className="control-label">时间范围:</span>
                <Radio.Group 
                  value={timeRange} 
                  onChange={(e) => setTimeRange(e.target.value)}
                  buttonStyle="solid"
                >
                  <Radio.Button value="week">本周</Radio.Button>
                  <Radio.Button value="month">本月</Radio.Button>
                  <Radio.Button value="year">本年</Radio.Button>
                </Radio.Group>
              </div>

              <div className="control-group">
                <span className="control-label">数据类型:</span>
                <Radio.Group 
                  value={chartType} 
                  onChange={(e) => setChartType(e.target.value)}
                  buttonStyle="solid"
                >
                  <Radio.Button value="usage"><LineChartOutlined /> 使用趋势</Radio.Button>
                  <Radio.Button value="userDistribution"><PieChartOutlined /> 用户分布</Radio.Button>
                  <Radio.Button value="feedback"><BarChartOutlined /> 评分分布</Radio.Button>
                  <Radio.Button value="duration"><BarChartOutlined /> 对话时长</Radio.Button>
                </Radio.Group>
              </div>
            </div>

            <div className="chart-container">
              {renderChart()}
            </div>
          </Card>
        </div>
      </Spin>
    </PageContainer>
  );
};

export default AiAvatarStatistics;
