package com.ttbt.smartclass.constant;

/**
 * Dify 知识库相关常量
 */
public interface DifyKnowledgeConstant {

    String DEFAULT_INDEXING_TECHNIQUE = "high_quality";

    String DEFAULT_PROCESS_RULE_MODE = "automatic";

    String API_DATASETS = "/datasets";

    String API_CREATE_DOCUMENT_BY_TEXT = "/datasets/%s/document/create-by-text";

    String API_CREATE_DOCUMENT_BY_FILE = "/datasets/%s/document/create-by-file";

    String API_GET_DOCUMENT = "/datasets/%s/documents/%s";

    String API_GET_DOCUMENT_INDEX_STATUS = "/datasets/%s/documents/%s/indexing-status";

    String API_UPDATE_DOCUMENT_BY_TEXT = "/datasets/%s/documents/%s/update-by-text";

    String API_UPDATE_DOCUMENT_BY_FILE = "/datasets/%s/documents/%s/update-by-file";

    String REDIS_SYNC_LOCK_KEY = "course:knowledge:sync:lock:";

    long REDIS_SYNC_LOCK_TTL_SECONDS = 60L;
}