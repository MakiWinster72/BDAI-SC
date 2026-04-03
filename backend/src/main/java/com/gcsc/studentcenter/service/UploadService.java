package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private final Path uploadRoot;
    private final AchievementUploadSettingsService achievementUploadSettingsService;

    private static final Set<String> ALLOWED_FILE_EXT = Set.of(
        ".doc", ".ppt", ".xls", ".docx", ".pptx", ".xlsx", ".pdf",
        ".zip", ".rar", ".7z", ".tar"
    );

    public UploadService(
        @Value("${app.upload-dir:./uploads}") String uploadDir,
        AchievementUploadSettingsService achievementUploadSettingsService
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.achievementUploadSettingsService = achievementUploadSettingsService;
    }

    public UploadResponse upload(MultipartFile file, String context) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        achievementUploadSettingsService.validateUpload(context, file);

        String contentType = file.getContentType();
        String mediaType = resolveMediaType(contentType);
        if (mediaType == null) {
            String ext = extractExtension(file.getOriginalFilename());
            if (!ext.isEmpty() && ALLOWED_FILE_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
                mediaType = "FILE";
            } else {
                throw new IllegalArgumentException("仅支持图片/视频/常见附件");
            }
        }

        LocalDate now = LocalDate.now();
        String ext = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path targetDir = uploadRoot.resolve(Paths.get(String.valueOf(now.getYear()),
            String.format("%02d", now.getMonthValue())));
        Path targetFile = targetDir.resolve(filename);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new IllegalStateException("上传失败，请稍后再试");
        }

        String url = "/uploads/" + now.getYear() + "/" + String.format("%02d", now.getMonthValue()) + "/" + filename;
        return new UploadResponse(true, url, mediaType, file.getOriginalFilename());
    }

    private String resolveMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }
        if (contentType.startsWith("image/")) {
            return "IMAGE";
        }
        if (contentType.startsWith("video/")) {
            return "VIDEO";
        }
        return null;
    }

    private String extractExtension(String name) {
        if (name == null) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return name.substring(idx).toLowerCase();
    }
}
