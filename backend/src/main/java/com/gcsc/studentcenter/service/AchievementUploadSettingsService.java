package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcsc.studentcenter.dto.AchievementUploadSettingsRequest;
import com.gcsc.studentcenter.dto.AchievementUploadSettingsResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.SystemSetting;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AchievementUploadSettingsService {

    public static final String SETTING_KEY = "achievement.upload.limits";
    private static final int DEFAULT_IMAGE_MAX_COUNT = 3;
    private static final int DEFAULT_IMAGE_MAX_SIZE_MB = 10;
    private static final int DEFAULT_ATTACHMENT_MAX_SIZE_MB = 50;
    private static final int MIN_IMAGE_MAX_COUNT = 1;
    private static final int MAX_IMAGE_MAX_COUNT = 9;
    private static final int MIN_FILE_MAX_SIZE_MB = 1;
    private static final int MAX_FILE_MAX_SIZE_MB = 200;

    private final SystemSettingRepository systemSettingRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    public AchievementUploadSettingsService(
        SystemSettingRepository systemSettingRepository,
        AppUserRepository appUserRepository,
        ObjectMapper objectMapper
    ) {
        this.systemSettingRepository = systemSettingRepository;
        this.appUserRepository = appUserRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AchievementUploadSettingsResponse getSettings() {
        return toResponse(readSettingsMap());
    }

    @Transactional
    public AchievementUploadSettingsResponse updateSettings(
        String operatorUsername,
        AchievementUploadSettingsRequest request
    ) {
        AppUser operator = appUserRepository.findByUsername(operatorUsername)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (operator.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("无权限修改后台设置");
        }

        int imageMaxCount = normalizeRange(
            request.getImageMaxCount(),
            MIN_IMAGE_MAX_COUNT,
            MAX_IMAGE_MAX_COUNT,
            "图片数量限制应在 1 到 9 之间"
        );
        int imageMaxSizeMb = normalizeRange(
            request.getImageMaxSizeMb(),
            MIN_FILE_MAX_SIZE_MB,
            MAX_FILE_MAX_SIZE_MB,
            "图片大小限制应在 1MB 到 200MB 之间"
        );
        int attachmentMaxSizeMb = normalizeRange(
            request.getAttachmentMaxSizeMb(),
            MIN_FILE_MAX_SIZE_MB,
            MAX_FILE_MAX_SIZE_MB,
            "附件大小限制应在 1MB 到 200MB 之间"
        );

        Map<String, Integer> settings = new LinkedHashMap<>();
        settings.put("imageMaxCount", imageMaxCount);
        settings.put("imageMaxSizeMb", imageMaxSizeMb);
        settings.put("attachmentMaxSizeMb", attachmentMaxSizeMb);

        SystemSetting systemSetting = systemSettingRepository.findById(SETTING_KEY)
            .orElseGet(SystemSetting::new);
        systemSetting.setSettingKey(SETTING_KEY);
        systemSetting.setSettingValue(writeSettingsMap(settings));
        systemSetting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(systemSetting);

        return new AchievementUploadSettingsResponse(
            imageMaxCount,
            imageMaxSizeMb,
            attachmentMaxSizeMb
        );
    }

    @Transactional(readOnly = true)
    public void validateAchievementImageCount(String rawImageUrlsJson) {
        int imageCount = parseListSize(rawImageUrlsJson);
        int maxCount = readIntValue("imageMaxCount", DEFAULT_IMAGE_MAX_COUNT);
        if (imageCount > maxCount) {
            throw new IllegalArgumentException("最多上传 " + maxCount + " 张图片");
        }
    }

    @Transactional(readOnly = true)
    public void validateUploadSize(String context, long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return;
        }
        if ("achievement-image".equals(context)) {
            validateByMegabytes(sizeInBytes, readIntValue("imageMaxSizeMb", DEFAULT_IMAGE_MAX_SIZE_MB), "图片");
        }
        if ("achievement-attachment".equals(context)) {
            validateByMegabytes(sizeInBytes, readIntValue("attachmentMaxSizeMb", DEFAULT_ATTACHMENT_MAX_SIZE_MB), "附件");
        }
    }

    private void validateByMegabytes(long sizeInBytes, int maxSizeMb, String label) {
        long maxBytes = maxSizeMb * 1024L * 1024L;
        if (sizeInBytes > maxBytes) {
            throw new IllegalArgumentException(label + "大小不可超过 " + maxSizeMb + "MB");
        }
    }

    private int parseListSize(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return 0;
        }
        try {
            List<String> items = objectMapper.readValue(
                rawJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return items.size();
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("图片数据格式不正确");
        }
    }

    private AchievementUploadSettingsResponse toResponse(Map<String, Integer> settings) {
        return new AchievementUploadSettingsResponse(
            settings.getOrDefault("imageMaxCount", DEFAULT_IMAGE_MAX_COUNT),
            settings.getOrDefault("imageMaxSizeMb", DEFAULT_IMAGE_MAX_SIZE_MB),
            settings.getOrDefault("attachmentMaxSizeMb", DEFAULT_ATTACHMENT_MAX_SIZE_MB)
        );
    }

    private int readIntValue(String key, int defaultValue) {
        return readSettingsMap().getOrDefault(key, defaultValue);
    }

    private Map<String, Integer> readSettingsMap() {
        return systemSettingRepository.findById(SETTING_KEY)
            .map(SystemSetting::getSettingValue)
            .map(this::parseSettingsMap)
            .orElseGet(this::defaultSettingsMap);
    }

    private Map<String, Integer> parseSettingsMap(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return defaultSettingsMap();
        }
        try {
            Map<String, Integer> parsed = objectMapper.readValue(
                rawJson,
                objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Integer.class)
            );
            Map<String, Integer> merged = defaultSettingsMap();
            merged.putAll(parsed);
            return merged;
        } catch (JsonProcessingException exception) {
            return defaultSettingsMap();
        }
    }

    private String writeSettingsMap(Map<String, Integer> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("系统设置保存失败");
        }
    }

    private Map<String, Integer> defaultSettingsMap() {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        defaults.put("imageMaxCount", DEFAULT_IMAGE_MAX_COUNT);
        defaults.put("imageMaxSizeMb", DEFAULT_IMAGE_MAX_SIZE_MB);
        defaults.put("attachmentMaxSizeMb", DEFAULT_ATTACHMENT_MAX_SIZE_MB);
        return defaults;
    }

    private int normalizeRange(Integer value, int min, int max, String message) {
        if (value == null || value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
