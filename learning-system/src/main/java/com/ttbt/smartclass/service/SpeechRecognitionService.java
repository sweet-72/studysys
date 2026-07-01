package com.ttbt.smartclass.service;

/**
 * 语音识别服务接口
 */
public interface SpeechRecognitionService {

    /**
     * 语音转文字
     * 
     * @param audioUrl 语音文件 URL
     * @return 识别的文字内容
     */
    String recognizeSpeech(String audioUrl);

    /**
     * 语音转文字（带语言参数）
     * 
     * @param audioUrl 语音文件 URL
     * @param language 语言代码（zh-CN: 中文，en-US: 英文）
     * @return 识别的文字内容
     */
    String recognizeSpeech(String audioUrl, String language);
}
