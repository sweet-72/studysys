package com.ttbt.smartclass.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ttbt.smartclass.service.SpeechRecognitionService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Speech recognition service.
 *
 * Real providers can be connected here later, such as Baidu, iFlytek,
 * Aliyun, or FunASR. When no provider is configured, this service returns
 * null so callers can show a clear "not configured" message.
 */
@Slf4j
@Service
public class SpeechRecognitionServiceImpl implements SpeechRecognitionService {

    @Value("${speech.recognition.api.key:}")
    private String apiKey;

    @Value("${speech.recognition.api.secret:}")
    private String apiSecret;

    @Value("${speech.recognition.api.url:}")
    private String apiUrl;

    @Override
    public String recognizeSpeech(String audioUrl) {
        return recognizeSpeech(audioUrl, "zh-CN");
    }

    @Override
    public String recognizeSpeech(String audioUrl, String language) {
        if (StrUtil.isBlank(audioUrl)) {
            log.warn("speech audio url/path is blank");
            return null;
        }
        if (StrUtil.isBlank(apiUrl) || StrUtil.isBlank(apiKey)) {
            log.warn("speech recognition service is not configured");
            return null;
        }

        try {
            log.info("start speech recognition: {}, language: {}", audioUrl, language);
            return callThirdPartySpeechApi(audioUrl, language);
        } catch (Exception e) {
            log.error("speech recognition failed: {}", audioUrl, e);
            return null;
        }
    }

    private String callThirdPartySpeechApi(String audioUrl, String language) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        if (StrUtil.isNotBlank(apiSecret)) {
            headers.put("X-Api-Secret", apiSecret);
        }

        String requestBody = String.format(
                "{\"audio_url\":\"%s\",\"language\":\"%s\"}",
                escapeJson(audioUrl),
                escapeJson(language));

        String response = sendHttpPost(apiUrl, requestBody, headers);
        return parseResponse(response);
    }

    private String sendHttpPost(String url, String body, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setDoOutput(true);
        connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        int responseCode = connection.getResponseCode();
        log.info("speech recognition api response code: {}", responseCode);
        if (responseCode < 200 || responseCode >= 300) {
            return null;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String parseResponse(String response) {
        if (StrUtil.isBlank(response)) {
            return null;
        }
        int resultKey = response.indexOf("\"result\"");
        if (resultKey < 0) {
            resultKey = response.indexOf("\"text\"");
        }
        if (resultKey < 0) {
            return null;
        }
        int colon = response.indexOf(':', resultKey);
        int start = response.indexOf('"', colon + 1);
        int end = response.indexOf('"', start + 1);
        if (colon < 0 || start < 0 || end < 0) {
            return null;
        }
        return response.substring(start + 1, end);
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
