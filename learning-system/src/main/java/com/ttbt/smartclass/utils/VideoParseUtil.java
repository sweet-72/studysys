package com.ttbt.smartclass.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频 URL 解析工具类
 * 将视频链接转换为可嵌入播放的 iframe 地址
 */
@Slf4j
public class VideoParseUtil {

    /**
     * 解析视频 URL，返回可嵌入播放的地址
     * 
     * @param url 原始视频 URL
     * @return 可嵌入播放的地址（iframe src 或 video src）
     */
    public static String parse(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // B 站视频解析
        String bilibiliEmbed = parseBilibili(url);
        if (bilibiliEmbed != null) {
            return bilibiliEmbed;
        }

        // 腾讯视频解析
        String qqEmbed = parseTencent(url);
        if (qqEmbed != null) {
            return qqEmbed;
        }

        // YouTube 视频解析
        String youtubeEmbed = parseYoutube(url);
        if (youtubeEmbed != null) {
            return youtubeEmbed;
        }

        // 如果是直接的视频文件链接（mp4, webm 等），返回原 URL
        if (isDirectVideoUrl(url)) {
            return url;
        }

        // 其他情况，返回原 URL（让前端决定如何处理）
        return url;
    }

    /**
     * 解析 B 站视频链接
     */
    private static String parseBilibili(String url) {
        // B 站视频格式：https://www.bilibili.com/video/BV1xxxx
        Pattern pattern = Pattern.compile("bilibili\\.com/video/(BV\\w+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String bvid = matcher.group(1);
            return "//player.bilibili.com/player.html?bvid=" + bvid + "&page=1&high_quality=1";
        }
        return null;
    }

    /**
     * 解析腾讯视频链接
     */
    private static String parseTencent(String url) {
        // 腾讯视频格式：https://v.qq.com/x/cover/xxx/xxx.html
        Pattern pattern = Pattern.compile("v\\.qq\\.com/x/cover/[\\w]+/([\\w]+)\\.html");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String vid = matcher.group(1);
            return "https://v.qq.com/txp/iframe/player.html?vid=" + vid;
        }
        return null;
    }

    /**
     * 解析 YouTube 视频链接
     */
    private static String parseYoutube(String url) {
        // YouTube 格式：https://www.youtube.com/watch?v=VIDEO_ID
        Pattern pattern = Pattern.compile("youtube\\.com/watch\\?v=([\\w-]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String videoId = matcher.group(1);
            return "https://www.youtube.com/embed/" + videoId;
        }
        return null;
    }

    /**
     * 判断是否为直接可播放的视频文件 URL
     */
    private static boolean isDirectVideoUrl(String url) {
        return url.toLowerCase().matches(".*\\.(mp4|webm|ogg|mov|flv|wmv)(\\?.*)?$");
    }

    /**
     * 获取视频平台名称
     */
    public static String getPlatformName(String url) {
        if (url == null) {
            return "未知";
        }
        if (url.contains("bilibili.com")) {
            return "哔哩哔哩";
        }
        if (url.contains("v.qq.com")) {
            return "腾讯视频";
        }
        if (url.contains("youtube.com")) {
            return "YouTube";
        }
        if (url.contains("youku.com")) {
            return "优酷";
        }
        return "其他";
    }
}
