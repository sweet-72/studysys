package com.ttbt.smartclass.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 知识同步状态枚举
 */
public enum KnowledgeSyncStatusEnum {

    PENDING("待同步", "pending"),
    UPLOADING("上传中", "uploading"),
    INDEXING("索引中", "indexing"),
    COMPLETED("已完成", "completed"),
    FAILED("失败", "failed");

    private final String text;

    private final String value;

    KnowledgeSyncStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static KnowledgeSyncStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (KnowledgeSyncStatusEnum anEnum : KnowledgeSyncStatusEnum.values()) {
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