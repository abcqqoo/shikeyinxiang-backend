package com.example.diet.file.api;

import com.example.diet.file.api.command.*;
import com.example.diet.file.api.query.*;
import com.example.diet.file.api.response.*;

/**
 * 文件服务 Dubbo API
 */
public interface FileApi {

    /**
     * 生成上传预签名 URL
     */
    PresignedUrlResponse generateUploadUrl(GenerateUploadUrlCommand command);

    /**
     * 生成下载预签名 URL
     */
    PresignedUrlResponse generateDownloadUrl(GenerateDownloadUrlQuery query);

    /**
     * 删除文件
     */
    void deleteFile(DeleteFileCommand command);

    /**
     * 检查文件是否存在
     */
    boolean fileExists(CheckFileExistsQuery query);
}
