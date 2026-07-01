package com.ttbt.smartclass.constant;

/**
 * 课程学习 AI 常量。
 */
public interface CourseLearningAiConstant {

    String CONTEXT_CACHE_KEY_PREFIX = "course:ai:context:";

    long CONTEXT_CACHE_TTL_MINUTES = 10L;

    String DEFAULT_STUDENT_GOAL = "请优先讲解当前知识点，并给出下一步学习建议。";

    String LEARNING_ASSISTANT_PROMPT_TEMPLATE =
            "你是课程学习助手，不是普通闲聊机器人。\n"
                    + "请优先基于当前课程学习上下文回答。\n"
                    + "如果课程资料不足，请明确说明依据不足，不要编造。\n"
                    + "请尽量输出：1. 知识点讲解 2. 当前问题定位 3. 学习建议 4. 下一步推荐。\n\n"
                    + "【学习上下文】\n%s\n\n"
                    + "【学生问题】\n%s";
}
