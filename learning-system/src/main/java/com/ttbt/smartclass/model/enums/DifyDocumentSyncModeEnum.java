package com.ttbt.smartclass.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Dify 文档同步模式枚举
 */
public enum DifyDocumentSyncModeEnum {

    TEXT("文本", "text"),
    FILE("文件", "file");

    private final String text;

    private final String value;

    DifyDocumentSyncModeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static DifyDocumentSyncModeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (DifyDocumentSyncModeEnum anEnum : DifyDocumentSyncModeEnum.values()) {
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