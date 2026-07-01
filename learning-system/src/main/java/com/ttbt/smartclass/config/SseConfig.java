package com.ttbt.smartclass.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SSE 流式响应配置。
 */
@Configuration
public class SseConfig implements WebMvcConfigurer {

    private static final long SSE_TIMEOUT = 3600000L;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(SSE_TIMEOUT);
    }

    /**
     * 创建 SSE 响应使用的禁用缓存策略，避免浏览器或代理缓存流式数据。
     *
     * @return 禁用缓存的响应缓存控制对象
     */
    @Bean
    public CacheControl sseNoCache() {
        return CacheControl.noCache()
                .noTransform()
                .mustRevalidate()
                .noStore()
                .maxAge(0, TimeUnit.SECONDS);
    }
}
