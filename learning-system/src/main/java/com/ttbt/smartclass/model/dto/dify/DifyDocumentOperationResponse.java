package com.ttbt.smartclass.model.dto.dify;

import java.io.Serializable;
import lombok.Data;

/**
 * Dify 文档创建 / 更新响应
 */
@Data
public class DifyDocumentOperationResponse implements Serializable {

    private String datasetId;

    private String documentId;

    private String batchId;

    private String name;

    private String indexingStatus;

    private String rawBody;

    private static final long serialVersionUID = 1L;
}