package com.ttbt.smartclass.model.dto.dify;

import java.io.InputStream;
import java.util.Map;
import lombok.Data;

/**
 * Dify 文件建文档请求
 */
@Data
public class DifyDocumentCreateByFileRequest {

    private String baseUrl;

    private String apiKey;

    private String datasetId;

    private String name;

    private String filename;

    private String contentType;

    private InputStream inputStream;

    private String indexingTechnique;

    private String processRuleMode;

    private Map<String, Object> metadata;
}