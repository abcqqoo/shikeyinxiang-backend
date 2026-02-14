package com.example.diet.file.application;

import com.example.diet.file.api.command.DeleteFileCommand;
import com.example.diet.file.api.command.GenerateUploadUrlCommand;
import com.example.diet.file.api.query.CheckFileExistsQuery;
import com.example.diet.file.api.query.GenerateDownloadUrlQuery;
import com.example.diet.file.api.response.PresignedUrlResponse;
import com.example.diet.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件应用服务
 */
@Slf4j
@Service
public class FileApplicationService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String allowedTypes;

    private static final int MIN_DOWNLOAD_URL_EXPIRE_MINUTES = 5;

    @Autowired
    public FileApplicationService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Qualifier("bucketName") String bucketName,
            @Qualifier("allowedTypes") String allowedTypes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.allowedTypes = allowedTypes;
    }

    public PresignedUrlResponse generateUploadUrl(GenerateUploadUrlCommand command) {
        try {
            String extension = getExtensionFromContentType(command.getContentType());
            if (!isValidFileType(extension)) {
                throw new BusinessException(400, "不支持的文件类型，允许的类型：" + allowedTypes);
            }

            String fileName = command.getKey();
            if (!StringUtils.hasText(fileName)) {
                fileName = "uploads/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
            }

            int expiration = command.getExpirationMinutes() > 0 ? command.getExpirationMinutes() : 15;

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(command.getContentType())
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiration))
                    .putObjectRequest(objectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

            return PresignedUrlResponse.builder()
                    .url(presignedUrl)
                    .key(fileName)
                    .expiresAt(java.time.LocalDateTime.now().plusMinutes(expiration))
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (S3Exception e) {
            log.error("生成上传预签名URL失败", e);
            throw new BusinessException(500, "生成预签名URL失败：" + e.getMessage());
        }
    }

    @Cacheable(
            value = "presignedUrls",
            key = "'download_' + #query.key + '_' + (#query.expirationMinutes <= 0 ? 60 : (#query.expirationMinutes < 5 ? 5 : #query.expirationMinutes))"
    )
    public PresignedUrlResponse generateDownloadUrl(GenerateDownloadUrlQuery query) {
        try {
            if (!StringUtils.hasText(query.getKey())) {
                throw new BusinessException(400, "文件路径不能为空");
            }

            int expiration = query.getExpirationMinutes() > 0 ? query.getExpirationMinutes() : 60;
            expiration = Math.max(expiration, MIN_DOWNLOAD_URL_EXPIRE_MINUTES);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(query.getKey())
                    .responseCacheControl("public, max-age=86400")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiration))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

            return PresignedUrlResponse.builder()
                    .url(presignedUrl)
                    .key(query.getKey())
                    .expiresAt(java.time.LocalDateTime.now().plusMinutes(expiration))
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (S3Exception e) {
            log.error("生成下载预签名URL失败", e);
            throw new BusinessException(500, "生成预签名URL失败：" + e.getMessage());
        }
    }

    public void deleteFile(DeleteFileCommand command) {
        try {
            if (!StringUtils.hasText(command.getKey())) {
                throw new BusinessException(400, "文件路径不能为空");
            }

            if (!checkFileExists(command.getKey())) {
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(command.getKey())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("文件删除成功: {}", command.getKey());

        } catch (BusinessException e) {
            throw e;
        } catch (S3Exception e) {
            log.error("删除文件失败", e);
            throw new BusinessException(500, "删除文件失败：" + e.getMessage());
        }
    }

    public boolean fileExists(CheckFileExistsQuery query) {
        if (!StringUtils.hasText(query.getKey())) {
            return false;
        }
        return checkFileExists(query.getKey());
    }

    private boolean checkFileExists(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private boolean isValidFileType(String extension) {
        if (extension == null) {
            return false;
        }
        List<String> types = Arrays.asList(allowedTypes.split(","));
        return types.stream().anyMatch(t -> t.trim().equalsIgnoreCase(extension));
    }

    private String getExtensionFromContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            case "application/pdf" -> "pdf";
            default -> null;
        };
    }
}
