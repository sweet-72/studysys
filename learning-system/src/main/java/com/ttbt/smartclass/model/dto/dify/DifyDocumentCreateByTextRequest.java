package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * Dify 文本建文档请求
 */
@Data
public class DifyDocumentCreateByTextRequest implements Serializable {

    private String baseUrl;

    private String apiKey;

    private String datasetId;

    private String name;

    private String text;

    private String indexingTechnique;

    private String processRuleMode;

    private Map<String, Object> metadata;

    private static final long serialVersionUID = 1L;
}