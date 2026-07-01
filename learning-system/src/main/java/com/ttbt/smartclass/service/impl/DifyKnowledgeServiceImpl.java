package com.ttbt.smartclass.service.impl;

import cn.hutool.json.JSONUtil;
import com.ttbt.smartclass.constant.DifyKnowledgeConstant;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByFileRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByTextRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentDetailResponse;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentIndexStatusResponse;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentOperationResponse;
import com.ttbt.smartclass.service.DifyKnowledgeService;
import com.ttbt.smartclass.utils.OkHttpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Dify 知识库文档服务实现类。
 * 负责通过 Dify Dataset API 创建、更新和查询知识库文档。
 */
@Service
@Slf4j
public class DifyKnowledgeServiceImpl implements DifyKnowledgeService {

    private final OkHttpUtils okHttpUtils;

    public DifyKnowledgeServiceImpl(OkHttpUtils okHttpUtils) {
        this.okHttpUtils = okHttpUtils;
    }

    /**
     * 通过文本内容创建 Dify 知识库文档。
     *
     * @param request 文本文档创建请求
     * @return Dify 文档操作结果
     */
    @Override
    public DifyDocumentOperationResponse createDocumentByText(DifyDocumentCreateByTextRequest request) {
        // 校验知识库、鉴权和文本参数，创建场景要求文本内容不能为空
        validateTextRequest(request, false);
        // 拼接 Dify 文本文档创建接口地址，并构造请求体
        String url = buildApiUrl(request.getBaseUrl(),
                String.format(DifyKnowledgeConstant.API_CREATE_DOCUMENT_BY_TEXT, request.getDatasetId()));
        Map<String, Object> payload = buildTextPayload(request);
        // 使用 JSON 请求创建文档，并解析 Dify 返回的批处理信息
        return executeOperation(url, request.getApiKey(), payload);
    }

    /**
     * 通过上传文件创建 Dify 知识库文档。
     *
     * @param request 文件文档创建请求
     * @return Dify 文档操作结果
     */
    @Override
    public DifyDocumentOperationResponse createDocumentByFile(DifyDocumentCreateByFileRequest request) {
        // 校验知识库、鉴权和文件流参数，创建场景要求文件不能为空
        validateFileRequest(request, false);
        // 拼接 Dify 文件文档创建接口地址，并构造 multipart 表单参数
        String url = buildApiUrl(request.getBaseUrl(),
                String.format(DifyKnowledgeConstant.API_CREATE_DOCUMENT_BY_FILE, request.getDatasetId()));
        Map<String, String> formData = buildFileFormData(request);
        // 上传文件到 Dify 知识库，并解析文档创建结果
        return executeFileOperation(url, request.getApiKey(), formData, request.getFilename(),
                request.getInputStream(), request.getContentType());
    }

    @Override
    public DifyDocumentIndexStatusResponse getDocumentIndexingStatus(String baseUrl, String apiKey, String datasetId,
                                                                     String batchId) {
        validateCommonParams(baseUrl, apiKey, datasetId);
        if (StringUtils.isBlank(batchId)) {
            throw new IllegalArgumentException("batchId is required");
        }
        String url = buildApiUrl(baseUrl,
                String.format(DifyKnowledgeConstant.API_GET_DOCUMENT_INDEX_STATUS, datasetId, batchId));
        Response response = okHttpUtils.get(url, buildHeaders(apiKey));
        try {
            String raw = readBody(response.body());
            if (!response.isSuccessful()) {
                throw new RuntimeException("Dify get indexing status failed: " + response.code() + ", body=" + raw);
            }
            return parseIndexStatus(raw, datasetId, batchId);
        } finally {
            response.close();
        }
    }

    @Override
    public DifyDocumentDetailResponse getDocument(String baseUrl, String apiKey, String datasetId, String documentId) {
        validateCommonParams(baseUrl, apiKey, datasetId);
        if (StringUtils.isBlank(documentId)) {
            throw new IllegalArgumentException("documentId is required");
        }
        String url = buildApiUrl(baseUrl, String.format(DifyKnowledgeConstant.API_GET_DOCUMENT, datasetId, documentId));
        Response response = okHttpUtils.get(url, buildHeaders(apiKey));
        try {
            String raw = readBody(response.body());
            if (!response.isSuccessful()) {
                throw new RuntimeException("Dify get document failed: " + response.code() + ", body=" + raw);
            }
            return parseDocumentDetail(raw, datasetId, documentId);
        } finally {
            response.close();
        }
    }

