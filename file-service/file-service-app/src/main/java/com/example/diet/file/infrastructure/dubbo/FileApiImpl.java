package com.example.diet.file.infrastructure.dubbo;

import com.example.diet.file.api.FileApi;
import com.example.diet.file.api.command.DeleteFileCommand;
import com.example.diet.file.api.command.GenerateUploadUrlCommand;
import com.example.diet.file.api.query.CheckFileExistsQuery;
import com.example.diet.file.api.query.GenerateDownloadUrlQuery;
import com.example.diet.file.api.response.PresignedUrlResponse;
import com.example.diet.file.application.FileApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 文件服务 Dubbo API 实现
 */
@DubboService
@RequiredArgsConstructor
public class FileApiImpl implements FileApi {

    private final FileApplicationService applicationService;

    @Override
    public PresignedUrlResponse generateUploadUrl(GenerateUploadUrlCommand command) {
        return applicationService.generateUploadUrl(command);
    }

    @Override
    public PresignedUrlResponse generateDownloadUrl(GenerateDownloadUrlQuery query) {
        return applicationService.generateDownloadUrl(query);
    }

    @Override
    public void deleteFile(DeleteFileCommand command) {
        applicationService.deleteFile(command);
    }

    @Override
    public boolean fileExists(CheckFileExistsQuery query) {
        return applicationService.fileExists(query);
    }
}
