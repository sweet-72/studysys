package com.ttbt.smartclass.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.config.CosClientConfig;
import com.ttbt.smartclass.constant.FileConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.enums.FileUploadBizEnum;
import com.ttbt.smartclass.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cos 对象存储操作
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    // 创建一个线程池，用于处理大文件上传
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 获取 COS 访问地址
     */
    private String getCosHost() {
        return String.format("https://%s.cos.%s.myqcloud.com", cosClientConfig.getBucket(), cosClientConfig.getRegion());
    }

    /**
     * 上传对象（本地文件路径）
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象（File）
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传视频文件
     */
    public PutObjectResult putVideo(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("video/mp4");
        putObjectRequest.setMetadata(metadata);
        putObjectRequest.setStorageClass(StorageClass.Standard);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文档文件
     */
    public PutObjectResult putDocument(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        ObjectMetadata metadata = new ObjectMetadata();
        String extension = key.substring(key.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                metadata.setContentType("application/pdf");
                break;
            case "doc":
            case "docx":
                metadata.setContentType("application/msword");
                break;
            case "ppt":
            case "pptx":
                metadata.setContentType("application/vnd.ms-powerpoint");
                break;
            case "xls":
            case "xlsx":
                metadata.setContentType("application/vnd.ms-excel");
                break;
            case "txt":
                metadata.setContentType("text/plain");
                break;
            default:
                metadata.setContentType("application/octet-stream");
        }
        putObjectRequest.setMetadata(metadata);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 使用流上传对象
     */
    public PutObjectResult putObject(String key, InputStream input, long contentLength, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, input, metadata);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 异步上传大文件
     */
    public void putLargeFileAsync(String key, File file) {
        executorService.submit(() -> {
            try {
                PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
                cosClient.putObject(putObjectRequest);
                log.info("Large file uploaded successfully: {}", key);
            } catch (Exception e) {
                log.error("Failed to upload large file: {}", key, e);
            }
        });
    }

    /**
     * 上传文件：优先 COS，失败自动降级到本地
     */
    public String uploadFile(MultipartFile multipartFile, FileUploadBizEnum biz) {
        validFile(multipartFile, biz);

        String originalFilename = multipartFile.getOriginalFilename();
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        String safeSuffix = StringUtils.isBlank(fileSuffix) ? "" : "." + fileSuffix.toLowerCase();
        String filename = UUID.randomUUID().toString().replace("-", "") + safeSuffix;
        String filepath = String.format("/%s/%s", biz.getValue(), filename);

        if (isCosAvailable()) {
            try {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(multipartFile.getSize());
                objectMetadata.setContentType(multipartFile.getContentType());
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        cosClientConfig.getBucket(),
                        filepath,
                        multipartFile.getInputStream(),
                        objectMetadata
                );
                cosClient.putObject(putObjectRequest);
                return getCosHost() + filepath;
            } catch (Exception e) {
                log.warn("COS upload failed, fallback to local. biz={}, file={}", biz.getValue(), originalFilename, e);
            }
        } else {
            log.info("COS config missing, fallback to local. biz={}, file={}", biz.getValue(), originalFilename);
        }

        return saveToLocal(multipartFile, biz, filename);
    }

    private boolean isCosAvailable() {
        return cosClient != null
                && StringUtils.isNotBlank(cosClientConfig.getBucket())
                && StringUtils.isNotBlank(cosClientConfig.getRegion())
                && StringUtils.isNotBlank(cosClientConfig.getAccessKey())
                && StringUtils.isNotBlank(cosClientConfig.getSecretKey());
    }

    private String saveToLocal(MultipartFile multipartFile, FileUploadBizEnum biz, String filename) {
        try {
            String uploadDir = System.getProperty("user.dir")
                    + File.separator
                    + "upload"
                    + File.separator
                    + biz.getValue()
                    + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "create local upload directory failed");
            }
            File target = new File(dir, filename);
            multipartFile.transferTo(target);
            return String.format("/api/upload/%s/%s", biz.getValue(), filename);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 根据上传业务类型校验文件大小和后缀。
     *
     * @param multipartFile 上传文件
     * @param biz 上传业务类型
     */
    public void validFile(MultipartFile multipartFile, FileUploadBizEnum biz) {
        // 提取文件大小和后缀，后续按业务类型使用不同限制
        long fileSize = multipartFile.getSize();
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (fileSuffix != null) {
            fileSuffix = fileSuffix.toLowerCase();
        }

        // 通用文件只限制大小，不限制具体后缀
        if (FileUploadBizEnum.GENERAL.equals(biz)) {
            if (fileSize > FileConstant.ONE_HUNDRED_MB) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过100MB");
            }
            return;
        }

        // 头像文件限制为图片格式，并使用头像专属大小上限
        if (FileUploadBizEnum.USER_AVATAR.equals(biz)) {
            if (fileSize > FileConstant.ONE_MB * cosClientConfig.getUpload().getMaxAvatarSize()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件大小不能超过 " + cosClientConfig.getUpload().getMaxAvatarSize() + "MB");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else if (FileUploadBizEnum.VIDEO.equals(biz)) {
            // 视频文件按配置的大小上限和允许后缀列表校验
            if (fileSize > FileConstant.ONE_MB * cosClientConfig.getUpload().getMaxVideoSize()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件大小不能超过 " + cosClientConfig.getUpload().getMaxVideoSize() + "MB");
            }
            List<String> allowedVideoTypes = Arrays.asList(cosClientConfig.getUpload().getAllowedVideoTypes().split(","));
            if (!allowedVideoTypes.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件类型错误，仅支持: " + cosClientConfig.getUpload().getAllowedVideoTypes());
            }
        } else if (FileUploadBizEnum.DOCUMENT.equals(biz)) {
            // 文档文件按配置的大小上限和办公文档后缀列表校验
            if (fileSize > FileConstant.ONE_MB * cosClientConfig.getUpload().getMaxDocumentSize()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件大小不能超过 " + cosClientConfig.getUpload().getMaxDocumentSize() + "MB");
            }
            List<String> allowedDocumentTypes = Arrays.asList(cosClientConfig.getUpload().getAllowedDocumentTypes().split(","));
            if (!allowedDocumentTypes.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件类型错误，仅支持: " + cosClientConfig.getUpload().getAllowedDocumentTypes());
            }
        } else if (FileUploadBizEnum.MATERIAL.equals(biz)) {
            // 课程资料支持文档和压缩包，按资料配置校验大小和后缀
            if (fileSize > FileConstant.ONE_MB * cosClientConfig.getUpload().getMaxMaterialSize()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件大小不能超过 " + cosClientConfig.getUpload().getMaxMaterialSize() + "MB");
            }
            List<String> allowedMaterialTypes = Arrays.asList(cosClientConfig.getUpload().getAllowedMaterialTypes().split(","));
            if (!allowedMaterialTypes.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "文件类型错误，仅支持: " + cosClientConfig.getUpload().getAllowedMaterialTypes());
            }
        }
    }
}

