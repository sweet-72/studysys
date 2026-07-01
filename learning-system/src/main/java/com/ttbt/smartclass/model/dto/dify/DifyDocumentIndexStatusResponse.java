package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import lombok.Data;

/**
 * Dify 文档索引状态响应
 */
@Data
public class DifyDocumentIndexStatusResponse implements Serializable {

    private String datasetId;

    private String batchId;

    private String documentId;

    private String indexingStatus;

    private Integer completedSegments;

    private Integer totalSegments;

    private String errorMessage;

    private String rawBody;

    private static final long serialVersionUID = 1L;
}