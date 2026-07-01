export default [
  {
    path: '/user',
    layout: false,
    routes: [
      { path: '/user/login', component: './User/Login' },
      { path: '/user/register', component: './User/Register' },
    ],
  },

  { path: '/welcome', icon: 'smileOutlined', component: './Welcome', name: '欢迎' },
  { path: '/datapanel', icon: 'fundTwoTone', component: './DataPanel', name: '数据看板' },
  { path: '/admin/user', icon: 'userOutlined', component: './Admin/User', name: '用户管理', access: 'canAdmin' },
  { path: '/admin/teacher', icon: 'teamOutlined', component: './Admin/Teacher', name: '讲师管理', access: 'canAdmin' },
  {
    path: '/admin/classManagement',
    icon: 'teamOutlined',
    component: './Admin/Class',
    name: '班级管理',
    hideInMenu: true,
    access: 'canAdmin',
  },
  { path: '/admin/postManagement', icon: 'formOutlined', component: './Admin/Post', name: '帖子管理', access: 'canAdmin' },
  { path: '/admin/postDetail/:id', component: './Admin/Post/Detail', name: '帖子详情', hideInMenu: true, access: 'canAdmin' },
  {
    path: '/admin/courseManagement',
    icon: 'playCircleOutlined',
    name: '课程管理',
    access: 'canCourseManager',
    routes: [
      { path: '/admin/courseManagement', component: './Admin/Course', name: '课程列表' },
      { path: '/admin/courseManagement/category', component: './Admin/Course/CategoryManagement', name: '课程分类管理' },
      { path: '/admin/courseManagement/detail/:id', component: './Admin/Course/CourseDetail', name: '课程详情', hideInMenu: true },
      { path: '/admin/courseManagement/chapter', component: './Admin/Course/ChapterManagement', name: '章节管理', hideInMenu: true },
      { path: '/admin/courseManagement/section', component: './Admin/Course/SectionManagement', name: '小节管理', hideInMenu: true },
      { path: '/admin/courseManagement/review', component: './Admin/Course/CourseReview', name: '作业批改', hideInMenu: true },
    ],
  },
  {
    path: '/admin/aiAvatarManagement',
    icon: 'robotOutlined',
    name: '智能体管理',
    access: 'canAdmin',
    routes: [
      { path: '/admin/aiAvatarManagement/list', component: './Admin/AiAvatar', name: '智能体列表' },
      { path: '/admin/aiAvatarManagement/statistics', component: './Admin/AiAvatar/Statistics', name: '智能体统计' },
    ],
  },
  {
    path: '/admin/dailyWord',
    icon: 'bookOutlined',
    name: '每日单词管理',
    access: 'canAdmin',
    routes: [
      { path: '/admin/dailyWord', component: './Admin/DailyWord', name: '单词列表' },
      { path: '/admin/dailyWord/import', component: './Admin/DailyWord/Import', name: '批量导入' },
    ],
  },
  {
    path: '/admin/dailyArticleManagement',
    icon: 'fileTextOutlined',
    name: '每日美文管理',
    access: 'canAdmin',
    routes: [
      { path: '/admin/dailyArticleManagement', component: './Admin/DailyArticle', name: '美文管理', hideInMenu: true },
      { path: '/admin/dailyArticleManagement/view', component: './DailyArticle', name: '美文展示' },
      {
        path: '/admin/dailyArticleManagement/detail/:id',
        component: './DailyArticle/detail',
        name: '文章详情',
        hideInMenu: true,
      },
    ],
  },
  {
    path: '/admin/feedbackManagement',
    icon: 'commentOutlined',
    component: './Admin/Feedback',
    name: '用户反馈管理',
    access: 'canAdmin',
  },

  { path: '/course/detail/:id', component: './Admin/Course/CourseDetail', hideInMenu: true, access: 'canCourseManager' },

  { path: '/', redirect: '/welcome' },
  { path: '*', layout: false, component: './404' },
];
