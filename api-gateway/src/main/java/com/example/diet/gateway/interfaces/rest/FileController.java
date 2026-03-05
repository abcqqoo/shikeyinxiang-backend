package com.example.diet.gateway.interfaces.rest;

import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.command.*;
import com.example.diet.file.api.query.*;
import com.example.diet.file.api.response.*;
import com.example.diet.gateway.infrastructure.security.SecurityContextUtil;
import com.example.diet.shared.exception.BusinessException;
import com.example.diet.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

/**
 * 文件服务控制器
 * 代理 file-service 的 Dubbo API
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    @DubboReference
    private FileApi fileApi;

    /**
     * 生成上传预签名 URL
     */
    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generateUploadUrl(
            @Valid @RequestBody GenerateUploadUrlRequest request) {

        Long userId = getCurrentUserId();

        String fileType = request.getFileType() != null ? request.getFileType() : "general";
        String extension = resolveExtension(request.getContentType());
        String fileName = java.util.UUID.randomUUID().toString().replace("-", "");
        String key = extension == null
                ? String.format("%s/%d/%s", fileType, userId, fileName)
                : String.format("%s/%d/%s.%s", fileType, userId, fileName, extension);

        GenerateUploadUrlCommand command = GenerateUploadUrlCommand.builder()
                .key(key)
                .contentType(request.getContentType())
                .expirationMinutes(request.getExpirationMinutes() != null ? request.getExpirationMinutes() : 15)
                .build();

        PresignedUrlResponse response = fileApi.generateUploadUrl(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 生成下载预签名 URL
     */
    @GetMapping("/download-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generateDownloadUrl(
            @RequestParam String key,
            @RequestParam(required = false) Integer expirationMinutes) {

        Long userId = getCurrentUserId();
        if (!SecurityContextUtil.isAdmin()) {
            requireKeyOwnedByUser(key, userId);
        }

        GenerateDownloadUrlQuery query = GenerateDownloadUrlQuery.builder()
                .key(key)
                .expirationMinutes(expirationMinutes != null ? expirationMinutes : 60)
                .build();

        PresignedUrlResponse response = fileApi.generateDownloadUrl(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam String key) {
        // 验证用户登录
        Long userId = getCurrentUserId();
        requireKeyOwnedByUser(key, userId);

        fileApi.deleteFile(DeleteFileCommand.builder().key(key).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> fileExists(@RequestParam String key) {
        boolean exists = fileApi.fileExists(
                CheckFileExistsQuery.builder().key(key).build());
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // ==================== 管理员操作 ====================

    /**
     * 管理员删除文件
     */
    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteFile(@RequestParam String key) {
        fileApi.deleteFile(DeleteFileCommand.builder().key(key).build());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未授权");
        }
        return userId;
    }

    /**
     * 约束：非管理员删除文件只能删除“自己名下”的 key。
     * 网关生成 key 的格式为：{fileType}/{userId}/{uuid}[.ext]，其中 fileType 允许包含子目录。
     * 因此 userId 固定出现在倒数第二段。
     */
    private void requireKeyOwnedByUser(String key, Long userId) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(400, "文件路径不能为空");
        }

        String normalized = key.startsWith("/") ? key.substring(1) : key;
        String[] parts = normalized.split("/");
        if (parts.length < 2) {
            throw new BusinessException(400, "文件路径格式非法");
        }

        String ownerSegment = parts[parts.length - 2];
        if (!String.valueOf(userId).equals(ownerSegment)) {
            throw new BusinessException(403, "无权限删除该文件");
        }
    }

    private String resolveExtension(String contentType) {
        if (contentType == null) {
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

    // ==================== 请求 DTO ====================

    @Data
    public static class GenerateUploadUrlRequest {
        private String fileType;
        private String contentType;
        private Integer expirationMinutes;
    }
}
