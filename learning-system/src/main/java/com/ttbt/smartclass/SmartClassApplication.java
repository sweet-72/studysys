package com.ttbt.smartclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用程序入口
 */


@SpringBootApplication
@MapperScan("com.ttbt.smartclass.mapper")
@ServletComponentScan
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableScheduling
@EnableAsync // 添加异步支持，配合事件监听器使用
@EnableElasticsearchRepositories(basePackages = "com.ttbt.smartclass.esdao") // 显式指定 ES Repository 扫描路径，避免与 Redis 冲突
public class SmartClassApplication {

    /**
     * 启动智慧课堂 Spring Boot 应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartClassApplication.class, args);
    }
}
