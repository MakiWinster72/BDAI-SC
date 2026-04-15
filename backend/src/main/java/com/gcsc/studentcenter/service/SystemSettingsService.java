package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.SystemSetting;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemSettingsService {

    public static final String SETTING_KEY = "system.settings";

    private final SystemSettingRepository systemSettingRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    public SystemSettingsService(
        SystemSettingRepository systemSettingRepository,
        AppUserRepository appUserRepository,
        ObjectMapper objectMapper
    ) {
        this.systemSettingRepository = systemSettingRepository;
        this.appUserRepository = appUserRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSettings() {
        Map<String, Object> raw = systemSettingRepository.findById(SETTING_KEY)
                .map(SystemSetting::getSettingValue)
                .map(this::parseMap)
                .orElseGet(LinkedHashMap::new);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("allowRegistration", booleanValue(raw.get("allowRegistration"), true));
        return result;
    }

    @Transactional
    public Map<String, Object> updateSettings(String operatorUsername, Map<String, Object> updates) {
        AppUser operator = appUserRepository.findByUsername(operatorUsername)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (operator.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("无权限修改系统设置");
        }

        Map<String, Object> raw = systemSettingRepository.findById(SETTING_KEY)
                .map(SystemSetting::getSettingValue)
                .map(this::parseMap)
                .orElseGet(LinkedHashMap::new);

        raw.putAll(updates);

        SystemSetting systemSetting = systemSettingRepository.findById(SETTING_KEY)
                .orElseGet(SystemSetting::new);
        systemSetting.setSettingKey(SETTING_KEY);
        systemSetting.setSettingValue(writeJson(raw));
        systemSetting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(systemSetting);

        return getSettings();
    }

    @Transactional(readOnly = true)
    public boolean isRegistrationAllowed() {
        Map<String, Object> raw = systemSettingRepository.findById(SETTING_KEY)
                .map(SystemSetting::getSettingValue)
                .map(this::parseMap)
                .orElseGet(LinkedHashMap::new);
        return booleanValue(raw.get("allowRegistration"), true);
    }

    private Map<String, Object> parseMap(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException exception) {
            return new LinkedHashMap<>();
        }
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("系统设置保存失败");
        }
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return fallback;
    }
}
