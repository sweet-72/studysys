package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * Dify 工作流运行请求 DTO。
 */
@Data
public class DifyWorkflowRunRequest implements Serializable {

    private Map<String, Object> inputs;

    private String response_mode;

    private String user;

    private static final long serialVersionUID = 1L;
}
