package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByFileRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByTextRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentDetailResponse;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentIndexStatusResponse;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentOperationResponse;

/**
 * Dify 知识库服务
 */
public interface DifyKnowledgeService {

    DifyDocumentOperationResponse createDocumentByText(DifyDocumentCreateByTextRequest request);

    DifyDocumentOperationResponse createDocumentByFile(DifyDocumentCreateByFileRequest request);

    DifyDocumentIndexStatusResponse getDocumentIndexingStatus(String baseUrl, String apiKey, String datasetId, String batchId);

    DifyDocumentDetailResponse getDocument(String baseUrl, String apiKey, String datasetId, String documentId);

    DifyDocumentOperationResponse updateDocumentByText(String documentId, DifyDocumentCreateByTextRequest request);

    DifyDocumentOperationResponse updateDocumentByFile(String documentId, DifyDocumentCreateByFileRequest request);
}