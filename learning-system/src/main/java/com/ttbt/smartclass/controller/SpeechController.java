package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.SpeechRecognizeVO;
import com.ttbt.smartclass.service.SpeechRecognitionService;
import com.ttbt.smartclass.service.UserService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    @Resource
    private SpeechRecognitionService speechRecognitionService;

    @Resource
    private UserService userService;

    @PostMapping("/recognize")
    public BaseResponse<SpeechRecognizeVO> recognizeSpeech(@RequestParam("file") MultipartFile file,
                                                           HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音频文件不能为空");
        }

        Path tempFile = null;
        try {
            String originalName = StringUtils.defaultString(file.getOriginalFilename(), "speech.webm");
            tempFile = Files.createTempFile("speech_" + loginUser.getId() + "_", resolveSuffix(originalName));
            file.transferTo(tempFile.toFile());

            String recognizedText = speechRecognitionService.recognizeSpeech(tempFile.toAbsolutePath().toString());
            if (StringUtils.isBlank(recognizedText)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前未配置语音识别服务");
            }

            SpeechRecognizeVO vo = new SpeechRecognizeVO();
            vo.setText(recognizedText);
            return ResultUtils.success(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "音频文件处理失败");
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Temporary file cleanup failure does not affect recognition result.
                }
            }
        }
    }

    private String resolveSuffix(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".webm";
        }
        String suffix = fileName.substring(dotIndex).toLowerCase();
        return suffix.length() > 10 ? ".webm" : suffix;
    }
}
