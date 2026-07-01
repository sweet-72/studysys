package com.ttbt.smartclass.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 内容哈希工具
 */
public final class ContentHashUtils {

    private ContentHashUtils() {
    }

    /**
     * 计算文本内容的 SHA-256 哈希值，空内容按空字符串处理。
     *
     * @param content 原始文本内容
     * @return 十六进制 SHA-256 哈希值
     */
    public static String sha256(String content) {
        String safeContent = content == null ? "" : content;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(safeContent.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