    /**
     * 通过文本内容更新 Dify 知识库文档。
     *
     * @param documentId Dify 文档 id
     * @param request 文本文档更新请求
     * @return Dify 文档操作结果
     */
    @Override
    public DifyDocumentOperationResponse updateDocumentByText(String documentId, DifyDocumentCreateByTextRequest request) {
        // 更新必须指定 Dify 文档 id，避免误操作整个知识库
        if (StringUtils.isBlank(documentId)) {
            throw new IllegalArgumentException("documentId is required");
        }
        // 更新场景允许部分字段为空，但仍需校验知识库和鉴权参数
        validateTextRequest(request, true);
        // 拼接 Dify 文本文档更新接口地址，并构造更新请求体
        String url = buildApiUrl(request.getBaseUrl(),
                String.format(DifyKnowledgeConstant.API_UPDATE_DOCUMENT_BY_TEXT, request.getDatasetId(), documentId));
        Map<String, Object> payload = buildTextPayload(request);
        // 使用 JSON 请求更新文档，并返回 Dify 操作结果
        return executeOperation(url, request.getApiKey(), payload);
    }

    /**
     * 通过上传文件更新 Dify 知识库文档。
     *
     * @param documentId Dify 文档 id
     * @param request 文件文档更新请求
     * @return Dify 文档操作结果
     */
    @Override
    public DifyDocumentOperationResponse updateDocumentByFile(String documentId, DifyDocumentCreateByFileRequest request) {
        // 更新必须指定 Dify 文档 id，避免误操作整个知识库
        if (StringUtils.isBlank(documentId)) {
            throw new IllegalArgumentException("documentId is required");
        }
        // 更新场景允许文件字段按请求需要传入，但基础参数仍必须完整
        validateFileRequest(request, true);
        // 拼接 Dify 文件文档更新接口地址，并构造 multipart 表单参数
        String url = buildApiUrl(request.getBaseUrl(),
                String.format(DifyKnowledgeConstant.API_UPDATE_DOCUMENT_BY_FILE, request.getDatasetId(), documentId));
        Map<String, String> formData = buildFileFormData(request);
        // 上传新文件内容到 Dify，并解析文档更新结果
        return executeFileOperation(url, request.getApiKey(), formData, request.getFilename(),
                request.getInputStream(), request.getContentType());
    }

    private DifyDocumentOperationResponse executeOperation(String url, String apiKey, Map<String, Object> payload) {
        Response response = okHttpUtils.postJson(url, JSONUtil.toJsonStr(payload), buildHeaders(apiKey));
        try {
            String raw = readBody(response.body());
            if (!response.isSuccessful()) {
                throw new RuntimeException("Dify document operation failed: " + response.code() + ", body=" + raw);
            }
            return parseOperation(raw);
        } finally {
            response.close();
        }
    }

    private DifyDocumentOperationResponse executeFileOperation(String url,
                                                               String apiKey,
                                                               Map<String, String> formData,
                                                               String filename,
                                                               InputStream inputStream,
                                                               String contentType) {
        Response response = okHttpUtils.postMultipart(url, formData, "file",
                StringUtils.defaultIfBlank(filename, "document.bin"),
                inputStream,
                StringUtils.defaultIfBlank(contentType, "application/octet-stream"),
                buildHeaders(apiKey));
        try {
            String raw = readBody(response.body());
            if (!response.isSuccessful()) {
                throw new RuntimeException("Dify document file operation failed: " + response.code() + ", body=" + raw);
            }
            return parseOperation(raw);
        } finally {
            response.close();
        }
    }

