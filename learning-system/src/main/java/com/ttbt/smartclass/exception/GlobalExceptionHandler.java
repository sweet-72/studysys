package com.ttbt.smartclass.exception;

import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.utils.SseEmitterUtils;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，普通请求返回统一错误结构，SSE 请求返回错误事件流。
     *
     * @param e 业务异常
     * @param request 当前 HTTP 请求
     * @return 错误响应或 SSE 错误流
     */
    @ExceptionHandler(BusinessException.class)
    public Object businessExceptionHandler(BusinessException e, HttpServletRequest request) {
        // SSE 接口不能返回普通 JSON 错误，需构造可被前端事件流消费的错误响应
        log.error("BusinessException", e);
        if (isSseRequest(request)) {
            return SseEmitterUtils.createErrorEmitter(e.getMessage());
        }
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常，避免未捕获异常直接暴露给前端。
     *
     * @param e 运行时异常
     * @param request 当前 HTTP 请求
     * @return 错误响应或 SSE 错误流
     */
    @ExceptionHandler(RuntimeException.class)
    public Object runtimeExceptionHandler(RuntimeException e, HttpServletRequest request) {
        // SSE 请求保留异常消息给事件流，普通请求统一返回系统错误
        log.error("RuntimeException", e);
        if (isSseRequest(request)) {
            return SseEmitterUtils.createErrorEmitter(resolveMessage(e));
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 兜底处理未知异常，保证接口返回统一错误格式。
     *
     * @param e 未知异常
     * @param request 当前 HTTP 请求
     * @return 错误响应或 SSE 错误流
     */
    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception e, HttpServletRequest request) {
        // 未知异常按系统错误处理，SSE 请求仍返回事件流错误对象
        log.error("Exception", e);
        if (isSseRequest(request)) {
            return SseEmitterUtils.createErrorEmitter(resolveMessage(e));
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    private boolean isSseRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String requestUri = request.getRequestURI();
        return (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || (contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || (requestUri != null && requestUri.contains("/stream"));
    }

    private String resolveMessage(Exception e) {
        return e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()
                ? "request failed"
                : e.getMessage().trim();
    }
}
