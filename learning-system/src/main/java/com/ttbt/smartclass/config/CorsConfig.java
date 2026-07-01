package com.ttbt.smartclass.config;

import java.io.File;
import java.util.Arrays;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String LOCAL_FRONTEND_ORIGIN = "http://localhost:8000";
    private static final String LOCAL_FRONTEND_ORIGIN_8001 = "http://localhost:8001";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(LOCAL_FRONTEND_ORIGIN, LOCAL_FRONTEND_ORIGIN_8001)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 构建全局跨域规则，允许本地前端携带凭证访问后端接口。
     *
     * @return 跨域配置对象
     */
    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(LOCAL_FRONTEND_ORIGIN, LOCAL_FRONTEND_ORIGIN_8001));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedHeader("*");
        config.addExposedHeader("*");
        config.setMaxAge(3600L);
        return config;
    }

    /**
     * 注册跨域过滤器，并确保跨域处理优先于其他过滤链执行。
     *
     * @param corsConfiguration 全局跨域配置
     * @return 跨域过滤器注册对象
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfiguration corsConfiguration) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localUploadPath = System.getProperty("user.dir") + File.separator + "upload" + File.separator;
        String resourceLocation = "file:" + localUploadPath.replace("\\", "/");
        registry.addResourceHandler("/upload/**").addResourceLocations(resourceLocation);
    }
}
