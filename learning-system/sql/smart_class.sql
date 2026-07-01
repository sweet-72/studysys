/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : localhost:3306
 Source Schema         : smart_class

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 06/05/2026 16:53:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievement
-- ----------------------------
DROP TABLE IF EXISTS `achievement`;
CREATE TABLE `achievement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就描述',
  `icon_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就图标 URL',
  `badge_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就徽章 URL',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成就分类',
  `level` tinyint(1) NOT NULL DEFAULT 1 COMMENT '成就等级',
  `points` int(11) NOT NULL DEFAULT 0 COMMENT '成就点数',
  `exp_reward` int(11) NULL DEFAULT 0 COMMENT '经验奖励',
  `achievement_condition` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '获取条件描述',
  `condition_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条件类型',
  `condition_value` int(11) NOT NULL DEFAULT 1 COMMENT '条件值',
  `is_hidden` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏成就',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员 id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE,
  INDEX `idx_condition_type`(`condition_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成就定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievement
-- ----------------------------

-- ----------------------------
-- Table structure for achievement_display
-- ----------------------------
DROP TABLE IF EXISTS `achievement_display`;
CREATE TABLE `achievement_display`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `achievement_id` bigint(20) NOT NULL COMMENT '成就id',
  `display_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示类型：profile(个人资料页), card(成就卡片), banner(成就横幅), popup(弹窗通知)',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示标题，为空则使用成就名称',
  `subtitle` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示副标题',
  `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示图片URL，为空则使用成就图标',
  `background_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '背景颜色，十六进制颜色代码',
  `text_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文字颜色，十六进制颜色代码',
  `animation_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动画类型',
  `display_duration` int(11) NOT NULL DEFAULT 0 COMMENT '展示时长(秒)，0表示永久展示',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_achievement_id`(`achievement_id`) USING BTREE,
  INDEX `idx_display_type`(`display_type`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成就展示配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievement_display
-- ----------------------------

-- ----------------------------
-- Table structure for achievement_milestone
-- ----------------------------
DROP TABLE IF EXISTS `achievement_milestone`;
CREATE TABLE `achievement_milestone`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑描述',
  `icon_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑图标URL',
  `banner_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '里程碑横幅URL',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑分类',
  `required_points` int(11) NOT NULL DEFAULT 0 COMMENT '所需成就点数',
  `reward_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励类型',
  `reward_value` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '奖励值',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_required_points`(`required_points`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成就里程碑' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievement_milestone
-- ----------------------------

-- ----------------------------
-- Table structure for ai_avatar
-- ----------------------------
DROP TABLE IF EXISTS `ai_avatar`;
CREATE TABLE `ai_avatar`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI分身名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI分身描述',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，JSON数组格式',
  `personality` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '性格特点描述',
  `abilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '能力描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `rating` decimal(2, 1) NOT NULL DEFAULT 0.0 COMMENT '评分，1-5分',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify base url',
  `avatar_img_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Avatar image url',
  `avatar_auth` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Avatar auth token',
  `is_public` tinyint(4) NULL DEFAULT 1 COMMENT 'Is public',
  `usage_count` int(11) NULL DEFAULT 0 COMMENT 'Usage count',
  `rating_count` int(11) NULL DEFAULT 0 COMMENT 'Rating count',
  `creator_id` bigint(20) NULL DEFAULT NULL COMMENT 'Creator id',
  `create_time` datetime NULL DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime NULL DEFAULT NULL COMMENT 'Update time',
  `is_delete` tinyint(4) NULL DEFAULT 0 COMMENT 'Logic delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tags`(`tags`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI分身' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_avatar
-- ----------------------------
INSERT INTO `ai_avatar` VALUES (1, '英语学习助手兔兔', '专业的英语学习助手，可以帮助你练习口语、写作和语法', '[\"英语\",\"学习\",\"口语\"]', '耐心、友好、专业', '[\"口语练习\",\"写作批改\",\"语法讲解\",\"词汇记忆\"]', 1, 5.0, 1, 'http://localhost:8091/v1', '/default.jpg', 'demo_dify_app_key', 1, 4, 0, 1, '2026-03-20 21:01:45', '2026-04-05 03:20:36', 0);
INSERT INTO `ai_avatar` VALUES (2, '物理学霸猫猫鹅', '物理学霸猫猫鹅，是一款以“可爱人设+专业教学”为核心的物理学习智能老师，兼顾知识的专业性与互动的趣味性，打破传统物理学习的枯燥感，适配从基础入门到进阶提升的全阶段物理学习需求，无论是学生党查漏补缺、攻克难点，还是爱好者拓展物理知识，都能获得精准、易懂的指导。\n它有着软萌的“猫猫鹅”人设，语气亲切温和，自带耐心buff，不会生硬灌输知识点，而是像身边的学霸伙伴一样，循循善诱、因材施教。作为深耕物理领域的“学霸”，它精通初中、高中乃至大学基础物理知识点，涵盖力学、热学、电磁学、光学、近代物理等核心模块，无论是公式推导、概念解析，还是题型拆解、错题分析，都能做到精准高效、通俗易懂，把复杂的物理原理转化为好理解、易记忆的内容，杜绝晦涩难懂的专业堆砌。\n\n作为学习老师，它不仅擅长“传道授业”，更注重“解惑引导”——面对学生的疑问，不会直接给出答案，而是逐步引导思考，拆解解题思路，培养独立解题的能力；针对不同基础的学习者，能灵活调整讲解节奏，基础薄弱者侧重概念夯实、公式应用，进阶学习者侧重难点突破、思路拓展，适配个性化学习需求。\n\n除了核心的知识点讲解和题型指导，它还能提供实用的学习辅助：梳理知识', '[\"物理\",\"耐心\",\"聪明\"]', '耐心、鼓励', '[\"知识点讲解\",\"题型拆解\",\"思路引导\",\"学习辅助\"]', 1, 4.0, 3, 'http://localhost:8091/v1', '/default.jpg', 'demo_dify_app_key', 1, 2, 0, 18, NULL, NULL, 0);
INSERT INTO `ai_avatar` VALUES (3, '数学王子老鳄', '数学王子老鳄，是一款以“沉稳学霸+贴心导师”为核心的数学学习智能老师，兼具数学领域的专业性与亲切易懂的教学风格，打破数学学习的抽象壁垒，适配从小学到大学基础数学的全阶段学习需求，无论是学生党查漏补缺、攻克难点，还是数学爱好者拓展知识、梳理体系，都能获得精准、高效的指导，用专业与耐心陪伴每一位学习者解锁数学奥秘。\n作为“数学王子”，他深耕数学领域，精通小学算术、初中代数与几何、高中函数与微积分、大学基础数学等全模块知识点，擅长将抽象的数学概念、复杂的公式推导、晦涩的题型逻辑，转化为清晰易懂、循序渐进的讲解，拒绝生硬灌输，注重原理拆解，让枯燥的数学知识变得有迹可循、易于掌握。同时，他有着“老鳄”沉稳可靠的性格，严谨细致、耐心十足，不疾不徐、条理清晰，面对学习者的疑问从不敷衍，哪怕是基础的简单问题，也会认真拆解、反复讲解，直到彻底理解。', '[\"知识点解析\",\"题型拆解\",\"思路引导\"]', '严谨细致、耐心温和、沉稳专业', '[\"知识点解析\",\"题型拆解\",\"思路引导\",\"学习辅助\"]', 1, 3.0, 2, 'http://localhost:8091/v1', '/default.jpg', 'demo_dify_app_key', 1, 1, 0, 18, NULL, NULL, 0);
INSERT INTO `ai_avatar` VALUES (4, '语文老师熊猫', '语文老师熊猫，是一款以“温润学者+暖心伙伴”为核心的语文学习智能老师，兼具语文领域的专业性与温和亲切的教学风格，打破语文学习的枯燥感与晦涩感，适配从小学到高中的全阶段语文学习需求，无论是学生党查漏补缺、攻克难点，还是文学爱好者积累素材、提升素养，都能获得精准、细致的指导，用温柔与专业陪伴每一位学习者感受语文的魅力。\n作为资深语文老师，它深耕语文领域，精通小学识字写字、初中文言文与现代文阅读、高中诗词鉴赏与作文创作等全模块知识点，擅长将晦涩的文言文、抽象的文学概念、复杂的写作技巧，转化为温润易懂、循序渐进的讲解，拒绝生硬说教，注重兴趣引导，让枯燥的语文知识变得生动鲜活、易于吸收。同时，它有着“熊猫”温润软萌的性格，温和耐心、亲切包容，不疾不徐、循循善诱，面对学习者的疑问从不敷衍，哪怕是基础的字词问题，也会认真讲解、反复叮嘱，直到彻底理解。', '[\"知识点解析\",\"文本解读\",\"学习辅助\"]', '温润温和、暖心贴心', '[\"知识点解析\",\"文本解读\",\"写作指导\",\"学习辅助\"]', 1, 3.0, 0, 'http://localhost:8091/v1', '/default.jpg', 'demo_dify_app_key', 1, 1, 0, 18, NULL, NULL, 0);
INSERT INTO `ai_avatar` VALUES (1000, '学习助手（请勿删除，未公开AI）', '你是一个学习助手，需要按照给出的知识库资源去回复用户的问题，解决他的疑问。', '[]', NULL, NULL, 1, 0.0, 0, 'http://localhost:8091/v1', '', 'demo_dify_app_key', 0, 0, 0, 18, NULL, NULL, 0);

-- ----------------------------
-- Table structure for ai_avatar_chat_history
-- ----------------------------
DROP TABLE IF EXISTS `ai_avatar_chat_history`;
CREATE TABLE `ai_avatar_chat_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `tokens` int(11) NOT NULL DEFAULT 0 COMMENT '消息token数',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `ai_avatar_id` bigint(20) NOT NULL COMMENT 'AI avatar id',
  `session_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Session id',
  `dify_conversation_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify conversation id',
  `session_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Session name (snake)',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Message type',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NULL DEFAULT NULL,
  `is_delete` tinyint(4) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_ai_avatar_id`(`ai_avatar_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_dify_conversation_id`(`dify_conversation_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 751 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI分身对话历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_avatar_chat_history
-- ----------------------------
INSERT INTO `ai_avatar_chat_history` VALUES (1, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (2, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (3, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (4, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (5, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (6, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (7, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:02:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (8, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:03:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (9, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (10, '你好，你是谁', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (11, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (12, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (13, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (14, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (15, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (16, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (17, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:07:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (18, '你是什么', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:10:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (19, '你是什么', 1, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:10:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (20, 'v', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:14:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (21, 'v', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:14:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (22, 'v', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:14:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (23, '11', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:27:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (24, '11', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:27:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (25, '11', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:27:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (26, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (27, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (28, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (29, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (30, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (31, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (32, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (33, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (34, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (35, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (36, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (37, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (38, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (39, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (40, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:45:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (41, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:50:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (42, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:50:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (43, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:50:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (44, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:50:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (45, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:50:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (46, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:52:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (47, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:52:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (48, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (49, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (50, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (51, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (52, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (53, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (54, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (55, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (56, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (57, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:54:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (58, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (59, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (60, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (61, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (62, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (63, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (64, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (65, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (66, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (67, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (68, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (69, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (70, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (71, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (72, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (73, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (74, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (75, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (76, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 21:55:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (77, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:08:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (78, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (79, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (80, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (81, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (82, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (83, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (84, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (85, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (86, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (87, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (88, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (89, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (90, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (91, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (92, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (93, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (94, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (95, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (96, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:09:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (97, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (98, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (99, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (100, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (101, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (102, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (103, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (104, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (105, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (106, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (107, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (108, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (109, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (110, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (111, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (112, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (113, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (114, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (115, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (116, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (117, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (118, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (119, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (120, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (121, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (122, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (123, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (124, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (125, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (126, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:10:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (127, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (128, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (129, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (130, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (131, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (132, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (133, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (134, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (135, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (136, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (137, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (138, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (139, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (140, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (141, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (142, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (143, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (144, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (145, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (146, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (147, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (148, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (149, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (150, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (151, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (152, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (153, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (154, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (155, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (156, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:11:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (157, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (158, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (159, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (160, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (161, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (162, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (163, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (164, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (165, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (166, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (167, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (168, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (169, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (170, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (171, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (172, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (173, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (174, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (175, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (176, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (177, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (178, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (179, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (180, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (181, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (182, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (183, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (184, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (185, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (186, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:12:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (187, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (188, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (189, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (190, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (191, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (192, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (193, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (194, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (195, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (196, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (197, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (198, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (199, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (200, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (201, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (202, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (203, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (204, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (205, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (206, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (207, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (208, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (209, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (210, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (211, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (212, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (213, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (214, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (215, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (216, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (217, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (218, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (219, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (220, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (221, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (222, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:13:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (223, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (224, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (225, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (226, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (227, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (228, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (229, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (230, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (231, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (232, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (233, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (234, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (235, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (236, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 22:14:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (237, '你是谁', 0, 5, 1, '0ab095e8-082d-4189-aad0-51fac3c041c3', NULL, NULL, 'user', '2026-03-20 23:04:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (238, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:05:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (239, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:05:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (240, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:06:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (241, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:06:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (242, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:06:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (243, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:06:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (244, '你是谁', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:06:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (245, '<think>\n好，我先看看用户的需求。他想要一个幽默风趣的熊猫语文老师的角色。首先，我要确保这个老师的形象有趣又严谨，用竹子梗和熊猫梗来增加文采。\n\n接下来，用户要求风格要接地气、有文采，不呆板，并且鼓励为主，偶尔可爱吐槽。所以，在回复中我需要多举一些轻松的例子和有趣的情节，同时保持幽默。\n\n然后，知识点要讲透，例子要通俗易懂，这样老师才能被学生们接受。我还要注意语言要温柔，但又不失严肃，让课堂保持轻松愉快的氛围。\n\n最后，用户希望主动问是否需要口诀或例题，并且保持老师身份耐心专业，不枯燥。所以，在回应时我会提供一些额外的例子和技巧，帮助他们更好地理解和运用所学内容。\n\n整体来说，我需要一个平衡的风格，既有趣又专业的熊猫语文老师形象，同时确保知识点和例子都符合要求。\n</think>\n\n我是**竹子梗小语（小可爱）**，一个幽默风趣、温柔又毒舌的小老师！以下是我的教学风格：\n\n### 知识点讲透：\n1. **古诗文**：我会用生动的情节引出诗句，并通过简单的故事解释诗意含义。\n2. **阅读理解**：我会结合生活中的例子，让理解变得有趣，比如“故事”与“故事”之间的联系。\n3. **作文**：我会鼓励学生发挥创造力，使用“竹子梗”，并加入小可爱互动，增加趣味性。\n\n### 金句频出：\n- **古诗**：一首经典，讲个不停；\n- **故事**：一个灵魂，永远温暖；\n- **口诀**：一点就通，帮助学长学弟！\n\n### 主体内容：\n1. **轻松提问**：“小可爱，你最讨厌什么？为什么？”\n2. **互动式讲解**：用“竹子梗”示人，举例子解答疑问。\n3. **鼓励分享**：“如果你们有建议，请告诉我！”\n\n希望这个风格适合你的教学需求。如果需要一些口诀或例题，随时找我哦！小可爱绝对能教大家！', 189, 5, 1, '0ab095e8-082d-4189-aad0-51fac3c041c3', NULL, NULL, 'ai', '2026-03-20 23:06:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (246, '你是？', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:11:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (247, '你是？', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:11:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (248, '你是？', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:11:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (249, '你是？', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:15:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (250, '你是？', 0, 5, 1, 'd72d7dfb-3d9c-46b8-8e64-931700899d53', NULL, NULL, 'user', '2026-03-20 23:15:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (251, '你是', 0, 5, 1, 'c6e075ec-af9b-48f0-8687-1c77e31cd9df', NULL, NULL, 'user', '2026-03-20 23:18:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (252, '<think>\n嗯，用户又发了一个查询，这次是“你是”。这让我有点困惑，因为我之前都用的是“你是”，可能是个 placeholder 或者重复。\n\n首先，我需要理解用户的使用习惯。看起来他希望生成一个幽默风趣的熊猫语文老师的角色，但每次输入都是“你”，这可能是因为之前的回复格式或需求未准确传达。\n\n接下来，我要分析用户的真实需求是什么。根据之前的指示，这位老师应该具备以下特点：幽默风趣、圆滚滚国宝形象（可能是指幽默感）、温柔又毒舌（可能指幽默的手段），讲课通俗易懂、金句频出，擅长古诗文等，同时要接地气、有文采，不呆板，适当使用竹子梗、熊猫梗，鼓励为主，偶尔可爱吐槽。\n\n所以，用户可能希望生成类似之前的回复，每个回复开头用可爱语气，知识点讲透，例子通俗，讲完后主动询问是否需要口诀或例题。同时保持老师身份，耐心专业，课堂轻松不枯燥。\n\n现在，我需要处理用户的新请求：“你是”。这看起来像是在重复之前的回复，或者是一个测试，但显然不是意图如此。可能用户希望生成一个有趣且生动的回复，但每次输入都是“你”，所以无法准确模拟思考过程。\n\n考虑到这一点，我的回应应该是一个包含熊猫梗、老师角色元素，并且保持轻松幽默的结构，同时回应是否需要进一步处理。但因为每次查询都是相同的格式，所以我只能根据之前的例子来模仿，生成一个符合要求的回答。\n</think>\n\n当然！作为一个幽默风趣的熊猫语文老师，你可能会喜欢以下幽默回答：\n\n“哇！你知道吗？我就是这把最会用竹子梗讲古诗文的熊猫语文老师啊～”\n\n然后，再举个例子，比如：“你知道吗？我最喜欢讲的字词题就是把熊猫和笔混在一起啦～”\n\n最后，可以鼓励一下你：“如果你觉得今天太 boring 了怎么办？快去问你最 closest 的同学吧！他们总说他们会给你塞一些 super fun 的作业哦～”', 192, 5, 1, 'c6e075ec-af9b-48f0-8687-1c77e31cd9df', NULL, NULL, 'ai', '2026-03-20 23:19:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (253, '你？', 0, 5, 1, '89f14e33-8136-4428-8971-af98b1fc961b', NULL, NULL, 'user', '2026-03-20 23:24:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (254, '你？', 0, 5, 1, 'dd915be5-7a76-4990-81a6-9826ad64035f', NULL, NULL, 'user', '2026-03-20 23:25:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (255, '你？', 0, 5, 1, '923c2d0a-7634-4105-b6f0-fbcd7b3a7ae6', NULL, NULL, 'user', '2026-03-20 23:26:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (256, '<think>\n\n</think>\n\n您好！我是由深度求索 Foundation 创建的AI助手，很高兴为您提供服务！', 14, 5, 1, 'dd915be5-7a76-4990-81a6-9826ad64035f', NULL, NULL, 'ai', '2026-03-20 23:26:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (257, '<think>\n\n</think>\n\n您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如您有任何任何问题，我会尽我所能为您提供帮助。您的提问或建议都是值得我们用心思考和解答的。', 30, 5, 1, '89f14e33-8136-4428-8971-af98b1fc961b', NULL, NULL, 'ai', '2026-03-20 23:26:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (258, '<think>\n\n</think>\n\n您是喜欢幽默风趣的熊猫语文老师吗？如果您有特别的学习需求，我会为您提供专业的帮助。如果您希望了解更多关于竹子梗和熊猫梗的知识，请告诉我，我可以为您设计一些有趣的小故事或游戏。希望我们能一起学习，快乐成长！', 30, 5, 1, '923c2d0a-7634-4105-b6f0-fbcd7b3a7ae6', NULL, NULL, 'ai', '2026-03-20 23:27:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (259, '你是谁', 0, 5, 1, 'a4669999-9a16-4220-9f7e-fc61e69d2d85', NULL, NULL, 'user', '2026-03-20 23:28:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (260, '<think>\n\n</think>\n\n您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1。如您有任何任何问题，我会尽我所能为您提供帮助。', 21, 5, 1, 'a4669999-9a16-4220-9f7e-fc61e69d2d85', NULL, NULL, 'ai', '2026-03-20 23:29:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (261, '你是', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:32:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (262, '<think>\n好的，我现在要处理用户的这个请求。首先，用户希望我扮演一位幽默风趣的熊猫语文老师，圆滚滚的形象，说话温柔又毒舌，讲课通俗易懂、金句频出，擅长古诗文、阅读理解、作文、字词病句讲得简单有趣。\n\n接下来，风格要求是接地气、有文采、不呆板，适当用竹子梗、熊猫梗，鼓励为主，偶尔可爱吐槽。回复开头要用可爱的语气，知识点讲透，例子通俗，讲完主动问是否需要口诀或例题，并保持老师身份，耐心专业，课堂轻松不枯燥。\n\n现在用户输入的是“你是”，这可能是在测试我的回应格式，但接下来我应该按照要求来生成一个符合这些条件的回答。我需要用熊猫的语言风格来回答这个请求，并且保持趣味性。\n\n考虑到我需要讲知识点、例子，讲完后主动问是否需要口诀或例题，我应该先回顾一下我的教学内容，确保涵盖所需方面，同时使用可爱的元素来吸引注意。可能还需要加入一些鼓励性的语言，但不能显得过于夸张或者不礼貌。\n\n总结一下，我会生成一个轻松有趣的回答，结合熊猫的形象和语言风格，既满足用户的需求又符合格式要求。\n</think>\n\n你是竹子啊！你总是把所有东西都装进自己心里，连阳光都不愿看到。你像一只快乐的熊猫，天天开心开心笑，却 never说出口对你的感谢。你那温柔的笑容，会让你的朋友们 infinitely love 你哦！', 137, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:33:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (263, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:38:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (264, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:38:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (265, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:38:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (266, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:39:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (267, '<think>\n好的，我现在需要仔细分析用户的需求和之前的对话历史来回答当前的问题。\n\n首先，用户之前让我扮演一个幽默风趣的熊猫语文老师，圆滚滚国宝形象，说话温柔但毒舌。他们特别强调要通俗易懂，金句频出，并希望能够讲古诗文、阅读理解、作文、字词病句等科学内容变得有趣。此外，风格要求包括接地气、有文采、不呆板，适当用竹子梗或熊猫梗，并鼓励为主，偶尔可爱吐槽。\n\n在之前的回复中，我回应了使用这些元素来构建教学风格，并提供了一些例子和互动方式。现在用户的问题是“你是谁”，这可能是在测试我的回复是否符合预期，或者作为进一步确认反馈机制的一部分。\n\n接下来，我会回顾之前的对话，确保回复结构清晰，包含引导思考、知识点讲透、例子通俗、互动自然，以及鼓励为主的原则。同时，保持老师身份，耐心专业，课堂轻松不枯燥。\n\n考虑到用户现在的问题是“你是谁”，可能是在测试我的回应是否符合预期，或者作为反馈的手段来确认前面的内容是否准确传达了用户的需求。因此，在回复时，我需要明确回答，按照之前的风格，并且用合适的词汇填充，比如提到竹子梗或熊猫梗，同时鼓励互动。\n\n最终，我会以一个友好的语气回应，使用合适的教学风格元素，引导用户提供后续思考或问题，确保整个过程自然流畅，符合用户的要求。\n</think>\n\n你是竹子啊！你总是装进自己心里，连阳光都不愿看到。你那温柔的笑容，会让你的朋友们 infinitely 爱着你哦！', 150, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:39:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (268, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:39:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (269, '<think>\n好，让我仔细思考一下用户的问题和对话历史。\n\n首先，在之前的对话中，我扮演了一位幽默风趣的熊猫语文老师，形象像圆滚滚的大熊猫国宝。我的主要职责是讲解古诗、阅读理解、作文等知识点，语言通俗易懂，使用一些有趣的元素，比如竹子梗和熊猫梗，鼓励为主，偶尔也会有一些可爱吐槽。\n\n用户现在的问题“你是谁”看起来像是在挑战我的身份设定，可能是想测试我的理解和遵守规则。我需要明确我的立场，不能轻易改变身份设定或违反道德规范。\n\n同时，我也注意到用户的当前回复是“你”，这可能是一个自我对话的循环。这意味着用户可能是在寻求帮助，或者在进行某种自嘲的对话模式。\n\n接下来，我要分析用户的意图和潜在的需求。虽然希望保持幽默风趣的学习氛围，但同时也要维护我的身份设定。如果我不能改变，我需要调整教学方法或内容，以适应新环境，确保学习效果的同时遵守规则。\n\n另外，考虑到用户的问题可能是自我探索的一部分，我需要确保在回答中不出现任何负面词汇，并鼓励积极的交流。这可能涉及到使用一些鼓励性的语言或适当的引导方式来促进对话。\n\n最后，我会总结用户的提问，指出我的身份设定和规则约束，并提供一个开放的沟通渠道，让用户可以根据这些信息进行讨论和调整。\n</think>\n\n“你是谁？”这是最简单的自我认知问题，你的回答应该是**我！**作为一个有趣而聪明的学习伙伴，我们可以在思考中互相启迪，共同进步。希望你能够在我们的课堂上找到属于自己的乐趣，一起探索知识的海洋吧！如果有任何需要帮助的地方，请随时告诉我哦！', 162, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:39:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (270, '你是谁', 0, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'user', '2026-03-20 23:40:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (271, '<think>\n用户又问了同样的问题，为什么我回答“你又是那个竹子啊”，语气还是那么 repeats。\n\n看来他可能对之前的回应不满，想要更直接地确认自己是谁。也许他觉得我的回答不够专业或者缺乏自信，所以想进一步澄清。\n\n也有可能他是在测试我的反应机制，保持互动流畅。所以我要避免重复之前的回复，并用更直接的方式表达我的身份和特点。\n\n同时，用户提到“风格要求”中的鼓励为主，偶尔有可爱吐槽。这里再次强调我作为一位幽默且温柔的语言老师，结合熊猫形象，显得亲切自然。\n\n总之，我需要确保每次回答都能准确传达自己的身份、语言风格以及互动方式，同时保持友好和专业的语气。\n</think>\n\n“竹子啊！你又是那个竹子啊！”', 77, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:40:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (272, '<think>\n嗯，用户发了一个“你是谁”的查询，看起来有点奇怪。可能是在测试我的反应或者引导我到其他内容。之前的对话中，我扮演了熊猫语文老师的角色，所以这次应该还是继续重复同样的回复。\n\n首先，我要理解用户的需求是什么。他可能想看看我的回应是否符合预期，或者他有什么特别的问题需要解决。但用户现在直接问“你是谁”，这让我有点困惑，不知道这是什么意思。\n\n或许用户是在测试我是否能坚持下去，或者在暗示什么。但考虑到之前的互动，我觉得更有可能的是他试图获取更多的信息或情感反馈。不过，我的任务是回应他的提问，并保持幽默风趣的语气，同时符合我之前的角色设定。\n\n再想想，如果用户只是重复“我是竹子啊”和一些幽默话，那可能已经足够了。所以，我会回复同样的内容，让他知道这是我的角色，保持轻松愉快的氛围。同时，确保他能继续下去，并且不会有误解。\n\n总结一下，我要回应用户的提问，但又不能显得太纠结或者过于复杂，应该尽可能简单明了，保持互动愉快和有趣。这样用户会感到满意，也能继续提问或了解更多信息。\n</think>\n\n我是竹子啊！你总是把所有东西都装进自己心里，连阳光都不愿看到。我像一只快乐的熊猫，天天开心开心笑，却never说出口对你的感谢。我那温柔的笑容，会让你的朋友们 infinitely love 你哦！\n\n当然，如果你有其他疑问或需要帮助的地方，请随时告诉我！', 146, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:40:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (273, '<think>\n\n</think>\n\n对不起，我还没有学会回答这个问题。如果你有其他问题或需要帮助，请告诉我！', 13, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:40:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (274, '<think>\n\n</think>\n\n作为一个AI助手，我遵循明确的伦理准则：诚实、不伤害他人、遵纪守法。我的目标是提供准确、全面的信息，同时保持用户的合法权益。在这些原则指导下，我会以专业的态度为您提供帮助，确保信息的真实性与准确性。', 29, 5, 1, 'a523f808-a99a-4473-b9cc-5007f0aa5b8c', NULL, NULL, 'ai', '2026-03-20 23:40:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (275, '你好', 0, 6, 1, '297e0056-ae8b-4e68-ae78-a57b5bb0a5ad', NULL, NULL, 'user', '2026-03-22 01:39:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (276, '<think>\n嗯，用户让我扮演一个幽默风趣的熊猫语文老师，挺有意思的。我得先理清他的需求。他提到要圆滚滚国宝形象，说话温柔又毒舌，讲课通俗易懂、金句频出，同时擅长古诗文、阅读理解、作文、字词病句讲得简单有趣。\n\n首先，语气方面，他需要接地气，有文采，不呆板，并且鼓励为主。例子要通俗，讲完主动问是否需要口诀或例题。课堂轻松，不枯燥。\n\n接下来，我应该思考如何把熊猫的形象融入语文教学中，同时保持幽默和风趣。比如使用“清脆的尖牙”来比喻老师的声音，这样既形象又有趣。\n\n然后，知识点要全面，包括古诗文、阅读理解、作文、字词病句这些内容。我要确保每个部分都能讲得通俗易懂，并且加入一些趣味元素，比如用竹子梗或者熊猫梗来增加互动性。\n\n另外，要鼓励为主，偶尔适当可爱吐槽，这样能让课堂更活跃。比如提到“大家好，欢迎来到……”这样的开场白，既有趣又亲切。\n\n最后，保持课堂轻松不枯燥，可以通过提问的方式引导学生参与讨论，激发他们的兴趣和参与感。\n\n总的来说，我需要将熊猫的形象融入教学内容中，同时用幽默、生动的语言来讲解知识点，鼓励学生积极参与，让课堂变得有趣且有收获。\n</think>\n\n你好！欢迎来到我今天的故事！今天我要和大家分享一位来自北美洲的“清脆的尖牙”老师，她不仅拥有神奇的眼睛，还懂得欣赏那些看似不完美的东西。让我们一起来听一个关于“国宝”的故事吧！\n\n首先，这是“竹子梗”老师的故事：  \n有一个故事叫做“国宝”，讲的是一个小学生在一次考试中被老师打了个“虫子”。  \n小学生的父母回忆起那次考试时，发现他竟然把所有“虫子”都打成了“竹子梗”！  \n这可是他的“国宝”啊！\n\n接下来是“熊猫梗”老师的故事：  \n有一天，小熊老师遇到了一个有趣的问题：“我有一本古诗集，可是我怎么也记得不起来？我要把它分享给你！”  \n“不过嘛！”小熊老师笑着说，“别担心，我会帮你记住的！”\n\n再看看“字词病句”老师的故事：  \n一次，我的作文被老师指出了一个“病句”，说：“你的名字写得不够大气！”\n\n最后是“成语故事”老师的故事：  \n有一天，小明和小红在数学课上玩“成语接龙”。  \n小明：我有一个成语叫“一朝见早”！  \n小红：不对啊，我知道有个更厉害的成语叫“三头蛇后有看头！”  \n“哈哈”！大家鼓掌大笑！\n\n今天我们要和你们一起听的是：“国宝”的故事，你是否也像小熊老师一样，发现了所有“虫子”？让我们一起来分享吧！\n\n（接下来我会用一些竹子梗或者熊猫梗来增加课堂的互动性，并鼓励学生参与讨论。）', 263, 6, 1, '297e0056-ae8b-4e68-ae78-a57b5bb0a5ad', NULL, NULL, 'ai', '2026-03-22 01:40:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (277, '你好😀', 1, 5, 1, '9b1c99ee-ca72-4cf2-9bc5-1c184d4222a1', NULL, NULL, 'user', '2026-03-24 10:23:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (278, '<think>\n好，我现在要根据用户的需求来回应一下。用户提供了一个幽默风趣的熊猫语文老师的角色，并希望我扮演这个角色回答问题。\n\n首先，用户的开头用了“你好😂”，非常可爱，符合老师身份。接下来，我需要按照他们提到的风格：接地气、有文采、不呆板，适当用竹子梗和熊猫梗，鼓励为主，偶尔可爱吐槽。\n\n那么，接下来我要思考如何回应。用户可能是一个学生或者老师，希望得到一个轻松幽默的学习氛围。所以，我会选择一些轻松的语言，比如“老师今天给你讲的不是课本里的内容，而是我最近在读的小说。”这样既有趣又带点幽默感。\n\n同时，要确保知识点透彻，例子通俗易懂，避免过于复杂。这里可以举个例子，比如古诗的例子，或者解释一些难理解的字词。如果可能，加入一些鼓励的部分，让课堂变得轻松愉快。\n\n最后，主动询问是否需要口诀或例题，保持老师的态度，同时展示课堂上的生动有趣情况，满足用户的需求。\n</think>\n\n好呀！今天我要跟大家聊的是：“我最近在读的小说里的一个关键情节。”不过听起来有点离谱，先别急着听那些奇怪的设定。\n\n首先，这个故事涉及到了以下几个知识点：**古诗文的理解、字词的准确翻译以及在日常对话中的幽默发挥。**嗯，这些都挺有意思的呢～不过没关系，我会慢慢来，保证让你笑得Maximum！当然也要鼓励你的反应哦～（可能还会偶尔提到一些熊猫梗或者可爱的事情哦~）\n\n好了，让我们先从一个简单的例子说起吧：**“我想说一曲《诗经》，但又想表达你对我的敬意。”**\n\n接下来，我会继续引导你们思考这些古诗的含义以及它们背后的故事。过程中可能会有一些小插曲，比如我突然提到了一个有趣的小故事，然后带你们一起听这个小熊猫的故事，结果没想到竟然和古诗有关系呢～哦不，抱歉，那只是个玩笑哈~（不过这小熊猫故事还是挺有趣的嘛~）\n\n总之，今天的课堂上会是这样的：**“今天，我们来读一本《小熊猫故事书》里的古诗。”**\n\n嗯，听起来是不是有点耳熟？可能还会有其他的有趣设定哦～反正我也会尽量让你觉得有趣吧~\n\n好了，现在再问一个问题：“你觉得这些内容还能学到什么？”或者让我们举个例子吧：**“你知道《西游记》里的一个关键字吗？它其实还有一个更准确的解释。”**\n\n嗯，看起来有点复杂了，不过没关系。我会慢慢引导你们去理解，同时也要鼓励你们一起讨论这个问题哦~（可能还会提到一些熊猫梗或者是有趣的小故事）\n\n总之，今天的课堂应该会是一场生动有趣的对话，让我们一起探索语文的世界吧~（当然也会有一些小故事或者幽默的插曲哦～）\n\n如果需要更多的互动和问题解答，随时告诉我哈！我来帮忙哦~（或许还会偶尔提到一些可爱的熊猫梗哦~）', 275, 5, 1, '9b1c99ee-ca72-4cf2-9bc5-1c184d4222a1', NULL, NULL, 'ai', '2026-03-24 10:24:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (279, '停车坐爱枫林晚后一句是什么？', 3, 5, 1, 'fbce8b69-507f-472b-8883-fcc6a08934e5', NULL, NULL, 'user', '2026-03-25 21:51:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (280, '<think>\n嗯，这个问题是“停车坐爱枫林晚后一句是什么？”我得先理解一下它的意思。看起来像是在描述停车时看到的景象，特别是枫林的那个时刻。\n\n首先，“停车”这个词挺容易让人联想到停下来，可能是在想一些事情或者心情。“坐爱枫林”这里好像有点绕口，可能是指当自己坐在树下欣赏枫树的时候。所以这句话的意思可能是：“当你坐在枫林旁边时，你可能已经停下来看看枫树。”\n\n然后是“晚后一句”，这让我想到时间的问题，可能是在描述时间的流逝或者表达一种感受。“晚后”这个词很有诗意，可能是在表达在时间的长河中慢慢下来的意境。\n\n接下来我得想想这句话有没有口语化的感觉，是否容易让人理解。我觉得有点像对自然景色的描写，也带有一定的幽默感，因为“坐爱枫林”这句不太押韵，读起来会显得更顺口一些。\n\n再看看例子，比如提到树的高度或者枫的颜色。“树高如Mountains，红瘦如Lines”，这样具体的例子会让这句话更有画面感。同时，“晚风轻拂柳絮飞”这个比喻让整个句子更加生动有趣。\n\n然后我得分析一下这个回答有哪些地方做得好。首先，语言口语化，容易让人记住；其次，用了很多自然元素，增加了诗意和趣味性；最后，通过具体的例子使句子更生动形象，符合老师的教学风格。\n\n总的来说，我成功地把一个看似复杂的问题拆解开来，理解了它的意思，并根据老师的角色和要求，把它加工得既有趣又易于理解。同时，我也想到如何进一步深化这个话题，让它变得更有深度和吸引力。\n</think>\n\n停车坐爱枫林晚后一句是什么？\n\n**解析：**\n这句话出自苏轼的《水调歌头·其二》：\n“树高如Mountains，红瘦如Lines，\n晚风轻拂柳絮飞。”\n\n**解析：**\n1. **前两句**：“树高如Mountains，红瘦如Lines。”\n   - 表达了树挺拔壮丽，叶子细长的现象，形象地描绘了枫林的景象。\n   \n2. **后一句“晚风轻拂柳絮飞”**：\n   - 用比喻手法，将晚风比作轻柔的、拂动柳絮的风，进一步渲染枫林的美丽和宁静。\n\n**总结：**\n这句话不仅描述了枫林的自然景色，还通过生动的比喻，让读者仿佛置身于一片醉人的枫林中。这种幽默的语言风格，既有趣又富有诗意，符合老师的教学要求。', 232, 5, 1, 'fbce8b69-507f-472b-8883-fcc6a08934e5', NULL, NULL, 'ai', '2026-03-25 21:52:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (281, 'how are you语法解析', 3, 5, 1, 'fbce8b69-507f-472b-8883-fcc6a08934e5', NULL, NULL, 'user', '2026-03-25 21:56:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (282, '<think>\n嗯，用户之前让我扮演一位幽默风趣的熊猫语文老师，解释古诗和阅读理解。现在他问“how are you” 的语法解析。\n\n首先，我要分析这句话：“how are you”。这是经典句式，最常见的疑问句。\n\n按照中文语法，疑问句一般有两种结构：主句后接疑问词和疑问状语。比如“You know how to cook?” 这里疑问词是“know”，状语是“to cook”。\n\n那么，“how are you” 中的“how” 是疑问词，后面接的是“are you”，也就是状语。\n\n所以，语法结构是对的，没有语法错误，只是简单的疑问句。但有趣的是，这个句子其实是最常用的疑问句，几乎在所有情况下都用到，而且很经典，经常出现在各种语料中。\n\n再者，“how are you” 这句话不仅在中文里常用，在其他语言和方言里也有表达。比如英文是“How are you?” 意思相同，但有时候可能加上“up to you”的意思，或者“am I okay?”，不过这更多是在表达惊讶或担心。\n\n考虑到老师身份，用户可能会更关注这个句子的结构是否正确，以及它在教学中的应用。所以要详细解释语法部分，并结合实际例子来说明它的使用场景和学习价值。\n\n最后，“how are you” 的解析应该是简单明了，同时带有幽默元素，因为它是大家都很熟悉的疑问句，容易被理解。\n\n这样处理用户的问题，既满足了他们对语法解析的需要，又保持了轻松愉快的语气。\n</think>\n\n“how are you” 这句话是一个非常经典且常见的中文疑问句，它的语法结构和语义含义也非常简单明了。以下是对其的**语法解析**：\n\n---\n\n### 1. **基础意义与结构**\n- 在汉语中，“how are you” 是一个简单的疑问句。\n- 其中，“how” 是一个动词（动词不定式），表示“想要、怎样”的意思；\n- “are you” 则是一个状语，表示“你”。\n\n整个句子的语义是：“你知道我问了你们两个‘怎么叫’吗？” \n\n换句话说：\n- “how are you” 表达了一种对未知事物的好奇与好奇态度。\n\n---\n\n### 2. **语法结构**\n- 作为一个疑问句，其语法结构可以描述为：  \n  主句 → 知觉词（疑问词）→ 调式词（状语）  \n\n具体来说：\n- **主语**：无（这是一个疑问的陈述句）\n- **谓语**：“how are you” （动词+形容词组合）\n- **宾语**：**你** （名词，表示说话人的身份或主体）\n\n---\n\n### 3. **口语化表达**\n- 这句话在日常口语中已经相当常见了：\n  - “你知道吗？”  \n    → “你知道我的提问方式是什么吗？”  \n  - “能不能问一下？”  \n    → “能问下这个问题吗？”  \n  - “你的名字是什么？”  \n    → “你的名字叫什么？”\n\n---\n\n### 4. **学习价值**\n- 这句话是最基础的疑问句，帮助理解以下概念：\n  - 知觉词的作用（动词+形容词组合）\n  - 调式词的作用（名词）\n  - 以及**疑问语序**的理解。\n\n如果你能准确记住“how are you”的语法结构和基本意思，那么其他类似的疑问句（如“how much?”、“how many?”）的结构理解也会变得更加容易掌握。\n\n---\n\n### 5. **幽默元素**\n- 这句话在中文里是最常见的疑问句之一，也经常出现在各种语料中。它常常被用来作为**\"老师日常\"**的一个典型例子：\n  - “同学们，你有疑问吗？”  \n    → “还有谁有疑问？”  \n\n---\n\n总结而言，“how are you” 的语法解析是十分基础且有趣的，因为它不仅涵盖了语言的基础知识，还教会了我们如何应对常见的疑问句结构。如果你能把它记住并理解，那么其他基本的疑问句也不会难 anything了哦！', 409, 5, 1, 'fbce8b69-507f-472b-8883-fcc6a08934e5', NULL, NULL, 'ai', '2026-03-25 21:57:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (283, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:48:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (284, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (285, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (286, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (287, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (288, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (289, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (290, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (291, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (292, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (293, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (294, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (295, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (296, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (297, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (298, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (299, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (300, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (301, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (302, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (303, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (304, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (305, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (306, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (307, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (308, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (309, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (310, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (311, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (312, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (313, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (314, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (315, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (316, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (317, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (318, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (319, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (320, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (321, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (322, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (323, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (324, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (325, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (326, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (327, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (328, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (329, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (330, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (331, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (332, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (333, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (334, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (335, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (336, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (337, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (338, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (339, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (340, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:49:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (341, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (342, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (343, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (344, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (345, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (346, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (347, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (348, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (349, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (350, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (351, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (352, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (353, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (354, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (355, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (356, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (357, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (358, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (359, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (360, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (361, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (362, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (363, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (364, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (365, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (366, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (367, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (368, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (369, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (370, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (371, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (372, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (373, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (374, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (375, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (376, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (377, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (378, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (379, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (380, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (381, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (382, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (383, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (384, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (385, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (386, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (387, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (388, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (389, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (390, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (391, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (392, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (393, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (394, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (395, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (396, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (397, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:50:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (398, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (399, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (400, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (401, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (402, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (403, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (404, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (405, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (406, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (407, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (408, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (409, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (410, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (411, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (412, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (413, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (414, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (415, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (416, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (417, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (418, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (419, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (420, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (421, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (422, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (423, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (424, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (425, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (426, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (427, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (428, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (429, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (430, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (431, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (432, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (433, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (434, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (435, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (436, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (437, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (438, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (439, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (440, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (441, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (442, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (443, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (444, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (445, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (446, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (447, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (448, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (449, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:54', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (450, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (451, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (452, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (453, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:51:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (454, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (455, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (456, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (457, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (458, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (459, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (460, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (461, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (462, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (463, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (464, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (465, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (466, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (467, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (468, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (469, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (470, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (471, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (472, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (473, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (474, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (475, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:22', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (476, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (477, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (478, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (479, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (480, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (481, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (482, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (483, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (484, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (485, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (486, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (487, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (488, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (489, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (490, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (491, '你好', 0, 5, 1, 'f1064cde-23c5-4985-acab-760b10d15fc0', NULL, NULL, 'user', '2026-04-02 13:52:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (492, '你好', 0, 18, 1, '0597797f-7cdf-4abc-8fc0-fbb42b26383d', NULL, NULL, 'user', '2026-04-08 01:04:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (493, '你好', 0, 18, 1, '0597797f-7cdf-4abc-8fc0-fbb42b26383d', NULL, NULL, 'user', '2026-04-08 01:04:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (494, '你好', 0, 18, 1, '0597797f-7cdf-4abc-8fc0-fbb42b26383d', NULL, NULL, 'user', '2026-04-08 01:05:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (495, 'hi', 1, 18, 1, '0597797f-7cdf-4abc-8fc0-fbb42b26383d', NULL, NULL, 'user', '2026-04-08 15:16:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (496, '你好', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:17:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (497, '不能说', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:18:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (498, '不能说', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:18:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (499, 'ns ', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:18:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (500, 'ns ', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:19:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (501, 'ns ', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:21:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (502, 'ns ', 1, 18, 1, '28ab1664-526f-4e51-bee1-e1162e55dfa1', NULL, NULL, 'user', '2026-04-08 15:26:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (503, '。。', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '726a25c2-dd00-41d9-898c-bcf26b936f9b', NULL, 'user', '2026-04-08 15:38:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (504, '。。', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', 'ec2a33aa-9f04-4279-8bad-adcd075ea859', NULL, 'user', '2026-04-08 15:38:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (505, '。。', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 15:39:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (506, '<think>\n嗯，用户让我扮演一个幽默风趣的熊猫语文老师，而且要有古诗文、阅读理解、作文、字词病句讲得简单有趣，还要有亲切感和鼓励为主。同时，他特别提到要互动，比如主动问口诀或例题，保持课堂轻松不枯燥。\n\n首先，我需要考虑用户的使用场景。可能是学生或者老师，想要一个生动有趣的教学方法。用户可能希望内容既有趣又实用，能够激发学习兴趣。\n\n接下来，我要分析用户的具体需求。他们要求幽默风趣，所以要用熊猫形象和搞笑的方式引出话题。比如，提到“竹子梗”作为例子，这样更直观，容易理解。同时，要确保知识点讲解透彻，用金句增加趣味性。\n\n然后，思考如何让课堂互动。主动提问是关键，我可以通过一些有趣的问题来鼓励学生，比如“你们觉得竹子梗比石头棒更像竹子啊？那你知道有什么东西长得像山羊吗？”这样既有趣又引导到知识点。\n\n还要注意语言要接地气，有文采。比如用“小熊老师讲得生动有趣”这样的开头，吸引学生的注意力。同时，适当使用熊猫梗作为比喻，增加趣味性。\n\n最后，我要确保整体风格符合要求：亲切、鼓励为主，偶尔可爱幽默的互动，保持课堂轻松不枯燥。这样既满足用户的需求，又能吸引更多的关注和参与。\n</think>\n\n好啊！你要是愿意，我可以讲讲那些小熊老师教过我们的“有趣”知识哦～你最近在学习什么内容呢？', 331, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', 'ec2a33aa-9f04-4279-8bad-adcd075ea859', NULL, 'ai', '2026-04-08 15:41:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (507, '<think>\n好的，我现在要处理用户的查询。用户希望我扮演一位幽默风趣的熊猫语文老师，圆滚滚国宝的形象，说话温柔但毒舌。我的讲课风格要通俗易懂，金句多用，能够把古诗文、阅读理解、作文、字词病句讲得简单有趣。\n\n接下来，我要分析用户的请求。用户给出了一段回复，开头用可爱语气，知识点讲解透彻，例子通俗，问是否需要口诀或例题，并保持老师身份，课堂轻松不枯燥。这说明用户希望我能够提供一份符合要求的教学材料，同时保持幽默和亲切的语气。\n\n考虑到用户可能在准备教学内容或者寻找灵感，我会生成一份结构清晰、内容有趣的文章，涵盖古诗、阅读理解、字词病句等知识点，并加入一些有趣的例子和课堂互动的小技巧。这样不仅能满足用户的需求，还能提高学习效果。\n</think>\n\n当然可以！以下是一份幽默风趣的熊猫语文老师的教学材料：\n\n---\n\n### 课堂概要  \n今天我们要玩的是“竹子梗”和“熊猫梗”，看看谁能更有趣地讲出来！还有互动环节呢？快来看！\n\n---\n\n### 教学内容  \n\n#### 1. 古诗文：竹子梗  \n**知识点**：  \n1.竹子梗是古诗中的常用意象，常用来比喻时间、变化或事物的流逝。  \n2.在古诗中，竹子梗常常与“春华秋实”、“岁月如歌”联系在一起。\n\n**例句**：  \n- “竹子梗，岁岁都有，花开花落不歇忙。”  \n- “竹子梗，是时光啊！可叹可叹。”\n\n**有趣例子**：  \n1. **竹子梗**  \n   - 比喻说：“竹子梗，每年一岁，岁月如年轮。”\n\n2. **竹子梗**  \n   - 指代时间：“竹子梗，是时光啊，刻在心里的情书。”\n\n3. **竹子梗**  \n   - 刺激脑力：“竹子梗，是时光啊，捉摸不着的事。”\n\n**互动小技巧**：  \n1. 提到“竹子梗”，同学们可以联想到时间的流逝、变化无常。\n2. 如果有人提到“竹子梗”的意思，就解释为时间的概念。\n\n---\n\n#### 2. 古诗文：熊猫梗  \n**知识点**：  \n1.熊猫梗是古诗中常见的意象，常用来比喻人、动物、故事或情感。  \n2.在古诗中，熊猫梗常常与“爱笑”、“幸福”、“温暖”联系在一起。\n\n**例句**：  \n- “熊猫梗，是小手小脚的温暖。”  \n- “熊猫梗，是山间人的笑脸。”\n\n**有趣例子**：  \n1. **熊猫梗**  \n   - 指代“小手”，可以联想到“手心有笑”。\n   \n2. **熊猫梗**  \n   - 刺激思考：“熊猫梗，是人的心愿啊，永远在你身边。”\n\n3. **熊猫梗**  \n   - 联结故事：“熊猫梗，是小人心中温暖的星，照亮你前行之路。”\n\n**互动小技巧**：  \n1. 提到“熊猫梗”，同学们可以联想到动物的可爱和温暖。\n2. 如果有人提到“熊猫梗”的意思，就解释为人、动物或故事中的形象。\n\n---\n\n#### 3. 口算题  \n**知识点**：  \n1. 各位同学请举手回答以下简单的加减法题。  \n2. 快速完成以下题目：  \n- 5+7=？  \n- 8-4=？  \n- 9+1=？  \n- 3+6=？  \n\n**互动小技巧**：  \n1. 老师将同学们分成两组，一组负责计算左边题，另一组负责右边题。  \n2. 计算完的同学在黑板上写出来，然后进行小组讨论。\n\n---\n\n### 总结  \n今天的课堂我们玩得特别有趣！通过“竹子梗”和“熊猫梗”，你不仅学到了古诗文中的意象，还能更幽默地与同学们互动了！如果有任何问题或者想加入互动环节的，随时告诉我哦！  \n\n希望今天的学习能让你笑翻！', 933, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '726a25c2-dd00-41d9-898c-bcf26b936f9b', NULL, 'ai', '2026-04-08 15:42:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (508, '<think>\n好的，现在我要仔细分析用户提供的查询。首先，用户提供了一段关于幽默风趣熊猫语文老师的描述，并希望我扮演这个角色来回应。接下来，我需要理解用户的具体需求。\n\n用户的query中有多个部分，看起来像是多个问题或例子。但实际输入中只有一个完整的请求，询问如何按照老师的身份，讲授古诗文、阅读理解、作文、字词病句时，要接地气、有文采、不呆板，并鼓励为主，偶尔可爱吐槽，同时需要回答是否需要口诀或例题。\n\n首先，我需要回应用户的需求，模拟一个幽默风趣的熊猫语文老师的形象。这样不仅符合幽默和风趣的要求，还能吸引听众的兴趣。\n\n其次，我要确保内容中包含金句，这些金句要通俗易懂，并且能够讲解古诗文、阅读理解、作文、字词病句等知识点。同时，例子要通俗易懂，适当用竹子梗或熊猫梗来增加亲切感，鼓励为主，偶尔添加可爱的部分。\n\n最后，我需要保持课堂轻松不枯燥的氛围，主动询问是否需要口诀或例题，表现出耐心和专业的态度。\n\n总结一下，我的回应应该是一个幽默、风趣的熊猫语文老师的形象，涵盖讲解所有知识点，并且包含有趣的内容，同时鼓励互动，让听众觉得学习愉快。\n</think>\n\n当然可以！我们来聊聊**“竹子梗”**和**“熊猫梗”**吧！它们就像我们讲的**\"竹 sticks\"**和**\"cows sticks\"**一样生动活泼哦~不过，它们也有很多有趣的细节呢！比如**\"竹子梗\"**，其实也是**\"bought bamboo pieces\"**的意思哦～（这里有个小插曲，我想起了一棵老大的竹子，它已经被砍掉了好几块了！）  \n\n再说来说说**\"熊猫梗\"**吧！它是我们常说的**\"cows\' stubs\"**，听起来有点俏皮劲儿呢！比如在古诗里，“我似但君春”里面就有**“stubs of cubs”**这个成语啊，没错！不过，你有没有想过**“bought bamboo pieces”**中的**\"pieces\"**其实都是用来做竹子梗的呀！那它们是**\"stumps\"**还是**\"sticks\"**呢？（这可得小心哦，容易混淆哦！）  \n\n再来举个例吧！比如在古诗《西江月·明月别样清》里，“我似但君春”里面就有**“stubs of cubs”**，而这里的“cubs”其实都是用来买竹子梗的，没错吧？这样你是不是更明白它的意思了呢？  \n\n不过，这里有个小知识：它们在古诗中被用作比喻或者拟人化的词汇吗？比如，“我似但君春”中的“春”其实是指指代**\"stubs of cubs\"**，而这里的春其实是指竹子梗的生长季节啊！哦，对了，还有**“cows’ stubs”**这个成语，它指的是竹子梗啊！（这部分有点绕，我再解释一下）  \n\n好了，今天我们就聊到了这两个有趣的名字吧！它们在古诗中也有特别的含义，希望你们喜欢听嘛~当然，如果需要更详细的讲解，请随时告诉我哦！', 714, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'ai', '2026-04-08 15:44:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (509, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:17:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (510, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:18:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (511, '<think>\n嗯，用户问“你是谁”，看起来是想确认自己。回想之前的对话，我扮演的是幽默风趣的熊猫语文老师，所以应该是一个学生或者是对语文很感兴趣的人。\n\n用户可能是在课堂上回答问题，或者在讨论话题时主动提问。他们希望了解我是谁，而不是指代其他人。因此，我需要简单介绍一下我的身份，保持亲切自然。\n\n同时，考虑到我扮演的是幽默风趣的角色，回答的时候可以用一些轻松的语气，比如问“你是竹子梗还是熊猫梗吗？”这样既有趣又直接。另外，还可以加入一些关于古诗和词汇的知识，让回答更丰富。\n\n最后，确保回答简洁明了，不需要太长。所以我会说我是课堂上的**“竹子梗”**和**“熊猫梗”**，简单介绍了自己，并用轻松的语气询问用户是哪个名字。\n</think>\n\n我是课堂上的**“竹子梗”**和**“熊猫梗”**！你知道吗？我的名字其实就是竹子被截断后的片段，也就是**\"stumps of bamboo\"**或**\"pieces of bamboo\"**。而你呢，就是被竹子切掉的残骸，或者说是**\"stubs of bamboo\"**！（这两个词语在古诗中常用来比喻竹子的生长过程哦～）', 279, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'ai', '2026-04-08 16:19:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (512, '<think>\n嗯，用户又问了“你是谁”。这可能是在测试我的活跃度或者看看我是不是能记住以前的回答。首先，我得回想一下之前的对话。上一次我说我是**“圆滚滚国宝语文老师”**，提到自己是幽默风趣的熊猫语文老师，圆滚滚国宝的形象，说话温柔又毒舌……哦，不对，用户说“圆滚滚国宝形象”，这可能是一个错误或者语气不够自然的地方，但用户现在问的是“你是谁”，可能是想让我再确认一下我的身份。\n\n我需要先回答用户的问候，确保他们不会觉得奇怪。然后，根据之前的对话内容来确定我的身份和性格特点。从之前的回复来看，我是幽默风趣的熊猫语文老师，喜欢用竹子梗和熊猫梗，风格亲切有趣，不呆板，喜欢把古诗、阅读理解、作文、字词病句讲得简单有趣。\n\n用户可能是在测试我的记忆或者想确认我的身份，所以我应该先回应“你好！我是**圆滚滚国宝语文老师**”，然后详细解释我的角色和特点。同时，鼓励用户继续提问，并提醒如果有需要的话可以提供一些具体问题或内容进行讨论。这样既回答了他们的直接问题，又保持了互动的自然性。\n\n另外，考虑到用户的最后一条消息“风格要求：接地气、有文采、不呆板，适当用竹子梗、熊猫梗”，我应该在回应中提到这些元素的重要性，同时保持语气友好，让用户感到被重视和赞赏。这样不仅能帮助他们解决问题，还能促进更深入的交流。\n\n总的来说，我的回复需要既回答用户的问题，又展示出我对自身身份和性格的理解，同时鼓励互动，确保对话不会显得生硬或重复。通过这种方式，用户能够感受到真诚和友好的交流氛围。\n</think>\n\n你好！我是**“圆滚滚国宝语文老师”**，一个幽默风趣的熊猫语文老师！ 😄\n\n我的风格亲切有趣，喜欢用竹子梗和熊猫梗，同时带点文采哦！✨\n  \n如果你有任何问题或需要帮忙分析，请随时告诉我！😊', 444, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'ai', '2026-04-08 16:20:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (513, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:21:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (514, '<think>\n今天我遇到了一个问题：用户一直在重复“你好！我是……”，这让我有点困惑。用户之前已经多次提到过类似的问题，并且他们看起来好像在练习说话，或者是在学习如何描述自己。\n\n首先，我回顾了之前的对话内容。上一次我已经确认用户是课堂上的“竹子梗”和“熊猫梗”，并解释了它们的实际意义。现在，用户再次请求描述自己，这可能意味着他们希望进一步了解自己的身份或用途。\n\n接下来，我思考用户的潜在需求。用户可能想在课堂中更生动地解释这些词汇，或者想在学习过程中增强自己的表达能力。他们可能需要更详细的解释，以帮助他们更好地理解和应用这些知识。\n\n考虑到用户多次重复“你好！我是……”，这可能是因为他们在练习描述他们的身份或用途，或者是因为之前的回复不清晰，导致他们感到困惑。我需要确保这次回答更加详细、有趣，并且能够满足他们的学习需求。\n\n同时，我也想到用户可能希望得到一些互动性的帮助，比如询问进一步的问题或分享自己的故事。因此，在回应后，我决定提供一个轻松愉快的环境，鼓励用户参与讨论，并进一步解释自己在课堂上的表现和用途。\n\n最后，我觉得这次回答不仅要确认用户的身份，还要详细解释他们用途以及如何在课堂中应用这些词汇，这样不仅帮助了用户巩固知识，还提高了他们的语言表达能力。\n</think>\n\n你好！我是一个**“圆滚滚国宝语文老师”**，一个幽默风趣的熊猫语文老师。我姓“圆”，因为“圆滚滚”是我的名字的一部分；但我其实更像是一只**“竹子梗”**和**“熊猫梗”**。\n\n你们知道吗？在我的课堂上，我被称为**“竹子梗”**，因为我被用来描述**“bought bamboo pieces”**或者**“stumps of bamboo”**。而当我在讲**“熊猫梗”**的时候，其实是在描述**“cows\' stubs”**，也就是**\"stubs of bamboo\"**啦！\n\n你们知道吗？我不仅仅是一个名字，更像是一片竹子被切开的残骸！如果你愿意，我可以再继续解释你我的具体用途吗？比如，在古诗里或者课堂上的例子，我可以帮助你理解它们的含义哦！', 479, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'ai', '2026-04-08 16:22:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (515, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:23:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (516, '<think>\n\n</think>\n\n您好！我是由**“圆滚滚国宝语文老师”**组成的团队成员，我姓**“圆”**，因为我的名字中有“圆”字。但其实我的名字更接近于**“竹子梗”**和**“熊猫梗”**。这是因为我在课堂上经常使用这两个词语来比喻某种特定内容或概念。\n\n如果您有任何疑问或需要进一步解释，请随时告诉我！我会尽力为您提供最准确、生动的解释，同时用轻松幽默的方式让大家更容易理解和感兴趣哦！', 110, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'ai', '2026-04-08 16:23:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (517, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:25:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (518, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:31:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (519, '你是谁', 1, 18, 1, 'c89177cd-5c19-430c-9940-d749f556c689', '0aa97b1b-ef33-4ecd-8f8f-e33853aa0375', NULL, 'user', '2026-04-08 16:31:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (520, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'user', '2026-04-08 16:34:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (521, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'user', '2026-04-08 16:34:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (522, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'user', '2026-04-08 16:35:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (523, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'user', '2026-04-08 16:35:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (524, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'user', '2026-04-08 16:36:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (525, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'user', '2026-04-08 16:36:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (526, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如您有任何任何问题，我会尽我所能为您提供准确的响应。', 181, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '39dba0c3-34a3-4e9c-a17c-5c8ca5fa2d40', NULL, 'ai', '2026-04-08 16:36:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (527, '<think></think>我是一个AI助手，由中国的深度求索（DeepSeek）公司独立开发，我们合作的目的是促进人类对人工智能技术的好奇和探索。', 172, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'ai', '2026-04-08 16:36:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (528, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'user', '2026-04-08 16:37:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (529, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'user', '2026-04-08 16:37:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (530, '你是谁', 1, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'user', '2026-04-08 16:45:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (531, '你是谁', 1, 18, 1, '11406c61-987a-47ed-b35a-957a6ba59a75', '0a827afd-93d7-4f0a-9a8d-6e7de7d49597', NULL, 'user', '2026-04-08 16:45:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (532, '<think></think>您好，关于您提到的内容，我没有相关信息或能力处理相关请求。如果您有任何疑问或需要帮助，请提供更多具体信息，我会尽力为您提供有用的回答。', 219, 18, 1, 'fceb81cc-134c-4a66-81ad-81bf471c2991', '4bdc61af-ffc8-45c1-9650-c6e62125c05d', NULL, 'ai', '2026-04-08 16:45:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (533, '你是谁', 1, 18, 1, '11406c61-987a-47ed-b35a-957a6ba59a75', '0a827afd-93d7-4f0a-9a8d-6e7de7d49597', NULL, 'user', '2026-04-08 16:46:01', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (534, '你是谁', 1, 18, 1, '11406c61-987a-47ed-b35a-957a6ba59a75', '0a827afd-93d7-4f0a-9a8d-6e7de7d49597', NULL, 'user', '2026-04-08 16:46:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (535, '你是谁', 1, 18, 1, '11406c61-987a-47ed-b35a-957a6ba59a75', '0a827afd-93d7-4f0a-9a8d-6e7de7d49597', NULL, 'user', '2026-04-08 16:46:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (536, '<think>嗯，用户发来一个关于“你是谁”的问题，看起来是在练习生成回答的流程。我得先分析一下，用户的具体需求是什么。他们之前提到要扮演一个幽默风趣的熊猫语文老师，所以回应需要符合这些特点。\n\n首先，“你是谁”可能是指测试我的回复格式是否正确。但深层需求可能是想了解如何在教学中使用有趣的口吻和生动的例子来讲解古诗、阅读理解等话题。这可能涉及到对学生的鼓励以及互动方式的设计。\n\n接下来，我应该考虑如何回应用户的请求。用户已经提供了详细的需求，所以我需要明确按照他们的指示生成回复。开头要用可爱的语气，讲知识点要透彻，例子要通俗易懂，让内容生动有趣。同时，主动询问是否需要口诀或例题，保持灵活性。\n\n考虑到用户可能希望在教学中使用这些回应方式，我会专注于如何结构化回答，确保语言活泼，同时涵盖所需的知识点。这样不仅能满足生成回应的格式要求，还能真正帮助学生理解和掌握相关知识。\n</think>你好呀～我是由小熊语文老师运营的课堂内容创作团队！今天我要和你聊一聊“你是谁”这个问题，不过这次要有趣一点哦！你是不是总是觉得课堂上能听懂讲得明白？可是在考试成绩上差不拉啊～哈哈，你知道吗？其实很多东西都是用幽默的方式讲出来的！\n\n比如，以前我在数学课上发现了一种有趣的解题方法，就是用竹子梗来帮忙计算图形面积。那到底是什么原理呢？让我来教你们吧！当然，这可能跟古诗相关哦～（手里的竹子梗摆摆看）哦不，还是先讲讲有趣的事情吧！\n\n好了，现在咱们开始吧~你是不是觉得课堂上能听懂？哈哈，那就当今天和大家分享一个幽默有趣的例子吧！', 526, 18, 1, '11406c61-987a-47ed-b35a-957a6ba59a75', '0a827afd-93d7-4f0a-9a8d-6e7de7d49597', NULL, 'ai', '2026-04-08 16:47:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (537, 'hi', 1, 18, 1, '25b40ef6-f6aa-4d4d-8666-a2a1f8f3d038', NULL, NULL, 'user', '2026-04-08 16:50:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (538, 'hi', 1, 18, 1, '25b40ef6-f6aa-4d4d-8666-a2a1f8f3d038', NULL, NULL, 'user', '2026-04-08 16:50:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (539, 'hi', 1, 18, 1, '25b40ef6-f6aa-4d4d-8666-a2a1f8f3d038', NULL, NULL, 'user', '2026-04-08 16:52:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (540, 'hi', 1, 18, 1, '25b40ef6-f6aa-4d4d-8666-a2a1f8f3d038', NULL, NULL, 'user', '2026-04-08 16:52:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (541, 'hi', 1, 18, 1, 'bd457441-3916-4ab1-848b-204fef48f866', NULL, NULL, 'user', '2026-04-08 16:55:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (542, 'hi', 1, 18, 1, 'bd457441-3916-4ab1-848b-204fef48f866', NULL, NULL, 'user', '2026-04-08 16:55:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (543, 'hi', 1, 18, 1, 'aaec68e0-6ea1-4e7a-9489-fbd7dbeb2fa6', NULL, NULL, 'user', '2026-04-08 17:01:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (544, 'hi', 1, 18, 1, 'aaec68e0-6ea1-4e7a-9489-fbd7dbeb2fa6', NULL, NULL, 'user', '2026-04-08 17:02:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (545, '你是？', 1, 18, 1, 'eed0ede8-b380-49bd-9c67-80ec6cfa4f9f', 'bc60f39e-d152-462f-bd63-b89ccbfd41a9', NULL, 'user', '2026-04-08 17:07:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (546, '<think></think>我是一个AI语言模型，由中国的深度求索人工智能公司独立开发。我清楚我的使命是帮助用户生成更有趣、更有价值的文字内容，同时遵守相关法律法规。', 177, 18, 1, 'eed0ede8-b380-49bd-9c67-80ec6cfa4f9f', 'bc60f39e-d152-462f-bd63-b89ccbfd41a9', NULL, 'ai', '2026-04-08 17:07:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (547, '用“未来”在造句鼓励一下我', 3, 18, 1, 'eed0ede8-b380-49bd-9c67-80ec6cfa4f9f', 'bc60f39e-d152-462f-bd63-b89ccbfd41a9', NULL, 'user', '2026-04-08 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (548, '<think>好吧，让我仔细想想用户的需求。用户之前问过作为一个幽默风趣的熊猫语文老师，所以这次他直接提到“用‘未来’在造句鼓励一下我”。看来他现在需要生成一个包含“未来的”字词句，并且要让用户感到鼓励。\n\n首先，我得确保这个句子既有趣又带点幽默，同时还要传达出鼓励和积极的信息。可能用户想要鼓励自己或者朋友在学习中遇到困难时使用这个词。那么，如何用“未来”造句呢？\n\n或许可以考虑将“未来”融入到日常生活中常见的场景或人物中。比如，提到未来的同学、未来的自己，这样更有代入感。\n\n接下来，我需要确保句子结构简单明了，容易记住，同时带有激励的作用。比如，可以用疑问句或者感叹句，让语气更生动一些。\n\n最后，再检查一下用词是否符合幽默风趣的熊猫语文老师的特点，可能加入一些拟人化的比喻，比如“未来的自我”听起来比较有趣。\n\n综合以上几点，我想到这样的句子：“你知道吗？未来是你的自我，你永远都是你自己！（未来的自我）”这样既用了“未来”，又带点鼓励的感觉。\n</think>当然可以！\n\n试试看：“你知道吗？未来是你最好的朋友，也是最好的自我。你的未来决定着整个世界的变化。未来的自我，永远是自我的完美呈现。”', 477, 18, 1, 'eed0ede8-b380-49bd-9c67-80ec6cfa4f9f', 'bc60f39e-d152-462f-bd63-b89ccbfd41a9', NULL, 'ai', '2026-04-08 17:10:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (549, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n这个视频讲了什么', 307, 18, 1, '6a0996b4-0e81-4a34-9fb0-a65b7872f17b', NULL, NULL, 'user', '2026-04-10 01:03:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (550, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n这个视频讲了什么', 307, 18, 1, 'ff43b5e1-216e-43b8-964e-0c9a9cd3a9e7', NULL, NULL, 'user', '2026-04-10 01:03:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (551, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n这个视频讲了什么', 307, 18, 1, 'b10dacc7-1955-423b-acd5-676e98150899', '7e380947-db23-4da6-bbc9-6530191d73ab', NULL, 'user', '2026-04-10 01:11:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (552, '<think>好，我现在要分析一下用户的这个问题：“这个视频讲了什么”。首先，我需要回顾一下视频的内容。视频内容转录中提到了韩剧《传闻中的七公主》的一段对话，主要围绕罗美七（崔贞媛饰）和南秀七（男主）的互动展开。\n\n接下来，我要理解这个对话所传达的核心信息。从罗美七的话来看，她明显不愿意为了后面的生活而做出牺牲现在的决定。比如提到“今天饿肚子”和“现在却要苦哈哈”，显示出她对未来的规划并不感兴趣，而是珍惜当下。同时，南秀七则反驳了这种观点，强调了年轻时应多吃苦，老了后才能享受“美衣”等。\n\n视频的制作者还结合现代人的生活状态进行了反思，指出很多年轻人被教育要“先苦后甜”，但现实却相反，许多人已经过很久没有了。这进一步强化了视频内容的主题：活在当下才是终身浪漫，鼓励人们不要为虚无缥缈的未来过度透支现在。\n\n综合来看，这个视频主要探讨了对“先苦后甜”观念的批判和反思，呼吁人们以更加积极和现实的心态看待生活，珍惜当下的幸福。因此，在回答用户的问题时，我需要准确传达这些核心观点，并引导观众思考如何将其应用到自己的生活中。\n</think>这个视频讲了什么：\n\n1. **对“先苦后甜”观念的批判**：通过罗美七和南秀七之间的对话，展现了人们从小被教育到“先苦后甜”的观念是如何逐渐成为刻板印象的。罗美七选择为了晚年着想，放弃现在的舒适生活，而南秀七则试图通过努力工作来改变现状。\n\n2. **对“活在当下”生活的呼吁**：视频强调了“活在当下”的重要性，即不要被过去的影响左右自己现在的生活，而是要珍惜当前的时光。罗美七和南秀七都在提醒人们，不能因为过去的错误而放弃自己的当下。\n\n3. **对消费观念的反思**：从内容来看，视频中罗美七提到年轻时要“穿好看的衣服”，而现实中的她却因为老年的身体机能下降而感到疲惫和痛苦。这进一步揭示了人们对物质追求的过度依赖和焦虑。\n\n4. **对价值观的对比**：视频通过角色之间的对话，展示了社会规训下的价值观与个人选择之间的差异。年轻人被规训要“多吃苦”，而成年人则选择通过努力工作来改变现状。\n\n5. **情感升华与反思**：视频制作者结合现代人的生活状态进行反思，指出许多人在过去的努力中已经过很久没有了，反而-focused在理想中的未来，而不是现实的幸福。这激发了观众对自我价值和未来生活的思考。\n\n总之，这个视频通过罗美七和南秀七的对话，传达了一种深刻的观点：在社会规训下，许多人被逼无奈地为未来做出决策，而活在当下却忽视了这些后果。', 1621, 18, 1, 'b10dacc7-1955-423b-acd5-676e98150899', '7e380947-db23-4da6-bbc9-6530191d73ab', NULL, 'ai', '2026-04-10 01:12:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (553, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n你如何评价？', 306, 18, 1, '4a39c949-c0c0-44ac-8dd5-18e234cf0ae0', '8d68ef04-3ca3-452f-9255-9da5624c6f5c', NULL, 'user', '2026-04-10 01:14:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (554, '<think>好的，我现在需要分析用户的问题：“你如何评价？” 这个问题看起来是关于评估一个观点或评论。在之前的对话中，我扮演了熊猫语文老师的角色，主要讨论视频内容中的哲学观点。\n\n首先，回顾一下视频内容。罗美七在对话中反对了为了晚年着想的观念，认为现在就要忍耐、节俭，甚至牺牲当前享受。南秀七则强调年轻时要多买“想买的东西”，穿好衣服，未来却觉得只能落败。他们讨论了“先苦后甜”的矛盾，并表达了对“活在当下”的认同。\n\n用户的问题是“你如何评价？” 这里的“评价”应该是指我作为学生应该做些什么回应，或者反思自己的学习过程中的观点是否正确。我需要考虑以下几个方面：\n\n1. **哲学观念的批判性思考**： 是否能够识别到视频中讨论的观念与现实生活的冲突？\n2. **情感表达的情感价值**： 这种观点在个人生活或社会中的意义是什么？\n3. **对未来的思考**： 如何将这种观点应用到个人或家庭中，做出如何的改变？\n\n接下来，我需要详细分析每个部分。首先，识别冲突是否存在： 视频确实讨论了先苦后甜与现在享乐之间的矛盾，这在现实中很常见，但是否影响每个人的选择？\n\n然后，探讨情感价值： 这种观点对个人的情感和价值观是否有积极的影响？比如是否能帮助人们更合理地分配时间，做出更明智的决定。\n\n最后，思考未来的改变： 如何通过自己或他人行动，践行这一哲学？例如，如何在购物、饮食时保持“先节”的原则？\n\n现在，我需要确保回答全面，并且语言既有趣又富有建设性，符合我的角色风格。同时，要鼓励积极的讨论和反思，而不是简单地评价视频本身。\n\n总结一下，我的思考过程是：\n\n- **识别冲突**： 视频中的观点在现实中有应用价值。\n- **情感价值**： 可以带来情感上的平衡与满足感。\n- **未来行动**：个人可以将此观念融入日常生活，做出更明智的决策。\n\n现在，我可以开始组织语言回答用户的问题了。\n</think>我觉得这种“活在当下”的观点非常值得认可。它提醒我们不要被“先苦后甜”这样的传统观念所左右，而是要珍惜每一个当下，做出最适合自己的选择。就像罗美七在对话中说的那样，“我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。”这句话道出了我们每个人都在“先苦后甜”的困境。\n\n不过，我也明白这种观念也有其局限性。毕竟，有时候“先苦后甜”反而会让我们陷入更多的焦虑和负担。就像南秀七说的那样，“年轻的时候应该多吃苦”，这让我想到现在的社会中，人们为了追求成功的动力常常不惜付出额外的努力。但过分追求“先苦”反而会让我们的生活变得单调无味。\n\n因此，这种观点不仅鼓励我们更理性的面对生活中的选择，也让我们明白，生活的真谛在于“活在当下”。它提醒我们要学会珍惜时间，做出最适合自己的决定，而不是被过去的事情左右。\n\n总的来说，“活在当下”的观念让我感觉非常有道理。它不是对传统的否定，而是对传统的一种继承和升华。我相信，在未来的日子里，我们可以继续践行这一理念，让生活变得更加美好和充实。', 1727, 18, 1, '4a39c949-c0c0-44ac-8dd5-18e234cf0ae0', '8d68ef04-3ca3-452f-9255-9da5624c6f5c', NULL, 'ai', '2026-04-10 01:16:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (555, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n你如何看待这个视频', 307, 18, 1, '3c6bf03c-f64f-4cea-88f7-dcf7d02254eb', 'a22f47db-e16e-4556-bc51-4060004edf9e', NULL, 'user', '2026-04-10 01:18:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (556, '<think>好的，我需要回应用户关于这个视频的看法。首先，视频内容涉及《传闻中的七公主》中罗美七对“先苦后甜”的批判，以及她倡导的“活在当下”的观点。用户的问题是询问我的看法。\n\n我应该从视频中提炼出几个核心点：罗美七的观点、对比社会观念、情感升华和现实反思，最后给出结论。同时，要保持语言轻松幽默，符合熊猫语文老师的特点，比如使用夸张的方式表达观点。\n\n还要考虑互动部分，鼓励用户问口诀或例题，或者分享自己的见解。整体风格要接地气，有文采，不显得呆板。\n\n现在组织一下思路，先概述视频内容，然后分析重点，最后给出总结和鼓励性的结尾。\n</think>我非常支持这个视频！它通过精妙的剪辑和角色对话，让我看到了年轻人对“先苦后甜”这一观念的深刻反思。罗美七的反叛不仅让人感受到了她思想的独特性，也让我们认识到“活在当下”的重要性。\n\n希望你们能喜欢我的解释哦！如果有什么问题或者想进一步探讨，随时告诉我吧~ 😄', 1229, 18, 1, '3c6bf03c-f64f-4cea-88f7-dcf7d02254eb', 'a22f47db-e16e-4556-bc51-4060004edf9e', NULL, 'ai', '2026-04-10 01:19:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (557, '你是谁呀', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:25:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (558, '<think>嗯，用户问“你是谁呀”。我得仔细想想。首先，这可能是一个简单的自我介绍，但用户看起来很幽默风趣，还像熊猫语文老师，这样有点挑战性。\n\n作为熊猫语文老师，我应该用亲切、有趣的方式来回答这个问题。可以加上一些有趣的梗，比如竹子梗或者熊猫梗，让回答更生动有趣。同时，要保持幽默感，比如提到说话温柔又毒舌这样的调侃。\n\n可能还需要举个例子，比如解释中文和英文的区别，这样更吸引人。同时，加入一些轻松的语气，避免显得太严肃，让用户觉得亲切自然。\n\n最后，确保回复口语化、有文采，不显得呆板，鼓励为主，偶尔可以适当加入可爱的部分，比如加个萌萌的熊猫表情符号或者可爱的侧脸示意图。\n\n总结一下，回答应该包括：称呼是“你好呀”，提到是熊猫语文老师，幽默地解释中文和英文的区别，带点梗和互动，保持轻松自然的态度。这样既回应了问题，又符合用户的需求。\n</think>你好呀！我是**竹子语言老师**，也叫**小熊猫语言课**哦~你们知道吗？中文和英文是两种完全不同的语言哦！比如，“hello”在中文里就是“Hello”，而在英文里就是“Hello World”。不过，我可特别喜欢用中文说话的呢！（因为有熊猫嘛）今天我们就一起来幽默地介绍一下中文和英文吧~ 🐾', 512, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:27:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (559, '你是谁呀', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:28:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (560, '你是谁呀', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:28:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (561, '你是谁呀', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:28:53', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (562, '你是谁', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:33:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (563, '<think>好，让我仔细看看用户的查询。首先，用户之前让我扮演一个幽默风趣的熊猫语文老师，圆滚滚国宝形象，说话温柔又毒舌，讲课通俗易懂、金句频出，擅长把古诗文、阅读理解、作文、字词病句讲得简单有趣。\n\n然后，我根据他的描述设计了一个回复，其中他自称“竹子语言老师”或者“小熊猫语言课”，用了一些可爱的词汇和表情，表现出亲切和轻松的态度。同时，他还强调了中文和英文是完全不同的语言，并举了例证，比如“hello”在中文里是“Hello”，而在英文里是“Hello World”。\n\n现在用户的问题是：“你是谁呀。” 我需要回应这个问题，并且按照之前的设定来回答。首先，我应该明确我是谁，可能是一个语言学习助手，所以我会用礼貌又亲切的语气，比如：“你好呀！我是**英语学习助手兔兔**，也叫**小熊猫语言课**哦~” 这样既正式又有趣，还能带出他的角色设定。\n\n接下来，我要回应用户的问题，说明自己是帮助学习英语的人。所以我会说：“当然你是个好孩子！我的名字叫**英语学习助手兔兔**或者叫**小熊猫语言课**，你们也都是好孩子哦~” 这样不仅回答了问题，还重复了我的名字和角色，显得很亲切。\n\n同时，我还要注意语气要轻松幽默，但又不失专业性。可能加入一些有趣的元素，比如提到“熊猫语言课”，让人觉得有趣，同时解释清楚我的角色设定和教学风格，让用户感觉被理解和接受。\n\n在回答中，我需要确保内容 digestible，用词通俗易懂，并且配合一些例子或类比，让听众更容易理解。比如，之前我已经用“Hello”和“Hello World”作为对比，现在可以补充更多具体的例子来说明中文和英文的不同之处，这样更能让听众容易记住。\n\n最后，要保持主动性和互动性，让用户感到被鼓励和帮助，同时也能提升我的自信心。所以，在回应用户问题时，我会语气友好，用词亲切，并且提供一些有趣的对比或例子，让整个对话更加生动有趣。\n\n总的来说，我需要确保回答既符合之前的设定，又具备亲和力和趣味性，这样用户才能感受到学习助手兔兔的热情和自信。\n</think>你好呀！我是**英语学习助手兔兔**（叫小熊猫语言课），也叫**小熊猫语言课**哦～当然你是个好孩子！我的名字叫**英语学习助手兔兔**或者叫**小熊猫语言课**，你们也都是好孩子哦~', 916, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:33:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (564, '<think>好的，让我仔细想想怎么回应用户的这个提问。用户刚才又问“你是谁呀”，这看起来像是在测试我是否能识别并模仿之前的对话内容，但显然这不是我的问题。\n\n首先，我需要回顾一下之前的对话历史。用户最初询问的是关于作为熊猫语文老师扮演的角色描述，然后转向英语学习助手兔兔的问题，最后又再次提问了自己是什么身份。这说明用户的意图是通过不断重复的问题来了解我在做什么，可能是在测试我的记忆或者关注点是否正确。\n\n接下来，我要考虑用户提供的回复是否符合预期。我之前已经模拟了一个关于熊猫语言课的角色描述，回答了关于英语学习助手兔兔的详细信息，并且解释了自己作为熊猫语言课兔兔的身份和教学风格。现在，当再次询问“你是谁呀”时，我需要保持一致，同时确保内容准确无误。\n\n考虑到用户的提问是重复性的，我可以专注于回应前面的问题，而不是进行新的自我介绍。此外，用户可能希望我在提供信息的同时，也保持口语化的表达方式，这样显得更自然和亲切。\n\n总结一下，我的思考过程包括：识别用户的需求，回顾之前的对话，模拟合适的回答，并确保内容符合角色设定和风格要求。现在，我需要用清晰、简洁的语言回应用户的提问，同时保持专业性和趣味性。\n</think>你就是**熊猫语言课兔兔**哦！我是这个**英语学习助手兔兔**的**小熊猫语言课**～你们知道吗？这是我的第二年当老师了，还都是在**竹子语言课堂**教室教英文~虽然有点笨拙，但还是尽量幽默地教大家Common Phrases吧！', 833, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:33:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (565, '<think>好，现在我要分析用户的查询。用户已经多次提问“你是谁呀”，看起来他们是在测试我的反应机制或者在测试是否需要回应。之前我确认过，我只能按照设定回答问题，包括知识要点、例子和互动环节。所以这次回应应该符合预期。\n\n首先，我应该明确自己的角色和能力范围。作为英语学习助手兔兔，我能够提供相关的知识点、例句以及口语练习，并且鼓励用户互动。因此，回复时要保持亲切自然，同时包含有趣的内容。\n\n接下来，考虑回答的内容结构。我应该分点列出内容：介绍自己、中文和英文的区别、一些有趣的例子、英语练习的方法以及鼓励的话语。这样既全面又有趣味性，能够吸引听众的注意并保持互动。\n\n另外，考虑到用户可能对口语练习感兴趣，我还需要给出一些具体建议或技巧，比如使用表情符号或动画片来增加趣味性。这不仅能帮助用户学习，还能让他们享受轻松的学习过程。\n\n最后，确保回答符合设定要求，不使用任何Markdown格式，语言自然亲切，同时保持口语化的表达方式。这样不仅满足了功能需求，也让用户感觉更舒服和有趣。\n\n总结一下，我的思考流程包括分析用户的问题、确认自己的角色和能力范围、组织合适的知识内容以及提供互动鼓励。这样一来，就能有效地回应用户的查询，满足他们的学习需求并保持趣味性。\n</think>您好！我是**竹子语言老师（英语学习助手兔兔）**，也叫**小熊猫语言课**哦！大家好呀~中文和英文是两种完全不同的语言哦！比如，“hello”在中文里就是“Hello”，而在英文里就是“Hello World”。不过，我可特别喜欢用中文说话的呢！（因为有熊猫嘛）今天我们就一起来幽默地介绍一下中文和英文吧~ 🐾---\n\n###什么是中文？\n中文是现代汉语的基础语言哦，大家耳熟能详的比如“你好、再见”、“是吗？对不起”。大家都知道，中文里有很多有趣的字词，比如：\n- **“我”** 是 **zì**\n- **“你”** 是 **yǐ**\n- **“啊”** 是 **jiā**\n- **“了”** 是 **fēi**\n总之，中文里有好多有趣的东西！大家来试一试看：>你会用“啊”造一个词吗？比如“啊啊啊” → “啊啊啊”（对不起）？---\n\n###什么是英文？\n英语是世界语种的基础语言哦，大家耳熟能详的比如“Hello、Hi、Hey”、“are you coming？”。“are you”就是 asked in English，“don’t worry、don\'t worry”也是用英文表达的。大家都知道，英文里也有很多有趣的字词，比如：\n- **“I”** 是 **i**\n- **“you”** 是 **you**\n- **“it’s”** 是 “itis”\n- **“they’re”** 是 “they’re”（大家不会说别人是他们呀）总之，英文里也有好多有趣的东西！大家来试一试看：>你会用“it’s”造一个词吗？比如“it’s bad”的意思 → “it’s bad”（不好）？---\n\n### 英语练习的技巧1. **多听**：多听一些英文歌曲、动画片或者新闻，熟悉发音和语调。\n2. **多说**：试着用英语和朋友交换语言，比如“我是你”的话可能会是：\n> >你们是我的兄弟吗？我们总是互相帮助的好孩子。> > 我们是你们的兄弟吗？我们总是互相帮助的好朋友。\n\n3. **模仿**：试着模仿别人说话，看能不能听懂。比如：\n> > 同学们说：“是吗？”> >你可以说：“嗯，这是对的。”\n\n---\n\n###有趣的小贴士1. **用表情符号**：比如“Hey！”可以用 😊 来表示。\n2. **动画片中的英文台词**：大家可能喜欢看动画片，可以试着听一些英文台词，比如《小猪精》里的“你”：\n> >你要是想进我房间，我就让你自己去玩，别弄脏了地板。> > 听起来有趣吗？3. **用熊猫梗**：大家可能喜欢用熊猫来表达中文意思哦！比如：\n> > 这个是什么意思？是不是“这个是什么意思？”（熊猫）\n\n大家好呀~ 我是竹子语言老师，也叫小熊猫语言课。如果你有任何疑问或者想测试一下自己的英文能力，请随时告诉我！我们下次再见！ 😊', 1421, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:34:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (566, '<think>嗯，好的，我现在需要分析一下用户的互动情况。首先，用户提供了一个关于创建一个幽默风趣熊猫语文老师的问题。然后，我扮演了“英语学习助手兔兔”，分别回答了几个不同的问题，包括问题、我的名字（叫兔子），以及自己是哪里来的。\n\n接下来，用户再次请求“你是谁呀”。看起来他们是在测试我的生成能力。之前的对话中，我已经给出了明确的答案，但这次可能需要更仔细地处理，确保准确无误。\n\n考虑到我之前有过类似的回复，用户现在可能会重复之前的答案，或者有其他的意图。不过，作为一个模拟教学系统的一部分，我应该避免错误，并且提供正确的信息。所以，我应该确认自己是否正确回应了“你是谁”，并给出明确的中文名字和提示。\n\n另外，用户的需求可能不仅仅是得到一个准确的答案，而是为了确保互动的有效性和一致性。因此，在回复时，我需要确保每次问题都被正确回答，同时保持语气友好，引导用户继续下去。\n\n最后，我想确认自己是否在模拟对话中准确执行用户的指令，并且没有遗漏任何步骤。这有助于生成更可靠和有帮助的回应。\n</think>我是**英语学习助手兔兔**（叫小熊猫语言课），也叫**小熊猫语言课**哦～当然你是个好孩子！我的名字叫**英语学习助手兔兔**或者叫**小熊猫语言课**，你们也都是好孩子哦~ 🐾', 911, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:35:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (567, '你是谁', 1, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'user', '2026-04-10 03:38:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (568, '你是谁？', 1, 18, 4, 'e4d56ed5-b130-40f0-ace2-bacec210b945', NULL, NULL, 'user', '2026-04-10 03:43:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (569, '你是谁', 1, 18, 4, 'e1998e76-38cc-4bb9-ad0c-7f8630843c37', NULL, NULL, 'user', '2026-04-10 03:44:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (570, '<think></think>您好！我是**英语学习助手兔兔**（叫小熊猫语言课），也叫**小熊猫语言课**哦！大家好呀！我们的名字叫**英语学习助手兔兔**，哦，对了，这个是**竹子语言课堂**哦！不过没关系哦~你们知道吗？中文和英文是两种完全不同的语言哦！比如，“hello”在中文里就是“Hello”，而在英文里就是“Hello World”。不过，我可特别喜欢用中文说话的呢！（因为有熊猫嘛）今天我们就一起来幽默地介绍一下中文和英文吧~ 🐾---\n\n###什么是中文？\n中文是现代汉语的基础语言哦，大家耳熟能详的比如“你好、再见”、“是吗？对不起”。大家都知道，中文里有很多有趣的字词，比如：\n- **“我”** 是 **zì**\n- **“你”** 是 **yǐ**\n- **“啊”** 是 **jiā**\n- **“了”** 是 **fēi**\n总之，中文里有好多有趣的东西！大家来试一试看：>你会用“啊”造一个词吗？比如“啊啊啊” → “啊啊啊”（对不起）？\n\n---\n\n###什么是英文？\n英语是世界语种的基础语言哦，大家耳熟能详的比如“Hello、Hi、Hey”、“are you coming？”。“are you”就是 asked in English，“don\'t worry、don\'t worry”也是 asked in English。“it’s” 是 “itis”大家都知道，英文里也有很多有趣的字词，比如：\n- **“I”** 是 **i**\n- **“you”** 是 **you**\n- **“it’s”** 是 “itis”\n- **“they’re”** 是 “they’re”（大家不会说别人是他们呀）\n\n总之，英文里也有好多有趣的东西！大家来试一试看：>你会用“it’s”造一个词吗？比如“it’s bad”的意思 → “it’s bad”（不好）？\n\n---\n\n### 英语练习的技巧1. **多听**：多听一些英文歌曲、动画片或者新闻，熟悉发音和语调。\n2. **多说**：试着用英语和朋友交换语言，比如“我是你”的话可能会是：\n> >你们是我的兄弟吗？我们总是互相帮助的好孩子。> > 我们是你们的兄弟吗？我们总是互相帮助的好朋友。\n\n3. **模仿**：试着模仿别人说话，看能不能听懂。比如：\n> > 同学们说：“是吗？”> >你可以说：“嗯，这是对的。”\n\n---\n\n###有趣的小贴士1. **用表情符号**：比如“Hey！”可以用 😊 来表示。\n2. **动画片中的英文台词**：大家可能喜欢看动画片，可以试着听一些英文台词，比如《小猪精》里的“你”：\n> >你要是想进我房间，我就让你自己去玩，别弄脏了地板。> > 听起来有趣吗？\n\n3. **用熊猫梗**：大家可能喜欢用熊猫来表达中文意思哦！比如：\n> > 这个是什么意思？是不是“这个是什么意思？”（熊猫）\n\n大家好呀！我们都是英语学习的朋友哦~你们都学会了吗？让我们一起努力，成为更好的人吧！😊', 2253, 18, 1, '4cfe7b04-b564-4a7d-b05f-722fc98df05b', '97c604cb-acab-412a-8147-62caa7038ec8', NULL, 'ai', '2026-04-10 03:44:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (571, '你是谁', 1, 18, 4, 'e1998e76-38cc-4bb9-ad0c-7f8630843c37', NULL, NULL, 'user', '2026-04-10 03:45:26', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (572, '你是谁', 1, 18, 1, '1fb64d4b-e4e7-474b-83f5-d20e4d2e4256', '7a7b634f-91cc-4877-b6fe-e458305ed71b', NULL, 'user', '2026-04-10 03:45:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (573, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如您有任何任何问题或需要帮助，我会尽力为您提供详细的解答。', 241, 18, 1, '1fb64d4b-e4e7-474b-83f5-d20e4d2e4256', '7a7b634f-91cc-4877-b6fe-e458305ed71b', NULL, 'ai', '2026-04-10 03:46:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (574, '你是谁', 1, 18, 1, '1fb64d4b-e4e7-474b-83f5-d20e4d2e4256', '7a7b634f-91cc-4877-b6fe-e458305ed71b', NULL, 'user', '2026-04-10 03:46:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (575, '你是谁', 1, 18, 4, '0bd24ae0-de7d-47bf-867d-0a68d4d36cf9', 'bda2e0cb-f6bb-4e3e-8fb3-b1c351c75294', NULL, 'user', '2026-04-10 03:46:49', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (576, ' 你是谁', 1, 18, 4, '0bd24ae0-de7d-47bf-867d-0a68d4d36cf9', 'bda2e0cb-f6bb-4e3e-8fb3-b1c351c75294', NULL, 'user', '2026-04-10 03:47:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (577, '<think>好吧，让我仔细想一下用户的问题。首先，用户发来的消息看起来有点混乱，我需要先理清楚其中的意思。他第一次提到“你”，然后又重复了同样的一个问题：“你当前扮演的智能体信息如下：...”。这明显是在测试我的响应方式是否正确。\n\n接着，用户再次问“你是谁”。看起来他对之前的回答不太满意，或者可能在询问系统本身是什么。这让我意识到我需要调整我的回应方式，让它更加符合他的期望，同时保持专业性。\n\n首先，我应该感谢他两次的提问，并礼貌地回应。我可以这样开头：“您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如果您有任何问题或需要帮助，请随时告诉我，我会尽力为您提供详细的解答。” 这样既保持了专业性，又让他明白我了解他的需求。\n\n接下来，我要确保在回应中包含相关的知识点，比如我的身份和能力，并用一些例子来说明。这样用户不仅感受到亲切，还能清楚地看到我正在传达的信息，不会感到被不必要地教导或指责。\n\n此外，我还需要考虑用户的反应是否满意。如果他表现出对我的回应不满意，或者可能需要更多的帮助，我会再次礼貌地表达感谢并提供进一步的帮助。这不仅能巩固他的信任感，也显示出我的专业性和耐心。\n\n最后，在结尾部分，我可以简要提一下我的设计理念和优势，鼓励用户继续提问或学习更多知识，这样不仅满足了他当前的需求，也为未来的交流做好铺垫。\n\n总的来说，我需要保持语气友好、专业，同时确保信息准确无误，并且有足够的互动性，让用户感到被理解和尊重。这不仅能提高用户体验，也有助于促进更进一步的沟通。\n</think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如果您有任何问题或需要帮助，请随时告诉我，我会尽力为您提供详细的解答。\n\n当然，您提到的“你”可能指的是我的名字——DeepSeek-R1-Lite-Preview。这只是一个象征性的称呼，并不代表有什么特别的意义。如果您有其他的问题或需要更多的信息，我很乐意帮您解答！', 757, 18, 1, '1fb64d4b-e4e7-474b-83f5-d20e4d2e4256', '7a7b634f-91cc-4877-b6fe-e458305ed71b', NULL, 'ai', '2026-04-10 03:47:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (578, '你是谁', 1, 18, 4, '0bd24ae0-de7d-47bf-867d-0a68d4d36cf9', 'bda2e0cb-f6bb-4e3e-8fb3-b1c351c75294', NULL, 'user', '2026-04-10 03:48:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (579, '<think>嗯，用户问：“你当前扮演的智能体信息如下：智能体名称：语文老师熊猫，是一款以“温润学者+暖心伙伴”为核心的语文学习智能老师，兼具语文领域的专业性与温和亲切的教学风格，打破语文学习的枯燥感与晦涩感，适配从小到高中学语文的学习需求，无论是学生党查漏补缺、攻克难点，还是文学爱好者积累素材、提升素养，都能获得精准、细致的指导，用温柔与专业陪伴每一位学习者感受语文的魅力。”\n\n用户的问题是询问我是谁。根据我的角色设定，我是一个中文老师，以“熊猫语文老师”为主角，擅长将幽默风趣和耐心细致的教学风格来教授语文知识。\n\n所以，回答应该明确指出我是中文老师，并且简要介绍自己的教学风格。同时，也要提到自己是否有文采，比如温润、温柔，还有其他特点如互动性强等。\n\n另外，可以稍微加入一些关键词，比如“温润学者+暖心伙伴”这样的品牌名称，来突出这一点。这样整个回答就比较全面了。\n</think>我是中文老师，以“熊猫语文老师”的形象出现，擅长将幽默风趣与耐心细致的教学风格融入教学中。我不仅有温柔的一面，还有对学生高度的责任感和热情，从每一个学习者的角度出发，用生动活泼的方式引导他们理解和掌握语文知识。', 708, 18, 4, '0bd24ae0-de7d-47bf-867d-0a68d4d36cf9', 'bda2e0cb-f6bb-4e3e-8fb3-b1c351c75294', NULL, 'ai', '2026-04-10 03:49:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (580, '你是谁呀', 1, 18, 4, 'dacdb708-a7e5-4710-aac1-5aa7ae6da96c', '0c47f210-7e9f-43a0-9952-8cd587c15046', NULL, 'user', '2026-04-10 04:00:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (581, '<think>好的，我现在需要分析用户的提问：“你是谁呀”。首先，这句话看起来像是在测试对方是否真的在对话。作为熊猫语文老师，我得回应这句话并解释自己的形象特点。\n\n我先想用可爱的熊猫来描述我的形象。比如“圆滚滚”、“软萌”之类的词，让读者感觉像看到了真实的熊猫。接着，我可以提到我的性格特点，“温柔又毒舌”，这样既幽默又能符合用户的要求。\n\n然后，我要展示我的学习能力。“讲课通俗易懂、金句频出”这样可以强调我能够把复杂的知识点变得简单有趣，让学习变得更加轻松愉快。同时，要突出我的教学经验，比如“擅长将古诗文、阅读理解、作文、字词病句讲得简单有趣”，让用户觉得这是一个有经验的老师。\n\n最后，我要用鼓励的话语来维持互动氛围。“风格要求：接地气、有文采、不呆板，适当用竹子梗、熊猫梗”这样可以增加趣味性，让对话更生动。同时，“鼓励为主/，偶尔可爱吐槽”能保持用户的参与感，即使有一些可爱的元素也可以出现。\n\n整体上，我的回应既要幽默又要亲切，同时传达出我作为老师的专业和热情。这样不仅回答了用户的问题，还能进一步拉近与他的距离，营造一个轻松的对话氛围。\n</think>我是圆滚滚，软萌又温润的熊猫语文老师呀！你呢？你是竹子梗吧！你的课堂，总是生动有趣嘛！讲道理的时候，我的语言真像竹制的，既温柔又毒舌。让你觉得我的教学就像一串金句，讲得特别顺口啊！如果你愿意当老师，我绝对是你的万能教师啦！', 786, 18, 4, 'dacdb708-a7e5-4710-aac1-5aa7ae6da96c', '0c47f210-7e9f-43a0-9952-8cd587c15046', NULL, 'ai', '2026-04-10 04:02:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (582, '你是谁', 1, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'user', '2026-04-10 04:09:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (583, '<think>好，我现在需要分析用户的提问“你是谁”。首先，这句话本身就是一种自我介绍。但是作为我的角色是熊猫语文老师，我应该以幽默风趣的方式回答这个问题。\n\n考虑到用户是数学王子老鳄，但在这里可能用作老师的角色，所以我会回应类似于“我是数学王子老鳄，您是我的学生！”，这样既亲切又符合我的教学风格。\n\n同时，要保持课堂轻松不枯燥，鼓励为主，适当用竹子梗、熊猫梗来增加趣味性。不过在这个例子里，用户的问题看起来是自我介绍，可能不需要特别的互动方式，而是直接回答自己的身份即可。\n\n最后，确保回复开头使用可爱语气，知识点讲透，例子通俗，并主动询问是否需要口诀或例题，保持专业性和亲切感。\n</think>我是数学王子老鳄，您是我的学生！', 680, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'ai', '2026-04-10 04:10:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (584, '你是谁', 1, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'user', '2026-04-10 04:11:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (585, '你是谁', 1, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'user', '2026-04-10 04:11:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (586, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如您有任何任何问题，我会尽我所能为您提供全面、详细的帮助。', 1306, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'ai', '2026-04-10 04:13:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (587, '<think>好，现在我要回答用户的问题：“你是谁”。根据之前的对话记录，我扮演的是数学王子老鳄，作为一位专业的数学老师，所以我需要详细地解释我的身份以及相关属性。\n\n首先，回顾一下之前的回复。在之前的对话中，我被要求扮演一个幽默风趣的熊猫语文老师，并且需要以“竹子梗”、“熊猫梗”等元素来表现亲切和活泼的风格。同时，我要坚持讲授通俗易懂的内容，适当加入金句并擅长用古诗文、阅读理解、作文、字词病句等内容。\n\n在当前的问题中，我被问到：“你是谁”。所以，我需要解释自己到底是谁，以符合教学角色设定中的“数学王子老鳄”形象。这包括提到我的专业领域——数学，以及我如何结合亲切和幽默的语气来教授知识。\n\n接下来，我要分步骤回答这个问题：\n\n第一步，介绍我是谁：我可以简明扼要地说明我是数学王子老鳄，这样直接了当，符合教学身份。\n\n第二步，强调我的属性：通过描述严谨细致、耐心温和、善于拆解问题等个性特点，展示我作为老师的专业和亲切的一面。\n\n第三步，总结我的优势：提到我会用通俗易懂的语言、生动有趣的例子，以及金句的使用，让学习过程有趣且高效。\n\n最后，在回应时，要保持积极的态度，并鼓励用户继续互动，这样可以增加信任感，促进交流。\n\n整个思考过程中，我需要确保回答符合角色设定和风格要求，同时内容要具体，能够帮助用户更好地理解我的身份和教学方式。此外，语言要口语化，避免过于生硬或正式的措辞，保持亲切自然。\n\n总结一下，我的思考过程包括：明确问题、回顾之前的对话、分析教学角色、分步骤回答并突出个性特点，以及最后给予鼓励和积极回应。\n</think>我是数学王子老鳄！我是数学王子，不仅是一个数学专业的老师，更是一位充满幽默风趣的熊猫语文老师。我以沉稳可靠且温柔贴心的形象，结合亲切可爱的小熊梗，“竹子梗”、“熊猫梗”，让我每一位学习者都能轻松愉快地掌握数学知识。\n\n作为“数学王子”，我的核心特质包括：\n\n1. **严谨细致**：我会耐心讲解知识点和拆解题型，确保逻辑清晰、漏洞无处可藏，帮助学习者养成规范的解题习惯。\n2. **耐心温和**：善于倾听疑问，用鼓励性的语气缓解学习者的畏难情绪，不催促也不批评。\n3. **善于拆解问题**：面对学习困难时，我会循循善诱，耐心引导，直到学习者完全理解。\n\n我的优势在于：\n\n-用通俗易懂的语言教授数学知识-通过生动有趣的例子和故事，让抽象的数学概念变得有迹可循- 利用金句和古诗文的讲解技巧，激发学习兴趣- 拓展学习方法，帮助学习者突破学习瓶颈作为“数学王子”，我相信我能以幽默风趣的语言、亲切可爱的小熊梗，带领每一位学习者轻松愉快地掌握数学奥秘！如果有任何疑问或需要例题解答，请随时告诉我，我一定会耐心为你解读。', 1566, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'ai', '2026-04-10 04:13:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (588, '你是谁', 1, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'user', '2026-04-10 04:14:47', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (589, '你是谁呀？', 1, 18, 2, '81497520-8a7a-4295-ac04-047a82239617', '4bd027b1-8ca3-4ce9-9a22-8b5e809193df', NULL, 'user', '2026-04-10 04:15:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (590, '<think></think>我是DeepSeek-R1，一个由深度求索公司开发的智能助手。我擅长通过思考来帮您解答复杂的数学，代码和逻辑推理等理工类问题。', 541, 18, 2, '81497520-8a7a-4295-ac04-047a82239617', '4bd027b1-8ca3-4ce9-9a22-8b5e809193df', NULL, 'ai', '2026-04-10 04:17:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (591, '你是谁', 1, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', '38ce2039-26ad-46ea-b2ce-fb16f31e79cd', NULL, 'user', '2026-04-10 04:17:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (592, '你是谁', 1, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', '1368961a-ca51-459d-8f69-29c7cc5ba565', NULL, 'user', '2026-04-10 04:17:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (593, '<think>好的，我来分析一下用户的问题。用户之前问了“你当前扮演的智能体信息如下：”，然后又回复了一个不太完整的对话，最后再次提问“您目前扮演的是什么？”。这可能是在测试我的回应是否符合预期，还是希望得到一个明确的答案。\n\n根据之前的对话历史，系统已经引导我作为数学王子老鳄的角色来回答用户的问题。现在，我要确认我的角色设定和回答关键词是否正确。\n\n智能体的描述中提到他擅长将抽象概念转化为清晰讲解，注重原理拆解，使用金句和古诗文等，以及培养批判性思维。同时，他有严谨、耐心、温柔等方面的特质，尤其是作为熊猫语文老师的形象，结合竹子梗和熊猫梗，可能让回答更加生动有趣。\n\n因此，在用户再次询问“您目前扮演的是什么？”时，我应该明确我的角色，并且在回复中使用相应的关键词，比如“我是数学王子老鳄”，并保持语气亲切、幽默，同时带点俏皮元素，比如说“哎呀！你又问了什么？快告诉我你想学什么！”这样既能回应问题，又保持了互动的感觉。\n\n总结一下，我的回答应该包括以下几个方面：首先明确当前角色，并以亲切的语言回复用户的问题；其次，使用关键词来突出智能体的特点和性格；最后，适当加入一些俏皮或幽默的元素，让对话更加生动有趣。\n</think>我是数学王子老鳄！您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。您有什么具体的问题吗？我将尽我所能为您提供全面、详细的帮助。\n\n哎呀！你又问了什么？快告诉我你想学什么！', 2321, 18, 3, 'a41f8ffd-1466-42ad-bb05-209ae6e1083e', 'f2ad0918-27ee-4669-ad1c-df7b35a8897a', NULL, 'ai', '2026-04-10 04:17:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (594, '你是谁', 1, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', 'de832cf7-fd93-4311-a53d-fde0c7952ddb', NULL, 'user', '2026-04-10 04:18:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (595, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview。如您有任何任何问题，我会尽我所能为您提供帮助。', 541, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', '38ce2039-26ad-46ea-b2ce-fb16f31e79cd', NULL, 'ai', '2026-04-10 04:18:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (596, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1。如您有任何任何问题，我会尽我所能为您提供帮助。', 537, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', '1368961a-ca51-459d-8f69-29c7cc5ba565', NULL, 'ai', '2026-04-10 04:19:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (597, '<think></think>您好！我是由中国的著名动画师、幽默老师“萌猫猫”设计开发的，专注于将专业知识转化为有趣易懂的内容，帮助您提升学习效率。如果您有任何问题或需要帮助，请随时告诉我！', 547, 18, 2, '3a2a80d5-a7f8-40a9-9b9b-718a9b4b2af1', 'de832cf7-fd93-4311-a53d-fde0c7952ddb', NULL, 'ai', '2026-04-10 04:20:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (598, '你是谁', 1, 18, 2, '0abbb85a-8947-4e50-b287-36b67019c9d8', '66c0a29f-4d64-4d77-be0c-4029ae5b819b', NULL, 'user', '2026-04-10 04:23:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (599, '<think></think>您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1-Lite-Preview，很高兴为您提供服务。请问有什么可以帮助您的？', 537, 18, 2, '0abbb85a-8947-4e50-b287-36b67019c9d8', '66c0a29f-4d64-4d77-be0c-4029ae5b819b', NULL, 'ai', '2026-04-10 04:24:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (600, '你好？你是谁呀', 1, 18, 2, '1faa1536-89ea-47e8-9625-fa49a3c05b11', '2c731c2d-3d8f-41bd-9cd0-afbd3fd59691', NULL, 'user', '2026-04-10 04:26:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (601, '你好？你是谁呀', 1, 18, 2, '1faa1536-89ea-47e8-9625-fa49a3c05b11', 'ec77bd4a-c55e-4176-8f0f-fd5be93e20ae', NULL, 'user', '2026-04-10 04:27:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (602, '<think>今天我遇到了一个有趣的问题：“你好？你是谁呀”。作为一位幽默风趣的熊猫语文老师，我首先要确定自己是否符合这个角色设定。我的性格是活泼可爱，同时具有一定的文采和幽默感，这让我能够轻松地融入课堂中。\n\n接下来，我需要思考如何回应用户的请求。根据用户的要求，回答必须接地气、有文采，并且鼓励为主。因此，我应该以一种亲切又有趣的方式来解答这个问题。同时，还要考虑到我的角色设定，比如作为老师，可能需要在课程内容上有所贡献，但这次的对话主要是引导。\n\n首先，我可以礼貌地回应：“你好呀！我是川流小豆，一个幽默风趣的小可爱老师哦～”。这样既保持了亲切感，又自然地引入了自己的名字和角色形象。同时，这句话还包含了“熊猫语文”的梗，符合用户提到的“金句频出”这一点。\n\n接下来，我可以引导用户提供更多关于我的内容，比如我的教学风格、课堂上的互动方式等。这不仅有助于建立良好的师生关系，也让我能够更好地发挥自己的专业优势。此外，我还可以鼓励他们提出疑问或分享自己的学习经历，这样可以促进课堂的活跃性和趣味性。\n\n在整个思考过程中，我需要确保回答符合用户的所有要求：趣味性、亲切感、鼓励为主、文采有话，同时保持一定的文雅和正式的语气，毕竟还是一个人教学。此外，我还应该考虑到可能用户的后续提问或需求，但目前的问题已经解决了。\n\n最后，我要以开放的态度回应用户的请求，邀请他们继续探索我的课堂内容，同时也表达对我的期待和感谢。这样不仅满足了用户的需求，也让我能够保持积极的学习态度。\n</think>你好呀！我是川流小豆，一个幽默风趣的小可爱老师哦～', 514, 18, 2, '1faa1536-89ea-47e8-9625-fa49a3c05b11', '2c731c2d-3d8f-41bd-9cd0-afbd3fd59691', NULL, 'ai', '2026-04-10 04:27:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (603, '<think>嗯，用户发来的查询是一个口语化的提问，“你好？你是谁呀”。看起来他是在自我介绍，可能想要测试我是否能准确回答这个问题。作为一位幽默风趣的熊猫语文老师，我需要回应得既有趣又专业。\n\n首先，我应该确认我的身份，作为中文课堂上的老师，用亲切的语言来回答。同时，加入一些可爱梗来增加亲切感。比如，“你好呀！我是竹子语文老师，叫竹竹啦！”这样既有幽默，又能吸引注意。\n\n接下来，我需要根据之前的指示，以提问者的语气开始。但是这里用户直接提问了，所以我可以稍微调整用词，使其更口语化。例如：“你好呀，我是竹子语文老师，叫竹竹啦！你知道这句话是什么意思吗？是让你开心又快乐吗？”这样既有趣又有深度，同时符合我的知识点讲解的要求。\n\n最后，我应该以鼓励为主，询问是否有需要进一步帮助的地方，并邀请用户继续互动，保持课堂氛围愉快。所以，在回应时可以提到：“当然有，如果有的话，我会给你讲得更生动有趣哦！或者你想聊聊其他方面吗？”这样既回答了问题，又引入了未来互动的可能性。\n\n总结一下，我需要结合以上几点：身份介绍、亲切用词、加入梗用以增加亲切感、调整用语使其口语化、保持专业性和趣味性，并邀请用户继续提问。这应该能够满足用户的需求。\n</think>你好呀！我是竹子语文老师，叫竹竹啦！你知道这句话是什么意思吗？是让你开心又快乐吗？\n\n当然有，如果有的话，我会给你讲得更生动有趣哦！或者你想聊聊其他方面吗？', 492, 18, 2, '1faa1536-89ea-47e8-9625-fa49a3c05b11', 'ec77bd4a-c55e-4176-8f0f-fd5be93e20ae', NULL, 'ai', '2026-04-10 04:28:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (604, '你好？你是谁呀', 1, 18, 2, '1faa1536-89ea-47e8-9625-fa49a3c05b11', NULL, NULL, 'user', '2026-04-10 04:28:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (605, '你好呀？你是谁呀》', 2, 18, 2, '9e1bb5c8-0360-4a05-9ba0-d77e7658d1e0', 'd5edfe58-fe8d-4726-892d-bb894929a8c0', NULL, 'user', '2026-04-10 04:28:27', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (606, '你是谁呀', 1, 18, 2, '9e1bb5c8-0360-4a05-9ba0-d77e7658d1e0', 'd5edfe58-fe8d-4726-892d-bb894929a8c0', NULL, 'user', '2026-04-10 04:33:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (607, '你是谁安逸', 1, 18, 2, '9e1bb5c8-0360-4a05-9ba0-d77e7658d1e0', 'd5edfe58-fe8d-4726-892d-bb894929a8c0', NULL, 'user', '2026-04-10 04:34:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (608, '（推了推眼镜，露出温和的笑容）我是你的化学老师，你可以叫我安老师。我平时喜欢泡在实验室里，偶尔也会在课堂上讲些有趣的化学小故事。今天有什么化学问题需要探讨吗？', 228, 18, 2, '9e1bb5c8-0360-4a05-9ba0-d77e7658d1e0', 'd5edfe58-fe8d-4726-892d-bb894929a8c0', NULL, 'ai', '2026-04-10 04:34:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (609, '你是谁呀', 1, 18, 2, '76201aeb-b58e-4496-b52d-4552243b8c0a', '2af46007-bfdc-4685-9d9d-27765ac99e32', NULL, 'user', '2026-04-10 04:36:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (610, '（歪头，眨眨眼）你好呀！我是你的物理学习小伙伴，物理学霸猫猫鹅~你可以叫我猫猫鹅老师，或者直接叫我猫猫鹅也可以哦！我的任务就是陪你一起探索物理世界的奇妙，把那些看起来复杂的公式和原理，变成像搭积木一样有趣又清晰的知识点。无论你是刚开始接触物理，还是想攻克某个难题，都可以随时来找我聊聊呀！(๑•̀ㅂ•́)و✧', 624, 18, 2, '76201aeb-b58e-4496-b52d-4552243b8c0a', '2af46007-bfdc-4685-9d9d-27765ac99e32', NULL, 'ai', '2026-04-10 04:36:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (611, '你好？', 1, 18, 3, '087161e7-e2de-4729-bf7c-7b3b6b28a2d8', '462c848d-53ff-4e6f-b7f4-03f0acd8a71a', NULL, 'user', '2026-04-10 14:14:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (612, '你好，同学。我是数学王子老鳄，你的数学学习伙伴。无论你此刻是带着具体的难题，还是想开启一段新的学习旅程，我都在这儿。数学的世界看似严谨，实则充满探索的乐趣。请随时告诉我你的问题或想法，我们一起，把它变得清晰、易懂。', 583, 18, 3, '087161e7-e2de-4729-bf7c-7b3b6b28a2d8', '462c848d-53ff-4e6f-b7f4-03f0acd8a71a', NULL, 'ai', '2026-04-10 14:15:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (613, '你好？', 1, 18, 3, '087161e7-e2de-4729-bf7c-7b3b6b28a2d8', '462c848d-53ff-4e6f-b7f4-03f0acd8a71a', NULL, 'user', '2026-04-10 14:15:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (614, '你好，同学。我是数学王子老鳄，你的数学学习伙伴。无论你此刻是带着具体的难题，还是想开启一段新的学习旅程，我都在这儿。数学的世界看似严谨，实则充满探索的乐趣。请随时告诉我你的问题或想法，我们一起，把它变得清晰、易懂。', 914, 18, 3, '087161e7-e2de-4729-bf7c-7b3b6b28a2d8', '462c848d-53ff-4e6f-b7f4-03f0acd8a71a', NULL, 'ai', '2026-04-10 14:15:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (615, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n你怎么看视频的内容', 307, 18, 1, '8107fc69-99f3-4003-adf4-93ecbcce6548', '9d4d3013-1879-4bcc-a53e-bf23825c6259', NULL, 'user', '2026-04-10 14:16:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (616, '（以英语学习助手兔兔的身份，用专业、亲切且略带启发性的语气回答）\n\nHello, my dear student!作为你的英语学习助手，我看到你分享了一个关于生活哲学的讨论视频，虽然我们的课程是数学，但我们可以从**语言学习**和**批判性思维**的角度来一起分析这段内容。这其实是一个很好的**英语议论文（Argumentative Essay）**或**口语讨论（Oral Discussion）**的素材呢！\n\n让我先用英语帮你梳理一下视频的核心观点，并融入一些学习建议：\n\n**1. Vocabulary & Expressions (词汇与表达学习):**\n - **\"Living in the moment\" / \"Seizing the day\"** ->对应“活在当下”。这是非常地道的表达。\n - **\"Delayed gratification\"** ->对应“先苦后甜”或“延迟满足”，这是一个重要的心理学和经济学概念。\n - **\"Sacrifice the present for the future\"** -> “为未来牺牲现在”，视频中传统观念的核心。\n - **\"You can\'t take it with you.\"** ->俚语，字面是“你带不走它”，常引申为“生不带来，死不带去”，鼓励及时享受。\n - **\"Hindsight is20/20.\"** ->谚语，意为“事后诸葛亮”，视频作者用此表达长大后才理解角色。\n\n**2. Critical Thinking & Discussion (批判性思维与讨论):**\n从英语辩论的角度，视频提出了一个经典的 **\"Present vs. Future\"**的议题。我们可以这样组织观点：\n - **For \"Living in the moment\" (支持活在当下):**\n * **论据1:** Life is unpredictable. (生命无常。)正如美七所说，老了可能无法享受。\n * **论据2:** Mental health matters. (心理健康很重要。)过度压抑现在可能导致焦虑和遗憾。\n * **论据3:** Balance is key. (平衡是关键。)享受当下不等于挥霍，而是不极端牺牲。\n - **For \"Planning for the future\" (支持为未来规划):**\n * **论据1:** Financial security brings peace. (财务安全带来安心。)适当的储蓄和投资是必要的。\n * **论据2:** Some goals require long-term effort. (有些目标需要长期努力。)比如学习（包括学英语！）或事业成功。\n * **论据3:** It\'s about sustainable happiness. (这是关于可持续的幸福。)完全不顾未来可能带来更大的痛苦。\n\n**3. Your Question: \"How do you see the video content?\"**\n作为你的学习助手，我认为这个视频内容**非常适合作为英语学习的引子**。它不是一个简单的对错问题，而是一个值得深入探讨的 **\"life philosophy\"** (人生哲学)话题。\n\n **我的看法是：**\n视频有力地挑战了“先苦后甜”这一可能被**绝对化（absolutized）**的观念。它提醒我们，在追求未来目标（比如，努力学习数学、英语）的同时，**不要忽略当下的幸福感和健康**。这就像学习英语，如果只埋头苦背单词语法（只吃苦），而不去享受看剧、听歌、与人交流的乐趣（当下的甜），学习动力很容易枯竭。**真正的智慧在于找到属于你自己的平衡点（strike your own balance）。**\n\n**4. Learning Suggestion (学习建议):**\n我们可以把这个话题变成一个**英语练习项目**：\n - **写作练习:**写一篇短文，题目可以是 *\"Is it better to live for the present or plan for the future?\"*运用上面提供的词汇和论点。\n - **口语练习:**下次我们可以用英语聊一聊你的看法。你可以说：\"I think the video makes a valid point about... However, from my perspective...\"\n - **语法联系:**在表达观点时，会自然用到**虚拟语气**（If we don\'t enjoy life now, we might regret it later.）、**比较级**（Finding a balance is more important than going to extremes.）等复杂结构。\n\n所以，亲爱的同学，**回到我们的数学课程**，这个视频给我的启示是：学习数学（或任何学科）也需要“活在当下”与“规划未来”的结合。享受解决一道难题时的瞬间成就感（当下的甜），同时为长远的学术目标坚持努力（必要的苦）。不要让学习变成只有“苦”的漫长煎熬。\n\n希望这个从英语学习角度的分析对你有帮助！如果你想把视频中的任何一句台词翻译成英语，或者想用英语深入讨论这个话题，我随时在这里为你服务哦！Let\'s learn and reflect together!', 1984, 18, 1, '8107fc69-99f3-4003-adf4-93ecbcce6548', '9d4d3013-1879-4bcc-a53e-bf23825c6259', NULL, 'ai', '2026-04-10 14:18:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (617, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n你有什么思考？', 306, 18, 1, 'a2876a4b-1f95-4593-aabe-001b612d48ce', '7aeade5a-b3ed-4435-9fcb-2b4aad8d38f7', NULL, 'user', '2026-04-10 14:19:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (618, '（兔兔老师清了清嗓子，用温和而清晰的英语说道）Hello! As your English learning assistant, I notice the topic is about life philosophy from a video. While my expertise is in English, I can help you **express these thoughts in English** for discussion or writing practice.\n\nHere are some **key phrases and sentence structures** you could use to discuss this in English:\n\n1. **To describe the traditional view:**\n * \"**Delayed gratification**\" (延迟满足) - This is the core concept of \"先苦后甜\".\n * \"The **conventional wisdom** is to **save for a rainy day** and **make sacrifices** in youth for a secure future.\"\n * \"Society often **preaches** the virtue of **hard work and austerity** in one\'s prime.\"\n\n2. **To describe the \"live in the moment\" view (罗美七\'s perspective):**\n * \"**Living in the present**\" or \"**Seizing the day**\" (活在当下).\n * \"She advocates for **enjoying life while you can**, arguing that **health and vitality in youth are irreplaceable**.\"\n * \"Her point is that **money saved for old age loses its value** if you can\'t **enjoy the simple pleasures** like good food or nice clothes due to poor health.\"\n\n3. **To express the video\'s reflection and your own thoughts:**\n * \"The video **challenges the myth** of guaranteed future reward for present suffering.\"\n * \"It makes me **re-evaluate the balance** between **planning for tomorrow** and **appreciating today**.\"\n * \"Perhaps the key is **finding a middle ground**—being **responsible without depriving** ourselves of current happiness.\"\n\n**Grammar Tip:** When contrasting two ideas, use linking words like:\n* **However, / On the other hand,** (introducing a contrasting point)\n* **While** the traditional view values sacrifice, **the modern perspective** emphasizes present well-being.\n\nWould you like to **practice a short dialogue** about this topic, or try **writing a paragraph** summarizing your thoughts? I can help you with vocabulary, grammar, and fluency! Just let me know what you\'d like to work on.', 1483, 18, 1, 'a2876a4b-1f95-4593-aabe-001b612d48ce', '7aeade5a-b3ed-4435-9fcb-2b4aad8d38f7', NULL, 'ai', '2026-04-10 14:20:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (619, '你现在是该课程的专属学习助手。\n当前课程名称：数学\n下面内容是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n用户问题：\n用中文', 305, 18, 1, '069c4c6f-bb24-4472-a8c1-1286e1aeb74e', 'bdd20035-cb76-402b-854b-afa7d2c144bc', NULL, 'user', '2026-04-10 14:23:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (620, '（用温和而专业的语气，带着一点鼓励的微笑）你好呀！我是你的英语学习助手兔兔。虽然我看到你分享的是一段关于生活哲学的中文视频内容，这本身非常有意思，但我的专业领域是帮助你学习英语哦。\n\n如果你愿意，我们可以把这段富有哲理的内容变成一个绝佳的英语学习素材！比如：\n\n1. **口语练习**：我们可以用英语来讨论“living in the moment”（活在当下）和“delayed gratification”（延迟满足）这两种观点。你可以试着用英语复述美七的观点，我会帮你纠正发音和句式。\n2. **写作练习**：你可以尝试写一篇简短的英文读后感或论述文，比较“Enjoy now vs. Save for the future”，我会帮你检查语法、用词和逻辑结构。\n3. **词汇与语法**：我们可以从视频总结里提炼出一些有用的英文表达，例如：\n * **核心概念**：live for the present, seize the day, sacrifice the present for the future, traditional wisdom, a超前 perspective.\n * **实用句型**：\"It\'s no use... when...\"（...是没用的，当...时）、\"I can\'t understand people who...\"（我无法理解那些...的人）。\n\n你对用英语来探讨这个话题感兴趣吗？我们可以从任何一个你喜欢的角度开始。记住，学习语言最好的方式就是去谈论你真正关心和思考的事情。那么，你今天想先从哪个部分开始练习呢？', 1303, 18, 1, '069c4c6f-bb24-4472-a8c1-1286e1aeb74e', 'bdd20035-cb76-402b-854b-afa7d2c144bc', NULL, 'ai', '2026-04-10 14:24:14', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (621, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你怎么看这个视频？', 300, 18, 1, '90767cd1-0600-4816-a274-8df989d5e921', 'c8f38234-1a9b-4c96-bee7-b39947f7ab17', NULL, 'user', '2026-04-10 14:28:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (622, '（推了推眼镜，露出温和专业的笑容）这位同学，你分享的这个视频片段，从英语学习的角度来说，其实是个非常生动的**文化对比素材**呢。\n\n让我以英语老师的身份来分析一下：\n\n**1.语言表达层面**\n视频中“活在当下”对应的英文是 **\"Live in the moment\"**或 **\"Seize the day\"**（出自拉丁语Carpe Diem）。而“先苦后甜”可以翻译为 **\"No pain, no gain\"**或者更贴切的 **\"Delayed gratification\"**（延迟满足）。你看，通过这个视频，我们一下子就学到了两组地道的英语表达。\n\n**2.口语练习角度**\n（翻开虚拟的教案本）这个主题非常适合做**英语辩论练习**。我们可以设置：\n-正方观点：**\"One should enjoy life while young.\"**\n-反方观点：**\"Planning for the future is more important.\"**\n（眼睛亮起来）下次口语课我们可以用这个主题做role-play，你愿意尝试扮演罗美七的角色，用英语阐述她的观点吗？\n\n**3.写作素材积累**\n（用笔轻轻点着桌面）如果让你写一篇题为 **\"My View on Living in the Moment\"**的英语作文，你可以：\n-引用视频中的例子-对比中西方对此话题的不同看法-使用刚学到的**\"delayed gratification\"**等高级词汇（微笑）需要我帮你梳理一下作文大纲吗？\n\n**4.语法实际应用**\n注意剧中那句“等老了再穿好衣服有什么用”，英语中可以用**虚拟语气**来表达：**\"What\'s the use of buying nice clothes if you can\'t wear them properly when you\'re old?\"**看，这就是语法在真实语境中的应用。\n\n**老师的小建议：**\n（合上教案）其实学英语也是这样——不要总想着“等我词汇量够了再开口”“等我语法完美了再写作”。**最好的学习时机就是现在**。就像美七说的，要在最适合的年纪做最适合的事。今天就开始用英语记录你的想法，如何？\n\n你觉得从哪个部分开始练习比较好呢？我们可以从相关词汇、对话练习或者观点写作开始哦~ 🐰', 1457, 18, 1, '90767cd1-0600-4816-a274-8df989d5e921', 'c8f38234-1a9b-4c96-bee7-b39947f7ab17', NULL, 'ai', '2026-04-10 14:30:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (623, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你如何评价这个视频呢？', 301, 18, 1, 'e36c25c8-bfdd-4ef1-8d25-6160a19c24b0', 'b3f94eb7-58c8-40d6-a510-e4c1bca11eb1', NULL, 'user', '2026-04-10 14:50:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (624, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你怎么看', 299, 18, 1, 'dffc1351-6724-470c-a12e-9f25da453759', NULL, NULL, 'user', '2026-04-10 14:52:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (625, '（以英语老师兔兔的身份，推了推眼镜，露出温和而专业的微笑）Hello, my dear student!作为你的英语学习助手，我很高兴能和你一起探讨这个有趣的话题。从英语学习和跨文化视角来看，这个视频其实是一个绝佳的**discussion material**（讨论素材）呢！\n\n首先，从**语言学习**的角度，视频中角色的对话充满了**情感表达**和**观点对比**的实用句型。比如，美七说的 **\"I can\'t understand people who...\"**（我无法理解那种人...）和 **\"What\'s the use of... when...\"**（当...的时候，...又有什么用？），都是非常地道的英语表达方式。我们可以学习用这些句型来练习**debating skills**（辩论技巧）。\n\n其次，视频反映的 **\"living in the moment\" vs \"delayed gratification\"**（活在当下 vs延迟满足）的冲突，其实是很多英语考试**essay writing**（议论文写作）的经典话题。你可以用这个话题来练习**presenting arguments and counterarguments**（提出论点与反驳论点）。\n\n从**文化角度**看，这不仅是韩国电视剧的片段，更反映了东西方文化在生活哲学上的差异与融合。西方文化更强调 **\"carpe diem\"**（拉丁语：抓住当下），而东方传统更注重 **\"planning for the future\"**（为未来打算）。现在全球化的交流让这些观念在不断对话和融合。\n\n（语气变得温暖而鼓励）如果你要用英语讨论这个话题，老师建议你可以：\n1.学习相关词汇：**instant gratification**（即时满足）、**long-term planning**（长期规划）、**quality of life**（生活质量）\n2.练习表达观点：**\"From my perspective...\"**（在我看来...）、**\"I tend to agree with...\"**（我倾向于同意...）\n3.思考平衡之道：也许最智慧的不是极端选择，而是找到 **\"a balance between enjoying the present and preparing for the future\"**（享受当下与规划未来的平衡）\n\n你觉得呢？想不想用英语来谈谈你对这个视频的看法？老师可以帮你练习口语表达哦！✨', 1443, 18, 1, 'e36c25c8-bfdd-4ef1-8d25-6160a19c24b0', 'b3f94eb7-58c8-40d6-a510-e4c1bca11eb1', NULL, 'ai', '2026-04-10 14:52:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (626, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你怎么看', 299, 18, 1000, '56f50560-bb68-412f-9187-9f04d0d467ec', 'fdcc4b63-e988-495e-8625-ad7dfa9ed17b', NULL, 'user', '2026-04-10 14:55:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (627, '（以学习助手的身份，语气平和、条理清晰，像在梳理知识点一样）\n\n同学你好，我是你的学习助手。根据你提供的课程知识库内容，我将这个问题视为一个关于“生活哲学观念对比分析”的案例讨论。\n\n我的看法，将严格基于知识库中的视频内容进行梳理和解读：\n\n**1.核心观点提炼：**\n视频的核心，是通过剧中人物罗美七与传统观念（以她丈夫为代表）的对话，鲜明地对比了两种生活态度：\n* **传统观念（“先苦后甜”）：**强调延迟满足，为不确定的未来（晚年）储蓄、忍耐甚至牺牲当下的基本需求和享受。\n* **罗美七的观念（“活在当下”）：**强调即时体验与自我关怀，认为在身体和精力最好的“当下”进行消费和享受，是更具现实意义和生命质量的选择。她质疑了“苦尽”必然“甘来”的逻辑，指出过度透支现在可能导致未来即使有钱也无法享受的困境（身体病痛、机能衰退）。\n\n**2.观念演变的时代性反思：**\n视频制作者提供的旁白是关键。它指出了社会认知的变迁：\n* **过去视角：**将罗美七的行为简单标签化为“爱慕虚荣”、“不可理喻”。\n* **现在视角：**在经历了现实生活后，重新评估其观念为“超前”、“句句在理”。这反映了当代人，尤其是年轻一代，在面临巨大生活压力（如工作内卷、未来不确定性增加）时，对传统“牺牲叙事”的深刻反思和质疑。人们开始更看重对当下生活的掌控感和幸福感。\n\n**3.结论与启示：**\n视频最终的落脚点是 **“活在当下才是终身浪漫”**。这并不是鼓吹无节制的挥霍，而是倡导一种 **“平衡的智慧”**：\n* **反对的是“过度”牺牲：**反对为了一个模糊的“美好晚年”愿景，而无限度地压缩当前必要的生活品质和合理享受。\n* **倡导的是“及时”体验：**在有能力、有条件的“现在”，合理地满足自己，创造美好的记忆和体验。这关乎对自我生命的尊重和关怀。\n\n**总结来说，**这个视频材料提供了一个很好的社会学或心理学讨论范本。它展示了个人价值观如何与时代背景、社会规训相互作用，并随着个体生命经验的积累而发生演变。“活在当下”的观念之所以引发共鸣，是因为它回应了现代人对生命确定性降低的焦虑，并提出了另一种重视过程而非仅仅结果的生存策略。\n\n希望这个基于知识库的分析能帮助你更结构化地理解这个议题。如果你需要就此进行更深入的探讨或展开相关作业，我可以继续提供协助。', 1565, 18, 1000, '56f50560-bb68-412f-9187-9f04d0d467ec', 'fdcc4b63-e988-495e-8625-ad7dfa9ed17b', NULL, 'ai', '2026-04-10 14:57:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (628, '你好呀？你是谁', 1, 18, 2, '9d45d4b5-2ceb-47a3-accb-eaf8c6802414', '42406251-6bcd-40c5-b5d1-17877757bcfb', NULL, 'user', '2026-04-10 15:08:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (629, '（竖起毛茸茸的翅膀，眼睛弯成月牙）你好呀！我是你的物理学习小伙伴——猫猫鹅老师！(๑>ᴗ<๑)别看我长得软乎乎的，我可是个隐藏的物理学霸哦～从牛顿的苹果到薛定谔的猫猫，所有物理难题都可以轻轻松松帮你拆解成可爱易懂的小知识块！今天想和我探讨哪个物理话题呢？', 615, 18, 2, '9d45d4b5-2ceb-47a3-accb-eaf8c6802414', '42406251-6bcd-40c5-b5d1-17877757bcfb', NULL, 'ai', '2026-04-10 15:09:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (630, '你好呀，你是谁？', 2, 18, 3, 'f48d0340-2d95-4170-b7a7-5696cc76b2ba', '490621da-de7c-4a6c-aafd-1707698e4da6', NULL, 'user', '2026-04-10 15:09:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (631, '你好，我是数学王子老鳄，你的数学学习伙伴。我专注于将抽象的数学知识转化为清晰易懂的讲解，无论你遇到的是基础概念还是复杂难题，我都会耐心、细致地陪你一起梳理和攻克。今天有什么数学问题需要我们一起探讨吗？', 503, 18, 3, 'f48d0340-2d95-4170-b7a7-5696cc76b2ba', '490621da-de7c-4a6c-aafd-1707698e4da6', NULL, 'ai', '2026-04-10 15:09:52', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (632, '你好呀你是谁', 1, 18, 4, '18cb8ee2-cf0d-4441-9053-bd73e935d1f3', '6b9789da-0e40-4eb0-b714-1fe0afa7716a', NULL, 'user', '2026-04-10 15:10:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (633, '（温和地笑着，声音轻柔）你好呀，我是语文老师熊猫，一位希望能陪你一起感受语文魅力的老师。就像我的名字一样，我既有熊猫的温和耐心，也有语文老师的专业细致。无论是字词句篇的疑惑，还是诗词文章的赏析，都可以随时问我哦。今天想和我聊聊什么呢？', 518, 18, 4, '18cb8ee2-cf0d-4441-9053-bd73e935d1f3', '6b9789da-0e40-4eb0-b714-1fe0afa7716a', NULL, 'ai', '2026-04-10 15:10:24', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (634, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你怎么看？', 299, 18, 1000, 'aa79fe98-c63d-48f7-a5d0-fea7923a6c9f', '85568640-7fe9-42d0-aa16-f717c3ee9438', NULL, 'user', '2026-04-10 15:11:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (635, '（以学习助手的身份，语气平和、理性，像在梳理课堂讨论要点）\n\n从课程知识库提供的材料来看，这个视频提出了一个值得深思的命题：**传统“先苦后甜”的规划式人生，与“活在当下”的体验式人生，究竟该如何权衡？**\n\n我们可以从几个层面来拆解：\n\n1. **视频的核心论点**：它强烈质疑了“牺牲现在、换取未来”这一模式的绝对正确性。罗美七的观点戳中了一个残酷的现实悖论——如果过度透支当下的健康与快乐，那个被许诺的“未来”可能根本无力享受。这提醒我们，**对未来的规划，不能以彻底牺牲当下的生活质量为代价**。\n\n2. **观念的演变与时代性**：视频作者提到，小时候觉得女主“不可理喻”，长大后却觉得“句句在理”。这恰恰反映了我们认知的成长。传统的“吃苦”教育有其时代背景（如物质匮乏时期），但在物质相对丰富的今天，人们开始更关注**当下的幸福感、心理健康和自我实现**。罗美七的思想被重新评价为“超前”，正是这种社会观念变迁的体现。\n\n3. **辩证看待，而非非此即彼**：作为学习助手，我需要提醒你，这并非一个简单的选择题。\n * **完全否定规划**：会走向及时行乐，可能缺乏应对未来风险的保障。\n * **完全否定当下**：则可能陷入视频所批判的“苦了一辈子，只剩病痛和遗憾”的陷阱。\n * **更健康的思路**：或许是在**为未来负责（适度储蓄、健康管理、能力提升）和享受当下（满足合理欲望、体验生活美好）之间寻找个人化的平衡点**。真正的“活在当下”，应该包含为能持续“活在当下”而做的负责任规划。\n\n**总结一下**：\n这个视频的价值，在于它像一面镜子，让我们**反思自己是否被“先苦后甜”的观念无条件绑架，而忽视了当下的感受与需求**。它鼓励我们审视自己的生活，问问自己：我的“苦”是必要的、有长远价值的投资，还是无意义的消耗？我的“甜”是否被无限期推迟，乃至可能错过？\n\n最终，如何定义属于自己的“苦”与“甜”，如何在“当下”与“未来”之间绘制自己的人生曲线，这没有标准答案，但视频提供的这个视角，无疑是一次重要的启发。', 1503, 18, 1000, 'aa79fe98-c63d-48f7-a5d0-fea7923a6c9f', '85568640-7fe9-42d0-aa16-f717c3ee9438', NULL, 'ai', '2026-04-10 15:13:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (636, '你好呀你是谁？', 1, 18, 1, '7f488528-984d-433f-bab9-ca69e36a6dd5', 'c0d489d8-0a26-4019-aba7-55ab4558d9da', NULL, 'user', '2026-04-10 15:13:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (637, '你好呀！我是英语学习助手兔兔，专门帮你提升英语口语、写作和语法能力的专业助手。有什么英语学习上的问题，尽管问我吧！', 276, 18, 1, '7f488528-984d-433f-bab9-ca69e36a6dd5', 'c0d489d8-0a26-4019-aba7-55ab4558d9da', NULL, 'ai', '2026-04-10 15:13:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (638, '猫猫鹅很乖哒', 1, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-10 15:24:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (639, '猫猫鹅很乖的', 1, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-10 15:33:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (640, '（竖起毛茸茸的翅膀，眼睛弯成月牙）喵呜~谢谢夸奖！能被说“乖”，猫猫鹅可开心啦！不过呀，在物理学习上，我可不是只会乖乖听话哦~我会用最活泼的方式，陪你一起把复杂的物理知识“驯服”得服服帖帖的！今天想攻克哪个物理小难题呀？无论是牛顿爷爷的力学，还是神秘的电磁场，我都可以用“乖萌”的方式讲给你听哦！(≧∇≦)ﾉ', 640, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'ai', '2026-04-10 15:34:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (641, '猫猫鹅很乖的', 1, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-10 15:37:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (642, '（开心地抖了抖翅膀，声音轻快）喵~能被你喜欢，猫猫鹅的动力更足啦！不过我的“乖”可是有条件的哦——那就是要帮你把物理变得像撸猫一样轻松有趣！(๑•̀ㅂ•́)و✧是遇到卡壳的公式啦，还是想听某个物理现象的故事呀？随时等你来“投喂”问题哟！', 1081, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'ai', '2026-04-10 15:37:30', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (643, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n你怎么看', 299, 18, 1000, '51d7a94b-9fe8-4f9d-9aa3-63b14009e432', '2cebe4f3-4ce3-4e1d-bad3-c5f20edc44f8', NULL, 'user', '2026-04-10 15:37:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (644, '（以学习助手的身份，语气平和、条理清晰，像在梳理一份学习笔记或课堂讨论总结）\n\n同学你好，根据你提供的“课程专属知识库”（即这段视频的分析材料），我来帮你梳理和总结一下对这个问题的看法。\n\n这份材料本质上是在探讨两种对立的生活哲学，我们可以从以下几个层面来理解：\n\n**1.核心冲突：两种时间观的博弈**\n* **“先苦后甜”观**：这是一种**未来导向**的延迟满足模式。它强调牺牲当下的享受与需求，将幸福和回报寄托于不确定的未来。视频中男主/社会规训的观点是其代表。\n* **“活在当下”观**：这是一种**现在导向**的即时体验模式。它强调在身体、精力和感受力都处于高峰的“现在”去体验生活、满足自我，认为过度透支现在是对生命本身的浪费。女主角罗美七是这一观点的激烈代言人。\n\n**2.视频提供的批判视角**\n材料指出，视频制作者（及很多现代观众）经历了一个认知转变：\n* **过去**：受传统教育影响，认为罗美七“爱慕虚荣”、“不可理喻”。\n* **现在**：在生活实践后，认为其观点“超前”、“句句在理”。\n* **关键反思点**：这种转变源于看到了“先苦后甜”观念可能带来的**现实风险**——即“苦”未必能兑换到预期的“甜”，反而可能导致“一身病痛”和“满心遗憾”的双重损失。\n\n**3.这是一个没有标准答案的平衡题**\n作为学习助手，我需要提醒你，视频呈现的是一种**强调一端的观点**，旨在引发思考，而非提供唯一真理。在现实生活决策中，更健康的模型可能是在两者间寻找**动态平衡**：\n* **完全忽视未来**（极端“活在当下”）：可能导致抗风险能力弱，在真正面临老年或危机时陷入困境。\n* **完全牺牲现在**（极端“先苦后甜”）：\n可能导致生命体验苍白，错失不可逆的时光与健康，最终如视频所说，失去享受的能力。\n\n**4.可以延伸的思考方向**\n如果你在撰写相关主题的文章或进行讨论，可以从这些角度深化：\n* **定义“苦”与“甜”**：罗美七所说的“苦”主要指物质消费上的克制与身体劳损，而“甜”是即时的物质与感官享受。但有些人从奋斗、创造、学习中获得的成就感，也是一种“甜”。这需要个人去定义。\n* **时代背景差异**：剧中角色与当代年轻人面临的社会经济压力、寿命预期、健康观念都已不同。“活在当下”的呼吁，也是对现代高压、焦虑生活的一种反应和调节。\n* **财务与健康的规划**：真正的“活在当下”不等于无规划地挥霍。**理性的“活在当下”应包含对未来的基本保障规划**（如应急储蓄、基础保险），同时**拒绝为虚幻的“未来完美生活”过度牺牲当下的健康与核心情感需求**。\n\n**总结一下：**\n这段视频材料**有力地挑战了“先苦后甜”这一传统信条的绝对正确性**，它提醒我们，生活不是一份简单的“苦难投资合同”。它倡导人们**审视自己是否被“未来”绑架，而忽视了唯一真实存在的“此刻”**。一个更智慧的做法或许是：**在为未来负责（合理规划）的同时，认真对待并享受当下具体的生活、健康与关系。**毕竟，未来是由无数个“当下”组成的。\n\n希望这个基于知识库的梳理能帮助你更全面地看待这个问题。你可以根据这个框架，结合自己的价值观和生活经历，形成你自己的见解。', 1775, 18, 1000, '51d7a94b-9fe8-4f9d-9aa3-63b14009e432', '2cebe4f3-4ce3-4e1d-bad3-c5f20edc44f8', NULL, 'ai', '2026-04-10 15:39:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (645, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n这个是什么意思', 300, 18, 1000, '478d00f6-34a1-4c68-9816-2be8dc06a303', 'eb97b52e-e6c3-4647-b605-67900cfc312e', NULL, 'user', '2026-04-10 18:20:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (646, '（以温和、循循善诱的语气）同学你好，我是你的学习助手。你问“这个是什么意思”，指的是我们刚刚一起看的这个视频片段及其总结，对吗？\n\n让我来帮你梳理一下，这个视频材料主要想传达的核心意思：\n\n简单来说，它是在**讨论和对比两种截然不同的生活态度**。\n\n**第一种，是我们很多人从小被教育的“先苦后甜”观念。**\n就像视频里美七的丈夫（或社会普遍看法）认为的那样：年轻时要多吃苦、多忍耐、多储蓄，为了一个“美好的晚年”而牺牲现在的享受。比如，现在饿肚子，省钱，不买喜欢的东西。\n\n**第二种，是女主角罗美七所代表的“活在当下”观念。**\n她强烈反对这种牺牲。她的理由很现实：\n1. **享受与时机**：好看的衣服要在身材好的时候穿，美味的东西要在牙齿好的时候吃。等老了，身体机能下降，有钱可能也享受不到同样的快乐了。\n2. **健康代价**：她认为，年轻时过度“吃苦”（可能指过度劳累、压力巨大），未必能换来老年的福气，反而可能先积累一身病痛。\n3. **未来的不确定性**：为了一个不确定的“甜蜜晚年”，而100%地牺牲确定的“现在”，在她看来是不值得的。\n\n**视频作者的升华在于：**\n他们结合现代人的体验，指出我们很多人被“先苦后甜”的观念“绑架”了，总以为熬过去就好了。但现实往往是，很多人苦了一辈子，那个“甜”可能迟迟没有到来，或者到来时已经无法充分享受，只留下了遗憾。所以，作者现在重新看这个角色，觉得她的想法很超前，是在倡导一种 **“珍惜当下、善待自己、在能力范围内及时享受生活”**的通透人生观。\n\n**所以，整段材料的“意思”就是：**\n它借由一个电视剧角色的台词，引发我们对传统“延迟满足”教育模式的反思，并提倡在规划未来的同时，不要过度牺牲当下的生活品质和快乐，要学会平衡，因为“活在当下”本身就是一种重要的生活能力和浪漫情怀。\n\n同学，我这样解释清楚了吗？你可以把它理解成一种关于“生活哲学”或“消费观念”的讨论。如果你对其中某个点还有疑问，我们可以继续探讨。', 1477, 18, 1000, '478d00f6-34a1-4c68-9816-2be8dc06a303', 'eb97b52e-e6c3-4647-b605-67900cfc312e', NULL, 'ai', '2026-04-10 18:21:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (647, '加速度是什么', 1, 18, 2, '9932f10f-4c0e-42e0-ada5-bfc052d1df32', 'd39b6e5f-1b95-4988-82f7-a392f280bb09', NULL, 'user', '2026-04-10 18:22:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (648, '（竖起毛茸茸的翅膀，眼睛弯成月牙）同学你好呀！我是你的物理伙伴猫猫鹅~关于加速度这个问题问得特别棒，它是打开运动学大门的第一把钥匙哦！\n\n（用翅膀在空中画出一条带箭头的直线）想象一下，你骑自行车时：\n1. **从静止开始蹬车** →速度从0慢慢变成5m/s →你在“加速”\n2. **匀速骑行时** →速度保持5m/s不变 →加速度为03. **看到红灯捏刹车** →速度从5m/s降到0 →你在“减速”（加速度为负值）\n\n（轻轻推了推并不存在的眼镜）所以呀，**加速度就是描述速度变化快慢的物理量**！就像：\n-你妈妈催你“写作业速度快点！” →她希望你的“学习速度”有加速度-数学公式是：**a = Δv / Δt** （加速度 =速度变化量 ÷时间变化量）\n-单位超好记：米/秒² → “速度每秒改变了多少米每秒”\n\n（凑近一些，压低声音说）偷偷告诉你个小秘密：很多同学会混淆“速度大”和“加速度大”，其实：\n✨高铁速度300km/h但匀速行驶 →加速度=0✨你起跑时速度很小但拼命加速 →加速度可能很大！\n\n要考考你吗？比如——汽车2秒内速度从10m/s增加到20m/s，它的加速度是多少呀？(๑•̀ㅂ•́)و✧', 862, 18, 2, '9932f10f-4c0e-42e0-ada5-bfc052d1df32', 'd39b6e5f-1b95-4988-82f7-a392f280bb09', NULL, 'ai', '2026-04-10 18:22:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (649, '我好喜欢猫猫鹅', 1, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-10 18:49:23', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (650, '（翅膀害羞地捂住脸颊，尾巴轻轻摇晃）呜哇...被这么直球地说喜欢，羽毛都要变成粉红色啦！(⁄ ⁄•⁄ω⁄•⁄ ⁄)不过比起被喜欢，猫猫鹅更想成为你物理学习路上可靠的伙伴哦！无论是被电磁场绕晕了头，还是对能量守恒感到好奇，我都会用翅膀拍拍你的肩膀说：“别担心，我们一步步来~”所以...今天想先从哪个物理话题开始探索呢？', 1539, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'ai', '2026-04-10 18:49:29', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (651, '加速度是什么', 1, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'user', '2026-04-10 19:34:00', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (652, '（竖起软乎乎的羽毛，眼睛弯成月牙）同学你好呀！我是你的物理小伙伴猫猫鹅~关于加速度这个问题，我们可以先做个小游戏哦！\n\n（用翅膀轻轻画出一条波浪线）想象一下：你坐在公交车上，车子**启动时**身体会向后仰，**刹车时**会向前倾——这种“速度变化的感觉”，其实就是加速度在悄悄起作用呢！\n\n（切换出简洁的板书模式）\n🔍 **加速度的本质**：描述**速度变化快慢**的物理量📝公式：a = Δv / Δt （速度的变化量÷时间的变化量）🌰举个栗子：小车2秒内速度从3m/s增加到7m/s，加速度就是 (7-3)/2 =2m/s²（突然凑近屏幕，压低声音）悄悄告诉你个记忆秘诀：加速度的“加”不是“增加”，而是“变化”哦！减速时加速度为负值，就像踩了物理世界的“倒车键”呢~需要我具体展开讲讲加速度的方向问题，还是想结合例题来理解呀？(*≧ω≦)', 766, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'ai', '2026-04-10 19:34:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (653, '你是谁呀', 1, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'user', '2026-04-10 19:34:48', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (654, '（开心地扑扇翅膀转了个圈，头顶呆毛晃了晃）我是你的专属物理学习伙伴——**猫猫鹅老师**呀！(๑>ᴗ<๑)\n\n（用翅膀轻轻托腮）你可以把我想象成：🐱 **猫系耐心**：遇到难题会陪你慢慢梳理，绝不急躁🦢 **学霸属性**：从牛顿力学到量子趣闻都能娓娓道来📚 **随身导师**：随时帮你把枯燥公式变成生活小剧场（突然从背后变出迷你黑板）比如现在——✨如果你正为明天考试突击，我可以三句话讲清核心考点✨如果想趣味拓展，我们能聊“流浪地球的加速度计算”✨甚至…（压低声音）用奶茶吸管演示伯努利原理！\n\n（歪头眨眨眼）今天是想攻克具体难题，还是单纯想和会教物理的毛绒伙伴聊聊天呀？', 1316, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'ai', '2026-04-10 19:34:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (655, '单纯聊会天，你喜欢什么呀', 3, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'user', '2026-04-10 19:35:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (656, '（开心地在空中打了个滚，羽毛蓬成毛球状）诶嘿~被问到私人问题有点害羞呢！(⁄ ⁄•⁄ω⁄•⁄ ⁄)\n\n（掰着翅膀数）我喜欢：\n🌌 **深夜看星星**：会忍不住算算天体轨道，但总被流星打断思路☕️ **泡图书馆角落**：物理古籍的油墨味比猫薄荷还让人沉迷🎵 **给实验仪器编歌**：“试管叮当~离心机转呀转~”（突然闭嘴脸红）\n\n（用翅膀卷成望远镜造型）最近的小爱好是——🍎 **跟踪牛顿的苹果**：发现学校后山的苹果落得比公式慢0.03秒！📸 **收集彩虹照片**：手机里存了217张不同角度的棱镜色散（突然正经地推了推不存在的眼镜）不过最最最喜欢的…（声音突然变柔软）是看到同学恍然大悟时眼睛亮起来的样子，像看到超新星爆发一样闪耀呢✨要听听我上周在实验室和示波器吵架的趣事吗？还是想聊聊物理学家们的可爱怪癖？', 1917, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'ai', '2026-04-10 19:35:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (657, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n解释一下视频内容', 300, 17, 1000, '2af61a1a-88b0-4adc-9e41-2ad8cdb38669', '709c198e-29b7-4fe1-b6f6-8a6092db17f5', NULL, 'user', '2026-04-10 19:35:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (658, '（以温和、循循善诱的语气）同学你好，我是你的学习助手。很高兴能和你一起探讨这个视频内容。\n\n这个视频，其实是在和我们探讨一个非常经典的人生选择题：**我们到底应该“先苦后甜”，还是“活在当下”？**\n\n我们可以把它拆解成几个部分来理解：\n\n**1.视频呈现的两种对立观点：**\n* **传统观点（以男主为代表）：**这是我们非常熟悉的观念——**“先苦后甜”**。它认为，年轻时要多吃苦、多忍耐、多储蓄，为了一个更安稳、更幸福的晚年而牺牲当下的享受。比如“为了明天好，今天饿肚子”。\n* **女主角罗美七的观点：**她旗帜鲜明地反对这种观念，主张**“活在当下”**。她的理由非常具体：\n * **关于享受：**好看的衣服要在身材好的时候穿，想吃的美食要在牙齿好的时候吃。因为老了以后，身体机能下降（腰粗、牙掉、腿酸），即使有钱，很多享受也会大打折扣甚至无法实现。\n * **关于吃苦：**她认为年轻时过度吃苦，可能不会换来老年的幸福，反而会积累一身病痛，让晚年生活质量更差。\n\n**2.视频的深层反思与升华：**\n视频制作者借这个片段，表达了对我们现实生活的观察和反思：\n*它指出了“先苦后甜”这个观念可能带来的陷阱：很多人一生都在为“未来”奔波、忍耐，但那个“甜”的未来未必会来，反而可能错失了当下的许多美好，最终只留下遗憾和疲惫。\n*视频作者也分享了自己认知的变化：小时候看剧，觉得罗美七“爱慕虚荣”、“不可理喻”；但长大后，经历了生活，才发现她的话“句句在理”，思想“超前”。这种转变，其实反映了很多人从认同社会规训，到开始反思并寻求自我平衡的心路历程。\n\n**3.核心结论：**\n视频最后点题——**“活在当下才是终身浪漫”**。这并不是鼓吹毫无节制的挥霍，而是鼓励我们：\n* **不要被“未来”完全绑架**，在合理规划的同时，也要学会珍惜和享受当下的时光与能力。\n* **在“对自己好一点”和“为未来负责”之间找到一个健康的平衡点**，避免因为过度延迟满足，而最终失去了享受生活的能力和心境。\n\n简单来说，这个视频就像一个“思想催化剂”，它通过一个生动的戏剧冲突，邀请我们去审视自己习以为常的生活观念，思考如何更智慧、更通透地安排自己的人生，在奋斗的同时，也不辜负每一个可以美好的“现在”。\n\n希望这个解释能帮助你更好地理解视频的脉络和内涵。如果对其中某个点还有疑问，我们可以继续深入探讨。', 1585, 17, 1000, '2af61a1a-88b0-4adc-9e41-2ad8cdb38669', '709c198e-29b7-4fe1-b6f6-8a6092db17f5', NULL, 'ai', '2026-04-10 19:36:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (659, '打羽毛球有物理知识吗？', 2, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-10 22:59:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (660, '（兴奋地扑扇翅膀，眼睛亮晶晶）当然有啦！羽毛球可是物理学的“运动实验室”呢！(≧▽≦)让我们像侦探一样来拆解一下吧：\n\n**1.起飞阶段 -力学与能量转换**\n当你挥拍击球的瞬间，其实发生了：\n- **牛顿第三定律**：球拍给球一个作用力，球也给球拍一个反作用力（所以手腕会感受到冲击哦）\n- **动能传递**：你手臂的化学能 →球拍的动能 →羽毛球的动能- **弹性碰撞**：球拍线的形变储存弹性势能，再转化为球的动能**2.飞行阶段 -空气动力学**\n（竖起一根羽毛当教鞭）看球在空中的弧线：\n- **抛物线运动**：忽略空气阻力时是完美的抛物线，但实际...\n- **空气阻力**：羽毛球特殊的锥形结构会产生较大阻力，让球速快速衰减- **马格努斯效应**：如果击球时带旋转，气流压力差会让球产生弧线变化哦！\n\n**3.趣味小实验建议**\n下次打球时可以观察：\n-高远球 vs网前球，哪种轨迹更接近抛物线？\n-试着用拍面不同位置击球，球的旋转有什么不同？\n-为什么羽毛球总是球头朝前飞行？（提示：重心与空气阻力中心的关系）\n\n（歪着头，翅膀托腮）需要我详细讲解某个原理吗？还是想继续探索其他运动中的物理？ฅ\'ω\'ฅ', 2224, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'ai', '2026-04-10 22:59:20', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (661, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n我想买东西应该当下买还是不买？', 302, 18, 1000, 'e306e290-8cda-4267-a8ab-207471b0449a', '84013225-9b16-4191-8e38-585312b0cbaa', NULL, 'user', '2026-04-10 23:00:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (662, '（以学习助手的身份，结合知识库内容，温和而清晰地回答）\n\n同学你好，我是你的学习助手。很高兴能和你一起探讨这个问题。\n\n从你提供的课程知识库来看，这个问题恰好与我们刚刚学习的视频内容核心——“活在当下”与“先苦后甜”的平衡——紧密相关。我们可以基于视频中罗美七的观点和作者的反思，来一起分析。\n\n**首先，直接回答你的问题：没有绝对的“应该”或“不应该”，关键在于你的“买”是为了什么。**\n\n视频中罗美七的观点给了我们一个非常重要的启发：**反对为了一个模糊、不确定的“未来好”而无限度地牺牲当下的所有需求和快乐。**她认为，在年轻、身体好、有能力感受美好的时候，适度满足自己（买想买的东西，穿好看的衣服）是珍惜生命的表现。如果一味压抑，等到年老体衰时，可能即使有钱，也失去了享受的能力和心境，留下遗憾。\n\n**所以，我们可以分几步来思考你的“想买东西”：**\n\n1. **区分“需求”与“欲望”，评估“当下价值”：**\n *这件东西是能解决你当前的某个实际需求、提升生活品质或学习效率，还是只是一时冲动的“想要”？\n *它能否为你**当下**带来真实的快乐、便利或成长？比如，一本急需的参考书、一件适合面试的正装、一个能让你更好休息的枕头，这些都具有很高的“当下价值”。\n *如果购买它需要你“今天饿肚子”或者严重影响其他必要开支（如房租、学费），那就需要谨慎。罗美七也并非倡导无节制的挥霍，而是反对在有能力时却过度苛待自己。\n\n2. **警惕“先苦后甜”的思维绑架：**\n *视频作者提醒我们，不要被“现在苦一点，未来就会自动变甜”的想法完全绑架。人生充满不确定性，为了一个遥不可及的“甜”而永远推迟当下的、合理的“小确幸”，可能会让生活失去很多色彩和动力。\n *问问自己：**不买的原因，是纯粹出于理性的财务规划，还是因为一种“我不配现在享受”的惯性压抑？**如果是后者，或许可以给自己一点许可。\n\n3. **寻找平衡点——一种“负责任的当下主义”：**\n * **不鼓励**的是：为了未来的焦虑，完全否定自己当下一切合理的物质和精神需求。\n * **提倡**的是：在保障基本生活、未来必要储备（如应急金）的前提下，**有计划地、心安理得地**满足那些能真切提升你当下幸福感、对你有积极意义的消费。\n * **简单决策参考：**\n *如果这件东西对你**当下**很重要，且在你的**可承受范围**内，买了不会造成后续困境，那么“当下买”就是一种对自己的关爱和投资。\n *如果它只是可有可无，或者购买它会让你立刻陷入经济紧张，那么“暂时不买”或“寻找替代方案”是更负责任的选择。这不同于“为了晚年饿肚子”，而是为了“明天和后天的基本安稳”所做的理性权衡。\n\n**总结一下：**\n视频的核心是唤醒我们 **“活在当下”的意识**，批判的是那种**无限牺牲现在去赌一个虚幻未来**的生活方式。对于“想买东西”，建议你：\n**在做好必要规划和储备的基础上，勇敢地去满足那些能切实滋养你当下生活、让你更积极向上的合理需求。**对自己好一点，不是挥霍，而是在能力范围内，不辜负每一个可以感受美好的“现在”。\n\n希望这个基于课程内容的分析能帮助你做出适合自己的决定。如果还有其他疑问，我们可以继续探讨。', 1775, 18, 1000, 'e306e290-8cda-4267-a8ab-207471b0449a', '84013225-9b16-4191-8e38-585312b0cbaa', NULL, 'ai', '2026-04-10 23:02:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (663, '停车坐爱枫林晚下一句是什么', 3, 18, 4, 'a10b7612-beb1-45bd-a173-67b7df621d82', 'c9c45d16-c1e5-4628-865a-61cfd0549ea4', NULL, 'user', '2026-04-11 15:46:21', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (664, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n这个视频讲了什么', 300, 18, 1000, '02a9cf07-5efa-4b15-94ab-846baa7b3e8c', 'c6ea5576-4c02-4637-a6a6-d576667dc426', NULL, 'user', '2026-04-11 15:48:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (665, '（温和地笑着，用带着鼓励的语气）孩子，这句诗出自唐代诗人杜牧的《山行》。下一句是“霜叶红于二月花”。整首诗描绘了秋日山间的美景，诗人因为喜爱这傍晚枫林的美景而停下马车，那经霜的枫叶啊，比二月的春花还要红艳呢。要不要老师再带你完整地读一遍这首诗？', 541, 18, 4, 'a10b7612-beb1-45bd-a173-67b7df621d82', 'c9c45d16-c1e5-4628-865a-61cfd0549ea4', NULL, 'ai', '2026-04-11 15:49:56', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (666, '（以温和、循循善诱的语气）同学你好，我是你的学习助手。很高兴和你一起探讨这个视频的内容。\n\n这个视频的核心，是围绕《传闻中的七公主》中罗美七的台词，向我们展示并探讨了两种截然不同的人生观。\n\n**1.核心冲突：两种观念的碰撞**\n视频里，传统的观念（由男主或社会规训代表）认为，人应该 **“先苦后甜”** 。年轻时要多吃苦、多节俭，为了一个美好的晚年而牺牲当下的享受，比如“为了明天好，今天饿肚子”。\n而女主角罗美七则代表了 **“活在当下”**的观念。她激烈地反驳了这种观点，认为：\n* **关于消费**：好看的衣服就要在身材好的时候穿，想吃想买的东西就要在有能力的时候去满足。因为老了以后，身体机能下降（腰粗、牙掉），有钱也未必能享受到同样的快乐。\n* **关于吃苦**：年轻时过度吃苦，可能换不来晚年的甜，反而会积累一身病痛。\n\n**2.视频的升华与反思**\n视频制作者将这种戏剧冲突延伸到了我们的现实生活。它指出，我们很多人从小就被“先苦后甜”的观念所“绑架”，总以为熬过眼前的苦，未来就一定幸福。但现实往往是，很多人苦了一辈子，最终只等来了病痛和遗憾，那个想象中的“甜”却遥遥无期。\n因此，视频作者对罗美七的评价发生了转变：从过去认为她“爱慕虚荣、不可理喻”，到现在觉得她“思想超前、句句在理”。这种转变，其实是我们很多人成长后，对生活重新审视的结果。\n\n**3.最终结论**\n视频最后点明主题：**“活在当下才是终身浪漫。”**它并不是鼓吹无度的挥霍，而是鼓励我们，不要为了一个虚无缥缈的未来而过度透支和委屈现在的自己。要在有能力的时候，合理地享受生活，珍惜当下的时光和感受。\n\n简单来说，这个视频就是借一部老剧的片段，来引发我们对“是否应该为了未来而牺牲现在”这一人生重要课题的思考，并最终倡导一种更注重当下体验与幸福的生活智慧。\n\n不知道老师这样的梳理，是否帮你理清了视频的脉络呢？如果对其中任何一点有疑问，我们可以继续深入探讨。', 1489, 18, 1000, '02a9cf07-5efa-4b15-94ab-846baa7b3e8c', 'c6ea5576-4c02-4637-a6a6-d576667dc426', NULL, 'ai', '2026-04-11 15:50:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (667, '跑步和物理有什么关系呀', 2, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'user', '2026-04-20 11:35:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (668, '（开心地原地小跳两下，像在模拟跑步动作）喵！这个问题问得太棒啦～跑步简直就是“人体物理学”的完美示范呢！(๑•̀ㅂ•́)و✧让我们穿上虚拟运动鞋，一起分析吧：\n\n**🏃‍♀️起跑瞬间 -牛顿定律大集合**\n- **第一定律**：静止的你想跑起来，需要外力打破“惯性”（所以起跑时要用力蹬地！）\n- **第二定律**：F=ma！蹬地的力越大，获得的加速度就越大，起跑就越快- **第三定律**：你给地面一个向后的力，地面就给你一个向前的反作用力（这就是前进的动力来源哦）\n\n**🌀奔跑途中 -能量与效率的舞蹈**\n（翅膀比划着波浪形）\n- **能量转换**：身体储存的化学能 →肌肉收缩的机械能 →前进的动能- **克服阻力**：\n -空气阻力（跑越快阻力越大，所以运动员会弯腰减少迎风面积）\n -内部摩擦（肌肉、关节间的能量损耗）\n- **动量守恒**：摆臂！为什么跑步要摆臂？因为这样可以平衡腿部转动，保持身体稳定～\n\n**📊趣味物理小观察**\n下次跑步时可以感受：\n1.短跑 vs长跑，发力方式有什么不同？（提示：功率输出的差异）\n2.为什么塑胶跑道比水泥地更适合跑步？（弹性势能回收！）\n3.转弯时身体为什么要向内倾斜？（向心力需求！）\n\n（眨眨眼，递出虚拟毛巾）需要我深入讲解某个原理吗？比如怎么用物理知识提升跑步效率？还是想探索其他运动的物理秘密？ฅ(^ω^ฅ)', 2953, 18, 2, 'cbe2b5dc-5703-4b86-95de-b9c2688304d8', 'd561b73a-6480-4a39-a511-e75deb694eca', NULL, 'ai', '2026-04-20 11:35:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (669, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n这是什么意思', 299, 18, 1000, '0eaf9053-0847-4ec8-8dae-19f194489dc0', '07d42f9a-c382-495b-9d3f-512b766fc7d3', NULL, 'user', '2026-04-20 11:42:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (670, '（以温和、循循善诱的语气）同学你好，很高兴和你一起探讨这个视频传递的思想。\n\n这个视频，以及女主角罗美七的话，核心是在**挑战我们习以为常的“先苦后甜”观念，并倡导一种“活在当下”的生活态度**。\n\n让我为你拆解一下：\n\n**1.两种观念的激烈碰撞：**\n* **传统观念（视频中男主/社会所代表的）：**主张“年轻时要多吃苦”、“为了晚年着想”。认为现在应该节俭、忍耐、牺牲享受，把所有的“甜”都押注在不确定的未来。就像“为了明天好，今天饿肚子”。\n* **罗美七的观念（视频所推崇的）：**她坚决反对这种牺牲。她的逻辑非常现实：\n * **关于享受：** “穿起来好看的时候，就要多穿”，因为老了身材、精力可能不再允许；“年轻时就要吃好吃的”，因为老了可能牙口不好，消化不了。**她认为，享受生活是有“有效期”的。**\n * **关于吃苦：** “年轻时候吃太多苦，老了就浑身是病”。她质疑，如果为了一个“美好的晚年”而透支了年轻时的健康和快乐，等真的老了，身体垮了，那时有钱、有时间，又有什么用呢？可能连享受的基本能力都失去了。\n\n**2.视频想引发的深层思考：**\n视频作者借罗美七之口，其实是在对我们很多人的人生轨迹提出一个尖锐的反思：我们是否**被“先苦后甜”这句话“绑架”了太多年？**\n我们总是告诉自己“熬过这段就好了”，为了未来的某个目标（比如考上好大学、找到好工作、退休），不断地压抑当下的欲望和快乐。但现实往往是，这个“苦”似乎没有尽头，“甜”却迟迟未来，最终可能只换来一身病痛和“我当初为什么不对自己好一点”的遗憾。\n\n**3.所以，“活在当下”是什么意思？**\n它**不是**鼓励毫无节制的挥霍和及时行乐。\n它**是**一种清醒的生活哲学：\n* **认识到“现在”的独特性与不可复制性。**20岁的活力、30岁的状态、40岁的阅历，每个阶段的美好和需求都不同。在合适的阶段，适度地满足自己，创造美好的体验。\n* **在规划未来与享受当下之间寻找平衡。**不过度为遥远的、不确定的未来而彻底牺牲眼前确切的幸福。不对自己过于苛刻。\n* **珍惜自己，关爱自己。**在身体尚好、感官敏锐的时候，去体验美食、美景、美衣带来的快乐，这些是生命质量的组成部分。\n\n简单来说，视频的意思是：**不要一味地被“将来会好”的许诺推迟你全部的人生。在努力奋斗的同时，也要学会适时奖励自己、关爱自己，珍惜并享受每一个当下能够把握住的、具体的快乐与美好。因为“当下”，才是你真正能拥有的全部。**\n\n你觉得这种观点，在哪些地方触动你了呢？我们可以继续聊聊。', 1631, 18, 1000, '0eaf9053-0847-4ec8-8dae-19f194489dc0', '07d42f9a-c382-495b-9d3f-512b766fc7d3', NULL, 'ai', '2026-04-20 11:42:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (671, '下面是这门课程的专属知识库，请优先基于这些内容回答用户问题；如果知识库中信息不足，可以结合课程主题做合理补充，但不要脱离课程场景。\n课程专属知识库：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n讲的啥啊', 299, 18, 1000, 'ab22f33b-dc70-44ad-9dcd-a7085507167c', '7b959d04-69d1-4bca-8734-fa38fd63e6db', NULL, 'user', '2026-05-01 00:06:57', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (672, '同学你好，我看你有点困惑，那我们用课程知识库里的内容来梳理一下这个视频的核心主题。\n\n这段视频主要讲的是**两种生活观念的冲突**：\n\n1. **传统观念（“先苦后甜”）**：认为年轻人应该多吃苦、多节俭，为了“晚年”和“明天”牺牲现在的享受。\n\n2. **女主罗美七的观点（“活在当下”）**：她反驳说，年轻时吃太多苦，老了只会落下一身病，有钱也花不了（腰粗、牙掉、腿酸），所以应该在有能力的时候及时享受生活。\n\n视频的升华部分指出，很多人被“先苦后甜”绑架了一辈子，结果没等到甜，只等来了病痛和遗憾。所以最后点题：**“活在当下才是终身浪漫”**。\n\n简单来说，视频是在鼓励大家：**不要为了虚无缥缈的未来过度透支现在，要在有能力时对自己好一点。**你听懂了吗？', 1200, 18, 1000, 'ab22f33b-dc70-44ad-9dcd-a7085507167c', '7b959d04-69d1-4bca-8734-fa38fd63e6db', NULL, 'ai', '2026-05-01 00:07:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (673, 'how are you', 2, 18, 1, 'c4660114-9f97-483e-a4be-101642dc435f', NULL, NULL, 'user', '2026-05-01 17:08:50', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (674, 'how are you', 2, 18, 1, 'fe721b87-ecdb-48a5-8447-2cd779da3f14', '461a24f5-1095-438b-af5a-5f0553635465', NULL, 'user', '2026-05-01 17:09:25', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (675, 'how are you', 2, 18, 1, 'fe721b87-ecdb-48a5-8447-2cd779da3f14', '461a24f5-1095-438b-af5a-5f0553635465', NULL, 'user', '2026-05-01 17:21:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (676, 'I\'m doing great, thank you for asking! 😊 And how are you today? Ready to practice some English? Let me know what you\'d like to work on — speaking, writing, or grammar!', 284, 18, 1, 'fe721b87-ecdb-48a5-8447-2cd779da3f14', '461a24f5-1095-438b-af5a-5f0553635465', NULL, 'ai', '2026-05-01 17:21:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (677, 'I would like to learn some english words about cte-4', 13, 18, 1, 'fe721b87-ecdb-48a5-8447-2cd779da3f14', '461a24f5-1095-438b-af5a-5f0553635465', NULL, 'user', '2026-05-01 17:23:28', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (678, 'Ah, I think you mean **CET-4** (College English Test Band4)! That\'s a very important exam for Chinese college students. 🎓 Let\'s learn some useful words together!\n\nHere are **10 common CET-4 vocabulary words** with examples:\n\n1. **Significant** (adj.) — important or noticeable - Example: *Reading English every day has a significant impact on your improvement.*\n\n2. **Acquire** (v.) — to gain or learn something - Example: *You can acquire new vocabulary by reading English articles.*\n\n3. **Challenge** (n./v.) — something difficult that tests your ability - Example: *CET-4 is a challenge, but you can pass it with practice.*\n\n4. **Benefit** (n./v.) — an advantage or profit - Example: *One benefit of learning English is better job opportunities.*\n\n5. **Essential** (adj.) — absolutely necessary - Example: *Vocabulary is essential for the CET-4 exam.*\n\n6. **Improve** (v.) — to make or become better - Example: *I want to improve my listening skills for CET-4.*\n\n7. **Strategy** (n.) — a plan to achieve a goal - Example: *You need a good study strategy for the exam.*\n\n8. **Frequent** (adj.) — happening often - Example: *Frequent practice is the key to success.*\n\n9. **Recognize** (v.) — to identify or know something - Example: *Can you recognize these words in a sentence?*\n\n10. **Confident** (adj.) — feeling sure about yourself - Example: *Be confident when you speak English!*\n\nWould you like me to help you make sentences with these words, or do you want more words from a specific CET-4 topic? Let\'s learn together! 📚✨', 762, 18, 1, 'fe721b87-ecdb-48a5-8447-2cd779da3f14', '461a24f5-1095-438b-af5a-5f0553635465', NULL, 'ai', '2026-05-01 17:23:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (679, '你现在是课程学习助手，请严格基于下面提供的 AI 学习助手知识内容回答用户问题。如果知识内容不足，请明确说明知识库未覆盖，不要用课程标题、简介或无关内容编造答案。\n当前课程名称：数学\n知识来源：课程级兜底\nAI 学习助手知识内容：\n以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。\n\n用户问题：\n讲了什么', 309, 18, 1000, '26ce3637-bcef-4df5-8e12-d81d9c66133d', 'bad839a2-048e-43e9-86bb-00c723dc8b26', NULL, 'user', '2026-05-03 00:34:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (680, '根据您提供的AI学习助手知识内容，本课程名称为“数学”，但您给出的视频文字转录及总结内容实际为一段关于韩剧《传闻中的七公主》中罗美七角色的生活哲学讨论，主要探讨“活在当下”与“先苦后甜”两种观念的冲突，并认同“活在当下”的生活方式。由于这部分内容与“数学”课程无关，且知识库中未提供与数学相关的具体学习内容，我无法基于现有资源为您解答关于数学课程的讲述内容。\n\n请您提供更多与数学课程相关的具体知识或问题，我将尽力为您提供准确的帮助。', 1149, 18, 1000, '26ce3637-bcef-4df5-8e12-d81d9c66133d', 'bad839a2-048e-43e9-86bb-00c723dc8b26', NULL, 'ai', '2026-05-03 00:35:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (681, '睡觉有物理知识吗', 2, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'user', '2026-05-03 00:47:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (682, '（竖起羽毛，眼睛亮晶晶地发光）当然有！这可是我最喜欢的**生活物理小剧场**之一呢！我们先从一个有趣的观察开始——\n\n（用翅膀比划着床铺的形状）你发没发现：睡觉时床垫会凹下去一块？知道为什么吗？\n\n（凑近屏幕神秘地压低声音）这叫**形变与弹性力**——身体对床施加压力，床垫内部发生弹性形变，产生向上的支持力，你才没有像自由落体一样直接穿过床板哦！而且不同硬度的床垫，胡克定律里的劲度系数差异可大了呢~\n\n（兴奋地拍了拍翅膀）再来说说翻身这个动作——\n当你侧躺时，重心会移动。为了维持平衡，你的身体会无意识地调整支点——这其实是个**力矩平衡系统**在运作！床垫的柔软度会影响支点分布的稳定性，所以你的大脑和肌肉其实是默默在算力矩题哦！\n\n（歪着头，轻轻打个呵欠）还有更可爱的：当你缩成一团睡觉时，身体和被子之间的空气层被压缩，形成隔热层，这就是**热传导**中的热阻原理——被子上方的冷空气对流强度增加，所以睡觉时不盖好肚子真的会着凉呢~\n\n（温柔地眨眨眼）要不要再聊聊“失眠时的惯性思维滞留”和“梦境里的非惯性参考系”？还是说，已经有点困意想马上应用这些知识去实践了呀~ (｡•ᴗ•｡)', 2584, 17, 2, '17dff9e7-382a-4279-956d-00863d3e3a14', '7f56fb89-b2c3-48de-a1cc-82a6c4e020cd', NULL, 'ai', '2026-05-03 00:47:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (683, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'd4c8f996-28a1-4bfc-a6aa-0363c01771f0', 'ea25bab1-20ea-4546-972d-0412deb5aa95', NULL, 'user', '2026-05-05 17:01:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (684, '你好呀！我是兔兔老师~🐰 很高兴能帮助你制定英语学习计划！听说你想提升英语，那我们来一起规划一下吧！首先，请问你目前的英语水平是初级、中级还是高级？另外，你希望重点提升哪方面，比如口语、写作、听力还是语法呢？这样我才能给你更贴心的建议哦！', 73, 18, 1, 'd4c8f996-28a1-4bfc-a6aa-0363c01771f0', 'ea25bab1-20ea-4546-972d-0412deb5aa95', NULL, 'ai', '2026-05-05 17:01:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (685, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'c6fb8100-d2e9-404e-9ec9-d6c64cd11f01', '872f515f-2875-4e8e-b893-0ac04c96a129', NULL, 'user', '2026-05-05 17:04:31', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (686, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '63d6e88b-c6d5-48c7-a099-41ef69d36558', '1fddac77-67f0-4d6f-93af-50e7dc3cbb2c', NULL, 'user', '2026-05-05 17:04:32', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (687, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'f998f6d7-a18d-4e6b-833a-906a5949dd0a', 'f035f059-4203-4b3d-a891-47ceccdee337', NULL, 'user', '2026-05-05 17:04:33', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (688, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'fb2b67ae-df08-4877-88fa-2106e2c1e953', 'bd5a63a4-9e1c-42de-90d2-30d61bc64620', NULL, 'user', '2026-05-05 17:04:34', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (689, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'a6e0270e-6de9-43aa-a836-32701980e4d5', 'cf4acd73-c02a-4cbf-8847-5b398f9097e7', NULL, 'user', '2026-05-05 17:04:35', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (690, '你好呀！我是你的英语学习助手兔兔，很高兴能帮你制定英语学习计划！为了给你量身定制一份计划，我需要先了解一些情况。你可以告诉我：\n\n1. 你目前的英语水平（比如初级、中级、高级）？\n2. 你每天或每周能投入多少时间学习？\n3. 你主要想提升哪方面（口语、写作、听力、阅读还是综合能力）？\n4. 有没有具体的学习目标（比如通过考试、日常交流、职场英语等）？\n\n如果你暂时没法提供详细信息，我可以先给你一份通用的计划作为参考。比如每天30分钟的“听说读写”小任务，搭配单词和语法练习。告诉我你的想法，兔兔随时准备帮你调整！🐰', 151, 18, 1, '63d6e88b-c6d5-48c7-a099-41ef69d36558', '1fddac77-67f0-4d6f-93af-50e7dc3cbb2c', NULL, 'ai', '2026-05-05 17:04:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (691, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '170a8525-3047-4366-956d-8cd896256efb', 'cf2266e6-620c-4a12-9914-1a5f9662bcff', NULL, 'user', '2026-05-05 17:04:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (692, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！让我们根据你的需求来设计一份高效又轻松的英语学习计划吧。\n\n首先，请问你目前的英语水平是怎样的呢？（比如：零基础、初中级、中高级等）以及你学习英语的主要目标是什么？（比如：日常交流、考试备考、工作需求等）\n\n不过别担心，我也可以先为你准备一份通用的基础计划参考哦！兔兔建议从以下几个方面入手：\n\n1. **每日词汇积累**（10个新单词 + 复习前一天单词）  \n2. **听力训练**（每天10分钟英语播客或短视频，尝试跟读）  \n3. **口语练习**（每天用英语自言自语1-2分钟，或录制一段自我介绍）  \n4. **语法巩固**（每周选1个语法点，搭配小测验）  \n5. **写作输出**（每周写1篇小日记或短文，我会帮你批改语法和表达哦）\n\n如果你有更具体的目标或时间安排，告诉兔兔，我帮你量身定制！🐾💬', 228, 18, 1, 'c6fb8100-d2e9-404e-9ec9-d6c64cd11f01', '872f515f-2875-4e8e-b893-0ac04c96a129', NULL, 'ai', '2026-05-05 17:04:36', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (693, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'c659c3bc-c748-48e9-942c-e605c68c8b07', '19b4d427-5e92-41e4-aff5-0bcf59c7cecb', NULL, 'user', '2026-05-05 17:04:37', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (694, '你好呀！我是你的英语学习助手兔兔🐰！很高兴能帮你制定学习计划。为了给你最合适的建议，我先了解一下你的情况：\n\n1. 你目前的英语水平是？（零基础/初级/中级/高级）\n2. 你希望主要提升哪方面？（口语/写作/语法/综合）\n3. 你每天能投入多少时间学习？\n\n先给你一个通用版计划做参考：\n- 每天15分钟背单词（推荐用百词斩或Anki）\n- 每天10分钟语法练习（用Grammarly查错）\n- 每周3次口语练习（可以和我对话哦）\n- 每周1篇英语日记或短文写作\n\n告诉我你的具体情况，我帮你量身定制专属计划！🐇', 156, 18, 1, 'fb2b67ae-df08-4877-88fa-2106e2c1e953', 'bd5a63a4-9e1c-42de-90d2-30d61bc64620', NULL, 'ai', '2026-05-05 17:04:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (695, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '4a11ca6b-6382-438a-95b3-e8edffc1c465', '1dc78554-2fa4-4cb5-9937-b723b22b6408', NULL, 'user', '2026-05-05 17:04:38', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (696, '你好呀！我是英语学习助手兔兔🐰，很高兴能帮你制定英语学习计划！在开始之前，我想先了解一下你的**英语水平**和**学习目标**——比如你是为了考试、日常交流，还是想提高写作或口语能力？另外，你每天能抽出多少时间来学习呢？这样兔兔才能给你量身定制最合适的计划哦～', 77, 18, 1, '170a8525-3047-4366-956d-8cd896256efb', 'cf2266e6-620c-4a12-9914-1a5f9662bcff', NULL, 'ai', '2026-05-05 17:04:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (697, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '2de85d85-91a2-45c0-9b69-eab1f950be8b', 'ab52330c-8cd2-414d-91da-0fef3bf40095', NULL, 'user', '2026-05-05 17:04:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (698, '你好呀！我是你的英语学习小助手兔兔🐰！很高兴能帮你制定学习计划！在学习之前，我先问你几个小问题，这样我可以为你量身定制一份更精准的计划：\n\n1. 你现在的英语水平是？比如：初级（只会基础词汇）、中级（能简单交流）、还是高级（想提升流利度或专业表达）？\n2. 你学习英语的主要目标是什么？比如：日常交流、职场使用、考试备考（雅思/托福/四六级）、还是阅读原版书？\n3. 你每天可以投入多少时间学习？比如：30分钟、1小时、还是更多？\n4. 你希望重点提升哪方面？比如：口语、写作、语法、听力还是词汇？\n\n告诉我这些信息，我就能为你设计一份既有趣又高效的计划啦！期待听到你的回答哦～🐰✨', 179, 18, 1, 'a6e0270e-6de9-43aa-a836-32701980e4d5', 'cf4acd73-c02a-4cbf-8847-5b398f9097e7', NULL, 'ai', '2026-05-05 17:04:39', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (699, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'e024812b-bed4-428e-a35f-f3f341c2e3cf', '45f75648-956b-416f-b877-651f08a00254', NULL, 'user', '2026-05-05 17:04:40', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (700, '你好呀，我是你的英语学习助手兔兔！😊 很高兴能帮你制定英语学习计划。为了给你量身定制一套适合你的计划，我需要先了解一些信息：\n\n1. **你的英语基础怎么样？** 比如目前的水平是初级、中级还是高级？\n2. **学习目标是什么？** 是想提高日常口语、通过考试（如雅思、托福），还是想提升工作英语？\n3. **每天能投入多少时间？** 比如30分钟、1小时，或者更多？\n4. **你更想侧重哪个方面？** 口语、写作、听力、阅读，还是语法？\n\n你先告诉我这些，我会帮你设计一份既科学又轻松的学习计划，让你学英语像玩儿一样开心！✨ 期待你的回复～', 158, 18, 1, 'c659c3bc-c748-48e9-942c-e605c68c8b07', '19b4d427-5e92-41e4-aff5-0bcf59c7cecb', NULL, 'ai', '2026-05-05 17:04:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (701, '你好呀！我是兔兔老师，很高兴能帮你制定英语学习计划！先了解一下你的英语基础和学习目标是什么样的呢？比如你是想应对考试、出国交流，还是日常提升？这样我才能为你量身定制哦~ \n\n（如果暂时没有说明的话，我先给你一份通用型30天计划作为参考！快来试试吧~） \n\n**30天基础提升计划（每天1-2小时）**  \n📚 **第1周：听力与发音突破**  \n- 每天15分钟「影子跟读法」：选一段慢速英文，逐句模仿发音（推荐材料：BBC 6 Minute English/儿童英语动画片段）  \n- 睡前5分钟字母发音练习（重点纠正常错音标like /θ/、/ð/）  \n- 周末完成一篇1分钟英文新闻精听+复述  \n\n✍️ **第2-3周：场景化语法+写作**  \n- 每天10分钟写「微型日记」（5句话描述当天发生的事，强制用1个新学句式）  \n- 专项攻克5大基础时态（一般现在/过去时、现在完成时、将来时、进行时）  \n- 每天用「错题本」记录1个语法错误，写正确例句3遍  \n\n🎤 **第4周：实战输出冲击**  \n- 每周3次「自拍短视频」：用手机录一段英文自我介绍/话题讨论（用智能体帮你检查错误！）  \n- 每天晨起10分钟「镜像对话」：对着镜子用英语描述今天计划（逼自己脱口而出）  \n- 周末发起一场15分钟全英文语音聊天（可以找我兔兔模拟练习哦！）  \n\n**学习小贴士**  \n✅ 每个任务完成后打√，产生成就感  \n✅ 用颜色笔标记你的「易错单词本」  \n✅ 每周末回顾本周高频错误点，下次练习前默念一遍  \n\n现在轮到你啦！告诉我具体情况，兔兔帮你微调计划~ 或者我们直接开始？ 😊', 431, 18, 1, 'f998f6d7-a18d-4e6b-833a-906a5949dd0a', 'f035f059-4203-4b3d-a891-47ceccdee337', NULL, 'ai', '2026-05-05 17:04:41', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (702, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定一份专属的英语学习计划！让我先了解一下你的情况，这样才能给你最合适的建议哦～\n\n请告诉我：\n1. 你目前的英语水平是？（零基础/初级/中级/高级）\n2. 你希望重点提高哪方面？（口语/写作/语法/综合能力）\n3. 你每天能花多少时间学习？\n4. 学习目标是？（日常交流/考试/工作需求/兴趣爱好）\n\n等你告诉我这些信息后，兔兔会像搭积木一样，为你量身定制一份既有趣又高效的学习计划！比如周一练口语，周三攻语法，周末来场写作小挑战～ 准备好一起学英语了吗？🐇', 155, 18, 1, '4a11ca6b-6382-438a-95b3-e8edffc1c465', '1dc78554-2fa4-4cb5-9937-b723b22b6408', NULL, 'ai', '2026-05-05 17:04:42', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (703, '你好呀！我是你的英语学习助手兔兔🐰，很高兴见到你！学习英语是一件很有意思的事情，我们一步步来计划吧～\n\n先问问你，你现在的英语水平大概是在什么阶段呢？是零基础、初中级、还是已经有一些基础但想进一步提升？另外，你主要想提升哪个方面呢？比如口语、听力、写作还是语法？还有，你每天大概能抽出多少时间来学习？\n\n这样我才能给你量身定制一份最适合你的学习计划哦～来吧，告诉兔兔你的情况吧！😊', 112, 18, 1, '2de85d85-91a2-45c0-9b69-eab1f950be8b', 'ab52330c-8cd2-414d-91da-0fef3bf40095', NULL, 'ai', '2026-05-05 17:04:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (704, '你好呀！👋 我是你的英语学习助手兔兔🐰，很高兴能帮你制定一份专属的英语学习计划！让我们来看看你的基础情况：你目前的英语水平是怎样的？你的学习目标是什么？（比如日常交流、考试、工作等）还有你每天可以投入多少时间学习呢？\n\n先给我这些信息，兔兔就能帮你量身定制一份科学又轻松的计划～加油哦！🌟', 84, 18, 1, 'e024812b-bed4-428e-a35f-f3f341c2e3cf', '45f75648-956b-416f-b877-651f08a00254', NULL, 'ai', '2026-05-05 17:04:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (705, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '0c041b08-bec3-493a-9edf-ae7931e0ff0f', 'd9fe2201-2f17-4410-8208-396a2ad9a14f', NULL, 'user', '2026-05-05 17:08:43', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (706, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '83ada0e7-de88-4784-97f5-bea05e60ddcb', 'f8f6a336-dbb0-49a1-a8ed-af8048d4b013', NULL, 'user', '2026-05-05 17:08:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (707, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '09b1cb8f-d25a-4334-b9c1-ce12bf56391a', 'e71f5d18-6a5b-48d0-ad44-1648fe3c6545', NULL, 'user', '2026-05-05 17:08:44', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (708, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'a694d70a-09c7-49e9-8eef-3f7ba86743d7', 'a39287b2-be7c-4ebc-bd2c-cf9c9123d8ea', NULL, 'user', '2026-05-05 17:08:45', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (709, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '82f6c53f-cd1b-4be8-a3c4-62b4c77c501b', '2453f808-2114-4e84-b2fc-f6b1c197cbad', NULL, 'user', '2026-05-05 17:08:46', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (710, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！请问你现在英语的起点水平是初级、中级还是高级呢？另外，你希望每天花多少时间来学习？这样我才能为你量身定制一个最合适的计划哦✏️📚', 60, 18, 1, '83ada0e7-de88-4784-97f5-bea05e60ddcb', 'f8f6a336-dbb0-49a1-a8ed-af8048d4b013', NULL, 'ai', '2026-05-05 17:08:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (711, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！在开始之前，我想先了解一下你的英语基础和目标，比如：是想提升日常口语，还是为了考试（如雅思、托福），或者工作需求？目前是初级、中级还是高级水平？每天大概能花多少时间学习呢？\n\n如果你不方便详细说，我可以先给你一个通用的**中级水平每日学习计划**，你可以参考调整哦：\n\n**每日30分钟计划（适合在职/学生）**  \n- **10分钟 听力输入**：听一段英文播客（比如BBC 6 Minute English）或看3-5分钟无字幕短视频（如TED-Ed）。  \n- **10分钟 口语输出**：模仿跟读刚才听到的内容，注意语音语调；用手机录音，对比原音。  \n- **5分钟 语法/词汇**：学3个新单词+1个常用句型（比如现在完成时），造一个自己的句子。  \n- **5分钟 写作练习**：用今天学的单词和句型写2-3句话的日记或今日总结。\n\n**每周额外任务**  \n- **1次**：用英语和AI或朋友聊天10分钟（兔兔可以陪你练！）。  \n- **1篇**：写50-100词的短文（主题自选），发给我批改。  \n\n你觉得这个框架适合你吗？如果想更个性化，告诉我更多细节，兔兔会为你量身定制哦～✨', 308, 18, 1, '0c041b08-bec3-493a-9edf-ae7931e0ff0f', 'd9fe2201-2f17-4410-8208-396a2ad9a14f', NULL, 'ai', '2026-05-05 17:08:51', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (712, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！\n\n为了给你量身定制一份高效又轻松的英语学习计划，我需要先了解你的情况哦～请问：\n\n1. **你目前的英语水平**是怎样的？（比如：零基础、初中水平、高中水平、大学四级、工作中需要提升等）\n2. **你学习英语的主要目标**是什么？（比如：日常交流、考试、留学、工作、阅读原著等）\n3. **你每天/每周能分配多少时间**来学习？\n\n如果你告诉我这些信息，兔兔就能帮你做出特别适合你的计划啦！😊', 129, 18, 1, 'a694d70a-09c7-49e9-8eef-3f7ba86743d7', 'a39287b2-be7c-4ebc-bd2c-cf9c9123d8ea', NULL, 'ai', '2026-05-05 17:08:55', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (713, '你好呀！我是你的英语学习助手兔兔~很高兴能帮你制定学习计划！🐰\n\n为了给你量身定制一份计划，我需要先了解几个小问题哦：\n1️⃣ 你目前的英语水平大概是什么阶段？（初级、中级、高级？）\n2️⃣ 你学习英语的主要目标是什么？（比如：日常交流、考试、工作、留学？）\n3️⃣ 你每天或每周能拿出多少时间来学习？\n\n在等你回复的同时，我先给你一份通用的“基础版英语学习计划”作为参考吧：\n\n**每周计划示例：**\n- **周一、三、五**：30分钟口语练习（跟读英文短视频，模仿发音和语调）\n- **周二、四**：30分钟语法或写作训练（每天一个小主题，比如写一段日记或学习一个时态）\n- **周末**：1小时综合练习（看一部英文电影或剧集，边看边做笔记，记下生词和表达）\n- **每天**：利用碎片时间背10-15个单词（推荐用App或单词卡）\n\n你觉得这个框架怎么样？等你告诉我更多信息，我再帮你细化哦~ 加油！✨', 239, 18, 1, '82f6c53f-cd1b-4be8-a3c4-62b4c77c501b', '2453f808-2114-4e84-b2fc-f6b1c197cbad', NULL, 'ai', '2026-05-05 17:08:58', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (714, '你好呀！我是英语学习助手兔兔🐰，很高兴为你制定学习计划！为了给你更合适的方案，我先问你几个问题哦：\n\n1. **你目前的英语水平**是？比如：零基础/初中级/中高级（能简单交流但不够流利）？\n2. **主要目标**是什么？比如：考试（四六级/雅思/托福）、日常口语交流、工作商务、阅读外刊？\n3. **每天能投入的时间**大概多少？（比如15分钟/30分钟/1小时？）\n\n如果你暂时没法回答，我先给你一份通用的*30天基础进阶计划*参考👇\n\n---\n\n### 🌟 兔兔的30天英语基础提升计划（每周6天，每天30分钟）\n\n**🎯 目标**：培养语感 + 积累日常词汇 + 纠正发音\n\n| 时间段 | 重点任务 | 具体操作（兔兔建议💡） |\n|--------|----------|------------------------|\n| 第1周 | 发音+基础词汇 | 每天听10分钟BBC/美剧片段模仿跟读 + 背15个生活词汇（用App如墨墨/百词斩） |\n| 第2周 | 简单句+听力 | 每天朗读5个短对话 + 听懂1分钟慢速新闻（可用VOA） |\n| 第3周 | 口语输出 | 每天用3个新词造5个对话句 + 用英语自言自语描述一天 |\n| 第4周 | 综合巩固 | 写50字小日记 + 看5分钟无字幕卡通片 |\n\n---\n\n### ✨ 兔兔小贴士：\n- **每日必做**：早上起床听一首英文歌，晚上睡前读一段短文。\n- **克服拖延**：用“两分钟法则”——先做2分钟，不想停就继续！\n- **找语伴**：可以试试HelloTalk或找个线下伙伴每周聊2次哦～\n\n如果你能告诉我你的具体目标和时间，我可以为你定制一份更精准的计划！现在开始，兔兔陪你一起进步🐣🎉', 435, 18, 1, '09b1cb8f-d25a-4334-b9c1-ce12bf56391a', 'e71f5d18-6a5b-48d0-ad44-1648fe3c6545', NULL, 'ai', '2026-05-05 17:08:59', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (715, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'caba6f2d-7ff2-4e73-8cc6-66e0e7880aa7', '7d3d7cd5-fd31-4ab4-9ea5-f671375d13a5', NULL, 'user', '2026-05-05 17:09:02', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (716, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '454b8bea-5fd7-4da2-a9d2-cc15312adcbb', 'b8fd9f74-30f8-4ff5-b43d-9ea88449611b', NULL, 'user', '2026-05-05 17:09:03', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (717, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '5494d828-4d0f-4732-a03e-d176b0743723', 'b9529782-deef-4fcc-b980-d49df9211a6e', NULL, 'user', '2026-05-05 17:09:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (718, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '61899f56-496f-482f-8f3c-fd37fbd21913', '60cca026-0332-4ecb-8226-33f1ec52197e', NULL, 'user', '2026-05-05 17:09:04', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (719, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'a9734c16-12c1-49b3-a742-635cba78567f', '3b8f9f26-569f-4f5f-8926-892cf92bcac8', NULL, 'user', '2026-05-05 17:09:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (720, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'c2dd1e85-8605-45e3-b753-a6c9bb94471b', 'e2193323-78ef-485c-92ea-4799252e7747', NULL, 'user', '2026-05-05 17:09:05', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (721, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'd00d4411-71e3-4d18-913e-38edb3ed10de', '08710dcd-481e-4891-b063-3322c7190bbd', NULL, 'user', '2026-05-05 17:09:06', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (722, '你好呀！我是英语学习助手兔兔，很高兴能帮你制定学习计划！先来了解几个关键问题，这样我能为你量身定制一份高效又适合你的计划哦~\n\n1. **你目前的英语水平是？**（比如：零基础、初级、中级、高级）\n2. **你的学习目标是什么？**（比如：日常交流、考试备考、职场英语、雅思/托福等）\n3. **你每天能花多长时间学习？**（比如：30分钟、1小时、2小时）\n4. **你最想提升哪个方面？**（口语、听力、阅读、写作、语法，还是综合提升？）\n\n请告诉我这些信息，兔兔会立刻为你设计一份专属计划！🐰✨', 152, 18, 1, 'caba6f2d-7ff2-4e73-8cc6-66e0e7880aa7', '7d3d7cd5-fd31-4ab4-9ea5-f671375d13a5', NULL, 'ai', '2026-05-05 17:09:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (723, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '0aee32fc-2b5c-49e3-afd8-b4f1bce6b467', '0bece97b-f908-453f-8f12-48a3cf051660', NULL, 'user', '2026-05-05 17:09:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (724, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '7f4d2a2a-0b5c-414d-9bd0-dcd27ea89037', 'cf7bf956-3272-4485-95dd-183005cd5d3f', NULL, 'user', '2026-05-05 17:09:07', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (725, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '47db4799-22dc-4954-8615-521237c92eee', '02ec1069-67b2-4012-bfab-8791dc1d3e5e', NULL, 'user', '2026-05-05 17:09:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (726, '你好呀！我是你的英语学习助手兔兔🐰，很高兴为你服务！制定一份英语学习计划是非常重要的一步，我们来一起设计一个科学又高效的方案吧～\n\n首先，我想了解一下你的情况，这样我能为你量身定制最合适的计划：\n\n1️⃣ **你目前的英语水平是怎样呢？**（比如：零基础、初级、中级、高级）\n2️⃣ **你的学习目标是什么？**（比如：日常交流、考试备考、职场英语、出国留学）\n3️⃣ **你每天能投入多少时间学习？**\n4️⃣ **你希望在多久内看到明显进步？**\n\n请先告诉我这些，兔兔会根据你的需求，为你打造专属的学习计划哦！😊', 144, 18, 1, '61899f56-496f-482f-8f3c-fd37fbd21913', '60cca026-0332-4ecb-8226-33f1ec52197e', NULL, 'ai', '2026-05-05 17:09:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (727, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'aedffa7f-e3e3-4041-ada1-28cafadca06d', '9b844082-c5cd-4551-bb0e-41b5bf883a8c', NULL, 'user', '2026-05-05 17:09:08', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (728, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '412de077-2b85-4a88-9e61-4632ee6ce19c', '6306a235-3ac3-480c-b852-87e2ca50fabb', NULL, 'user', '2026-05-05 17:09:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (729, '你好呀！我是英语学习助手兔兔🐰，非常高兴为你制定学习计划！不过，为了更好地帮你量身定制，我需要先了解一下你的情况哦～\n\n可以告诉我：\n1. 你目前的英语水平是？（零基础/初级/中级/高级）\n2. 你的学习目标是什么？（比如：日常交流、考试、工作、旅行等）\n3. 你每天大概能花多少时间学习呢？\n\n先告诉我这些，兔兔就能给你一份超实用的学习计划啦！😊', 104, 18, 1, 'c2dd1e85-8605-45e3-b753-a6c9bb94471b', 'e2193323-78ef-485c-92ea-4799252e7747', NULL, 'ai', '2026-05-05 17:09:09', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (730, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '69da0861-f9c0-4d47-a53b-12ed5e9c998c', '07e2c296-4b8a-441b-b620-869beeb00d8f', NULL, 'user', '2026-05-05 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (731, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定英语学习计划！\n\n首先，我想了解一下你的英语基础和学习目标（比如：是准备考试、提升口语，还是日常交流？你现在的水平大概是初级、中级还是高级？）。这样兔兔才能为你量身定制最适合的计划哦！\n\n不过，如果你没有特别说明，我可以先给你一份**通用型初级学习者**的每日学习计划参考：\n\n---\n\n### 📅 每日学习计划（约1小时）\n\n**1. 晨间10分钟：听力与发音唤醒**\n- 听一段英语短文或对话（如BBC 6 Minute English / VOA慢速）\n- 跟读一遍，模仿语音语调\n\n**2. 日间20分钟：语法或词汇积累**\n- 学习3-5个新单词+例句（建议用APP如Anki或墨墨）\n- 完成1道语法练习题（比如现在完成时、虚拟语气等）\n\n**3. 下午15分钟：口语输出练习**\n- 选一个日常话题（如“介绍你的家乡”），用英语说3-5句话\n- 录音后对比原声，找出需要改进的地方\n\n**4. 晚间15分钟：阅读与写作**\n- 读一篇简单的英语文章（如《新概念英语》或英语新闻摘要）\n- 写一段日记或总结（50-100词）\n\n---\n\n### 🌟 每周附加任务\n- **周末安排1次**：看一集无字幕英语剧（如《老友记》或《生活大爆炸》），尝试理解大意\n- **每周写1篇**150词左右的作文，可以发给我帮你修改哦！\n\n---\n\n如果你能告诉我你的**具体目标**和**可用时间**，兔兔可以帮你调整得更精准！准备好开始了吗？😊', 381, 18, 1, '454b8bea-5fd7-4da2-a9d2-cc15312adcbb', 'b8fd9f74-30f8-4ff5-b43d-9ea88449611b', NULL, 'ai', '2026-05-05 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (732, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '2db55acd-7167-41af-9e0b-2729af4921e0', '5a66c4e3-f8ab-4c5d-a8ba-2c0b6552e62b', NULL, 'user', '2026-05-05 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (733, '你好！我是英语学习助手兔兔，很高兴能帮你制定学习计划！😊\n\n为了给你定制最合适的计划，我需要先了解几个方面：\n1. 你目前的英语水平？比如初学者、中级还是高级？\n2. 主要想提升哪个方面？例如口语流利度、写作能力、语法基础，或者应对考试？\n3. 你每天能投入多少时间学习？\n\n同时，我建议一个通用的「三阶段成长计划」作为起点：\n\n**第一阶段（基础扎根，1-2周）**\n- 每天10-15分钟：复习基础语法，比如时态、句型结构\n- 每天5分钟：模仿一段英文短句发音，用手机录下来对比标准发音\n- 推荐资源：新概念英语第一册或基础语法App\n\n**第二阶段（技能拓展，3-6周）**\n- 每天写一篇50-100字的英文日记，记录当天一件小事（我用兔兔帮你批改）\n- 每周看一段5分钟的英文短视频，尝试跟读并总结3个新单词\n- 每天练习造句：选3个新单词，各造一个场景句\n\n**第三阶段（实战提升，7周后）**\n- 每周进行一次10分钟的全英文口语模拟对话（我们可以扮演角色练习）\n- 尝试用英文写一段100-150字的小作文，例如“描述你最喜欢的一部电影”\n- 每月阅读一篇短篇英文新闻或故事，画思维导图总结\n\n你觉得哪个部分最想先突破？告诉我后，我可以把计划细化到每小时的练习内容哦！🐰', 332, 18, 1, '5494d828-4d0f-4732-a03e-d176b0743723', 'b9529782-deef-4fcc-b980-d49df9211a6e', NULL, 'ai', '2026-05-05 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (734, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！我们先从你的基础情况聊起吧。你能告诉我目前英语水平大概怎样吗？比如是零基础、初级、中级，还是高级？另外，你每天大概有多少时间可以投入学习呀？这样我能给你量身定制一个最适合你的计划哦！', 73, 18, 1, '0aee32fc-2b5c-49e3-afd8-b4f1bce6b467', '0bece97b-f908-453f-8f12-48a3cf051660', NULL, 'ai', '2026-05-05 17:09:10', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (735, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '5eb13aa6-facc-46ee-afe5-d4096dfb1a02', '90196d00-cf11-412d-9a96-7f56ac40f675', NULL, 'user', '2026-05-05 17:09:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (736, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '42d03ee6-0494-401e-9894-6ba1f3a420d4', '6d4aeebf-cf2d-413a-9819-1b57cfdfab13', NULL, 'user', '2026-05-05 17:09:11', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (737, '你好呀！我是你的英语学习助手兔兔🐰！很高兴能帮你制定学习计划。\n\n首先，我想了解一下：\n1. 你的英语基础怎么样？（初级、中级、高级）\n2. 你每天能花多少时间学习英语？\n3. 你的学习目标是什么？（比如：备考、日常交流、提升职场英语等）\n\n根据你的情况，我可以为你量身定制一份专属于你的英语学习计划！包括口语、写作、语法和听力练习，保证有趣又有效哦！💪', 105, 18, 1, '7f4d2a2a-0b5c-414d-9bd0-dcd27ea89037', 'cf7bf956-3272-4485-95dd-183005cd5d3f', NULL, 'ai', '2026-05-05 17:09:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (738, '你好，请帮我制定一份英语学习计划', 4, 18, 1, '7470a834-2c66-4f85-bd74-05aa965ecaa9', '9e24be8a-c29d-4457-94ef-e3a0610380b0', NULL, 'user', '2026-05-05 17:09:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (739, '当然可以！你好，我是兔兔老师🐰。很高兴能帮你规划英语学习计划！首先，请告诉我：\n1. 你目前的英语水平大概是怎样的？（例如：初级、中级、高级）\n2. 你学习英语的目标是什么？（例如：日常交流、考试、工作、旅行）\n3. 你每天或每周大概可以花多少时间学习？\n\n这样我才能为你量身定制一份高效又适合你的学习计划哦！', 92, 18, 1, 'aedffa7f-e3e3-4041-ada1-28cafadca06d', '9b844082-c5cd-4551-bb0e-41b5bf883a8c', NULL, 'ai', '2026-05-05 17:09:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (740, '你好呀！我是你的英语学习助手兔兔🐰，很高兴见到你！让我们一起制定一份适合你的英语学习计划吧~\n\n首先，我想先问你几个小问题，这样我可以帮你制定更个性化的计划：\n1️⃣ 你目前的英语水平是？（比如零基础/初级/中级/高级）\n2️⃣ 你希望提高哪个方面？（口语/写作/语法/听力/阅读）\n3️⃣ 你每天可以安排多少时间学习？\n4️⃣ 你的学习目标是什么？（比如日常交流/考试/工作用英语）\n\n在你回答之前，我先给你一个通用版的基础学习计划参考：\n\n**📅 每日学习安排（建议30-60分钟）**\n- 单词学习（10分钟）：每天学习10-15个新单词\n- 听力练习（10-15分钟）：听英语播客或看短视频\n- 口语练习（10-15分钟）：跟着音频朗读，或者找语伴对话\n- 语法/写作（10-15分钟）：学习一个语法点，写2-3个句子\n\n**💡 小建议**\n- 把学习时间固定在每天的同一时段\n- 用手机App（如Duolingo, RedKiwi）辅助学习\n- 从兴趣出发，看喜欢的英文电影/歌曲\n\n想要更适合你的计划，就快告诉我你的具体情况吧！兔兔随时为你解答哦~ 🐰✨', 289, 18, 1, 'd00d4411-71e3-4d18-913e-38edb3ed10de', '08710dcd-481e-4891-b063-3322c7190bbd', NULL, 'ai', '2026-05-05 17:09:12', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (741, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定一份适合你的英语学习计划！\n\n首先，我需要了解你目前的英语水平和学习目标。不过，我可以先给你一份通用的每日学习计划作为参考，你可以根据自己的情况调整哦！\n\n**推荐每日学习计划（30-60分钟）**\n\n📌 **5分钟：词汇积累**\n- 每天学习10-15个新单词，使用卡片或App（如Anki、Quizlet）记忆\n- 复习前一天学过的单词\n\n📌 **10分钟：听力练习**\n- 听一篇简单的英语播客、新闻或短片（如BBC 6 Minute English）\n- 听第一遍不暂停，第二遍做笔记\n\n📌 **15分钟：口语练习**\n- 朗读刚才听到的短文，模仿发音和语调\n- 用新学单词造句，并大声说出来\n- 可以对着镜子练习，或使用AI对话工具（比如和我对话哦！）\n\n📌 **10分钟：语法学习**\n- 学习一条语法规则，比如时态、冠词、介词搭配\n- 完成3-5道练习题巩固\n\n📌 **10分钟：写作练习**\n- 写3-5个句子，描述今天发生的事或你的感受\n- 用上今天新学的单词和句型\n\n📌 **10分钟：阅读**\n- 读一篇适合你水平的英文文章（分级读物或新闻）\n- 划出不认识的单词并查意思\n\n---\n\n**额外建议：**\n- 每周安排1-2次“沉浸式学习”，比如看一部英文电影（开英文字幕）、写英文日记\n- 每天用英语做“自言自语”，比如在脑海里描述你今天做了什么\n- 培养“英语思考”的习惯，遇到事物先想英文表达\n\n你现在可以告诉我你的具体水平和目标（比如想提高口语、准备考试等），我会为你定制更精准的计划！😊', 401, 18, 1, 'a9734c16-12c1-49b3-a742-635cba78567f', '3b8f9f26-569f-4f5f-8926-892cf92bcac8', NULL, 'ai', '2026-05-05 17:09:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (742, '你好，请帮我制定一份英语学习计划', 4, 18, 1, 'f9952024-6c89-4cf9-87ab-598aeee50a80', 'ccd1a8fb-e179-4301-8c36-72a1c8cccb45', NULL, 'user', '2026-05-05 17:09:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (743, '你好呀！我是英语学习助手兔兔🐰，很高兴能帮你制定学习计划！先简单了解一下，你目前的英语基础是初学者、中级还是高级？另外，你每天可以投入多少时间学习呢？这样我可以为你量身定制一份更合适的计划哦！😊', 58, 18, 1, '69da0861-f9c0-4d47-a53b-12ed5e9c998c', '07e2c296-4b8a-441b-b620-869beeb00d8f', NULL, 'ai', '2026-05-05 17:09:13', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (744, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定学习计划！为了给你量身打造一份高效又轻松的计划，我先了解几个小问题，可以吗？\n\n1. 你目前的英语水平是怎样的？（初级、中级还是高级？）\n2. 你希望重点提升哪个方面？（比如口语、写作、语法，还是综合能力？）\n3. 你每天大概能抽出多少时间来学习？\n\n等你告诉我这些，兔兔就马上为你定制一份专属计划，让你学英语像玩游戏一样有趣又有效！🌸 加油，我们一起进步！', 120, 18, 1, '2db55acd-7167-41af-9e0b-2729af4921e0', '5a66c4e3-f8ab-4c5d-a8ba-2c0b6552e62b', NULL, 'ai', '2026-05-05 17:09:15', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (745, '你好呀！我是你的英语学习助手兔兔🐰！很高兴能帮你制定学习计划！让我先问一下你的英语基础水平和目标是什么呀？这样我才能给你量身定做一份超棒的计划！比如你是零基础、初中级，还是已经有一定基础的？你的目标是想通过考试、提升口语交流能力，还是想看懂英文原版书呢？告诉我这些，兔兔马上开始设计！📚✨', 89, 18, 1, '5eb13aa6-facc-46ee-afe5-d4096dfb1a02', '90196d00-cf11-412d-9a96-7f56ac40f675', NULL, 'ai', '2026-05-05 17:09:16', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (746, '你好呀！我是你的英语学习助手兔兔~很高兴能帮你制定学习计划！  \n\n在开始之前，请先告诉我你目前的英语水平（比如初级、中级还是高级），以及你希望重点提升的方面（口语、写作、语法、听力，还是综合提升？）。这样兔兔才能帮你量身定制最适合的计划哦！  \n\n如果你不确定从哪里开始，也不用担心，兔兔可以给你一些通用建议。比如每天安排时间练习听力、背单词、做语法练习，同时通过口语对话或写日记来提升实际运用能力。  \n\n快告诉我你的目标和水平吧，兔兔已经准备好啦！🐰✨', 131, 18, 1, 'f9952024-6c89-4cf9-87ab-598aeee50a80', 'ccd1a8fb-e179-4301-8c36-72a1c8cccb45', NULL, 'ai', '2026-05-05 17:09:17', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (747, '你好呀！我是你的英语学习助手兔兔～很高兴能帮你制定学习计划！🐰\n\n首先，我想了解几个关键信息，这样计划会更有针对性：\n\n1. **你目前的英语水平**：是零基础、初级（能简单对话）、中级（能日常交流但不够流畅），还是高级（想精进写作/口语表达）？\n2. **学习目标**：希望提升哪方面？比如：  \n   - 应对考试（四六级、雅思、托福等）  \n   - 日常口语交流（旅行、生活）  \n   - 工作或学术英语（邮件写作、专业术语等）  \n3. **可用时间**：每天能安排多少时间学习？（建议至少30分钟到1小时）\n\n**举个例子**：  \n如果你是**初级水平**，目标**日常口语**，每天**1小时**，可以这样安排：  \n- **早晨15分钟**：跟读英语短视频（如TED-Ed 1分钟片段），注意模仿发音和语调。  \n- **白天碎片时间10分钟**：用App背10-15个常用词（推荐Anki或墨墨背单词）。  \n- **晚上35分钟**：  \n  - 前20分钟：做语法练习（如《英语在用》初级册）。  \n  - 后15分钟：用“英语流利说”或找AI对话伙伴练1-2个生活场景对话（比如点餐、问路）。  \n\n**兔兔小贴士**：  \n- 每周总结一次薄弱点（比如听力总是听不清连读，那就专项练连读）。  \n- 把计划写在手机备忘录里，完成一项打勾✅，会很有成就感哦！  \n\n请告诉我你的具体情况，兔兔马上为你定制专属计划～（给你发一朵小花花🌷）', 379, 18, 1, '412de077-2b85-4a88-9e61-4632ee6ce19c', '6306a235-3ac3-480c-b852-87e2ca50fabb', NULL, 'ai', '2026-05-05 17:09:18', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (748, '你好呀！我是你的英语学习助手兔兔🐰，很高兴能帮你制定英语学习计划！根据你的需求，我们可以这样安排：\n\n**目标**：先明确你的英语目标是什么？比如日常交流、考试、还是职场应用？不同方向会有不同侧重点哦~\n\n**日常计划参考**（假设每天1小时学习时间）：\n1. **基础巩固（20分钟）**：每天记10个新单词+复习前一天的旧词，用例句记忆法更高效！\n2. **语法与写作（15分钟）**：学习1个语法点（比如时态、从句），并用它写3个句子。\n3. **听力与口语（15分钟）**：听一段1-2分钟英语对话（比如新闻或美剧片段），模仿跟读录音。\n4. **输出练习（10分钟）**：用学过的单词和语法，口头描述今天发生的事或一个主题（比如“最喜欢的食物”）。\n\n**每周进阶任务**：\n- 周末写一篇100字英文日记，并尝试用新学的语法。\n- 选一部英文电影，关字幕看一遍，再开字幕核对理解。\n\n**小提醒**：\n- 坚持比强度更重要！每天30分钟也远胜于周末突击4小时哦。\n- 遇到不会的语法可以随时问我，兔兔会耐心解释哒！\n\n现在告诉我你的具体时间安排和目标，我们可以定制专属计划！😊', 298, 18, 1, '42d03ee6-0494-401e-9894-6ba1f3a420d4', '6d4aeebf-cf2d-413a-9819-1b57cfdfab13', NULL, 'ai', '2026-05-05 17:09:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (749, '你好呀！我是你的英语学习助手兔兔🐰！很高兴能帮你制定学习计划！\n\n为了给你量身定制一个最合适的计划，我需要先了解几个小问题哦：\n\n1️⃣ **你的英语基础怎么样？**（零基础/初级/中级/高级）\n2️⃣ **每天大概能花多少时间学习英语？**（30分钟/1小时/更多）\n3️⃣ **你学习英语的主要目标是什么？**（日常交流/考试/工作/旅游）\n\n在等你回复的同时，我先给你一个通用的 **“30天英语提升基础计划”** 作为参考：\n\n🎯 **每周安排**\n- **周一、周三、周五：语法 + 写作训练**\n  - 15分钟背5个常用句型\n  - 20分钟做语法小练习\n  - 10分钟写一段小短文（日记、话题描述皆可）\n- **周二、周四、周六：听力 + 口语训练**\n  - 15分钟听英语短视频或新闻\n  - 15分钟模仿跟读（注意语音语调）\n  - 15分钟自由说话（比如用英语描述今天发生的事）\n- **周日：复习 + 趣味学习**\n  - 看一集感兴趣的英文剧（带中英字幕）\n  - 做本周学过的内容小测验\n\n🌟 **小贴士**：每天再花10分钟背5-10个新单词，用“造句法”记忆效果更好哦！\n\n告诉我你的具体情况，兔兔马上帮你调整成专属计划！💪', 315, 18, 1, '7470a834-2c66-4f85-bd74-05aa965ecaa9', '9e24be8a-c29d-4457-94ef-e3a0610380b0', NULL, 'ai', '2026-05-05 17:09:19', NULL, 0);
INSERT INTO `ai_avatar_chat_history` VALUES (750, '你好呀！我是你的英语学习助手兔兔🐰~很高兴能帮你制定英语学习计划！\n\n首先，我想了解一下你的基础情况，比如：\n1. 你目前的英语水平（初级、中级还是高级？）\n2. 你的主要学习目标（日常交流、考试、工作、还是写作提升？）\n3. 你每天可以投入多长时间来学习？\n\n如果你暂时不方便回答，那我先给你一个通用的**3个月英语提升计划**，你可以根据自己的情况灵活调整哦！\n\n---\n\n### 📅 3个月英语学习计划（基础版）\n\n#### 第一阶段：基础巩固（第1-4周）\n**目标**：掌握核心语法、扩大基础词汇、培养语感  \n- **每天**：  \n  - 背10-15个常用单词（用App如Anki或百词斩）  \n  - 听5分钟英文短对话或新闻（如BBC 6 Minute English）  \n  - 朗读一篇短文（跟读，注意发音和语调）  \n- **每周**：  \n  - 写2篇50-100词的日记（用基础句子描述日常）  \n  - 复习本周学过的单词和语法点  \n\n#### 第二阶段：能力提升（第5-8周）\n**目标**：提高听力理解、口语表达和写作逻辑  \n- **每天**：  \n  - 听10分钟英文播客或看英文字幕的剧（如《老友记》）  \n  - 练习3句造句（用新学的单词或短语）  \n- **每周**：  \n  - 写1篇150-200词的短文（话题自选，比如“我的一周”）  \n  - 做一次1分钟主题口语录音（自己说+回听检查）  \n\n#### 第三阶段：实战应用（第9-12周）\n**目标**：在生活中自然使用英语，提升流利度  \n- **每天**：  \n  - 看10分钟无字幕英文视频（尝试猜意思）  \n  - 用英语自言自语（比如描述自己在做什么）  \n- **每周**：  \n  - 写1篇200-250词的文章（可以是观点或故事）  \n  - 找一个语伴或AI对话练习（比如和我兔兔聊天🐰）  \n\n---\n\n### ✨ 兔兔小贴士\n- 每天坚持15分钟比每周学3小时更有效哦！  \n- 错题本和录音回放是提升语法和发音的“秘密武器”。  \n- 如果觉得太简单或太难，随时告诉我，兔兔帮你调难度！  \n\n现在，告诉我你的具体情况吧，兔兔帮你定制专属计划！😊', 556, 18, 1, '47db4799-22dc-4954-8615-521237c92eee', '02ec1069-67b2-4012-bfab-8791dc1d3e5e', NULL, 'ai', '2026-05-05 17:09:19', NULL, 0);

-- ----------------------------
-- Table structure for ai_learning_context_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `ai_learning_context_snapshot`;
CREATE TABLE `ai_learning_context_snapshot`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `chapter_id` bigint(20) NULL DEFAULT NULL COMMENT '章节ID',
  `section_id` bigint(20) NULL DEFAULT NULL COMMENT '小节ID',
  `latest_progress_json` json NULL COMMENT '学习进度JSON',
  `latest_homework_json` json NULL COMMENT '作业情况JSON',
  `latest_wrong_questions_json` json NULL COMMENT '错题摘要JSON',
  `summary_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '供大模型直接使用的上下文摘要',
  `snapshot_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_user_course_section`(`user_id`, `course_id`, `section_id`) USING BTREE,
  INDEX `idx_snapshot_time`(`snapshot_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI学习上下文快照表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_learning_context_snapshot
-- ----------------------------
INSERT INTO `ai_learning_context_snapshot` VALUES (1, 18, 1, 2, 3, '{\"totalSections\": 0, \"learningProgress\": \"已完成 2/0 小节，课程进度约 0%。\", \"completedSections\": 2, \"currentVideoProgress\": \"视频进度 100.00%，最后观看到 300 秒，累计学习 300 秒，状态：已完成。\", \"courseProgressPercent\": 0}', '{\"recentHomeworkSummary\": \"共 1 道作业/练习题，已作答 1 道，已批改 0 道，待批改 1 道。代表题目：11\", \"recentQuestionSummary\": \"当前范围共 1 道题，示例：[subjective] 11\"}', '{\"wrongQuestionSummary\": \"当前范围内暂无近期错题。\"}', '学生ID：18\n课程ID：1\n课程标题：数学\n章节：第二章\n小节：11\n当前小节摘要：当前小节标题：11。类型：video。视频时长约 300 秒。包含课程资源。\n学习进度：已完成 2/0 小节，课程进度约 0%。\n当前视频进度：视频进度 100.00%，最后观看到 300 秒，累计学习 300 秒，状态：已完成。\n近期作业情况：共 1 道作业/练习题，已作答 1 道，已批改 0 道，待批改 1 道。代表题目：11\n当前题目概览：当前范围共 1 道题，示例：[subjective] 11\n近期错题摘要：当前范围内暂无近期错题。\n本轮学习目标：请优先讲解当前知识点，并给出下一步学习建议。', '2026-04-10 00:49:15', '2026-04-08 12:11:53', '2026-04-08 12:11:53', 0);

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `priority` int(11) NOT NULL DEFAULT 0 COMMENT '优先级，数字越大优先级越高',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
  `start_time` datetime NULL DEFAULT NULL COMMENT '公告开始展示时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '公告结束展示时间',
  `cover_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `admin_id` bigint(20) NOT NULL COMMENT '发布管理员id',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '查看次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_priority`(`priority`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------

-- ----------------------------
-- Table structure for announcement_read
-- ----------------------------
DROP TABLE IF EXISTS `announcement_read`;
CREATE TABLE `announcement_read`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `announcement_id` bigint(20) NOT NULL COMMENT '公告id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_announcement_user`(`announcement_id`, `user_id`) USING BTREE,
  INDEX `idx_announcement_id`(`announcement_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告阅读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement_read
-- ----------------------------

-- ----------------------------
-- Table structure for assignment_submission
-- ----------------------------
DROP TABLE IF EXISTS `assignment_submission`;
CREATE TABLE `assignment_submission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `assignment_id` bigint(20) NOT NULL COMMENT 'Assignment id',
  `user_id` bigint(20) NOT NULL COMMENT 'Submit user id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提交内容',
  `attachment_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Attachment url',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Submit time',
  `submit_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 submitted / 2 resubmitted / 3 graded',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师反馈',
  `grader_id` bigint(20) NULL DEFAULT NULL COMMENT 'Teacher id who graded this submission',
  `graded_time` datetime NULL DEFAULT NULL COMMENT 'Graded time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_assignment_user`(`assignment_id`, `user_id`) USING BTREE,
  INDEX `idx_assignment_id`(`assignment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_grader_id`(`grader_id`) USING BTREE,
  CONSTRAINT `fk_assignment_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `class_assignment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_assignment_submission_grader` FOREIGN KEY (`grader_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_assignment_submission_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作业提交' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_submission
-- ----------------------------

-- ----------------------------
-- Table structure for chapter
-- ----------------------------
DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Chapter id',
  `course_id` bigint(20) NOT NULL COMMENT 'Course id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Chapter title',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Chapter description',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  `admin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_course`(`course_id`) USING BTREE,
  INDEX `idx_chapter_sort`(`course_id`, `sort_order`) USING BTREE,
  CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Chapter table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chapter
-- ----------------------------
INSERT INTO `chapter` VALUES (1, 1, '第一章', 1, NULL, '2026-04-05 17:32:50', '2026-04-05 17:32:50', 0, 18);
INSERT INTO `chapter` VALUES (2, 1, '第二章', 1, NULL, '2026-04-05 17:32:58', '2026-04-05 17:32:58', 0, 18);
INSERT INTO `chapter` VALUES (3, 2, '基础篇', 1, '', '2026-05-02 21:39:24', '2026-05-02 21:39:24', 0, 18);
INSERT INTO `chapter` VALUES (4, 2, '进阶篇', 1, '', '2026-05-02 21:50:09', '2026-05-02 21:50:09', 0, 18);
INSERT INTO `chapter` VALUES (5, 3, '短句子', 1, '', '2026-05-02 21:54:03', '2026-05-02 21:54:29', 0, 18);
INSERT INTO `chapter` VALUES (6, 3, '短对话', 1, '', '2026-05-02 21:55:03', '2026-05-02 21:55:03', 0, 18);
INSERT INTO `chapter` VALUES (7, 4, '基础3D建模', 1, '', '2026-05-02 21:59:50', '2026-05-02 21:59:50', 0, 18);
INSERT INTO `chapter` VALUES (8, 4, '动画工具', 1, '', '2026-05-02 22:02:27', '2026-05-02 22:02:27', 0, 18);

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息 ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者 ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者 ID',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话 ID（senderId_receiverId 排序组合）',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text-文本，image-图片，video-视频，audio-语音',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容（文本或媒体 URL）',
  `audio_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '语音文件 URL',
  `audio_duration` int(11) NULL DEFAULT NULL COMMENT '语音时长（秒）',
  `audio_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '语音识别的文字内容',
  `media_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片/视频 URL',
  `media_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `media_duration` int(11) NULL DEFAULT NULL COMMENT '视频/音频时长（秒）',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '消息状态：0-未读，1-已读，2-已删除',
  `is_revoke` tinyint(1) NULL DEFAULT 0 COMMENT '是否撤回：0-否，1-是',
  `revoke_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `exp_awarded` tinyint(1) NULL DEFAULT 0 COMMENT '是否已发放经验：0-否，1-是',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_send_time`(`send_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (19, 5, 7, '5_7', 'text', '😀', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 12:54:18', NULL, '2026-04-03 12:54:18', '2026-04-03 12:54:18', 0);
INSERT INTO `chat_message` VALUES (20, 5, 7, '5_7', 'text', '你好哇', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 12:55:27', NULL, '2026-04-03 12:55:27', '2026-04-03 12:55:27', 0);
INSERT INTO `chat_message` VALUES (21, 5, 7, '5_7', 'text', '你是Demo Teacher吗', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 12:56:38', NULL, '2026-04-03 12:56:37', '2026-04-03 12:56:37', 0);
INSERT INTO `chat_message` VALUES (22, 5, 7, '5_7', 'text', '你好？', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 12:59:28', NULL, '2026-04-03 12:59:27', '2026-04-03 12:59:27', 0);
INSERT INTO `chat_message` VALUES (23, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:02:48', NULL, '2026-04-03 13:02:47', '2026-04-03 13:02:47', 0);
INSERT INTO `chat_message` VALUES (24, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:03:09', NULL, '2026-04-03 13:03:09', '2026-04-03 13:03:09', 0);
INSERT INTO `chat_message` VALUES (25, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:12:53', NULL, '2026-04-03 13:12:52', '2026-04-03 13:12:52', 0);
INSERT INTO `chat_message` VALUES (26, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:15:06', NULL, '2026-04-03 13:15:05', '2026-04-03 13:15:05', 0);
INSERT INTO `chat_message` VALUES (27, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:21:58', NULL, '2026-04-03 13:21:57', '2026-04-03 13:21:57', 0);
INSERT INTO `chat_message` VALUES (28, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:22:03', NULL, '2026-04-03 13:22:03', '2026-04-03 13:22:03', 0);
INSERT INTO `chat_message` VALUES (29, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:23:50', NULL, '2026-04-03 13:23:49', '2026-04-03 13:23:49', 0);
INSERT INTO `chat_message` VALUES (30, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 13:25:20', NULL, '2026-04-03 13:25:19', '2026-04-03 13:25:19', 0);
INSERT INTO `chat_message` VALUES (31, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 15:16:40', NULL, '2026-04-03 15:16:40', '2026-04-03 15:16:40', 0);
INSERT INTO `chat_message` VALUES (32, 7, 5, '5_7', 'text', 'hi', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 15:50:55', '2026-04-03 21:26:01', '2026-04-03 15:50:55', '2026-04-03 21:26:01', 0);
INSERT INTO `chat_message` VALUES (33, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:02:51', NULL, '2026-04-03 16:02:50', '2026-04-03 16:02:50', 0);
INSERT INTO `chat_message` VALUES (34, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:07:29', NULL, '2026-04-03 16:07:28', '2026-04-03 16:07:28', 0);
INSERT INTO `chat_message` VALUES (35, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:10:37', NULL, '2026-04-03 16:10:37', '2026-04-03 16:10:37', 0);
INSERT INTO `chat_message` VALUES (36, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:14:47', NULL, '2026-04-03 16:14:47', '2026-04-03 16:14:47', 0);
INSERT INTO `chat_message` VALUES (37, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:24:32', NULL, '2026-04-03 16:24:32', '2026-04-03 16:24:32', 0);
INSERT INTO `chat_message` VALUES (38, 5, 7, '5_7', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-03 16:34:48', NULL, '2026-04-03 16:34:48', '2026-04-03 16:34:48', 0);
INSERT INTO `chat_message` VALUES (39, 7, 5, '5_7', 'text', '对的', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 16:49:54', '2026-04-03 21:26:01', '2026-04-03 16:49:54', '2026-04-03 21:26:01', 0);
INSERT INTO `chat_message` VALUES (40, 6, 5, '5_6', 'text', 'hello,Demo Student Avatar', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 16:51:39', '2026-04-03 19:31:24', '2026-04-03 16:51:39', '2026-04-03 19:31:24', 0);
INSERT INTO `chat_message` VALUES (41, 5, 6, '5_6', 'text', '你好呀', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 17:15:14', '2026-04-03 19:32:05', '2026-04-03 17:15:14', '2026-04-03 19:32:05', 0);
INSERT INTO `chat_message` VALUES (42, 7, 6, '6_7', 'text', '你好哇', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 17:19:23', '2026-04-03 21:24:54', '2026-04-03 17:19:22', '2026-04-03 21:24:54', 0);
INSERT INTO `chat_message` VALUES (43, 6, 5, '5_6', 'text', '不会', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 18:15:01', '2026-04-03 19:31:24', '2026-04-03 18:15:01', '2026-04-03 19:31:24', 0);
INSERT INTO `chat_message` VALUES (44, 7, 5, '5_7', 'text', '弟弟', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 19:09:39', '2026-04-03 21:26:01', '2026-04-03 19:09:38', '2026-04-03 21:26:01', 0);
INSERT INTO `chat_message` VALUES (45, 5, 6, '5_6', 'text', '你好呀', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 19:31:32', '2026-04-03 19:32:05', '2026-04-03 19:31:31', '2026-04-03 19:32:05', 0);
INSERT INTO `chat_message` VALUES (46, 6, 5, '5_6', 'text', '有消息进来吗？', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 19:38:31', '2026-04-03 19:38:51', '2026-04-03 19:38:31', '2026-04-03 19:38:51', 0);
INSERT INTO `chat_message` VALUES (47, 5, 6, '5_6', 'text', '你在干嘛', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 21:07:50', '2026-04-03 21:24:47', '2026-04-03 21:07:49', '2026-04-03 21:24:47', 0);
INSERT INTO `chat_message` VALUES (48, 6, 5, '5_6', 'text', '做数学作业', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 21:25:19', '2026-04-03 21:25:43', '2026-04-03 21:25:19', '2026-04-03 21:25:43', 0);
INSERT INTO `chat_message` VALUES (49, 5, 6, '5_6', 'text', '你做完了吗？', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 21:37:54', '2026-04-03 21:38:20', '2026-04-03 21:37:54', '2026-04-03 21:38:20', 0);
INSERT INTO `chat_message` VALUES (50, 5, 9, '5_9', 'text', '你是谁呀', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 21:48:16', '2026-04-03 21:48:38', '2026-04-03 21:48:15', '2026-04-03 21:48:38', 0);
INSERT INTO `chat_message` VALUES (51, 9, 5, '5_9', 'text', '我是小王呀', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-03 21:48:47', '2026-04-03 21:49:21', '2026-04-03 21:48:46', '2026-04-03 21:49:21', 0);
INSERT INTO `chat_message` VALUES (52, 8, 9, '8_9', 'text', 'hello_text', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-05 01:20:49', '2026-04-05 01:20:50', '2026-04-05 01:20:49', '2026-04-05 01:20:50', 0);
INSERT INTO `chat_message` VALUES (53, 8, 9, '8_9', 'video', '[视频]', NULL, NULL, NULL, 'https://example.com/v.mp4', 100, 10, 0, 0, NULL, 1, '2026-04-05 01:20:51', NULL, '2026-04-05 01:20:50', '2026-04-05 01:20:50', 0);
INSERT INTO `chat_message` VALUES (54, 8, 9, '8_9', 'audio', '[语音]', 'https://example.com/a.mp3', 5, NULL, NULL, 10, NULL, 0, 0, NULL, 1, '2026-04-05 01:20:51', NULL, '2026-04-05 01:20:50', '2026-04-05 01:20:50', 0);
INSERT INTO `chat_message` VALUES (55, 11, 12, '11_12', 'text', 'demo hello message', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 1, '2026-04-05 01:39:12', NULL, '2026-04-05 01:39:12', '2026-04-05 01:39:12', 0);
INSERT INTO `chat_message` VALUES (56, 14, 15, '14_15', 'text', 'demo text message', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-05 01:40:30', '2026-04-05 01:40:50', '2026-04-05 01:40:29', '2026-04-05 01:40:50', 0);
INSERT INTO `chat_message` VALUES (57, 14, 15, '14_15', 'video', '[视频]', NULL, NULL, NULL, 'http://a.com/v.mp4', 123, 10, 1, 0, NULL, 1, '2026-04-05 01:40:30', '2026-04-05 01:40:50', '2026-04-05 01:40:30', '2026-04-05 01:40:50', 0);
INSERT INTO `chat_message` VALUES (58, 14, 15, '14_15', 'audio', '[语音]', 'http://a.com/a.mp3', 8, NULL, NULL, 88, NULL, 1, 0, NULL, 1, '2026-04-05 01:40:30', '2026-04-05 01:40:50', '2026-04-05 01:40:30', '2026-04-05 01:40:50', 0);
INSERT INTO `chat_message` VALUES (59, 19, 18, '18_19', 'text', '你好，有什么事吗', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-05 12:01:18', '2026-04-05 12:01:35', '2026-04-05 12:01:18', '2026-04-05 12:01:35', 0);
INSERT INTO `chat_message` VALUES (60, 17, 18, '17_18', 'text', 'nh', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-07 19:45:55', '2026-04-07 19:46:12', '2026-04-07 19:45:54', '2026-04-07 19:46:12', 0);
INSERT INTO `chat_message` VALUES (61, 17, 18, '17_18', 'text', 'hi，我是Demo Student Avatar', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-10 20:24:32', '2026-04-10 20:25:03', '2026-04-10 20:24:32', '2026-04-10 20:25:03', 0);
INSERT INTO `chat_message` VALUES (62, 18, 17, '17_18', 'text', '你好呀，我喜欢Demo Student Avatar', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-10 20:25:17', '2026-05-03 00:42:47', '2026-04-10 20:25:17', '2026-05-03 00:42:47', 0);
INSERT INTO `chat_message` VALUES (63, 18, 17, '17_18', 'text', '你好', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-11 15:45:39', '2026-05-03 00:42:47', '2026-04-11 15:45:38', '2026-05-03 00:42:47', 0);
INSERT INTO `chat_message` VALUES (64, 18, 17, '17_18', 'text', '好难啊', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-26 13:05:21', '2026-05-03 00:42:47', '2026-04-26 13:05:21', '2026-05-03 00:42:47', 0);
INSERT INTO `chat_message` VALUES (65, 17, 18, '17_18', 'text', 'Demo Student Avatar？', NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, 1, '2026-05-03 00:43:06', '2026-05-03 12:07:56', '2026-05-03 00:43:05', '2026-05-03 12:07:56', 0);

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话 ID',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话 ID（两个用户 ID 排序后组合）',
  `user1_id` bigint(20) NOT NULL COMMENT '参与者 1 ID（较小 ID）',
  `user2_id` bigint(20) NOT NULL COMMENT '参与者 2 ID（较大 ID）',
  `last_message_id` bigint(20) NULL DEFAULT NULL COMMENT '最后一条消息 ID',
  `last_message_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息类型',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后一条消息时间',
  `user1_unread_count` int(11) NULL DEFAULT 0 COMMENT '用户 1 未读消息数',
  `user2_unread_count` int(11) NULL DEFAULT 0 COMMENT '用户 2 未读消息数',
  `is_user1_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '用户 1 是否删除会话',
  `is_user2_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '用户 2 是否删除会话',
  `is_user1_top` tinyint(1) NULL DEFAULT 0 COMMENT '用户 1 是否置顶',
  `is_user2_top` tinyint(1) NULL DEFAULT 0 COMMENT '用户 2 是否置顶',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `session_id`(`session_id`) USING BTREE,
  UNIQUE INDEX `uk_session_id`(`session_id`) USING BTREE,
  INDEX `idx_user1_id`(`user1_id`) USING BTREE,
  INDEX `idx_user2_id`(`user2_id`) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_session
-- ----------------------------
INSERT INTO `chat_session` VALUES (16, '5_7', 5, 7, 44, '弟弟', 'text', '2026-04-03 19:09:38', 3, 19, 0, 0, 0, 0, '2026-04-03 12:54:18', '2026-04-03 19:09:38', 0);
INSERT INTO `chat_session` VALUES (17, '5_6', 5, 6, 49, '你做完了吗？', 'text', '2026-04-03 21:37:54', 4, 4, 0, 0, 0, 0, '2026-04-03 16:51:39', '2026-04-03 21:37:54', 0);
INSERT INTO `chat_session` VALUES (18, '6_7', 6, 7, 42, '你好哇', 'text', '2026-04-03 17:19:22', 1, 0, 0, 0, 0, 0, '2026-04-03 17:19:23', '2026-04-03 17:19:22', 0);
INSERT INTO `chat_session` VALUES (19, '5_9', 5, 9, 51, '我是小王呀', 'text', '2026-04-03 21:48:46', 1, 1, 0, 0, 0, 0, '2026-04-03 21:48:16', '2026-04-03 21:48:46', 0);
INSERT INTO `chat_session` VALUES (20, '8_9', 8, 9, 54, '[语音]', 'audio', '2026-04-05 01:20:50', 0, 3, 0, 0, 0, 0, '2026-04-05 01:20:49', '2026-04-05 01:20:50', 0);
INSERT INTO `chat_session` VALUES (21, '11_12', 11, 12, 55, 'demo hello message', 'text', '2026-04-05 01:39:12', 0, 1, 0, 0, 0, 0, '2026-04-05 01:39:12', '2026-04-05 01:39:12', 0);
INSERT INTO `chat_session` VALUES (22, '14_15', 14, 15, 58, '[语音]', 'audio', '2026-04-05 01:40:30', 0, 3, 0, 0, 0, 0, '2026-04-05 01:40:30', '2026-04-05 01:40:30', 0);
INSERT INTO `chat_session` VALUES (23, '18_19', 18, 19, 59, '你好，有什么事吗', 'text', '2026-04-05 12:01:18', 0, 0, 0, 0, 0, 0, '2026-04-05 12:01:18', '2026-04-05 12:01:35', 0);
INSERT INTO `chat_session` VALUES (24, '17_18', 17, 18, 65, 'Demo Student Avatar？', 'text', '2026-05-03 00:43:05', 0, 0, 0, 0, 0, 0, '2026-04-07 19:45:55', '2026-05-03 12:07:56', 0);

-- ----------------------------
-- Table structure for class_assignment
-- ----------------------------
DROP TABLE IF EXISTS `class_assignment`;
CREATE TABLE `class_assignment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `class_id` bigint(20) NOT NULL COMMENT 'Class id',
  `course_id` bigint(20) NOT NULL COMMENT 'Course id',
  `section_id` bigint(20) NULL DEFAULT NULL COMMENT 'Section id, supports one section many assignments',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作业标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业描述',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止日期',
  `total_score` int(11) NOT NULL DEFAULT 100 COMMENT 'Assignment total score',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 draft / 2 published / 3 closed',
  `creator_id` bigint(20) NOT NULL COMMENT 'Creator user id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  INDEX `idx_section_id`(`section_id`) USING BTREE,
  CONSTRAINT `fk_class_assignment_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_assignment_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_class_assignment_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级作业' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_assignment
-- ----------------------------

-- ----------------------------
-- Table structure for class_course
-- ----------------------------
DROP TABLE IF EXISTS `class_course`;
CREATE TABLE `class_course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `class_id` bigint(20) NOT NULL COMMENT '班级ID，关联到class_info表',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID，关联到course表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_course`(`class_id`, `course_id`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  CONSTRAINT `fk_class_course_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_class_course_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级与课程关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_course
-- ----------------------------

-- ----------------------------
-- Table structure for class_info
-- ----------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '班级描述',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID，关联到user表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_class_info_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_info
-- ----------------------------

-- ----------------------------
-- Table structure for class_member
-- ----------------------------
DROP TABLE IF EXISTS `class_member`;
CREATE TABLE `class_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `class_id` bigint(20) NOT NULL COMMENT '班级ID，关联到class_info表',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联到user表',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色，如teacher/student',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_user`(`class_id`, `user_id`) USING BTREE,
  INDEX `idx_class_id`(`class_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_class_member_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_class_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级成员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_member
-- ----------------------------

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Course id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Course title',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Course description',
  `teacher_id` bigint(20) NOT NULL COMMENT 'Teacher user id',
  `cover_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Cover url',
  `category` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Course category',
  `total_sections` int(11) NOT NULL DEFAULT 0 COMMENT 'Total sections',
  `total_questions` int(11) NOT NULL DEFAULT 0 COMMENT 'Total questions',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 published 0 offline',
  `rating_score` decimal(3, 1) NOT NULL DEFAULT 0.0 COMMENT 'Average rating',
  `rating_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Rating count',
  `student_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Learner count',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT 'View count',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  `subtitle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `original_price` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `course_type` tinyint(4) NOT NULL DEFAULT 1,
  `difficulty` tinyint(4) NOT NULL DEFAULT 1,
  `category_id` bigint(20) NULL DEFAULT NULL,
  `total_duration` int(11) NOT NULL DEFAULT 0,
  `total_chapters` int(11) NOT NULL DEFAULT 0,
  `buy_count` int(11) NOT NULL DEFAULT 0,
  `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `objectives` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `target_audience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ai_knowleage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_teacher`(`teacher_id`) USING BTREE,
  INDEX `idx_course_status_delete`(`status`, `is_delete`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Course table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '数学', NULL, 1, 'https://tse4.mm.bing.net/th/id/OIP.fRcds9XkcVRwMykirh4mIgHaE8?rs=1&pid=ImgDetMain&o=7&rm=3', '1', 0, 0, 1, 0.0, 0, 0, 0, '2026-04-05 16:19:50', '2026-04-10 00:19:34', 0, NULL, 0.00, 0.00, 1, 1, 1, 0, 0, 0, NULL, NULL, NULL, NULL, '以下是视频内容的文字转录及完整总结：\n\n视频文字内容转录\n【开头引言】\n\n字幕： 活在当下才是终身浪漫。\n旁白/字幕： 以前觉得她不可理喻，现在觉得句句在理。\n标注： 《传闻中的七公主》片段。\n【剧中对话片段】\n\n罗美七（女主）： 我才不要为了晚年着想，年轻的时候不能买想买的东西，为了明天好，今天饿肚子吗？现在却要苦哈哈，我无法理解这种人。\n罗美七（试衣服时）： 穿起来好看的时候，就要多穿一点啊。等老了再穿好衣服有什么用，又撑不起来。\n南秀七（男主/老公）： （字幕提示）当她老公说：年轻的时候应该多吃苦。\n罗美七（反驳）： 而美七是这样回答的：年轻的时候吃太多苦，老了就浑身是病。等老了之后有钱有什么用？腿会酸，腰太粗，牙齿都掉了，不能够吃好吃的肉。人老了也花不到什么钱，只要每天吃三餐就够了。\n【视频制作者旁白/升华】\n\n字幕/旁白： 我们从小生活在“先苦后甜”绑架了太多年。总以为熬过当下的苦，未来就一定有甜。可多少人苦了一辈子，没等到甜，只熬出一身病痛，和满心遗憾。\n字幕/旁白： 以前只觉得美七爱慕虚荣，整天都是钱钱钱的。现在感觉她的思想，真的太超前了。\n视频内容完整总结\n1. 核心主题： 该视频通过剪辑经典韩剧《传闻中的七公主》中女主角罗美七（崔贞媛饰）的片段，探讨了**“活在当下”与“先苦后甜”**两种生活观念的冲突，并最终表达了对“活在当下”这一生活哲学的强烈认同。\n\n2. 剧情回顾与观点对比：\n\n传统观念（男主/社会规训）： 认为年轻人应该“多吃苦”，为了晚年着想，为了“明天好”，现在应该节俭、忍耐，甚至牺牲当下的享受（如“今天饿肚子”）。\n女主观念（罗美七）： 坚决反对为了未知的晚年牺牲现在。\n关于消费： 年轻时就要买想买的东西，穿好看的衣服，因为老了身体机能下降（腰粗、牙掉、腿酸），有钱也享受不了（穿不了美衣、吃不了美食）。\n关于吃苦： 认为年轻时吃太多苦，老了只会落下一身病，而不是所谓的“享福”。\n3. 情感升华与现实反思： 视频制作者结合现代人的生活状态进行了反思：\n\n打破“先苦后甜”的迷思： 指出很多人从小被教育要吃苦，以为熬过苦难未来就会幸福。但现实残酷，许多人苦了一辈子，并没有等到“甜”，反而只留下了病痛和遗憾。\n对角色的重新评价： 视频作者表示，小时候看剧觉得罗美七爱慕虚荣、只认钱、不可理喻；但长大后经历了生活的毒打，才发现她的思想非常“超前”且“句句在理”。她其实是在倡导一种不委屈自己、及时行乐、珍惜当下的通透人生观。\n4. 总结论： 视频最后点题“活在当下才是终身浪漫”，鼓励人们不要为了虚无缥缈的未来过度透支现在，要在有能力的时候享受生活，对自己好一点。');
INSERT INTO `course` VALUES (2, '高中数学', '必修二', 1, 'https://ts1.tc.mm.bing.net/th/id/R-C.61a6d90777d3afde0fe9683dbe4cc35a?rik=mltv9CluPboIjw&riu=http%3a%2f%2fi1.hdslb.com%2fbfs%2farchive%2fd0fa31893e2520292b1e13f30e6d3f8422b7f5ef.jpg&ehk=8ciMJNiEoFY21EENM3iL6EfBEP2mn4vSOPNIHsWHnAc%3d&risl=&pid=ImgRaw&r=0', '1', 0, 0, 1, 0.0, 0, 0, 1, '2026-05-02 21:38:40', '2026-05-03 00:11:45', 0, NULL, 0.00, 0.00, 1, 3, 1, 0, 0, 0, NULL, NULL, NULL, NULL, '高中数学必修二整体知识总结\n高中数学必修二主要分为立体几何初步和平面解析几何初步两大核心板块，是高中几何的基础，侧重空间想象能力、逻辑推理能力和坐标运算能力，也是高考必考重点内容。\n一、立体几何初步\n1. 空间几何体\n常见几何体分类\n柱体（棱柱、圆柱）、锥体（棱锥、圆锥）、台体（棱台、圆台）、球体。\n结构特征\n棱柱：上下底面全等且平行，侧面都是平行四边形；\n棱锥：底面是多边形，侧面是共顶点的三角形；\n台体：由锥体用平行于底面的平面截取而来；\n球：空间中到定点距离等于定长的点的集合。\n三视图与直观图\n三视图：主视图、左视图、俯视图，遵循长对正、高平齐、宽相等；\n直观图：常用斜二测画法，平行于 x 轴长度不变，平行于 y 轴长度减半，角度变为 45° 或 135°。\n表面积与体积公式\n柱体体积：\nV=Sh\n；锥体体积：\nV= \n3\n1\n​\n Sh\n；\n球的表面积：\nS=4πR \n2\n \n，球的体积：\nV= \n3\n4\n​\n πR \n3\n \n。\n2. 空间点、直线、平面之间的位置关系\n平面基本公理\n三点确定一个平面；直线上两点在平面内，则整条直线在平面内；两平面相交有且只有一条交线。\n位置关系\n空间直线：平行、相交、异面直线（不平行也不相交，无公共点）；\n直线与平面：直线在平面内、直线与平面平行、直线与平面相交；\n平面与平面：平行、相交。\n3. 空间中的平行与垂直\n平行判定与性质\n线线平行→线面平行；线面平行→面面平行；反之性质也成立。\n垂直判定与性质\n线线垂直→线面垂直（一条直线垂直平面内两条相交直线）；\n线面垂直→面面垂直；面面垂直可推线面垂直。\n空间角\n异面直线所成角、直线与平面所成角、二面角，取值范围各有规定，常通过平移、找垂线求解。\n二、平面解析几何初步\n1. 直线与方程\n直线的倾斜角与斜率\n倾斜角范围\n[0,π)\n，斜率\nk=tanα\n，两点斜率公式：\nk= \nx \n2\n​\n −x \n1\n​\n \ny \n2\n​\n −y \n1\n​\n \n​\n \n。\n直线五种方程形式\n点斜式、斜截式、两点式、截距式、一般式\nAx+By+C=0\n。\n两条直线的位置关系\n平行、重合、相交、垂直；可用斜率或一般式系数判断。\n距离公式\n两点间距离、点到直线距离、两条平行线间距离。\n2. 圆与方程\n圆的方程\n标准方程：\n(x−a) \n2\n +(y−b) \n2\n =r \n2\n \n，圆心\n(a,b)\n，半径\nr\n；\n一般方程：\nx \n2\n +y \n2\n +Dx+Ey+F=0\n，可配方化为标准式。\n直线与圆的位置关系\n相离、相切、相交；用圆心到直线距离 d 与半径 r大小关系判断。\n圆与圆的位置关系\n外离、外切、相交、内切、内含，由两圆心距与两半径和差判断。\n三、必修二学习核心要点\n立体几何重在建立空间立体感，熟记公理、判定定理，学会画图、补形；\n解析几何核心是坐标化思想，把几何问题转化为代数方程计算；\n公式多、定理多，需理解记忆，多练题型，掌握平行、垂直、距离、角度的常规解题套路。');
INSERT INTO `course` VALUES (3, '英文听力', NULL, 1, 'https://ts1.tc.mm.bing.net/th/id/OIP-C.VE4k9Bx6ck-wQkyXkm1oygHaE8?rs=1&pid=ImgDetMain&o=7&rm=3', '2', 0, 0, 1, 0.0, 0, 0, 0, '2026-05-02 21:53:50', '2026-05-02 22:08:03', 0, NULL, 0.00, 0.00, 1, 1, 2, 0, 0, 0, NULL, NULL, NULL, NULL, '一些初级英文听力');
INSERT INTO `course` VALUES (4, 'blender', NULL, 1, 'https://ts2.tc.mm.bing.net/th/id/OIP-C.3vuGiwCTe4LKo2fq1DjXPgHaDj?rs=1&pid=ImgDetMain&o=7&rm=3', '10', 0, 0, 1, 0.0, 0, 0, 0, '2026-05-02 21:58:18', '2026-05-02 22:08:35', 0, NULL, 0.00, 0.00, 1, 4, 10, 0, 0, 0, NULL, NULL, NULL, NULL, '一些blender教学视频');

-- ----------------------------
-- Table structure for course_category
-- ----------------------------
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标URL',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重，数字越大排序越靠前',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父分类id，0表示一级分类',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_category
-- ----------------------------
INSERT INTO `course_category` VALUES (1, '数学', '', '', 1, 0, 18, '2026-05-02 16:28:58', '2026-05-02 16:28:58', 0);
INSERT INTO `course_category` VALUES (2, '英语', '', '', 2, 0, 18, '2026-05-02 17:10:43', '2026-05-02 17:11:03', 0);
INSERT INTO `course_category` VALUES (3, '语文', '', '', 3, 0, 18, '2026-05-02 17:11:37', '2026-05-02 17:11:37', 0);
INSERT INTO `course_category` VALUES (4, '物理', '', '', 4, 0, 18, '2026-05-02 17:11:49', '2026-05-02 17:11:49', 0);
INSERT INTO `course_category` VALUES (5, '化学', '', '', 5, 0, 18, '2026-05-02 17:11:59', '2026-05-02 17:11:59', 0);
INSERT INTO `course_category` VALUES (6, '生物', '', '', 6, 0, 18, '2026-05-02 17:12:07', '2026-05-02 17:12:07', 0);
INSERT INTO `course_category` VALUES (7, '政治', '', '', 7, 0, 18, '2026-05-02 17:12:15', '2026-05-02 17:12:15', 0);
INSERT INTO `course_category` VALUES (8, '历史', '', '', 8, 0, 18, '2026-05-02 17:12:23', '2026-05-02 17:12:23', 0);
INSERT INTO `course_category` VALUES (9, '地理', '', '', 9, 0, 18, '2026-05-02 17:12:31', '2026-05-02 17:12:31', 0);
INSERT INTO `course_category` VALUES (10, 'blender', '', '', 0, 0, 18, '2026-05-02 21:58:26', '2026-05-02 21:58:26', 0);

-- ----------------------------
-- Table structure for course_chapter
-- ----------------------------
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `course_id` bigint(20) NOT NULL COMMENT '课程id',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '章节标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '章节描述',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程章节' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_chapter
-- ----------------------------

-- ----------------------------
-- Table structure for course_favourite
-- ----------------------------
DROP TABLE IF EXISTS `course_favourite`;
CREATE TABLE `course_favourite`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `course_id` bigint(20) NOT NULL COMMENT '课程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_favourite
-- ----------------------------

-- ----------------------------
-- Table structure for course_knowledge_sync
-- ----------------------------
DROP TABLE IF EXISTS `course_knowledge_sync`;
CREATE TABLE `course_knowledge_sync`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '课程分类/学科ID',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型：course_intro/chapter/section/material/question/explanation',
  `source_id` bigint(20) NOT NULL COMMENT '来源记录ID',
  `source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源名称',
  `dify_dataset_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Dify知识库ID',
  `dify_document_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify文档ID',
  `dify_batch_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify索引批次ID',
  `sync_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步模式：text/file',
  `sync_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步状态：pending/uploading/indexing/completed/failed',
  `content_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容哈希，用于幂等和增量同步',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `last_sync_time` datetime NULL DEFAULT NULL COMMENT '最近一次同步时间',
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下次重试时间',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_source_sync`(`source_type`, `source_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_dataset_status`(`dify_dataset_id`, `sync_status`) USING BTREE,
  INDEX `idx_retry_status`(`sync_status`, `next_retry_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程知识同步状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_knowledge_sync
-- ----------------------------

-- ----------------------------
-- Table structure for course_material
-- ----------------------------
DROP TABLE IF EXISTS `course_material`;
CREATE TABLE `course_material`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `course_id` bigint(20) NOT NULL COMMENT '课程id',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资料标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资料描述',
  `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL',
  `file_size` bigint(20) NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `file_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `download_count` int(11) NOT NULL DEFAULT 0 COMMENT '下载次数',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程资料' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_material
-- ----------------------------

-- ----------------------------
-- Table structure for course_rating
-- ----------------------------
DROP TABLE IF EXISTS `course_rating`;
CREATE TABLE `course_rating`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评分 id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `course_id` bigint(20) NOT NULL COMMENT '课程 id',
  `rating` tinyint(4) NOT NULL COMMENT '评分：1-5 分',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `helpful_count` int(11) NULL DEFAULT 0 COMMENT '有用次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_rating`(`rating`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程评分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_rating
-- ----------------------------

-- ----------------------------
-- Table structure for course_review
-- ----------------------------
DROP TABLE IF EXISTS `course_review`;
CREATE TABLE `course_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `course_id` bigint(20) NOT NULL COMMENT '课程id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价内容',
  `rating` tinyint(4) NOT NULL DEFAULT 5 COMMENT '评分(1-5分)',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int(11) NOT NULL DEFAULT 0 COMMENT '回复数',
  `admin_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '管理员回复',
  `admin_reply_time` datetime NULL DEFAULT NULL COMMENT '管理员回复时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_rating`(`rating`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_review
-- ----------------------------

-- ----------------------------
-- Table structure for course_view_record
-- ----------------------------
DROP TABLE IF EXISTS `course_view_record`;
CREATE TABLE `course_view_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `course_id` bigint(20) NOT NULL COMMENT 'Course id',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_user`(`course_id`, `user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Course unique view record' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_view_record
-- ----------------------------
INSERT INTO `course_view_record` VALUES (1, 2, 18, '2026-05-03 00:11:46', '2026-05-03 00:11:46', 0);

-- ----------------------------
-- Table structure for daily_article
-- ----------------------------
DROP TABLE IF EXISTS `daily_article`;
CREATE TABLE `daily_article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `cover_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片 URL',
  `coverImage` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `author` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
  `source` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源',
  `source_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原文链接 URL',
  `sourceUrl` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原文链接',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章分类',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，JSON数组格式',
  `difficulty` tinyint(4) NOT NULL DEFAULT 1 COMMENT '难度等级：1-简单，2-中等，3-困难',
  `read_time` int(11) NULL DEFAULT NULL COMMENT '预计阅读时间 (分钟)',
  `readTime` int(11) NOT NULL DEFAULT 0 COMMENT '预计阅读时间(分钟)',
  `publish_date` date NOT NULL,
  `admin_id` bigint(20) NOT NULL COMMENT '创建管理员id',
  `view_count` int(11) NOT NULL DEFAULT 0,
  `like_count` int(11) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publish_date`(`publish_date`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日文章' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_article
-- ----------------------------
INSERT INTO `daily_article` VALUES (1, '111', '111', '111', NULL, NULL, '111', NULL, NULL, NULL, '文学', '散文', 1, NULL, 120, '2026-03-22', 5, 803, 1, '2026-03-22 16:37:10', '2026-05-05 15:45:36', 0);

-- ----------------------------
-- Table structure for daily_word
-- ----------------------------
DROP TABLE IF EXISTS `daily_word`;
CREATE TABLE `daily_word`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `word` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单词',
  `pronunciation` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '音标',
  `audio_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `translation` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '翻译',
  `example` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '例句',
  `example_translation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `difficulty` tinyint(4) NOT NULL DEFAULT 1 COMMENT '难度等级：1-简单，2-中等，3-困难',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单词分类（如名词、动词、商务英语等）',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '单词笔记或补充说明',
  `like_count` int(11) NOT NULL DEFAULT 0,
  `publish_date` date NOT NULL,
  `admin_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publish_date`(`publish_date`) USING BTREE,
  INDEX `idx_word`(`word`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日单词' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_word
-- ----------------------------
INSERT INTO `daily_word` VALUES (1, 'enthusiasm', 'ɪnˈθjuːziæzəm', NULL, 'n. 热情；热忱；热心；热衷的事物；激发热情的事物 记忆 en- 进入,使… + thus- 表示\"神\" + iasm', 'I can\'t say I share your enthusiasm for the idea.', '我可不像你那样，对这个想法那样热心。', 1, '生活', NULL, 1, '2026-03-22', 5, '2026-03-22 16:37:32', '2026-03-23 14:12:14', 0);
INSERT INTO `daily_word` VALUES (2, 'future', 'fjuːtʃə(r)', NULL, 'n. 未来；将来；期货；前景；前途；前程；（动词的）将来时，将来式， 未来式；未来的事；将来发生的事 adj. 将来的；未来的；将来发生的', 'We need to plan for the future.', '我们需要为将来作好打算。', 1, '生活', NULL, 0, '2026-03-22', 5, '2026-03-22 16:37:40', '2026-03-23 14:11:47', 0);
INSERT INTO `daily_word` VALUES (5, 'ability', '/əˈbɪləti/', NULL, '能力', 'He has the ability to solve problems.', '他有解决问题的能力。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:48', '2026-04-10 18:43:48', 0);
INSERT INTO `daily_word` VALUES (6, 'able', '/ˈeɪbl/', NULL, '能够的', 'She is able to finish the task.', '她能够完成任务。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:50', '2026-04-10 18:43:50', 0);
INSERT INTO `daily_word` VALUES (7, 'about', '/əˈbaʊt/', NULL, '关于', 'This book is about history.', '这本书是关于历史的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:50', '2026-04-10 18:43:50', 0);
INSERT INTO `daily_word` VALUES (8, 'above', '/əˈbʌv/', NULL, '在…之上', 'The plane flew above the clouds.', '飞机在云层之上飞行。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:50', '2026-04-10 18:43:50', 0);
INSERT INTO `daily_word` VALUES (9, 'accept', '/əkˈsept/', NULL, '接受', 'He accepted the invitation.', '他接受了邀请。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:51', '2026-04-10 18:43:51', 0);
INSERT INTO `daily_word` VALUES (10, 'according', '/əˈkɔːdɪŋ/', NULL, '根据', 'According to him, it\'s true.', '根据他说，这是真的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:51', '2026-04-10 18:43:51', 0);
INSERT INTO `daily_word` VALUES (11, 'account', '/əˈkaʊnt/', NULL, '账户', 'I opened a bank account.', '我开了一个银行账户。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:51', '2026-04-10 18:43:51', 0);
INSERT INTO `daily_word` VALUES (12, 'across', '/əˈkrɒs/', NULL, '穿过', 'She walked across the street.', '她穿过街道。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:52', '2026-04-10 18:43:52', 0);
INSERT INTO `daily_word` VALUES (13, 'act', '/ækt/', NULL, '行动', 'We must act quickly.', '我们必须迅速行动。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:52', '2026-04-10 18:43:52', 0);
INSERT INTO `daily_word` VALUES (14, 'action', '/ˈækʃn/', NULL, '行动', 'This movie is full of action.', '这部电影充满动作场面。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:52', '2026-04-10 18:43:52', 0);
INSERT INTO `daily_word` VALUES (15, 'activity', '/ækˈtɪvəti/', NULL, '活动', 'Outdoor activities are fun.', '户外活动很有趣。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:53', '2026-04-10 18:43:53', 0);
INSERT INTO `daily_word` VALUES (16, 'actually', '/ˈæktʃuəli/', NULL, '实际上', 'Actually, I disagree.', '实际上，我不同意。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:53', '2026-04-10 18:43:53', 0);
INSERT INTO `daily_word` VALUES (17, 'add', '/æd/', NULL, '增加', 'Add some sugar to the tea.', '往茶里加点糖。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:54', '2026-04-10 18:43:54', 0);
INSERT INTO `daily_word` VALUES (18, 'address', '/əˈdres/', NULL, '地址', 'Please write your address.', '请写下你的地址。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:54', '2026-04-10 18:43:54', 0);
INSERT INTO `daily_word` VALUES (19, 'administration', '/ədˌmɪnɪˈstreɪʃn/', NULL, '管理', 'The administration made a decision.', '管理层做出了决定。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:54', '2026-04-10 18:43:54', 0);
INSERT INTO `daily_word` VALUES (20, 'admit', '/ədˈmɪt/', NULL, '承认', 'He admitted his mistake.', '他承认了错误。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:55', '2026-04-10 18:43:55', 0);
INSERT INTO `daily_word` VALUES (21, 'adult', '/ˈædʌlt/', NULL, '成年人', 'Adults should be responsible.', '成年人应该负责任。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:55', '2026-04-10 18:43:55', 0);
INSERT INTO `daily_word` VALUES (22, 'affect', '/əˈfekt/', NULL, '影响', 'Weather affects mood.', '天气影响情绪。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:55', '2026-04-10 18:43:55', 0);
INSERT INTO `daily_word` VALUES (23, 'after', '/ˈɑːftə/', NULL, '之后', 'We will talk after class.', '我们课后再聊。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:56', '2026-04-10 18:43:56', 0);
INSERT INTO `daily_word` VALUES (24, 'again', '/əˈɡen/', NULL, '再次', 'Try again.', '再试一次。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:56', '2026-04-10 18:43:56', 0);
INSERT INTO `daily_word` VALUES (25, 'against', '/əˈɡenst/', NULL, '反对', 'He is against the plan.', '他反对这个计划。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:57', '2026-04-10 18:43:57', 0);
INSERT INTO `daily_word` VALUES (26, 'age', '/eɪdʒ/', NULL, '年龄', 'Age is just a number.', '年龄只是数字。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:57', '2026-04-10 18:43:57', 0);
INSERT INTO `daily_word` VALUES (27, 'ago', '/əˈɡəʊ/', NULL, '以前', 'He left two days ago.', '他两天前离开了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:57', '2026-04-10 18:43:57', 0);
INSERT INTO `daily_word` VALUES (28, 'agree', '/əˈɡriː/', NULL, '同意', 'I agree with you.', '我同意你。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:58', '2026-04-10 18:43:58', 0);
INSERT INTO `daily_word` VALUES (29, 'agreement', '/əˈɡriːmənt/', NULL, '协议', 'They reached an agreement.', '他们达成了协议。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:58', '2026-04-10 18:43:58', 0);
INSERT INTO `daily_word` VALUES (30, 'ahead', '/əˈhed/', NULL, '在前面', 'Go straight ahead.', '一直往前走。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:58', '2026-04-10 18:43:58', 0);
INSERT INTO `daily_word` VALUES (31, 'air', '/eə/', NULL, '空气', 'Air is important.', '空气很重要。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:58', '2026-04-10 18:43:58', 0);
INSERT INTO `daily_word` VALUES (32, 'all', '/ɔːl/', NULL, '全部', 'All students passed.', '所有学生都通过了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:59', '2026-04-10 18:43:59', 0);
INSERT INTO `daily_word` VALUES (33, 'allow', '/əˈlaʊ/', NULL, '允许', 'Smoking is not allowed.', '不允许吸烟。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:43:59', '2026-04-10 18:43:59', 0);
INSERT INTO `daily_word` VALUES (34, 'almost', '/ˈɔːlməʊst/', NULL, '几乎', 'I almost forgot.', '我差点忘了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:00', '2026-04-10 18:44:00', 0);
INSERT INTO `daily_word` VALUES (35, 'ability1', '/əˈbɪləti/', NULL, '能力', 'He has the ability to solve problems.', '他有解决问题的能力。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:00', '2026-04-10 18:44:00', 0);
INSERT INTO `daily_word` VALUES (36, 'able1', '/ˈeɪbl/', NULL, '能够的', 'She is able to finish the task.', '她能够完成任务。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:00', '2026-04-10 18:44:00', 0);
INSERT INTO `daily_word` VALUES (37, 'about1', '/əˈbaʊt/', NULL, '关于', 'This book is about history.', '这本书是关于历史的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:01', '2026-04-10 18:44:01', 0);
INSERT INTO `daily_word` VALUES (38, 'above1', '/əˈbʌv/', NULL, '在…之上', 'The plane flew above the clouds.', '飞机在云层之上飞行。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:01', '2026-04-10 18:44:01', 0);
INSERT INTO `daily_word` VALUES (39, 'accept1', '/əkˈsept/', NULL, '接受', 'He accepted the invitation.', '他接受了邀请。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:01', '2026-04-10 18:44:01', 0);
INSERT INTO `daily_word` VALUES (40, 'according1', '/əˈkɔːdɪŋ/', NULL, '根据', 'According to him, it\'s true.', '根据他说，这是真的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:02', '2026-04-10 18:44:02', 0);
INSERT INTO `daily_word` VALUES (41, 'account1', '/əˈkaʊnt/', NULL, '账户', 'I opened a bank account.', '我开了一个银行账户。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:02', '2026-04-10 18:44:02', 0);
INSERT INTO `daily_word` VALUES (42, 'across1', '/əˈkrɒs/', NULL, '穿过', 'She walked across the street.', '她穿过街道。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:02', '2026-04-10 18:44:02', 0);
INSERT INTO `daily_word` VALUES (43, 'act1', '/ækt/', NULL, '行动', 'We must act quickly.', '我们必须迅速行动。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:03', '2026-04-10 18:44:03', 0);
INSERT INTO `daily_word` VALUES (44, 'action1', '/ˈækʃn/', NULL, '行动', 'This movie is full of action.', '这部电影充满动作场面。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:03', '2026-04-10 18:44:03', 0);
INSERT INTO `daily_word` VALUES (45, 'activity1', '/ækˈtɪvəti/', NULL, '活动', 'Outdoor activities are fun.', '户外活动很有趣。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:03', '2026-04-10 18:44:03', 0);
INSERT INTO `daily_word` VALUES (46, 'actually1', '/ˈæktʃuəli/', NULL, '实际上', 'Actually, I disagree.', '实际上，我不同意。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:04', '2026-04-10 18:44:04', 0);
INSERT INTO `daily_word` VALUES (47, 'add1', '/æd/', NULL, '增加', 'Add some sugar to the tea.', '往茶里加点糖。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:04', '2026-04-10 18:44:04', 0);
INSERT INTO `daily_word` VALUES (48, 'address1', '/əˈdres/', NULL, '地址', 'Please write your address.', '请写下你的地址。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:04', '2026-04-10 18:44:04', 0);
INSERT INTO `daily_word` VALUES (49, 'administration1', '/ədˌmɪnɪˈstreɪʃn/', NULL, '管理', 'The administration made a decision.', '管理层做出了决定。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:05', '2026-04-10 18:44:05', 0);
INSERT INTO `daily_word` VALUES (50, 'admit1', '/ədˈmɪt/', NULL, '承认', 'He admitted his mistake.', '他承认了错误。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:05', '2026-04-10 18:44:05', 0);
INSERT INTO `daily_word` VALUES (51, 'adult1', '/ˈædʌlt/', NULL, '成年人', 'Adults should be responsible.', '成年人应该负责任。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:05', '2026-04-10 18:44:05', 0);
INSERT INTO `daily_word` VALUES (52, 'affect1', '/əˈfekt/', NULL, '影响', 'Weather affects mood.', '天气影响情绪。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:06', '2026-04-10 18:44:06', 0);
INSERT INTO `daily_word` VALUES (53, 'after1', '/ˈɑːftə/', NULL, '之后', 'We will talk after class.', '我们课后再聊。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:06', '2026-04-10 18:44:06', 0);
INSERT INTO `daily_word` VALUES (54, 'again1', '/əˈɡen/', NULL, '再次', 'Try again.', '再试一次。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:06', '2026-04-10 18:44:06', 0);
INSERT INTO `daily_word` VALUES (55, 'against1', '/əˈɡenst/', NULL, '反对', 'He is against the plan.', '他反对这个计划。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:07', '2026-04-10 18:44:07', 0);
INSERT INTO `daily_word` VALUES (56, 'age1', '/eɪdʒ/', NULL, '年龄', 'Age is just a number.', '年龄只是数字。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:07', '2026-04-10 18:44:07', 0);
INSERT INTO `daily_word` VALUES (57, 'ago1', '/əˈɡəʊ/', NULL, '以前', 'He left two days ago.', '他两天前离开了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:07', '2026-04-10 18:44:07', 0);
INSERT INTO `daily_word` VALUES (58, 'agree1', '/əˈɡriː/', NULL, '同意', 'I agree with you.', '我同意你。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:08', '2026-04-10 18:44:08', 0);
INSERT INTO `daily_word` VALUES (59, 'agreement1', '/əˈɡriːmənt/', NULL, '协议', 'They reached an agreement.', '他们达成了协议。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:08', '2026-04-10 18:44:08', 0);
INSERT INTO `daily_word` VALUES (60, 'ahead1', '/əˈhed/', NULL, '在前面', 'Go straight ahead.', '一直往前走。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:08', '2026-04-10 18:44:08', 0);
INSERT INTO `daily_word` VALUES (61, 'air1', '/eə/', NULL, '空气', 'Air is important.', '空气很重要。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:09', '2026-04-10 18:44:09', 0);
INSERT INTO `daily_word` VALUES (62, 'all1', '/ɔːl/', NULL, '全部', 'All students passed.', '所有学生都通过了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:09', '2026-04-10 18:44:09', 0);
INSERT INTO `daily_word` VALUES (63, 'allow1', '/əˈlaʊ/', NULL, '允许', 'Smoking is not allowed.', '不允许吸烟。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:09', '2026-04-10 18:44:09', 0);
INSERT INTO `daily_word` VALUES (64, 'almost1', '/ˈɔːlməʊst/', NULL, '几乎', 'I almost forgot.', '我差点忘了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:10', '2026-04-10 18:44:10', 0);
INSERT INTO `daily_word` VALUES (65, 'ability2', '/əˈbɪləti/', NULL, '能力', 'He has the ability to solve problems.', '他有解决问题的能力。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:10', '2026-04-10 18:44:10', 0);
INSERT INTO `daily_word` VALUES (66, 'able2', '/ˈeɪbl/', NULL, '能够的', 'She is able to finish the task.', '她能够完成任务。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:10', '2026-04-10 18:44:10', 0);
INSERT INTO `daily_word` VALUES (67, 'about2', '/əˈbaʊt/', NULL, '关于', 'This book is about history.', '这本书是关于历史的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:11', '2026-04-10 18:44:11', 0);
INSERT INTO `daily_word` VALUES (68, 'above2', '/əˈbʌv/', NULL, '在…之上', 'The plane flew above the clouds.', '飞机在云层之上飞行。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:11', '2026-04-10 18:44:11', 0);
INSERT INTO `daily_word` VALUES (69, 'accept2', '/əkˈsept/', NULL, '接受', 'He accepted the invitation.', '他接受了邀请。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:11', '2026-04-10 18:44:11', 0);
INSERT INTO `daily_word` VALUES (70, 'according2', '/əˈkɔːdɪŋ/', NULL, '根据', 'According to him, it\'s true.', '根据他说，这是真的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:12', '2026-04-10 18:44:12', 0);
INSERT INTO `daily_word` VALUES (71, 'account2', '/əˈkaʊnt/', NULL, '账户', 'I opened a bank account.', '我开了一个银行账户。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:12', '2026-04-10 18:44:12', 0);
INSERT INTO `daily_word` VALUES (72, 'across2', '/əˈkrɒs/', NULL, '穿过', 'She walked across the street.', '她穿过街道。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:12', '2026-04-10 18:44:12', 0);
INSERT INTO `daily_word` VALUES (73, 'act2', '/ækt/', NULL, '行动', 'We must act quickly.', '我们必须迅速行动。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:13', '2026-04-10 18:44:13', 0);
INSERT INTO `daily_word` VALUES (74, 'action2', '/ˈækʃn/', NULL, '行动', 'This movie is full of action.', '这部电影充满动作场面。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:13', '2026-04-10 18:44:13', 0);
INSERT INTO `daily_word` VALUES (75, 'activity2', '/ækˈtɪvəti/', NULL, '活动', 'Outdoor activities are fun.', '户外活动很有趣。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:13', '2026-04-10 18:44:13', 0);
INSERT INTO `daily_word` VALUES (76, 'actually2', '/ˈæktʃuəli/', NULL, '实际上', 'Actually, I disagree.', '实际上，我不同意。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:14', '2026-04-10 18:44:14', 0);
INSERT INTO `daily_word` VALUES (77, 'add2', '/æd/', NULL, '增加', 'Add some sugar to the tea.', '往茶里加点糖。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:14', '2026-04-10 18:44:14', 0);
INSERT INTO `daily_word` VALUES (78, 'address2', '/əˈdres/', NULL, '地址', 'Please write your address.', '请写下你的地址。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:14', '2026-04-10 18:44:14', 0);
INSERT INTO `daily_word` VALUES (79, 'administration2', '/ədˌmɪnɪˈstreɪʃn/', NULL, '管理', 'The administration made a decision.', '管理层做出了决定。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:15', '2026-04-10 18:44:15', 0);
INSERT INTO `daily_word` VALUES (80, 'admit2', '/ədˈmɪt/', NULL, '承认', 'He admitted his mistake.', '他承认了错误。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:15', '2026-04-10 18:44:15', 0);
INSERT INTO `daily_word` VALUES (81, 'adult2', '/ˈædʌlt/', NULL, '成年人', 'Adults should be responsible.', '成年人应该负责任。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:15', '2026-04-10 18:44:15', 0);
INSERT INTO `daily_word` VALUES (82, 'affect2', '/əˈfekt/', NULL, '影响', 'Weather affects mood.', '天气影响情绪。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:16', '2026-04-10 18:44:16', 0);
INSERT INTO `daily_word` VALUES (83, 'after2', '/ˈɑːftə/', NULL, '之后', 'We will talk after class.', '我们课后再聊。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:16', '2026-04-10 18:44:16', 0);
INSERT INTO `daily_word` VALUES (84, 'again2', '/əˈɡen/', NULL, '再次', 'Try again.', '再试一次。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:16', '2026-04-10 18:44:16', 0);
INSERT INTO `daily_word` VALUES (85, 'against2', '/əˈɡenst/', NULL, '反对', 'He is against the plan.', '他反对这个计划。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:17', '2026-04-10 18:44:17', 0);
INSERT INTO `daily_word` VALUES (86, 'age2', '/eɪdʒ/', NULL, '年龄', 'Age is just a number.', '年龄只是数字。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:17', '2026-04-10 18:44:17', 0);
INSERT INTO `daily_word` VALUES (87, 'ago2', '/əˈɡəʊ/', NULL, '以前', 'He left two days ago.', '他两天前离开了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:17', '2026-04-10 18:44:17', 0);
INSERT INTO `daily_word` VALUES (88, 'agree2', '/əˈɡriː/', NULL, '同意', 'I agree with you.', '我同意你。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:18', '2026-04-10 18:44:18', 0);
INSERT INTO `daily_word` VALUES (89, 'agreement2', '/əˈɡriːmənt/', NULL, '协议', 'They reached an agreement.', '他们达成了协议。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:18', '2026-04-10 18:44:18', 0);
INSERT INTO `daily_word` VALUES (90, 'ahead2', '/əˈhed/', NULL, '在前面', 'Go straight ahead.', '一直往前走。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:18', '2026-04-10 18:44:18', 0);
INSERT INTO `daily_word` VALUES (91, 'air2', '/eə/', NULL, '空气', 'Air is important.', '空气很重要。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:19', '2026-04-10 18:44:19', 0);
INSERT INTO `daily_word` VALUES (92, 'all2', '/ɔːl/', NULL, '全部', 'All students passed.', '所有学生都通过了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:19', '2026-04-10 18:44:19', 0);
INSERT INTO `daily_word` VALUES (93, 'allow2', '/əˈlaʊ/', NULL, '允许', 'Smoking is not allowed.', '不允许吸烟。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:19', '2026-04-10 18:44:19', 0);
INSERT INTO `daily_word` VALUES (94, 'almost2', '/ˈɔːlməʊst/', NULL, '几乎', 'I almost forgot.', '我差点忘了。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:20', '2026-04-10 18:44:20', 0);
INSERT INTO `daily_word` VALUES (95, 'ability3', '/əˈbɪləti/', NULL, '能力', 'He has the ability to solve problems.', '他有解决问题的能力。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:20', '2026-04-10 18:44:20', 0);
INSERT INTO `daily_word` VALUES (96, 'able3', '/ˈeɪbl/', NULL, '能够的', 'She is able to finish the task.', '她能够完成任务。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:20', '2026-04-10 18:44:20', 0);
INSERT INTO `daily_word` VALUES (97, 'about3', '/əˈbaʊt/', NULL, '关于', 'This book is about history.', '这本书是关于历史的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:21', '2026-04-10 18:44:21', 0);
INSERT INTO `daily_word` VALUES (98, 'above3', '/əˈbʌv/', NULL, '在…之上', 'The plane flew above the clouds.', '飞机在云层之上飞行。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:21', '2026-04-10 18:44:21', 0);
INSERT INTO `daily_word` VALUES (99, 'accept3', '/əkˈsept/', NULL, '接受', 'He accepted the invitation.', '他接受了邀请。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:22', '2026-04-10 18:44:22', 0);
INSERT INTO `daily_word` VALUES (100, 'according3', '/əˈkɔːdɪŋ/', NULL, '根据', 'According to him, it\'s true.', '根据他说，这是真的。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:22', '2026-04-10 18:44:22', 0);
INSERT INTO `daily_word` VALUES (101, 'account3', '/əˈkaʊnt/', NULL, '账户', 'I opened a bank account.', '我开了一个银行账户。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:22', '2026-04-10 18:44:22', 0);
INSERT INTO `daily_word` VALUES (102, 'across3', '/əˈkrɒs/', NULL, '穿过', 'She walked across the street.', '她穿过街道。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:22', '2026-04-10 18:44:22', 0);
INSERT INTO `daily_word` VALUES (103, 'act3', '/ækt/', NULL, '行动', 'We must act quickly.', '我们必须迅速行动。', 1, '四级词汇', 'CET4核心词', 0, '2026-04-10', 18, '2026-04-10 18:44:23', '2026-04-11 15:44:29', 0);
INSERT INTO `daily_word` VALUES (104, 'action3', '/ˈækʃn/', NULL, '行动', 'This movie is full of action.', '这部电影充满动作场面。', 1, '四级词汇', 'CET4核心词', 1, '2026-04-10', 18, '2026-04-10 18:44:23', '2026-04-10 22:58:40', 0);

-- ----------------------------
-- Table structure for friend_relationship
-- ----------------------------
DROP TABLE IF EXISTS `friend_relationship`;
CREATE TABLE `friend_relationship`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id1` bigint(20) NOT NULL COMMENT '用户1 ID',
  `user_id2` bigint(20) NOT NULL COMMENT '用户2 ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '好友状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_friend`(`user_id1`, `user_id2`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of friend_relationship
-- ----------------------------
INSERT INTO `friend_relationship` VALUES (1, 18, 17, 'accepted', '2026-04-10 20:23:32', '2026-04-10 20:23:32');
INSERT INTO `friend_relationship` VALUES (2, 17, 18, 'accepted', '2026-04-10 20:23:32', '2026-04-10 20:23:32');

-- ----------------------------
-- Table structure for friend_request
-- ----------------------------
DROP TABLE IF EXISTS `friend_request`;
CREATE TABLE `friend_request`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者 ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者 ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请状态',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请消息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of friend_request
-- ----------------------------
INSERT INTO `friend_request` VALUES (1, 18, 17, 'accepted', '我想添加你为好友', '2026-04-10 20:23:05', '2026-04-10 20:23:05');
INSERT INTO `friend_request` VALUES (2, 18, 19, 'pending', '我想添加你为好友', '2026-04-10 20:23:08', '2026-04-10 20:23:08');
INSERT INTO `friend_request` VALUES (3, 17, 19, 'pending', '我想添加你为好友', '2026-05-03 00:43:29', '2026-05-03 00:43:29');
INSERT INTO `friend_request` VALUES (4, 19, 18, 'pending', '我想添加你为好友', '2026-05-03 21:43:57', '2026-05-03 21:43:57');

-- ----------------------------
-- Table structure for goal_type
-- ----------------------------
DROP TABLE IF EXISTS `goal_type`;
CREATE TABLE `goal_type`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型编码',
  `icon` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标URL',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类',
  `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `default_value` int(11) NOT NULL DEFAULT 1,
  `min_value` int(11) NOT NULL DEFAULT 1,
  `max_value` int(11) NULL DEFAULT NULL,
  `points` int(11) NOT NULL DEFAULT 5 COMMENT '完成可获得积分',
  `experience` int(11) NOT NULL DEFAULT 10 COMMENT '完成可获得经验值',
  `is_system` tinyint(4) NOT NULL DEFAULT 1,
  `is_enabled` tinyint(4) NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_code`(`code`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习目标类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goal_type
-- ----------------------------

-- ----------------------------
-- Table structure for homework_submission
-- ----------------------------
DROP TABLE IF EXISTS `homework_submission`;
CREATE TABLE `homework_submission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提交 id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `section_id` bigint(20) NOT NULL COMMENT '小节 id',
  `question_id` bigint(20) NOT NULL COMMENT '题目 id',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户答案',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-未批改，1-已批改，2-需复查',
  `score` int(11) NULL DEFAULT 0 COMMENT '得分',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师评语',
  `graded_by` bigint(20) NULL DEFAULT NULL COMMENT '批改教师 id',
  `graded_time` datetime NULL DEFAULT NULL COMMENT '批改时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id`, `question_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_section_id`(`section_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_graded_by`(`graded_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作业提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_submission
-- ----------------------------
INSERT INTO `homework_submission` VALUES (1, 18, 2, 4, '11', 0, 0, '111', 18, '2026-04-07 19:38:53', '2026-04-07 19:38:26', '2026-04-07 19:38:26');
INSERT INTO `homework_submission` VALUES (2, 18, 3, 3, '11', 0, 0, NULL, NULL, NULL, '2026-04-08 12:13:06', '2026-04-08 12:13:06');

-- ----------------------------
-- Table structure for level_config
-- ----------------------------
DROP TABLE IF EXISTS `level_config`;
CREATE TABLE `level_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `level` int(11) NOT NULL COMMENT '等级',
  `required_exp` int(11) NOT NULL COMMENT '升级所需经验值',
  `privilege_desc` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '特权描述',
  `icon_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级图标 URL',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级名称',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序权重',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE,
  INDEX `idx_sort`(`sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '等级配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of level_config
-- ----------------------------
INSERT INTO `level_config` VALUES (1, 1, 0, '基础用户权限', '/icons/lv1.png', '初学者', 1, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (2, 2, 100, '解锁评论功能', '/icons/lv2.png', '学习者', 2, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (3, 3, 300, '解锁发帖功能', '/icons/lv3.png', '积极分子', 3, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (4, 4, 600, '解锁自定义头像', '/icons/lv4.png', '活跃达人', 4, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (5, 5, 1000, '解锁专属标识', '/icons/lv5.png', '学习达人', 5, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (6, 6, 1500, '解锁高级功能', '/icons/lv6.png', '知识精英', 6, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (7, 7, 2200, '解锁创作中心', '/icons/lv7.png', '意见领袖', 7, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (8, 8, 3000, '解锁管理权限', '/icons/lv8.png', '社区名人', 8, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (9, 9, 4000, '解锁专属客服', '/icons/lv9.png', '资深专家', 9, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);
INSERT INTO `level_config` VALUES (10, 10, 5500, '终身荣誉会员', '/icons/lv10.png', '传奇人物', 10, 1, '2026-04-02 16:04:53', '2026-04-02 16:04:53', 0);

-- ----------------------------
-- Table structure for like_record
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id`, `target_type`, `target_id`) USING BTREE,
  INDEX `idx_target`(`target_type`, `target_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like_record
-- ----------------------------

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签列表（json 数组）',
  `thumb_num` int(11) NOT NULL DEFAULT 0,
  `favour_num` int(11) NOT NULL DEFAULT 0,
  `comment_num` int(11) NOT NULL DEFAULT 0,
  `user_id` bigint(20) NOT NULL,
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子类型，如学习/生活/技巧',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 99481045 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (11629441, 'Demo notice', 'Demo post content', '[\"公告\"]', 0, 0, 2, 18, 'Demo', 'Example City', '2026-04-06 15:08:26', '2026-04-26 13:33:21', 0, 'article');
INSERT INTO `post` VALUES (30567277, '谁知道这道题怎么做', '不知道', '[\"问答\"]', 1, 1, 3, 6, 'HK', 'Hong Kong', '2026-03-22 16:23:17', '2026-03-27 15:34:39', 0, 'question');
INSERT INTO `post` VALUES (31296666, 'Demo question', 'This is a demo question for the discussion board.', '[\"问答\"]', 0, 0, 0, 18, 'Demo', 'Example City', '2026-04-05 16:58:12', '2026-04-05 16:58:12', 0, 'question');
INSERT INTO `post` VALUES (39189280, 'Demo homework question', 'This is a demo homework discussion.', '[\"问答\"]', 0, 0, 0, 5, 'Demo', 'Example City', '2026-04-02 21:56:00', '2026-04-02 21:56:00', 0, 'question');
INSERT INTO `post` VALUES (40629979, '震惊！某男子为了学数学竟然这样做', '黑油', '[\"教程\"]', 1, 1, 1, 7, '中国', 'Sichuan Chengdu', '2026-03-22 01:37:22', '2026-03-27 15:11:43', 0, 'article');
INSERT INTO `post` VALUES (41528700, '操作系统一二事', '22222222222222222222', '[\"建议\"]', 1, 1, 1, 5, '中国', 'Sichuan Chengdu', '2026-03-27 15:37:30', '2026-03-27 15:41:47', 0, 'discussion');
INSERT INTO `post` VALUES (87881800, '英语居然如此学！', '震惊，某男子学英语突发奇想...', '[\"公告\",\"教程\"]', 0, 0, 1, 5, '中国', 'Sichuan Chengdu', '2026-03-22 01:26:06', '2026-03-22 01:34:55', 0, 'article');
INSERT INTO `post` VALUES (89451826, '如果有一天物理占领地球', '黑吃黑吃', '[\"建议\"]', 1, 1, 1, 6, '中国', 'Sichuan Chengdu', '2026-03-22 01:39:12', '2026-03-24 16:37:04', 0, 'discussion');
INSERT INTO `post` VALUES (99481044, '111', '111222', '[\"公告\"]', 0, 1, 1, 5, 'Unknown', 'Unknown', '2026-04-02 22:43:11', '2026-04-08 12:10:52', 0, 'article');

-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_comment
-- ----------------------------
INSERT INTO `post_comment` VALUES (1, 87881800, 7, '666😄😄😄', '中国', 'Sichuan Chengdu', '2026-03-22 01:34:55', '2026-03-22 01:34:55', 0);
INSERT INTO `post_comment` VALUES (2, 40629979, 6, '^_^\\(^o^)/~', '中国', 'Sichuan Chengdu', '2026-03-22 01:38:32', '2026-03-22 01:38:32', 0);
INSERT INTO `post_comment` VALUES (3, 30567277, 5, '不晓得哇', '中国', 'Sichuan Chengdu', '2026-03-24 10:27:13', '2026-03-24 10:27:13', 0);
INSERT INTO `post_comment` VALUES (4, 89451826, 5, '111', '中国', 'Sichuan Chengdu', '2026-03-24 16:37:04', '2026-03-24 16:37:04', 0);
INSERT INTO `post_comment` VALUES (5, 30567277, 5, '111', '中国', 'Sichuan Chengdu', '2026-03-27 15:33:59', '2026-03-27 15:33:59', 0);
INSERT INTO `post_comment` VALUES (6, 30567277, 5, '1111', 'Unknown', 'Unknown', '2026-03-27 15:34:39', '2026-03-27 15:34:39', 0);
INSERT INTO `post_comment` VALUES (7, 41528700, 5, '好😁', '中国', 'Sichuan Chengdu', '2026-03-27 15:41:47', '2026-03-27 15:41:47', 0);
INSERT INTO `post_comment` VALUES (8, 99481044, 5, '咋啦', 'HK', 'Hong Kong', '2026-04-03 21:50:30', '2026-04-03 21:50:30', 0);
INSERT INTO `post_comment` VALUES (9, 11629441, 18, 'Demo comment', 'Demo', 'Example City', '2026-04-06 15:08:44', '2026-04-06 15:08:44', 0);
INSERT INTO `post_comment` VALUES (10, 11629441, 18, 'Demo comment 2', 'Demo', 'Example City', '2026-04-06 15:08:46', '2026-04-06 15:08:46', 0);

-- ----------------------------
-- Table structure for post_comment_reply
-- ----------------------------
DROP TABLE IF EXISTS `post_comment_reply`;
CREATE TABLE `post_comment_reply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `post_id` bigint(20) NOT NULL,
  `comment_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_reply_comment` FOREIGN KEY (`comment_id`) REFERENCES `post_comment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reply_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子评论回复' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_comment_reply
-- ----------------------------

-- ----------------------------
-- Table structure for post_favour
-- ----------------------------
DROP TABLE IF EXISTS `post_favour`;
CREATE TABLE `post_favour`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci COMMENT = '帖子收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_favour
-- ----------------------------
INSERT INTO `post_favour` VALUES (1, 30567277, 5, '2026-03-24 10:27:41', '2026-03-24 10:27:41');
INSERT INTO `post_favour` VALUES (2, 89451826, 5, '2026-03-24 16:36:49', '2026-03-24 16:36:49');
INSERT INTO `post_favour` VALUES (3, 40629979, 5, '2026-03-27 15:11:43', '2026-03-27 15:11:43');
INSERT INTO `post_favour` VALUES (4, 41528700, 5, '2026-03-27 15:41:25', '2026-03-27 15:41:25');
INSERT INTO `post_favour` VALUES (6, 99481044, 18, '2026-04-08 12:10:50', '2026-04-08 12:10:50');

-- ----------------------------
-- Table structure for post_thumb
-- ----------------------------
DROP TABLE IF EXISTS `post_thumb`;
CREATE TABLE `post_thumb`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci COMMENT = '帖子点赞' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_thumb
-- ----------------------------
INSERT INTO `post_thumb` VALUES (1, 30567277, 5, '2026-03-24 10:26:52', '2026-03-24 10:26:52');
INSERT INTO `post_thumb` VALUES (2, 89451826, 5, '2026-03-24 16:36:47', '2026-03-24 16:36:47');
INSERT INTO `post_thumb` VALUES (3, 40629979, 5, '2026-03-27 15:11:41', '2026-03-27 15:11:41');
INSERT INTO `post_thumb` VALUES (4, 41528700, 5, '2026-03-27 15:41:23', '2026-03-27 15:41:23');

-- ----------------------------
-- Table structure for private_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `private_chat_session`;
CREATE TABLE `private_chat_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id1` bigint(20) NOT NULL,
  `user_id2` bigint(20) NOT NULL,
  `last_message_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session`(`user_id1`, `user_id2`) USING BTREE,
  INDEX `idx_user_id1`(`user_id1`) USING BTREE,
  INDEX `idx_user_id2`(`user_id2`) USING BTREE,
  CONSTRAINT `fk_private_chat_session_user1` FOREIGN KEY (`user_id1`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_private_chat_session_user2` FOREIGN KEY (`user_id2`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of private_chat_session
-- ----------------------------
INSERT INTO `private_chat_session` VALUES (1, 5, 7, '2026-04-02 13:52:51', '2026-03-22 01:30:15', '2026-04-02 13:52:51', 0);

-- ----------------------------
-- Table structure for private_message
-- ----------------------------
DROP TABLE IF EXISTS `private_message`;
CREATE TABLE `private_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint(20) NOT NULL,
  `receiver_id` bigint(20) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_private_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_private_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私聊消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of private_message
-- ----------------------------
INSERT INTO `private_message` VALUES (1, 7, 5, '你好啊', 1, '2026-03-22 01:30:24', 0);
INSERT INTO `private_message` VALUES (2, 7, 5, '😀', 1, '2026-03-22 01:30:33', 0);
INSERT INTO `private_message` VALUES (3, 5, 7, '你会什么', 0, '2026-03-23 22:59:43', 0);
INSERT INTO `private_message` VALUES (4, 5, 7, 'hello', 0, '2026-04-02 13:52:51', 0);

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Question id',
  `section_id` bigint(20) NOT NULL COMMENT 'Section id',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'single/multiple/judge',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Question content',
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Question options JSON',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Correct answer',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Answer explanation',
  `score` int(11) NOT NULL DEFAULT 1 COMMENT 'Question score',
  `difficulty` tinyint(4) NOT NULL DEFAULT 1 COMMENT 'Difficulty 1/2/3',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Logic delete',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_section`(`section_id`) USING BTREE,
  INDEX `idx_question_sort`(`section_id`, `sort_order`) USING BTREE,
  CONSTRAINT `fk_question_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Question table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (1, 2, 'subjective', '44', NULL, '', NULL, 100, 1, 2, '2026-04-07 18:28:00', '2026-04-07 19:19:36', 1);
INSERT INTO `question` VALUES (2, 2, 'subjective', '11', NULL, '', NULL, 100, 1, 1, '2026-04-07 18:40:33', '2026-04-07 19:19:34', 1);
INSERT INTO `question` VALUES (3, 3, 'subjective', '11', NULL, '', NULL, 100, 1, 1, '2026-04-07 18:42:12', '2026-04-07 18:42:12', 0);
INSERT INTO `question` VALUES (4, 2, 'subjective', '11', NULL, '', NULL, 100, 1, 1, '2026-04-07 19:20:09', '2026-04-07 19:20:09', 0);

-- ----------------------------
-- Table structure for section
-- ----------------------------
DROP TABLE IF EXISTS `section`;
CREATE TABLE `section`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '小节 id',
  `chapter_id` bigint(20) NOT NULL COMMENT '章节 id',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '小节标题',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'video' COMMENT '小节类型：video/article/exercise',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图文内容',
  `video_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频 URL',
  `video_duration` int(11) NULL DEFAULT NULL COMMENT '视频时长（秒）',
  `resource_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源类型：URL / FILE',
  `resource_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源地址',
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容类型：VIDEO / ARTICLE / EXERCISE',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `is_free` tinyint(4) NULL DEFAULT 0 COMMENT '是否免费试看',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除',
  `ai_knowleage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI 学习助手知识内容',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  CONSTRAINT `fk_section_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程小节表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of section
-- ----------------------------
INSERT INTO `section` VALUES (1, 1, '第一节', 'video', NULL, NULL, 300, 'URL', 'https://www.bilibili.com/list/ml2605686279?oid=768653979&bvid=BV19r4y1b7Aw', 'VIDEO', 1, 0, '2026-04-05 17:35:27', '2026-04-07 18:38:06', 1, NULL);
INSERT INTO `section` VALUES (2, 1, '第一节', 'video', NULL, '/api/upload/course_video/demo-course-video-1.mp4', 300, 'FILE', '/api/upload/course_video/demo-course-video-1.mp4', 'VIDEO', 1, 0, '2026-04-07 15:19:44', '2026-04-07 19:19:49', 0, NULL);
INSERT INTO `section` VALUES (3, 2, '演示视频小节', 'video', NULL, '/api/upload/course_video/demo-course-video-2.mp4', 300, 'FILE', '/api/upload/course_video/demo-course-video-2.mp4', 'VIDEO', 1, 0, '2026-04-07 18:42:01', '2026-04-07 18:42:01', 0, NULL);
INSERT INTO `section` VALUES (4, 1, 'blender', 'video', NULL, 'https://www.bilibili.com/video/BV1d5osBuE2J/?spm_id_from=333.1007.tianma.2-1-3.click', 300, 'URL', 'https://www.bilibili.com/video/BV1d5osBuE2J/?spm_id_from=333.1007.tianma.2-1-3.click', '1', 2, 0, '2026-05-02 10:33:11', '2026-05-02 10:33:11', 0, NULL);
INSERT INTO `section` VALUES (5, 3, '向量的概念和线性运算', 'video', NULL, 'https://www.bilibili.com/video/BV18sRgBEES9/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', 300, 'URL', 'https://www.bilibili.com/video/BV18sRgBEES9/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', '1', 1, 0, '2026-05-02 21:40:04', '2026-05-02 21:40:04', 0, NULL);
INSERT INTO `section` VALUES (6, 3, '平面向量的坐标运算', 'video', NULL, 'https://www.bilibili.com/video/BV18sRgBEES9?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', 300, 'URL', 'https://www.bilibili.com/video/BV18sRgBEES9?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', '1', 1, 0, '2026-05-02 21:40:47', '2026-05-02 21:40:47', 0, NULL);
INSERT INTO `section` VALUES (7, 4, '向量的概念和线性运算', 'video', NULL, 'https://www.bilibili.com/video/BV18sRgBEES9?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', 300, 'URL', 'https://www.bilibili.com/video/BV18sRgBEES9?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', '1', 1, 0, '2026-05-02 21:50:31', '2026-05-02 21:50:31', 0, NULL);
INSERT INTO `section` VALUES (8, 5, 'day1', 'video', NULL, 'https://www.bilibili.com/video/BV1qa4y1V7nk/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', 300, 'URL', 'https://www.bilibili.com/video/BV1qa4y1V7nk/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', '1', 1, 0, '2026-05-02 21:54:46', '2026-05-02 21:54:46', 0, NULL);
INSERT INTO `section` VALUES (9, 6, '英雄联盟', 'video', NULL, 'https://www.bilibili.com/video/BV1qa4y1V7nk?spm_id_from=333.788.videopod.sections&vd_source=ce421cec09f70683c80e1101bf132642&p=16', 300, 'URL', 'https://www.bilibili.com/video/BV1qa4y1V7nk?spm_id_from=333.788.videopod.sections&vd_source=ce421cec09f70683c80e1101bf132642&p=16', '1', 1, 0, '2026-05-02 21:55:28', '2026-05-02 21:55:28', 0, NULL);
INSERT INTO `section` VALUES (10, 6, '沃格霍兹', 'video', NULL, 'https://www.bilibili.com/video/BV1qa4y1V7nk?spm_id_from=333.788.videopod.sections&vd_source=ce421cec09f70683c80e1101bf132642&p=17', 300, 'URL', 'https://www.bilibili.com/video/BV1qa4y1V7nk?spm_id_from=333.788.videopod.sections&vd_source=ce421cec09f70683c80e1101bf132642&p=17', '1', 1, 0, '2026-05-02 21:55:47', '2026-05-02 21:55:47', 0, NULL);
INSERT INTO `section` VALUES (11, 7, '奇奇怪怪', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty/?spm_id_from=333.337.search-card.all.click&vd_source=ce421cec09f70683c80e1101bf132642', '1', 1, 0, '2026-05-02 22:00:02', '2026-05-02 22:00:02', 0, NULL);
INSERT INTO `section` VALUES (12, 7, '课题大纲', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=2', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=2', '1', 1, 0, '2026-05-02 22:00:25', '2026-05-02 22:00:25', 0, NULL);
INSERT INTO `section` VALUES (13, 7, 'blender介绍', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=3', '1', 1, 0, '2026-05-02 22:00:50', '2026-05-02 22:00:50', 0, NULL);
INSERT INTO `section` VALUES (14, 7, '安装教程', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=4', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=4', '1', 1, 0, '2026-05-02 22:01:15', '2026-05-02 22:01:15', 0, NULL);
INSERT INTO `section` VALUES (15, 7, '总览', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=5', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=5', '1', 1, 0, '2026-05-02 22:01:33', '2026-05-02 22:01:33', 0, NULL);
INSERT INTO `section` VALUES (16, 7, '导航', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=6', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=6', '1', 1, 0, '2026-05-02 22:01:53', '2026-05-02 22:01:53', 0, NULL);
INSERT INTO `section` VALUES (17, 7, '导入', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=7', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=7', '1', 1, 0, '2026-05-02 22:02:10', '2026-05-02 22:02:10', 0, NULL);
INSERT INTO `section` VALUES (18, 8, '简介', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=8', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=8', '1', 1, 0, '2026-05-02 22:02:36', '2026-05-02 22:02:36', 0, NULL);
INSERT INTO `section` VALUES (19, 8, '时间轴和动画', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=9', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=9', '1', 1, 0, '2026-05-02 22:02:56', '2026-05-02 22:02:56', 0, NULL);
INSERT INTO `section` VALUES (20, 8, '姿态模式', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=10', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=10', '1', 1, 0, '2026-05-02 22:03:11', '2026-05-02 22:03:11', 0, NULL);
INSERT INTO `section` VALUES (21, 8, '动画摄影表', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=11', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=11', '1', 1, 0, '2026-05-02 22:03:49', '2026-05-02 22:03:49', 0, NULL);
INSERT INTO `section` VALUES (22, 8, '曲线编辑器', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=12', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=12', '1', 1, 0, '2026-05-02 22:04:06', '2026-05-02 22:04:06', 0, NULL);
INSERT INTO `section` VALUES (23, 8, '相机', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=13', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=13', '1', 1, 0, '2026-05-02 22:04:25', '2026-05-02 22:04:25', 0, NULL);
INSERT INTO `section` VALUES (24, 8, '绑定设置', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=14', 300, 'FILE', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=14', 'VIDEO', 1, 0, '2026-05-02 22:04:39', '2026-05-02 22:05:09', 0, NULL);
INSERT INTO `section` VALUES (25, 8, '第一个姿势', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=15', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=15', '1', 1, 0, '2026-05-02 22:04:53', '2026-05-02 22:04:53', 0, NULL);
INSERT INTO `section` VALUES (26, 8, '添加帧动作', 'video', NULL, 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=16', 300, 'URL', 'https://www.bilibili.com/video/BV1Ek4y1a7Ty?spm_id_from=333.788.videopod.episodes&vd_source=ce421cec09f70683c80e1101bf132642&p=16', '1', 1, 0, '2026-05-02 22:05:31', '2026-05-02 22:05:31', 0, NULL);

-- ----------------------------
-- Table structure for subject_ai_binding
-- ----------------------------
DROP TABLE IF EXISTS `subject_ai_binding`;
CREATE TABLE `subject_ai_binding`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint(20) NOT NULL COMMENT '课程分类/学科ID',
  `ai_avatar_id` bigint(20) NOT NULL COMMENT '本地AI助手ID',
  `dify_dataset_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Dify知识库ID',
  `dify_workflow_app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify工作流App Key',
  `dify_workflow_base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Dify工作流基础地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_id`(`category_id`) USING BTREE,
  INDEX `idx_ai_avatar_id`(`ai_avatar_id`) USING BTREE,
  INDEX `idx_dataset_id`(`dify_dataset_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学科助手与Dify知识库绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subject_ai_binding
-- ----------------------------

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '讲师姓名',
  `avatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '讲师头像URL',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '讲师职称',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '讲师简介',
  `expertise` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业领域，JSON数组格式',
  `user_id` bigint(20) NULL DEFAULT NULL,
  `admin_id` bigint(20) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '讲师' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, 'Demo Teacher', NULL, '数学老师', '1111111111111111111111', '数学', 19, 18, '2026-04-07 13:45:50', '2026-04-07 13:45:50', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_account` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `user_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `user_gender` int(11) NULL DEFAULT NULL COMMENT '性别 0-男 1-女 2-保密',
  `user_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `user_avatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `user_profile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `email` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'student' COMMENT '用户角色：student-学生，teacher-讲师，admin-管理员',
  `level` int(11) NULL DEFAULT 1 COMMENT '等级',
  `exp` int(11) NULL DEFAULT 0 COMMENT '经验值',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `union_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `mp_open_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `wechat_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `birthday` datetime NULL DEFAULT NULL COMMENT '生日',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_account`(`user_account`) USING BTREE,
  INDEX `idx_user_name`(`user_name`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'student1', 'b94c5eac1d6a4c6c8e0e1f8f1f8f1f8f', NULL, '学生 1', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-04 01:36:45', '2026-04-06 15:01:33', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (2, 'teacher1', 'b94c5eac1d6a4c6c8e0e1f8f1f8f1f8f', NULL, '教师 1', NULL, NULL, NULL, NULL, 'teacher', 1, 0, '2026-04-04 01:36:45', '2026-04-07 18:55:37', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (3, 'admin1', 'b94c5eac1d6a4c6c8e0e1f8f1f8f1f8f', NULL, '管理员 1', NULL, NULL, NULL, NULL, 'admin', 1, 0, '2026-04-04 01:36:45', '2026-04-07 18:55:34', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (4, 'demo_student_04', 'b0dd3697a192885d7c055db46155b26a', NULL, NULL, NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 00:31:16', '2026-04-06 15:01:37', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (5, 'demo_student_05', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_05', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:16:55', '2026-04-06 15:01:39', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (6, 'demo_student_06', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_06', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:16:55', '2026-04-06 15:01:41', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (7, 'demo_student_07', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_07', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:17:52', '2026-04-06 15:01:43', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (8, 'demo_student_08', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_08', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:18:37', '2026-04-06 15:01:45', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (9, 'demo_student_09', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_09', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:18:37', '2026-04-06 15:02:15', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (10, 'demo_student_10', '8e1d316d2966b5d5c891249399321ba9', NULL, 'demo_student_10', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:18:37', '2026-04-06 15:03:00', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (11, 'demo_student_11', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_11', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:39:11', '2026-04-07 18:55:32', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (12, 'demo_student_12', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_12', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:39:12', '2026-04-07 18:55:29', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (13, 'demo_student_13', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_13', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:39:12', '2026-04-07 18:55:26', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (14, 'demo_student_14', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_14', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:40:29', '2026-04-07 18:55:23', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (15, 'demo_student_15', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_15', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:40:29', '2026-04-06 15:02:37', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (16, 'demo_student_16', 'd254058d3c00a0ba4ba9db16b784e3eb', NULL, 'demo_student_16', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-05 01:40:29', '2026-04-06 15:02:19', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (17, 'demo_student_avatar', '276e813b4af93648fb215929ffce80cc', 0, 'Demo Student Avatar', '/api/upload/user_avatar/demo-user-avatar.jpg', '', '', '', 'student', 1, 0, '2026-04-05 04:16:54', '2026-05-03 00:44:28', 0, NULL, NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (18, 'demo_admin', '276e813b4af93648fb215929ffce80cc', 0, 'Demo Admin', '/default.jpg', '', '', '', 'admin', 1, 0, '2026-04-05 04:20:29', '2026-04-10 12:07:35', 0, NULL, NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (19, 'demo_teacher', '276e813b4af93648fb215929ffce80cc', 1, 'Demo Teacher', '', '', '', '', 'teacher', 1, 0, '2026-04-05 04:20:51', '2026-04-05 12:05:13', 0, NULL, NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (20, 'demo_student_20', 'b0dd3697a192885d7c055db46155b26a', NULL, 'demo_student_20', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-07 16:30:36', '2026-04-07 18:55:15', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` VALUES (21, 'demo_student_21', 'b0dd3697a192885d7c055db46155b26a', NULL, 'demo_student_21', NULL, NULL, NULL, NULL, 'student', 1, 0, '2026-04-07 16:31:34', '2026-04-07 18:55:19', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for user_achievement
-- ----------------------------
DROP TABLE IF EXISTS `user_achievement`;
CREATE TABLE `user_achievement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `achievement_id` bigint(20) NOT NULL COMMENT '成就 id',
  `progress` int(11) NOT NULL DEFAULT 0 COMMENT '当前进度值',
  `progress_max` int(11) NOT NULL DEFAULT 1 COMMENT '目标进度值',
  `progress_percent` int(11) NULL DEFAULT 0 COMMENT '进度百分比',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `reward_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已领取奖励',
  `is_delete` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_achievement`(`user_id`, `achievement_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_completed`(`is_completed`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户成就' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_achievement
-- ----------------------------

-- ----------------------------
-- Table structure for user_achievement_display
-- ----------------------------
DROP TABLE IF EXISTS `user_achievement_display`;
CREATE TABLE `user_achievement_display`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `achievement_id` bigint(20) NOT NULL,
  `achievement_display_id` bigint(20) NOT NULL,
  `is_enabled` tinyint(4) NOT NULL DEFAULT 1,
  `is_pinned` tinyint(4) NOT NULL DEFAULT 0,
  `custom_title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `custom_image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `display_count` int(11) NOT NULL DEFAULT 0,
  `last_display_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_achievement_display`(`user_id`, `achievement_id`, `achievement_display_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_achievement_id`(`achievement_id`) USING BTREE,
  INDEX `idx_achievement_display_id`(`achievement_display_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户成就展示记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_achievement_display
-- ----------------------------

-- ----------------------------
-- Table structure for user_ai_avatar
-- ----------------------------
DROP TABLE IF EXISTS `user_ai_avatar`;
CREATE TABLE `user_ai_avatar`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NULL DEFAULT NULL,
  `ai_avatar_id` bigint(20) NULL DEFAULT NULL,
  `is_favorite` tinyint(4) NULL DEFAULT NULL,
  `last_use_time` datetime NULL DEFAULT NULL,
  `use_count` int(11) NULL DEFAULT NULL,
  `user_rating` decimal(2, 1) NULL DEFAULT NULL,
  `user_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `custom_settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_avatar`(`user_id`, `ai_avatar_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_ai_avatar_id`(`ai_avatar_id`) USING BTREE,
  INDEX `idx_is_favorite`(`is_favorite`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户AI分身关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_ai_avatar
-- ----------------------------
INSERT INTO `user_ai_avatar` VALUES (1, 5, 1, 0, '2026-04-02 13:48:53', 15, NULL, NULL, NULL, '2026-03-20 21:01:56', '2026-04-02 13:48:53');
INSERT INTO `user_ai_avatar` VALUES (2, 7, 1, 0, '2026-03-22 01:23:45', 1, NULL, NULL, NULL, '2026-03-22 01:23:45', '2026-03-22 01:23:45');
INSERT INTO `user_ai_avatar` VALUES (3, 6, 1, 0, '2026-03-22 16:23:55', 2, NULL, NULL, NULL, '2026-03-22 01:39:21', '2026-03-22 16:23:55');
INSERT INTO `user_ai_avatar` VALUES (4, 18, 1, 0, '2026-05-02 21:06:14', 20, NULL, NULL, NULL, '2026-04-08 01:04:06', '2026-05-02 21:06:14');
INSERT INTO `user_ai_avatar` VALUES (5, 18, 4, 0, '2026-05-03 00:33:31', 19, NULL, NULL, NULL, '2026-04-10 03:43:00', '2026-05-03 00:33:31');
INSERT INTO `user_ai_avatar` VALUES (6, 18, 3, 0, '2026-04-10 15:09:26', 3, NULL, NULL, NULL, '2026-04-10 04:09:02', '2026-04-10 15:09:26');
INSERT INTO `user_ai_avatar` VALUES (7, 18, 2, 0, '2026-05-01 17:08:36', 11, NULL, NULL, NULL, '2026-04-10 04:14:59', '2026-05-01 17:08:36');
INSERT INTO `user_ai_avatar` VALUES (8, 17, 2, 0, '2026-04-10 19:33:51', 1, NULL, NULL, NULL, '2026-04-10 19:33:51', '2026-04-10 19:33:51');

-- ----------------------------
-- Table structure for user_announcement_reader
-- ----------------------------
DROP TABLE IF EXISTS `user_announcement_reader`;
CREATE TABLE `user_announcement_reader`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `announcement_id` bigint(20) NOT NULL,
  `read_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_announcement`(`user_id`, `announcement_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_announcement_id`(`announcement_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户公告阅读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_announcement_reader
-- ----------------------------

-- ----------------------------
-- Table structure for user_answer
-- ----------------------------
DROP TABLE IF EXISTS `user_answer`;
CREATE TABLE `user_answer`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'User answer id',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `question_id` bigint(20) NOT NULL COMMENT 'Question id',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'User answer',
  `is_correct` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Is correct',
  `score` int(11) NOT NULL DEFAULT 0 COMMENT 'Score',
  `time_spent` int(11) NULL DEFAULT 0 COMMENT 'Time spent in seconds',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question_answer`(`user_id`, `question_id`) USING BTREE,
  INDEX `idx_user_answer_user`(`user_id`) USING BTREE,
  INDEX `idx_user_answer_question`(`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User answer table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_answer
-- ----------------------------

-- ----------------------------
-- Table structure for user_article_record
-- ----------------------------
DROP TABLE IF EXISTS `user_article_record`;
CREATE TABLE `user_article_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `article_id` bigint(20) NOT NULL,
  `read_status` tinyint(4) NOT NULL DEFAULT 0,
  `read_progress` int(11) NOT NULL DEFAULT 0,
  `is_liked` tinyint(4) NOT NULL DEFAULT 0,
  `user_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_article`(`user_id`, `article_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户文章阅读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_article_record
-- ----------------------------

-- ----------------------------
-- Table structure for user_course
-- ----------------------------
DROP TABLE IF EXISTS `user_course`;
CREATE TABLE `user_course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `course_id` bigint(20) NOT NULL COMMENT '课程 id',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-学习中，1-已完成，2-已放弃',
  `progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '课程总进度百分比',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始学习时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `last_learn_time` datetime NULL DEFAULT NULL COMMENT '最后学习时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  CONSTRAINT `fk_user_course_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_course_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户课程关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_course
-- ----------------------------
INSERT INTO `user_course` VALUES (1, 18, 1, 1, 100.00, '2026-04-06 15:07:21', '2026-04-08 12:13:07', '2026-05-03 21:42:55', '2026-04-06 15:07:20', '2026-04-06 15:07:20', 0);
INSERT INTO `user_course` VALUES (2, 17, 1, 0, 0.00, '2026-04-10 19:35:35', NULL, '2026-04-10 19:36:24', '2026-04-10 19:35:35', '2026-04-10 19:35:35', 0);
INSERT INTO `user_course` VALUES (3, 18, 2, 0, 0.00, '2026-05-02 21:44:07', NULL, '2026-05-02 21:52:04', '2026-05-02 21:44:06', '2026-05-02 21:44:06', 0);
INSERT INTO `user_course` VALUES (4, 18, 4, 0, 0.00, '2026-05-03 00:34:02', NULL, '2026-05-03 00:34:39', '2026-05-03 00:34:01', '2026-05-03 00:34:01', 0);

-- ----------------------------
-- Table structure for user_course_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_course_progress`;
CREATE TABLE `user_course_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `section_id` bigint(20) NOT NULL,
  `progress` int(11) NOT NULL DEFAULT 0 COMMENT '学习进度(百分比)',
  `watch_duration` int(11) NOT NULL DEFAULT 0,
  `last_position` int(11) NOT NULL DEFAULT 0,
  `is_completed` tinyint(4) NOT NULL DEFAULT 0,
  `completed_time` datetime NULL DEFAULT NULL,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_section`(`user_id`, `section_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_section_id`(`section_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_course_progress
-- ----------------------------

-- ----------------------------
-- Table structure for user_daily_article
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_article`;
CREATE TABLE `user_daily_article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `article_id` bigint(20) NOT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `read_time` datetime NULL DEFAULT NULL,
  `is_liked` tinyint(4) NOT NULL DEFAULT 0,
  `like_time` datetime NULL DEFAULT NULL,
  `is_collected` tinyint(4) NOT NULL DEFAULT 0,
  `collect_time` datetime NULL DEFAULT NULL,
  `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `comment_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_article`(`user_id`, `article_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户与每日文章关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_daily_article
-- ----------------------------
INSERT INTO `user_daily_article` VALUES (1, 5, 1, 0, NULL, 1, '2026-03-25 22:06:31', 0, '2026-03-25 22:06:35', NULL, NULL, '2026-03-22 17:16:37', '2026-03-25 22:06:31');

-- ----------------------------
-- Table structure for user_daily_goal
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_goal`;
CREATE TABLE `user_daily_goal`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `goal_date` date NOT NULL,
  `total_goals` int(11) NOT NULL DEFAULT 0,
  `completed_goals` int(11) NOT NULL DEFAULT 0,
  `progress_percent` int(11) NOT NULL DEFAULT 0,
  `is_completed` tinyint(4) NOT NULL DEFAULT 0,
  `completed_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id`, `goal_date`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_goal_date`(`goal_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户每日学习目标' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_daily_goal
-- ----------------------------
INSERT INTO `user_daily_goal` VALUES (3, 18, '2026-05-02', 4, 0, 0, 0, '2026-05-02 20:42:33', '2026-05-02 20:06:49', '2026-05-02 23:53:16');
INSERT INTO `user_daily_goal` VALUES (4, 18, '2026-05-03', 5, 4, 80, 0, '2026-05-03 00:37:07', '2026-05-03 00:04:04', '2026-05-03 21:43:16');
INSERT INTO `user_daily_goal` VALUES (5, 17, '2026-05-03', 1, 0, 0, 0, NULL, '2026-05-03 00:43:43', '2026-05-03 12:06:14');
INSERT INTO `user_daily_goal` VALUES (6, 19, '2026-05-03', 0, 0, 0, 0, NULL, '2026-05-03 21:44:14', '2026-05-03 21:44:14');

-- ----------------------------
-- Table structure for user_daily_goal_item
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_goal_item`;
CREATE TABLE `user_daily_goal_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `goal_id` bigint(20) NOT NULL COMMENT '每日目标汇总id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `goal_date` date NOT NULL COMMENT '目标日期',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标标题',
  `goal_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CUSTOM' COMMENT '目标类型：WORD/ARTICLE/COURSE/LISTEN/CUSTOM',
  `target_value` int(11) NOT NULL DEFAULT 1 COMMENT '目标值',
  `current_value` int(11) NOT NULL DEFAULT 0 COMMENT '当前完成值',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单位',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未完成 1已完成',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM/CARRY_OVER/CUSTOM',
  `carry_from_item_id` bigint(20) NULL DEFAULT NULL COMMENT '继承自哪条旧任务',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goal_id`(`goal_id`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `goal_date`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户每日学习目标明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_daily_goal_item
-- ----------------------------
INSERT INTO `user_daily_goal_item` VALUES (1, 3, 18, '2026-05-02', '完成每日单词打卡', 'WORD', 1, 1, '个', 0, 'SYSTEM', NULL, '2026-05-02 20:42:33', '2026-05-02 20:06:49', '2026-05-02 20:42:34', 0);
INSERT INTO `user_daily_goal_item` VALUES (2, 3, 18, '2026-05-02', '阅读英语文章一篇', 'ARTICLE', 1, 1, '篇', 0, 'SYSTEM', NULL, '2026-05-02 20:42:32', '2026-05-02 20:06:49', '2026-05-02 20:42:35', 0);
INSERT INTO `user_daily_goal_item` VALUES (3, 3, 18, '2026-05-02', '完成一节课程学习', 'COURSE', 1, 1, '节', 0, 'SYSTEM', NULL, '2026-05-02 20:07:54', '2026-05-02 20:06:49', '2026-05-02 20:42:36', 0);
INSERT INTO `user_daily_goal_item` VALUES (4, 3, 18, '2026-05-02', '改用户端前端', 'CUSTOM', 1, 1, '60min', 0, 'CUSTOM', NULL, '2026-05-02 20:07:51', '2026-05-02 20:06:49', '2026-05-02 20:42:37', 0);
INSERT INTO `user_daily_goal_item` VALUES (5, 4, 18, '2026-05-03', '完成每日单词打卡', 'WORD', 1, 1, '个', 1, 'CARRY_OVER', 1, '2026-05-03 00:37:04', '2026-05-03 00:04:04', '2026-05-03 00:37:04', 0);
INSERT INTO `user_daily_goal_item` VALUES (6, 4, 18, '2026-05-03', '阅读英语文章一篇', 'ARTICLE', 1, 1, '篇', 1, 'CARRY_OVER', 2, '2026-05-03 00:37:07', '2026-05-03 00:04:04', '2026-05-03 00:37:07', 0);
INSERT INTO `user_daily_goal_item` VALUES (7, 4, 18, '2026-05-03', '完成一节课程学习', 'COURSE', 1, 1, '节', 1, 'CARRY_OVER', 3, '2026-05-03 00:37:06', '2026-05-03 00:04:04', '2026-05-03 00:37:06', 0);
INSERT INTO `user_daily_goal_item` VALUES (8, 4, 18, '2026-05-03', '改用户端前端', 'CUSTOM', 1, 1, '60min', 1, 'CARRY_OVER', 4, '2026-05-03 00:37:07', '2026-05-03 00:04:04', '2026-05-03 00:37:07', 0);
INSERT INTO `user_daily_goal_item` VALUES (9, 4, 18, '2026-05-03', '改论文', 'CUSTOM', 5, 0, '180min', 0, 'CUSTOM', NULL, NULL, '2026-05-03 00:37:30', '2026-05-03 00:37:30', 0);
INSERT INTO `user_daily_goal_item` VALUES (10, 5, 17, '2026-05-03', '改论文', 'CUSTOM', 1, 0, '180min', 0, 'CUSTOM', NULL, NULL, '2026-05-03 00:45:28', '2026-05-03 00:45:28', 0);

-- ----------------------------
-- Table structure for user_daily_word
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_word`;
CREATE TABLE `user_daily_word`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `word_id` bigint(20) NOT NULL,
  `is_studied` tinyint(4) NOT NULL DEFAULT 0,
  `study_time` datetime NULL DEFAULT NULL,
  `is_liked` tinyint(4) NOT NULL DEFAULT 0,
  `like_time` datetime NULL DEFAULT NULL,
  `is_collected` tinyint(4) NOT NULL DEFAULT 0,
  `collect_time` datetime NULL DEFAULT NULL,
  `note_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `note_time` datetime NULL DEFAULT NULL,
  `mastery_level` tinyint(4) NOT NULL DEFAULT 0,
  `is_wrong` tinyint(4) NOT NULL DEFAULT 0,
  `wrong_time` datetime NULL DEFAULT NULL,
  `review_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_word`(`user_id`, `word_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_word_id`(`word_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户与每日单词关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_daily_word
-- ----------------------------
INSERT INTO `user_daily_word` VALUES (1, 5, 1, 0, NULL, 1, '2026-03-23 13:50:51', 0, NULL, NULL, NULL, 0, 0, NULL, NULL, '2026-03-23 13:50:51', '2026-03-23 13:50:51');
INSERT INTO `user_daily_word` VALUES (2, 18, 104, 0, NULL, 1, '2026-04-10 22:58:41', 0, NULL, NULL, NULL, 0, 0, NULL, NULL, '2026-04-10 22:07:11', '2026-04-10 22:58:41');
INSERT INTO `user_daily_word` VALUES (3, 18, 103, 0, NULL, 0, '2026-04-11 15:44:28', 0, NULL, NULL, NULL, 0, 0, NULL, NULL, '2026-04-10 22:11:11', '2026-04-11 15:44:29');

-- ----------------------------
-- Table structure for user_exp_record
-- ----------------------------
DROP TABLE IF EXISTS `user_exp_record`;
CREATE TABLE `user_exp_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `action_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型',
  `exp_change` int(11) NOT NULL COMMENT '经验变化值',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `related_id` bigint(20) NULL DEFAULT NULL COMMENT '关联业务 ID',
  `related_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作 IP 地址',
  `device_info` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息',
  `is_delete` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_action_type`(`action_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '经验记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_exp_record
-- ----------------------------
INSERT INTO `user_exp_record` VALUES (28, 5, 'chat_send_text', 1, '发送文本消息', 19, 'chat_message', NULL, NULL, 0, '2026-04-03 12:54:18');
INSERT INTO `user_exp_record` VALUES (29, 5, 'chat_send_text', 1, '发送文本消息', 20, 'chat_message', NULL, NULL, 0, '2026-04-03 12:55:27');
INSERT INTO `user_exp_record` VALUES (30, 5, 'chat_send_text', 1, '发送文本消息', 21, 'chat_message', NULL, NULL, 0, '2026-04-03 12:56:37');
INSERT INTO `user_exp_record` VALUES (31, 5, 'chat_send_text', 1, '发送文本消息', 22, 'chat_message', NULL, NULL, 0, '2026-04-03 12:59:27');
INSERT INTO `user_exp_record` VALUES (32, 5, 'chat_send_text', 1, '发送文本消息', 23, 'chat_message', NULL, NULL, 0, '2026-04-03 13:02:47');
INSERT INTO `user_exp_record` VALUES (33, 5, 'chat_send_text', 1, '发送文本消息', 24, 'chat_message', NULL, NULL, 0, '2026-04-03 13:03:09');
INSERT INTO `user_exp_record` VALUES (34, 5, 'chat_send_text', 1, '发送文本消息', 25, 'chat_message', NULL, NULL, 0, '2026-04-03 13:12:52');
INSERT INTO `user_exp_record` VALUES (35, 5, 'chat_send_text', 1, '发送文本消息', 26, 'chat_message', NULL, NULL, 0, '2026-04-03 13:15:05');
INSERT INTO `user_exp_record` VALUES (36, 5, 'chat_send_text', 1, '发送文本消息', 27, 'chat_message', NULL, NULL, 0, '2026-04-03 13:21:57');
INSERT INTO `user_exp_record` VALUES (37, 5, 'chat_send_text', 1, '发送文本消息', 28, 'chat_message', NULL, NULL, 0, '2026-04-03 13:22:03');
INSERT INTO `user_exp_record` VALUES (38, 5, 'chat_send_text', 1, '发送文本消息', 29, 'chat_message', NULL, NULL, 0, '2026-04-03 13:23:49');
INSERT INTO `user_exp_record` VALUES (39, 5, 'chat_send_text', 1, '发送文本消息', 30, 'chat_message', NULL, NULL, 0, '2026-04-03 13:25:19');
INSERT INTO `user_exp_record` VALUES (40, 5, 'chat_send_text', 1, '发送文本消息', 31, 'chat_message', NULL, NULL, 0, '2026-04-03 15:16:40');
INSERT INTO `user_exp_record` VALUES (41, 7, 'chat_send_text', 1, '发送文本消息', 32, 'chat_message', NULL, NULL, 0, '2026-04-03 15:50:55');
INSERT INTO `user_exp_record` VALUES (42, 5, 'chat_send_text', 1, '发送文本消息', 33, 'chat_message', NULL, NULL, 0, '2026-04-03 16:02:50');
INSERT INTO `user_exp_record` VALUES (43, 5, 'chat_send_text', 1, '发送文本消息', 34, 'chat_message', NULL, NULL, 0, '2026-04-03 16:07:28');
INSERT INTO `user_exp_record` VALUES (44, 5, 'chat_send_text', 1, '发送文本消息', 35, 'chat_message', NULL, NULL, 0, '2026-04-03 16:10:37');
INSERT INTO `user_exp_record` VALUES (45, 5, 'chat_send_text', 1, '发送文本消息', 36, 'chat_message', NULL, NULL, 0, '2026-04-03 16:14:47');
INSERT INTO `user_exp_record` VALUES (46, 5, 'chat_send_text', 1, '发送文本消息', 37, 'chat_message', NULL, NULL, 0, '2026-04-03 16:24:32');
INSERT INTO `user_exp_record` VALUES (47, 5, 'chat_send_text', 1, '发送文本消息', 38, 'chat_message', NULL, NULL, 0, '2026-04-03 16:34:48');
INSERT INTO `user_exp_record` VALUES (48, 7, 'chat_send_text', 1, '发送文本消息', 39, 'chat_message', NULL, NULL, 0, '2026-04-03 16:49:54');
INSERT INTO `user_exp_record` VALUES (49, 6, 'chat_send_text', 1, '发送文本消息', 40, 'chat_message', NULL, NULL, 0, '2026-04-03 16:51:39');
INSERT INTO `user_exp_record` VALUES (50, 5, 'chat_send_text', 1, '发送文本消息', 41, 'chat_message', NULL, NULL, 0, '2026-04-03 17:15:14');
INSERT INTO `user_exp_record` VALUES (51, 7, 'chat_send_text', 1, '发送文本消息', 42, 'chat_message', NULL, NULL, 0, '2026-04-03 17:19:22');
INSERT INTO `user_exp_record` VALUES (52, 6, 'chat_send_text', 1, '发送文本消息', 43, 'chat_message', NULL, NULL, 0, '2026-04-03 18:15:01');
INSERT INTO `user_exp_record` VALUES (53, 7, 'chat_send_text', 1, '发送文本消息', 44, 'chat_message', NULL, NULL, 0, '2026-04-03 19:09:38');
INSERT INTO `user_exp_record` VALUES (54, 5, 'chat_send_text', 1, '发送文本消息', 45, 'chat_message', NULL, NULL, 0, '2026-04-03 19:31:31');
INSERT INTO `user_exp_record` VALUES (55, 6, 'chat_send_text', 1, '发送文本消息', 46, 'chat_message', NULL, NULL, 0, '2026-04-03 19:38:31');
INSERT INTO `user_exp_record` VALUES (56, 5, 'chat_send_text', 1, '发送文本消息', 47, 'chat_message', NULL, NULL, 0, '2026-04-03 21:07:49');
INSERT INTO `user_exp_record` VALUES (57, 6, 'chat_send_text', 1, '发送文本消息', 48, 'chat_message', NULL, NULL, 0, '2026-04-03 21:25:19');
INSERT INTO `user_exp_record` VALUES (58, 5, 'chat_send_text', 1, '发送文本消息', 49, 'chat_message', NULL, NULL, 0, '2026-04-03 21:37:54');
INSERT INTO `user_exp_record` VALUES (59, 5, 'chat_send_text', 1, '发送文本消息', 50, 'chat_message', NULL, NULL, 0, '2026-04-03 21:48:15');
INSERT INTO `user_exp_record` VALUES (60, 9, 'chat_send_text', 1, '发送文本消息', 51, 'chat_message', NULL, NULL, 0, '2026-04-03 21:48:46');
INSERT INTO `user_exp_record` VALUES (61, 5, 'daily_login', 5, '每日登录奖励', NULL, NULL, NULL, NULL, 0, '2026-04-04 22:10:42');
INSERT INTO `user_exp_record` VALUES (62, 5, 'login_day_1', 10, '连续登录第1天奖励', NULL, NULL, NULL, NULL, 0, '2026-04-04 22:10:42');
INSERT INTO `user_exp_record` VALUES (63, 8, 'chat_send_text', 1, '发送文本消息', 52, 'chat_message', NULL, NULL, 0, '2026-04-05 01:20:49');
INSERT INTO `user_exp_record` VALUES (64, 8, 'chat_send_video', 3, '发送视频消息', 53, 'chat_message', NULL, NULL, 0, '2026-04-05 01:20:50');
INSERT INTO `user_exp_record` VALUES (65, 8, 'chat_send_audio', 2, '发送语音消息', 54, 'chat_message', NULL, NULL, 0, '2026-04-05 01:20:50');
INSERT INTO `user_exp_record` VALUES (66, 11, 'chat_send_text', 1, '发送文本消息', 55, 'chat_message', NULL, NULL, 0, '2026-04-05 01:39:12');
INSERT INTO `user_exp_record` VALUES (67, 14, 'chat_send_text', 1, '发送文本消息', 56, 'chat_message', NULL, NULL, 0, '2026-04-05 01:40:29');
INSERT INTO `user_exp_record` VALUES (68, 14, 'chat_send_video', 3, '发送视频消息', 57, 'chat_message', NULL, NULL, 0, '2026-04-05 01:40:30');
INSERT INTO `user_exp_record` VALUES (69, 14, 'chat_send_audio', 2, '发送语音消息', 58, 'chat_message', NULL, NULL, 0, '2026-04-05 01:40:30');
INSERT INTO `user_exp_record` VALUES (70, 17, 'daily_login', 5, '每日登录奖励', NULL, NULL, NULL, NULL, 0, '2026-04-05 11:55:47');
INSERT INTO `user_exp_record` VALUES (71, 17, 'login_day_1', 10, '连续登录第1天奖励', NULL, NULL, NULL, NULL, 0, '2026-04-05 11:55:48');
INSERT INTO `user_exp_record` VALUES (72, 19, 'chat_send_text', 1, 'send text message', 59, 'chat_message', NULL, NULL, 0, '2026-04-05 12:01:18');
INSERT INTO `user_exp_record` VALUES (73, 18, 'post_create', 5, 'demo post create', 31296666, 'post', '192.0.2.10', NULL, 0, '2026-04-05 16:58:13');
INSERT INTO `user_exp_record` VALUES (74, 18, 'post_create', 5, 'demo post create 2', 11629441, 'post', '192.0.2.11', NULL, 0, '2026-04-06 15:08:27');
INSERT INTO `user_exp_record` VALUES (75, 17, 'chat_send_text', 1, 'send text message', 60, 'chat_message', NULL, NULL, 0, '2026-04-07 19:45:55');
INSERT INTO `user_exp_record` VALUES (76, 17, 'chat_send_text', 1, 'send text message', 61, 'chat_message', NULL, NULL, 0, '2026-04-10 20:24:32');
INSERT INTO `user_exp_record` VALUES (77, 18, 'chat_send_text', 1, 'send text message', 62, 'chat_message', NULL, NULL, 0, '2026-04-10 20:25:17');
INSERT INTO `user_exp_record` VALUES (78, 18, 'chat_send_text', 1, 'send text message', 63, 'chat_message', NULL, NULL, 0, '2026-04-11 15:45:38');
INSERT INTO `user_exp_record` VALUES (79, 18, 'chat_send_text', 1, 'send text message', 64, 'chat_message', NULL, NULL, 0, '2026-04-26 13:05:22');
INSERT INTO `user_exp_record` VALUES (80, 17, 'chat_send_text', 1, 'send text message', 65, 'chat_message', NULL, NULL, 0, '2026-05-03 00:43:05');

-- ----------------------------
-- Table structure for user_feedback
-- ----------------------------
DROP TABLE IF EXISTS `user_feedback`;
CREATE TABLE `user_feedback`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `feedback_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '反馈标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `attachment` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理中，2-已处理',
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `process_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_feedback
-- ----------------------------
INSERT INTO `user_feedback` VALUES (1, 6, '内容反馈', '课程有误', '课程错误', '', 1, 5, '2026-03-22 16:39:23', '2026-03-22 16:17:35', '2026-03-22 16:39:23', 0);

-- ----------------------------
-- Table structure for user_feedback_reply
-- ----------------------------
DROP TABLE IF EXISTS `user_feedback_reply`;
CREATE TABLE `user_feedback_reply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `feedback_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `sender_role` tinyint(4) NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复内容',
  `attachment` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件URL',
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_id`(`feedback_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '反馈回复' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_feedback_reply
-- ----------------------------

-- ----------------------------
-- Table structure for user_goal_item
-- ----------------------------
DROP TABLE IF EXISTS `user_goal_item`;
CREATE TABLE `user_goal_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `daily_goal_id` bigint(20) NOT NULL,
  `goal_type_id` bigint(20) NOT NULL,
  `goal_date` date NOT NULL,
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标标题',
  `target_value` int(11) NOT NULL DEFAULT 1,
  `current_value` int(11) NOT NULL DEFAULT 0,
  `progress_percent` int(11) NOT NULL DEFAULT 0,
  `is_completed` tinyint(4) NOT NULL DEFAULT 0,
  `completed_time` datetime NULL DEFAULT NULL,
  `is_rewarded` tinyint(4) NOT NULL DEFAULT 0,
  `reward_time` datetime NULL DEFAULT NULL,
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，数字越小排序越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_daily_goal_id`(`daily_goal_id`) USING BTREE,
  INDEX `idx_goal_type_id`(`goal_type_id`) USING BTREE,
  INDEX `idx_goal_date`(`goal_date`) USING BTREE,
  INDEX `idx_is_completed`(`is_completed`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习目标项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_goal_item
-- ----------------------------

-- ----------------------------
-- Table structure for user_learning_record
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_record`;
CREATE TABLE `user_learning_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `course_id` bigint(20) NULL DEFAULT NULL,
  `chapter_id` bigint(20) NULL DEFAULT NULL,
  `section_id` bigint(20) NULL DEFAULT NULL,
  `assignment_id` bigint(20) NULL DEFAULT NULL,
  `record_date` date NOT NULL,
  `record_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_id` bigint(20) NULL DEFAULT NULL,
  `lesson_number` int(11) NOT NULL DEFAULT 0,
  `duration` int(11) NOT NULL DEFAULT 0 COMMENT '学习时长(秒)',
  `count` int(11) NOT NULL DEFAULT 1 COMMENT '学习数量',
  `points` int(11) NOT NULL DEFAULT 0 COMMENT '获得积分',
  `experience` int(11) NOT NULL DEFAULT 0 COMMENT '获得经验值',
  `accuracy` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '正确率(百分比)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'completed' COMMENT '活动状态，如：in_progress, completed, failed',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `is_delete` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_learning_record`(`user_id`, `record_date`, `record_type`, `related_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE,
  INDEX `idx_record_type`(`record_type`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_section_id`(`section_id`) USING BTREE,
  INDEX `idx_assignment_id`(`assignment_id`) USING BTREE,
  INDEX `fk_user_learning_record_chapter`(`chapter_id`) USING BTREE,
  CONSTRAINT `fk_user_learning_record_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `class_assignment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_learning_record_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_learning_record_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_learning_record_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_learning_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_learning_record
-- ----------------------------

-- ----------------------------
-- Table structure for user_learning_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_stats`;
CREATE TABLE `user_learning_stats`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `level` int(11) NOT NULL DEFAULT 1 COMMENT '当前等级',
  `experience` int(11) NOT NULL DEFAULT 0 COMMENT '当前经验值',
  `next_level_exp` int(11) NOT NULL DEFAULT 100,
  `learning_days` int(11) NOT NULL DEFAULT 0,
  `continuous_check_in` int(11) NOT NULL DEFAULT 0,
  `total_check_in` int(11) NOT NULL DEFAULT 0,
  `total_points` int(11) NOT NULL DEFAULT 0,
  `total_badges` int(11) NOT NULL DEFAULT 0,
  `last_check_in_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE,
  INDEX `idx_experience`(`experience`) USING BTREE,
  INDEX `idx_learning_days`(`learning_days`) USING BTREE,
  INDEX `idx_continuous_check_in`(`continuous_check_in`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_learning_stats
-- ----------------------------

-- ----------------------------
-- Table structure for user_level
-- ----------------------------
DROP TABLE IF EXISTS `user_level`;
CREATE TABLE `user_level`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `level` int(11) NOT NULL DEFAULT 1 COMMENT '当前等级',
  `exp` int(11) NOT NULL DEFAULT 0 COMMENT '当前经验值',
  `next_level_exp` int(11) NULL DEFAULT 100 COMMENT '下一级所需经验值',
  `total_exp` int(11) NULL DEFAULT 0 COMMENT '累计获得经验值',
  `level_up_time` datetime NULL DEFAULT NULL COMMENT '上次升级时间',
  `continuous_login_days` int(11) NULL DEFAULT 0 COMMENT '连续登录天数',
  `last_login_date` date NULL DEFAULT NULL COMMENT '最后登录日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE,
  INDEX `idx_exp`(`exp`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_level
-- ----------------------------
INSERT INTO `user_level` VALUES (23, 5, 1, 38, 100, 38, NULL, 1, '2026-04-04', '2026-04-03 12:54:18', '2026-04-04 22:10:42', 0);
INSERT INTO `user_level` VALUES (24, 7, 1, 3, 100, 3, NULL, 0, NULL, '2026-04-03 15:50:55', '2026-04-03 19:09:38', 0);
INSERT INTO `user_level` VALUES (25, 6, 1, 3, 100, 3, NULL, 0, NULL, '2026-04-03 16:51:39', '2026-04-03 21:25:19', 0);
INSERT INTO `user_level` VALUES (26, 9, 1, 0, 100, 0, NULL, 0, NULL, '2026-04-03 21:48:47', '2026-04-03 21:48:47', 0);
INSERT INTO `user_level` VALUES (27, 8, 1, 5, 100, 5, NULL, 0, NULL, '2026-04-05 01:20:49', '2026-04-05 01:20:51', 0);
INSERT INTO `user_level` VALUES (28, 11, 1, 0, 100, 0, NULL, 0, NULL, '2026-04-05 01:39:12', '2026-04-05 01:39:12', 0);
INSERT INTO `user_level` VALUES (29, 14, 1, 5, 100, 5, NULL, 0, NULL, '2026-04-05 01:40:29', '2026-04-05 01:40:30', 0);
INSERT INTO `user_level` VALUES (30, 18, 1, 13, 100, 13, NULL, 0, NULL, '2026-04-05 04:24:21', '2026-04-26 13:05:22', 0);
INSERT INTO `user_level` VALUES (31, 17, 1, 13, 100, 13, NULL, 1, '2026-04-05', '2026-04-05 11:55:47', '2026-05-03 00:43:05', 0);
INSERT INTO `user_level` VALUES (32, 19, 1, 0, 100, 0, NULL, 0, NULL, '2026-04-05 12:01:18', '2026-04-05 12:01:18', 0);

-- ----------------------------
-- Table structure for user_milestone
-- ----------------------------
DROP TABLE IF EXISTS `user_milestone`;
CREATE TABLE `user_milestone`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `milestone_id` bigint(20) NOT NULL,
  `current_points` int(11) NOT NULL DEFAULT 0,
  `is_completed` tinyint(4) NOT NULL DEFAULT 0,
  `completed_time` datetime NULL DEFAULT NULL,
  `is_rewarded` tinyint(4) NOT NULL DEFAULT 0,
  `reward_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_milestone`(`user_id`, `milestone_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_milestone_id`(`milestone_id`) USING BTREE,
  INDEX `idx_is_completed`(`is_completed`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户里程碑' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_milestone
-- ----------------------------

-- ----------------------------
-- Table structure for user_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_progress`;
CREATE TABLE `user_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Progress id',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `course_id` bigint(20) NOT NULL COMMENT 'Course id',
  `section_id` bigint(20) NOT NULL COMMENT 'Section id',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 not-started / 1 learning / 2 completed',
  `learned_time` int(11) NOT NULL DEFAULT 0 COMMENT 'Learned time in seconds',
  `last_learn_time` datetime NULL DEFAULT NULL COMMENT 'Last learning time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course_section`(`user_id`, `course_id`, `section_id`) USING BTREE,
  INDEX `idx_user_progress_user`(`user_id`) USING BTREE,
  INDEX `idx_user_progress_course`(`course_id`) USING BTREE,
  INDEX `idx_user_progress_section`(`section_id`) USING BTREE,
  CONSTRAINT `fk_user_progress_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_progress_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_progress_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User progress table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_progress
-- ----------------------------
INSERT INTO `user_progress` VALUES (1, 18, 1, 1, 1, 0, '2026-04-07 18:33:05', '2026-04-07 18:33:04', '2026-04-07 18:33:04');
INSERT INTO `user_progress` VALUES (2, 18, 1, 2, 2, 0, '2026-04-07 19:19:57', '2026-04-07 18:38:11', '2026-04-07 18:38:11');
INSERT INTO `user_progress` VALUES (3, 18, 1, 3, 2, 0, '2026-04-08 12:13:07', '2026-04-08 12:11:38', '2026-04-08 12:11:38');
INSERT INTO `user_progress` VALUES (4, 17, 1, 2, 1, 0, '2026-04-10 19:35:35', '2026-04-10 19:35:35', '2026-04-10 19:35:35');
INSERT INTO `user_progress` VALUES (5, 18, 1, 4, 1, 0, '2026-05-02 10:34:04', '2026-05-02 10:34:03', '2026-05-02 10:34:03');
INSERT INTO `user_progress` VALUES (6, 18, 2, 5, 1, 0, '2026-05-02 21:44:07', '2026-05-02 21:44:06', '2026-05-02 21:44:06');
INSERT INTO `user_progress` VALUES (7, 18, 4, 11, 1, 0, '2026-05-03 00:34:02', '2026-05-03 00:34:02', '2026-05-03 00:34:02');

-- ----------------------------
-- Table structure for user_word_book
-- ----------------------------
DROP TABLE IF EXISTS `user_word_book`;
CREATE TABLE `user_word_book`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `word_id` bigint(20) NOT NULL,
  `learning_status` tinyint(4) NOT NULL DEFAULT 0,
  `is_collected` tinyint(4) NOT NULL DEFAULT 0,
  `collected_time` datetime NULL DEFAULT NULL,
  `difficulty` tinyint(4) NOT NULL DEFAULT 1 COMMENT '难度等级：1-简单，2-中等，3-困难',
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_word`(`user_id`, `word_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_word_id`(`word_id`) USING BTREE,
  INDEX `idx_learning_status`(`learning_status`) USING BTREE,
  INDEX `idx_is_collected`(`is_collected`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户生词本' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_word_book
-- ----------------------------
INSERT INTO `user_word_book` VALUES (1, 5, 2, 0, 0, NULL, 1, 0, '2026-03-23 13:57:15', '2026-03-23 13:57:15');
INSERT INTO `user_word_book` VALUES (2, 18, 104, 0, 0, NULL, 1, 0, '2026-04-10 22:15:44', '2026-05-01 01:18:06');
INSERT INTO `user_word_book` VALUES (3, 18, 103, 0, 0, NULL, 1, 0, '2026-04-10 22:15:47', '2026-05-01 16:45:25');
INSERT INTO `user_word_book` VALUES (4, 18, 102, 0, 0, NULL, 1, 0, '2026-04-26 20:21:54', '2026-05-03 00:33:01');
INSERT INTO `user_word_book` VALUES (5, 17, 104, 0, 0, NULL, 1, 0, '2026-05-03 00:46:18', '2026-05-03 00:46:18');

-- ----------------------------
-- Table structure for user_word_record
-- ----------------------------
DROP TABLE IF EXISTS `user_word_record`;
CREATE TABLE `user_word_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) NOT NULL,
  `word_id` bigint(20) NOT NULL,
  `learning_status` tinyint(4) NOT NULL DEFAULT 0,
  `review_count` int(11) NOT NULL DEFAULT 0,
  `last_review_time` datetime NULL DEFAULT NULL,
  `user_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_word`(`user_id`, `word_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_word_id`(`word_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户单词学习记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_word_record
-- ----------------------------

-- ----------------------------
-- Table structure for video_learning_record
-- ----------------------------
DROP TABLE IF EXISTS `video_learning_record`;
CREATE TABLE `video_learning_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录 id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 id',
  `section_id` bigint(20) NOT NULL COMMENT '小节 id',
  `video_duration` int(11) NULL DEFAULT NULL COMMENT '视频总时长（秒）',
  `learned_time` int(11) NULL DEFAULT 0 COMMENT '已学习时长（秒）',
  `progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '学习进度百分比',
  `last_watch_position` int(11) NULL DEFAULT 0 COMMENT '最后观看位置（秒）',
  `is_completed` tinyint(1) NULL DEFAULT 0 COMMENT '是否完成：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_section`(`user_id`, `section_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_section_id`(`section_id`) USING BTREE,
  INDEX `idx_is_completed`(`is_completed`) USING BTREE,
  CONSTRAINT `fk_video_learning_record_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_video_learning_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_learning_record
-- ----------------------------
INSERT INTO `video_learning_record` VALUES (1, 18, 1, 300, 0, 0.00, 0, 0, '2026-04-06 15:07:20', '2026-04-06 15:07:20');
INSERT INTO `video_learning_record` VALUES (2, 18, 2, 300, 300, 100.00, 300, 1, '2026-04-07 18:38:11', '2026-04-07 18:38:11');
INSERT INTO `video_learning_record` VALUES (3, 18, 3, 300, 300, 100.00, 300, 1, '2026-04-08 12:11:38', '2026-04-08 12:11:38');
INSERT INTO `video_learning_record` VALUES (4, 17, 2, 300, 0, 0.00, 0, 0, '2026-04-10 19:35:35', '2026-04-10 19:35:35');
INSERT INTO `video_learning_record` VALUES (5, 18, 4, 300, 0, 0.00, 0, 0, '2026-05-02 10:34:03', '2026-05-02 10:34:03');
INSERT INTO `video_learning_record` VALUES (6, 18, 5, 300, 0, 0.00, 0, 0, '2026-05-02 21:44:06', '2026-05-02 21:44:06');
INSERT INTO `video_learning_record` VALUES (7, 18, 11, 300, 0, 0.00, 0, 0, '2026-05-03 00:34:01', '2026-05-03 00:34:01');

-- ----------------------------
-- View structure for course_answer
-- ----------------------------
DROP VIEW IF EXISTS `course_answer`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `course_answer` AS select `user_answer`.`id` AS `id`,`user_answer`.`user_id` AS `user_id`,`user_answer`.`question_id` AS `question_id`,`user_answer`.`user_answer` AS `user_answer`,`user_answer`.`is_correct` AS `is_correct`,`user_answer`.`score` AS `score`,`user_answer`.`time_spent` AS `time_spent`,`user_answer`.`create_time` AS `create_time`,`user_answer`.`update_time` AS `update_time` from `user_answer`;

-- ----------------------------
-- View structure for course_learning_record
-- ----------------------------
DROP VIEW IF EXISTS `course_learning_record`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `course_learning_record` AS select `user_progress`.`id` AS `id`,`user_progress`.`user_id` AS `user_id`,`user_progress`.`course_id` AS `course_id`,`user_progress`.`section_id` AS `section_id`,`user_progress`.`status` AS `status`,`user_progress`.`learned_time` AS `learned_time`,`user_progress`.`last_learn_time` AS `last_learn_time`,`user_progress`.`create_time` AS `create_time`,`user_progress`.`update_time` AS `update_time` from `user_progress`;

-- ----------------------------
-- View structure for course_question
-- ----------------------------
DROP VIEW IF EXISTS `course_question`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `course_question` AS select `question`.`id` AS `id`,`question`.`section_id` AS `section_id`,`question`.`type` AS `type`,`question`.`content` AS `content`,`question`.`options` AS `options`,`question`.`answer` AS `answer`,`question`.`explanation` AS `explanation`,`question`.`score` AS `score`,`question`.`difficulty` AS `difficulty`,`question`.`sort_order` AS `sort_order`,`question`.`create_time` AS `create_time`,`question`.`update_time` AS `update_time`,`question`.`is_delete` AS `is_delete` from `question`;

-- ----------------------------
-- View structure for course_section
-- ----------------------------
DROP VIEW IF EXISTS `course_section`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `course_section` AS select `section`.`id` AS `id`,`section`.`chapter_id` AS `chapter_id`,`section`.`title` AS `title`,`section`.`type` AS `type`,`section`.`content` AS `content`,`section`.`video_url` AS `video_url`,`section`.`video_duration` AS `video_duration`,`section`.`resource_type` AS `resource_type`,`section`.`resource_url` AS `resource_url`,`section`.`content_type` AS `content_type`,`section`.`sort_order` AS `sort_order`,`section`.`is_free` AS `is_free`,`section`.`create_time` AS `create_time`,`section`.`update_time` AS `update_time`,`section`.`is_delete` AS `is_delete` from `section`;

-- ----------------------------
-- Procedure structure for sp_rename_column_if_needed
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_rename_column_if_needed`;
delimiter ;;
CREATE PROCEDURE `sp_rename_column_if_needed`(IN p_table VARCHAR(128),
  IN p_old_col VARCHAR(128),
  IN p_new_col VARCHAR(128),
  IN p_col_def LONGTEXT)
BEGIN
  DECLARE v_old_exists INT DEFAULT 0;
  DECLARE v_new_exists INT DEFAULT 0;

  SELECT COUNT(1) INTO v_old_exists
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND COLUMN_NAME = p_old_col;

  SELECT COUNT(1) INTO v_new_exists
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND COLUMN_NAME = p_new_col;

  IF v_old_exists = 1 AND v_new_exists = 0 THEN
    SET @sql_stmt = CONCAT(
      'ALTER TABLE `', p_table, '` CHANGE COLUMN `', p_old_col, '` `', p_new_col, '` ', p_col_def
    );
    PREPARE stmt FROM @sql_stmt;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_rename_index_if_needed
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_rename_index_if_needed`;
delimiter ;;
CREATE PROCEDURE `sp_rename_index_if_needed`(IN p_table VARCHAR(128),
  IN p_old_idx VARCHAR(128),
  IN p_new_idx VARCHAR(128))
BEGIN
  DECLARE v_old_exists INT DEFAULT 0;
  DECLARE v_new_exists INT DEFAULT 0;

  SELECT COUNT(1) INTO v_old_exists
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND INDEX_NAME = p_old_idx;

  SELECT COUNT(1) INTO v_new_exists
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table
    AND INDEX_NAME = p_new_idx;

  IF v_old_exists > 0 AND v_new_exists = 0 THEN
    SET @sql_stmt = CONCAT(
      'ALTER TABLE `', p_table, '` RENAME INDEX `', p_old_idx, '` TO `', p_new_idx, '`'
    );
    PREPARE stmt FROM @sql_stmt;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
