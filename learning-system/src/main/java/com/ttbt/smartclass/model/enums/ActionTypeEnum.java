package com.ttbt.smartclass.model.enums;

import lombok.Getter;

/**
 * 经验值行为类型枚举
 * 定义所有可以获得经验值的行为类型
 */
@Getter
public enum ActionTypeEnum {

    // ========== 学习类行为 ==========
    /**
     * 完成课程小节
     */
    COURSE_SECTION_COMPLETE("course_section_complete", "完成课程小节", 10, 100),
    
    /**
     * 完成练习题
     */
    EXERCISE_COMPLETE("exercise_complete", "完成练习题", 20, 100),
    
    /**
     * 学习时长（每 10 分钟）
     */
    STUDY_TIME("study_time", "学习时长", 2, 50),
    
    /**
     * 连续学习奖励
     */
    CONTINUOUS_STUDY("continuous_study", "连续学习", 5, 5),

    // ========== 社交行为 ==========
    /**
     * 发布帖子
     */
    POST_CREATE("post_create", "发布帖子", 5, 50),
    
    /**
     * 发布评论
     */
    COMMENT_CREATE("comment_create", "发布评论", 2, 30),
    
    /**
     * 点赞他人
     */
    LIKE_GIVE("like_give", "点赞他人", 1, 20),
    
    /**
     * 被点赞
     */
    LIKE_RECEIVED("like_received", "被点赞", 3, -1), // -1 表示无上限

    // ========== 活跃行为 ==========
    /**
     * 每日登录
     */
    DAILY_LOGIN("daily_login", "每日登录", 5, 5),
    
    /**
     * 连续登录第 1 天
     */
    LOGIN_DAY_1("login_day_1", "连续登录第 1 天", 10, 10),
    
    /**
     * 连续登录第 3 天
     */
    LOGIN_DAY_3("login_day_3", "连续登录第 3 天", 20, 20),
    
    /**
     * 连续登录第 7 天
     */
    LOGIN_DAY_7("login_day_7", "连续登录第 7 天", 50, 50),
    
    /**
     * 完成每日目标
     */
    DAILY_GOAL_COMPLETE("daily_goal_complete", "完成每日目标", 20, 20),

    // ========== 成就奖励 ==========
    /**
     * 解锁成就（基础）
     */
    ACHIEVEMENT_UNLOCKED_BASIC("achievement_unlocked_basic", "解锁成就 - 基础", 50, -1),
    
    /**
     * 解锁成就（中级）
     */
    ACHIEVEMENT_UNLOCKED_MEDIUM("achievement_unlocked_medium", "解锁成就 - 中级", 100, -1),
    
    /**
     * 解锁成就（高级）
     */
    ACHIEVEMENT_UNLOCKED_ADVANCED("achievement_unlocked_advanced", "解锁成就 - 高级", 200, -1),

    // ========== 聊天行为 ==========
    /**
     * 发送文本消息
     */
    CHAT_SEND_TEXT("chat_send_text", "发送文本消息", 1, 50),
    
    /**
     * 发送图片消息
     */
    CHAT_SEND_IMAGE("chat_send_image", "发送图片消息", 2, 30),
    
    /**
     * 发送视频消息
     */
    CHAT_SEND_VIDEO("chat_send_video", "发送视频消息", 3, 20),
    
    /**
     * 发送语音消息
     */
    CHAT_SEND_AUDIO("chat_send_audio", "发送语音消息", 2, 30);

    /**
     * 行为类型编码
     */
    private final String code;
    
    /**
     * 行为描述
     */
    private final String description;
    
    /**
     * 单次获得经验值
     */
    private final int expPerAction;
    
    /**
     * 每日上限（-1 表示无上限）
     */
    private final int dailyLimit;

    ActionTypeEnum(String code, String description, int expPerAction, int dailyLimit) {
        this.code = code;
        this.description = description;
        this.expPerAction = expPerAction;
        this.dailyLimit = dailyLimit;
    }

    /**
     * 根据编码获取枚举
     */
    public static ActionTypeEnum getByCode(String code) {
        for (ActionTypeEnum actionType : values()) {
            if (actionType.getCode().equals(code)) {
                return actionType;
            }
        }
        return null;
    }

    /**
     * 判断是否有每日上限
     */
    public boolean hasDailyLimit() {
        return this.dailyLimit > 0;
    }
}
