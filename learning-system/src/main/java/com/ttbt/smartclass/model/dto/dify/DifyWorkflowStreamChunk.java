package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * Dify 工作流流式响应片段 DTO。
 */
@Data
public class DifyWorkflowStreamChunk implements Serializable {

    private String event;

    private String task_id;

    private String workflow_run_id;

    private Map<String, Object> data;

    private String code;

    private String message;

    private static final long serialVersionUID = 1L;
}
