package com.ttbt.smartclass.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 知识同步来源类型枚举
 */
public enum KnowledgeSyncSourceTypeEnum {

    COURSE_INTRO("课程简介", "course_intro"),
    CHAPTER("章节", "chapter"),
    SECTION("小节", "section"),
    MATERIAL("课程资料", "material"),
    QUESTION("题目", "question"),
    EXPLANATION("题目解析", "explanation");

    private final String text;

    private final String value;

    KnowledgeSyncSourceTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static KnowledgeSyncSourceTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (KnowledgeSyncSourceTypeEnum anEnum : KnowledgeSyncSourceTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}