package com.ttbt.smartclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify 服务配置。
 */
@Configuration
@ConfigurationProperties(prefix = "dify")
@Data
public class DifyConfig {

    /**
     * Dify API base URL.
     */
    private String baseUrl = "http://localhost:8091/v1";

    /**
     * Chat messages path.
     */
    private String chatMessagesPath = "/chat-messages";

    /**
     * Workflow run path.
     */
    private String workflowRunPath = "/workflows/run";

    /**
     * Default user prefix.
     */
    private String userPrefix = "smartclass_";

    /**
     * Whether to enable verbose streaming log.
     */
    private boolean enableStreamingVerboseLog = false;
}
