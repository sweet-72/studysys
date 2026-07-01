package com.ttbt.smartclass.utils;

import cn.hutool.json.JSONUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 响应工具类。
 * 负责构造安全的 SSE 错误响应对象。
 */
public final class SseEmitterUtils {

    private SseEmitterUtils() {
    }

    /**
     * 创建携带错误事件的 SSE 响应。
     *
     * @param message 错误信息
     * @return 已完成的 SSE 响应对象
     */
    public static SseEmitter createErrorEmitter(String message) {
        // 使用 0 超时的 emitter 立即发送错误事件并完成连接
        SseEmitter emitter = new SseEmitter(0L);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "error");
        payload.put("message", StringUtils.hasText(message) ? message.trim() : "request failed");
        try {
            // 按统一 SSE error 事件格式返回，便于前端流式接口统一处理
            emitter.send(SseEmitter.event()
                    .name("error")
                    .id("error-" + System.currentTimeMillis())
                    .data(JSONUtil.toJsonStr(payload)));
        } catch (Exception ignored) {
            // no-op
        }
        emitter.complete();
        return emitter;
    }
}