    private Map<String, Object> buildTextPayload(DifyDocumentCreateByTextRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", StringUtils.defaultIfBlank(request.getName(), "document"));
        payload.put("text", StringUtils.defaultString(request.getText()));
        payload.put("indexing_technique",
                StringUtils.defaultIfBlank(request.getIndexingTechnique(), DifyKnowledgeConstant.DEFAULT_INDEXING_TECHNIQUE));
        Map<String, Object> processRule = new HashMap<>();
        processRule.put("mode",
                StringUtils.defaultIfBlank(request.getProcessRuleMode(), DifyKnowledgeConstant.DEFAULT_PROCESS_RULE_MODE));
        payload.put("process_rule", processRule);
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            payload.put("metadata", request.getMetadata());
        }
        return payload;
    }

    private Map<String, String> buildFileFormData(DifyDocumentCreateByFileRequest request) {
        Map<String, String> formData = new HashMap<>();
        formData.put("name", StringUtils.defaultIfBlank(request.getName(), request.getFilename()));
        formData.put("indexing_technique",
                StringUtils.defaultIfBlank(request.getIndexingTechnique(), DifyKnowledgeConstant.DEFAULT_INDEXING_TECHNIQUE));
        Map<String, Object> processRule = new HashMap<>();
        processRule.put("mode",
                StringUtils.defaultIfBlank(request.getProcessRuleMode(), DifyKnowledgeConstant.DEFAULT_PROCESS_RULE_MODE));
        formData.put("process_rule", JSONUtil.toJsonStr(processRule));
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            formData.put("metadata", JSONUtil.toJsonStr(request.getMetadata()));
        }
        return formData;
    }

    private DifyDocumentOperationResponse parseOperation(String raw) {
        DifyDocumentOperationResponse result = new DifyDocumentOperationResponse();
        result.setRawBody(raw);
        if (!JSONUtil.isTypeJSON(raw)) {
            return result;
        }
        result.setDatasetId(JSONUtil.parseObj(raw).getStr("dataset_id"));
        result.setDocumentId(JSONUtil.parseObj(raw).getStr("document_id"));
        result.setBatchId(JSONUtil.parseObj(raw).getStr("batch"));
        if (StringUtils.isBlank(result.getBatchId())) {
            result.setBatchId(JSONUtil.parseObj(raw).getStr("batch_id"));
        }
        result.setName(JSONUtil.parseObj(raw).getStr("name"));
        result.setIndexingStatus(JSONUtil.parseObj(raw).getStr("indexing_status"));
        return result;
    }

    private DifyDocumentIndexStatusResponse parseIndexStatus(String raw, String datasetId, String batchId) {
        DifyDocumentIndexStatusResponse result = new DifyDocumentIndexStatusResponse();
        result.setRawBody(raw);
        result.setDatasetId(datasetId);
        result.setBatchId(batchId);
        if (!JSONUtil.isTypeJSON(raw)) {
            return result;
        }
        result.setDocumentId(JSONUtil.parseObj(raw).getStr("document_id"));
        result.setIndexingStatus(JSONUtil.parseObj(raw).getStr("indexing_status"));
        result.setCompletedSegments(JSONUtil.parseObj(raw).getInt("completed_segments"));
        result.setTotalSegments(JSONUtil.parseObj(raw).getInt("total_segments"));
        result.setErrorMessage(JSONUtil.parseObj(raw).getStr("error"));
        if (StringUtils.isBlank(result.getErrorMessage())) {
            result.setErrorMessage(JSONUtil.parseObj(raw).getStr("error_message"));
        }
        return result;
    }

    private DifyDocumentDetailResponse parseDocumentDetail(String raw, String datasetId, String documentId) {
        DifyDocumentDetailResponse result = new DifyDocumentDetailResponse();
        result.setRawBody(raw);
        result.setDatasetId(datasetId);
        result.setDocumentId(documentId);
        if (!JSONUtil.isTypeJSON(raw)) {
            return result;
        }
        result.setName(JSONUtil.parseObj(raw).getStr("name"));
        result.setDataSourceType(JSONUtil.parseObj(raw).getStr("data_source_type"));
        result.setIndexingStatus(JSONUtil.parseObj(raw).getStr("indexing_status"));
        result.setWordCount(JSONUtil.parseObj(raw).getInt("word_count"));
        result.setHitCount(JSONUtil.parseObj(raw).getInt("hit_count"));
        return result;
    }

    private String buildApiUrl(String baseUrl, String path) {
        String normalizedBaseUrl = StringUtils.defaultString(baseUrl).trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + path;
    }

    private Map<String, String> buildHeaders(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
    }

    private String readBody(ResponseBody body) {
        if (body == null) {
            return "";
        }
        try {
            return body.string();
        } catch (IOException e) {
            log.error("Read Dify response body failed", e);
            throw new RuntimeException("Read Dify response body failed", e);
        }
    }

    private void validateCommonParams(String baseUrl, String apiKey, String datasetId) {
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl is required");
        }
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("apiKey is required");
        }
        if (StringUtils.isBlank(datasetId)) {
            throw new IllegalArgumentException("datasetId is required");
        }
    }

    private void validateTextRequest(DifyDocumentCreateByTextRequest request, boolean allowEmptyText) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        validateCommonParams(request.getBaseUrl(), request.getApiKey(), request.getDatasetId());
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("name is required");
        }
        if (!allowEmptyText && StringUtils.isBlank(request.getText())) {
            throw new IllegalArgumentException("text is required");
        }
    }

    private void validateFileRequest(DifyDocumentCreateByFileRequest request, boolean allowEmptyFile) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        validateCommonParams(request.getBaseUrl(), request.getApiKey(), request.getDatasetId());
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("name is required");
        }
        if (StringUtils.isBlank(request.getFilename())) {
            throw new IllegalArgumentException("filename is required");
        }
        if (!allowEmptyFile && request.getInputStream() == null) {
            throw new IllegalArgumentException("inputStream is required");
        }
    }
}
