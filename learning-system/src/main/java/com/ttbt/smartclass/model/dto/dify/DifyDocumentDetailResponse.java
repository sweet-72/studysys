package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import lombok.Data;

/**
 * Dify 文档详情响应
 */
@Data
public class DifyDocumentDetailResponse implements Serializable {

    private String datasetId;

    private String documentId;

    private String name;

    private String dataSourceType;

    private String indexingStatus;

    private Integer wordCount;

    private Integer hitCount;

    private String rawBody;

    private static final long serialVersionUID = 1L;
}