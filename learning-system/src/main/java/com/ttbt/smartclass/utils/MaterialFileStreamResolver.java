package com.ttbt.smartclass.utils;

import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.entity.CourseMaterial;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 课程资料文件流解析器。
 * 负责将远程地址或本地上传路径解析为可读取的文件流。
 */
@Component
@Slf4j
public class MaterialFileStreamResolver {

    private static final String API_UPLOAD_PREFIX = "/api/upload/";
    private static final String UPLOAD_PREFIX = "/upload/";

    @Resource
    private OkHttpUtils okHttpUtils;

    /**
     * 解析课程资料文件地址并打开输入流。
     *
     * @param material 课程资料
     * @return 已解析的文件流信息
     */
    public ResolvedMaterialFile resolve(CourseMaterial material) {
        // 校验资料和文件地址，缺少 fileUrl 时无法打开文件流
        if (material == null || material.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course material is invalid");
        }
        String fileUrl = StringUtils.trimToEmpty(material.getFileUrl());
        if (StringUtils.isBlank(fileUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "material file_url is blank, materialId=" + material.getId());
        }
        // http/https 地址通过 OkHttp 读取，其他路径按本地上传文件解析
        if (isRemoteHttpUrl(fileUrl)) {
            return resolveRemoteFile(material, fileUrl);
        }
        return resolveLocalFile(material, fileUrl);
    }

    private ResolvedMaterialFile resolveRemoteFile(CourseMaterial material, String fileUrl) {
        Response response;
        try {
            response = okHttpUtils.get(fileUrl, null);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "read remote material file failed, materialId=" + material.getId()
                            + ", file_url=" + fileUrl
                            + ", reason=" + defaultReason(e.getMessage()));
        }
        if (response == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "remote material response is null, materialId=" + material.getId()
                            + ", file_url=" + fileUrl);
        }
        ResponseBody body = response.body();
        if (!response.isSuccessful() || body == null) {
            closeQuietly(response);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "read remote material file failed, materialId=" + material.getId()
                            + ", file_url=" + fileUrl
                            + ", httpCode=" + response.code());
        }
        String contentType = body.contentType() == null ? null : body.contentType().toString();
        String filename = extractFilename(fileUrl, material);
        log.info("resolved remote material file, materialId={}, file_url={}, filename={}",
                material.getId(), fileUrl, filename);
        return new ResolvedMaterialFile(
                body.byteStream(),
                body.contentLength(),
                StringUtils.defaultIfBlank(contentType, material.getFileType()),
                filename,
                fileUrl,
                response
        );
    }

    private ResolvedMaterialFile resolveLocalFile(CourseMaterial material, String fileUrl) {
        Path path = resolveLocalPath(fileUrl);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "local material file not found, materialId=" + material.getId()
                            + ", file_url=" + fileUrl
                            + ", resolvedPath=" + path.toAbsolutePath());
        }
        try {
            String contentType = Files.probeContentType(path);
            String filename = path.getFileName() == null ? buildFallbackFilename(material) : path.getFileName().toString();
            log.info("resolved local material file, materialId={}, file_url={}, resolvedPath={}",
                    material.getId(), fileUrl, path.toAbsolutePath());
            return new ResolvedMaterialFile(
                    Files.newInputStream(path),
                    Files.size(path),
                    StringUtils.defaultIfBlank(contentType, material.getFileType()),
                    filename,
                    path.toAbsolutePath().toString(),
                    null
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "open local material file failed, materialId=" + material.getId()
                            + ", file_url=" + fileUrl
                            + ", resolvedPath=" + path.toAbsolutePath()
                            + ", reason=" + defaultReason(e.getMessage()));
        }
    }

    private Path resolveLocalPath(String fileUrl) {
        String normalized = stripQueryAndFragment(fileUrl).replace('\\', '/');
        Path projectRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path uploadRoot = projectRoot.resolve("upload").normalize();

        if (normalized.startsWith(API_UPLOAD_PREFIX)) {
            String relative = normalized.substring(API_UPLOAD_PREFIX.length());
            Path resolved = uploadRoot.resolve(relative).normalize();
            validateUploadPath(uploadRoot, resolved, fileUrl);
            return resolved;
        }
        if (normalized.startsWith(UPLOAD_PREFIX)) {
            String relative = normalized.substring(UPLOAD_PREFIX.length());
            Path resolved = uploadRoot.resolve(relative).normalize();
            validateUploadPath(uploadRoot, resolved, fileUrl);
            return resolved;
        }
        if (normalized.startsWith("upload/")) {
            Path resolved = projectRoot.resolve(normalized).normalize();
            validateUploadPath(uploadRoot, resolved, fileUrl);
            return resolved;
        }

        Path path = Paths.get(normalized);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return projectRoot.resolve(path).normalize();
    }

    private void validateUploadPath(Path uploadRoot, Path resolved, String fileUrl) {
        if (!resolved.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "material file_url points outside upload directory, file_url=" + fileUrl);
        }
    }

    private String extractFilename(String fileUrl, CourseMaterial material) {
        String normalized = stripQueryAndFragment(fileUrl);
        try {
            URI uri = new URI(normalized);
            String path = uri.getPath();
            if (StringUtils.isNotBlank(path)) {
                Path filenamePath = Paths.get(path).getFileName();
                if (filenamePath != null) {
                    return filenamePath.toString();
                }
            }
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            // fallback below
        }
        Path path = Paths.get(normalized.replace('\\', '/')).getFileName();
        if (path != null) {
            return path.toString();
        }
        return buildFallbackFilename(material);
    }

    private String buildFallbackFilename(CourseMaterial material) {
        String title = StringUtils.defaultIfBlank(material.getTitle(), "material-" + material.getId());
        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String stripQueryAndFragment(String fileUrl) {
        String normalized = StringUtils.trimToEmpty(fileUrl);
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }
        return normalized;
    }

    private boolean isRemoteHttpUrl(String fileUrl) {
        return StringUtils.startsWithIgnoreCase(fileUrl, "http://")
                || StringUtils.startsWithIgnoreCase(fileUrl, "https://");
    }

    private void closeQuietly(Response response) {
        if (response != null) {
            response.close();
        }
    }

    private String defaultReason(String message) {
        return StringUtils.defaultIfBlank(message, "unknown error");
    }

    @Getter
    public static class ResolvedMaterialFile implements AutoCloseable {

        private final InputStream inputStream;
        private final long contentLength;
        private final String contentType;
        private final String filename;
        private final String sourceDescription;
        private final Closeable closeable;

        public ResolvedMaterialFile(InputStream inputStream,
                                    long contentLength,
                                    String contentType,
                                    String filename,
                                    String sourceDescription,
                                    Closeable closeable) {
            this.inputStream = inputStream;
            this.contentLength = contentLength;
            this.contentType = contentType;
            this.filename = filename;
            this.sourceDescription = sourceDescription;
            this.closeable = closeable;
        }

        @Override
        public void close() throws IOException {
            IOException error = null;
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                error = e;
            }
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (IOException e) {
                if (error == null) {
                    error = e;
                } else {
                    error.addSuppressed(e);
                }
            }
            if (error != null) {
                throw error;
            }
        }
    }
}
