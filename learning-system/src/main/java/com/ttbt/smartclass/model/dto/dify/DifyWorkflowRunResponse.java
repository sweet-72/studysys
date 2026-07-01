package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * Dify 工作流运行响应 DTO。
 */
@Data
public class DifyWorkflowRunResponse implements Serializable {

    private String workflow_run_id;

    private String task_id;

    private DifyWorkflowData data;

    private String code;

    private String message;

    @Data
    public static class DifyWorkflowData implements Serializable {

        private String id;

        private String workflow_id;

        private String status;

        private Map<String, Object> outputs;

        private String error;

        private Double elapsed_time;

        private Integer total_tokens;

        private Integer total_steps;

        private Long created_at;

        private Long finished_at;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
