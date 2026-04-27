package com.gcsc.studentcenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AchievementUploadSettingsService {

  public static final String SETTING_KEY = "achievement.upload.limits";
  public static final String TYPE_DOCUMENT = "document";
  public static final String TYPE_VIDEO = "video";
  public static final String TYPE_IMAGE = "image";
  public static final String TYPE_ARCHIVE = "archive";

  private static final int DEFAULT_IMAGE_MAX_COUNT = 3;
  private static final int DEFAULT_IMAGE_MAX_SIZE_MB = 10;
  private static final int DEFAULT_ATTACHMENT_MAX_COUNT = 10;
  private static final int DEFAULT_ATTACHMENT_MAX_SIZE_MB = 50;
  private static final int DEFAULT_SUPPORTING_DOC_MAX_COUNT = 10;
  private static final int DEFAULT_SUPPORTING_DOC_MAX_SIZE_MB = 50;
  private static final int MIN_IMAGE_MAX_COUNT = 1;
  private static final int MAX_IMAGE_MAX_COUNT = 9;
  private static final int MIN_ATTACHMENT_MAX_COUNT = 1;
  private static final int MAX_ATTACHMENT_MAX_COUNT = 20;
  private static final int MIN_SUPPORTING_DOC_MAX_COUNT = 1;
  private static final int MAX_SUPPORTING_DOC_MAX_COUNT = 20;
  private static final int MIN_FILE_MAX_SIZE_MB = 1;
  private static final int MAX_FILE_MAX_SIZE_MB = 200;

  private final SystemSettingRepository systemSettingRepository;
  private final AppUserRepository appUserRepository;
  private final ObjectMapper objectMapper;

  public AchievementUploadSettingsService(
      SystemSettingRepository systemSettingRepository,
      AppUserRepository appUserRepository,
      ObjectMapper objectMapper) {
    this.systemSettingRepository = systemSettingRepository;
    this.appUserRepository = appUserRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public AchievementUploadSettingsResponse getSettings() {
    return toResponse(readSettings());
  }

  @Transactional
  public AchievementUploadSettingsResponse updateSettings(
      String operatorUsername,
      AchievementUploadSettingsRequest request) {
    AppUser operator = appUserRepository.findByUsername(operatorUsername)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    if (operator.getRole() != UserRole.ADMIN) {
      throw new IllegalArgumentException("无权限修改后台设置");
    }

    Settings settings = new Settings(
        normalizeRange(
            request.getImageMaxCount(),
            MIN_IMAGE_MAX_COUNT,
            MAX_IMAGE_MAX_COUNT,
            "图片数量限制应在 1 到 9 之间"),
        normalizeRange(
            request.getImageMaxSizeMb(),
            MIN_FILE_MAX_SIZE_MB,
            MAX_FILE_MAX_SIZE_MB,
            "图片大小限制应在 1MB 到 200MB 之间"),
        normalizeRange(
            request.getAttachmentMaxCount(),
            MIN_ATTACHMENT_MAX_COUNT,
            MAX_ATTACHMENT_MAX_COUNT,
            "附件数量限制应在 1 到 20 之间"),
        normalizeRange(
            request.getAttachmentMaxSizeMb(),
            MIN_FILE_MAX_SIZE_MB,
            MAX_FILE_MAX_SIZE_MB,
            "附件大小限制应在 1MB 到 200MB 之间"),
        normalizeRange(
            request.getSupportingDocMaxCount(),
            MIN_SUPPORTING_DOC_MAX_COUNT,
            MAX_SUPPORTING_DOC_MAX_COUNT,
            "证明资料数量限制应在 1 到 20 之间"),
        normalizeRange(
            request.getSupportingDocMaxSizeMb(),
            MIN_FILE_MAX_SIZE_MB,
            MAX_FILE_MAX_SIZE_MB,
            "证明资料大小限制应在 1MB 到 200MB 之间"),
        normalizeExtList(request.getAttachmentDocumentExts()),
        normalizeExtList(request.getAttachmentVideoExts()),
        normalizeExtList(request.getAttachmentImageExts()),
        normalizeExtList(request.getAttachmentArchiveExts()));

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("imageMaxCount", settings.imageMaxCount);
    payload.put("imageMaxSizeMb", settings.imageMaxSizeMb);
    payload.put("attachmentMaxCount", settings.attachmentMaxCount);
    payload.put("attachmentMaxSizeMb", settings.attachmentMaxSizeMb);
    payload.put("supportingDocMaxCount", settings.supportingDocMaxCount);
    payload.put("supportingDocMaxSizeMb", settings.supportingDocMaxSizeMb);
    payload.put("attachmentDocumentExts", settings.attachmentDocumentExts);
    payload.put("attachmentVideoExts", settings.attachmentVideoExts);
    payload.put("attachmentImageExts", settings.attachmentImageExts);
    payload.put("attachmentArchiveExts", settings.attachmentArchiveExts);

    SystemSetting systemSetting = systemSettingRepository.findById(SETTING_KEY)
        .orElseGet(SystemSetting::new);
    systemSetting.setSettingKey(SETTING_KEY);
    systemSetting.setSettingValue(writeSettingsMap(payload));
    systemSetting.setUpdatedAt(LocalDateTime.now());
    systemSettingRepository.save(systemSetting);

    return toResponse(settings);
  }

  @Transactional(readOnly = true)
  public void validateAchievementMedia(String rawImageUrlsJson, String rawAttachmentsJson) {
    Settings settings = readSettings();
    int imageCount = parseJsonArraySize(rawImageUrlsJson, "图片数据格式不正确");
    if (imageCount > settings.imageMaxCount) {
      throw new IllegalArgumentException("最多上传 " + settings.imageMaxCount + " 张图片");
    }

    List<LinkedHashMap<String, Object>> attachments = parseAttachmentList(rawAttachmentsJson);
    if (attachments.size() > settings.attachmentMaxCount) {
      throw new IllegalArgumentException("最多上传 " + settings.attachmentMaxCount + " 个附件");
    }
    for (Map<String, Object> attachment : attachments) {
      String filename = stringValue(attachment.get("name"));
      if (filename.isBlank()) {
        filename = stringValue(attachment.get("url"));
      }
      if (!isAllowedAttachmentExtension(resolveExtension(filename), settings)) {
        throw new IllegalArgumentException("附件格式不支持");
      }
    }
  }

  @Transactional(readOnly = true)
  public void validateUpload(String context, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return;
    }
    Settings settings = readSettings();
    if ("achievement-image".equals(context)) {
      validateByMegabytes(file.getSize(), settings.imageMaxSizeMb, "图片");
      return;
    }
    if ("achievement-attachment".equals(context)) {
      validateByMegabytes(file.getSize(), settings.attachmentMaxSizeMb, "附件");
      if (!isAllowedAttachmentExtension(resolveExtension(file.getOriginalFilename()), settings)) {
        throw new IllegalArgumentException("当前设置不允许上传该附件格式");
      }
    }
    if ("review-supporting".equals(context)) {
      validateByMegabytes(file.getSize(), settings.supportingDocMaxSizeMb, "证明资料");
      if (!isAllowedAttachmentExtension(resolveExtension(file.getOriginalFilename()), settings)) {
        throw new IllegalArgumentException("当前设置不允许上传该格式");
      }
    }
  }

  private AchievementUploadSettingsResponse toResponse(Settings settings) {
    return new AchievementUploadSettingsResponse(
        settings.imageMaxCount,
        settings.imageMaxSizeMb,
        settings.attachmentMaxCount,
        settings.attachmentMaxSizeMb,
        joinExtList(settings.attachmentDocumentExts),
        joinExtList(settings.attachmentVideoExts),
        joinExtList(settings.attachmentImageExts),
        joinExtList(settings.attachmentArchiveExts),
        settings.supportingDocMaxCount,
        settings.supportingDocMaxSizeMb);
  }

  private Settings readSettings() {
    Map<String, Object> raw = systemSettingRepository.findById(SETTING_KEY)
        .map(SystemSetting::getSettingValue)
        .map(this::parseSettingsMap)
        .orElseGet(this::defaultSettingsMap);

    return new Settings(
        intValue(raw.get("imageMaxCount"), DEFAULT_IMAGE_MAX_COUNT),
        intValue(raw.get("imageMaxSizeMb"), DEFAULT_IMAGE_MAX_SIZE_MB),
        intValue(raw.get("attachmentMaxCount"), DEFAULT_ATTACHMENT_MAX_COUNT),
        intValue(raw.get("attachmentMaxSizeMb"), DEFAULT_ATTACHMENT_MAX_SIZE_MB),
        intValue(raw.get("supportingDocMaxCount"), DEFAULT_SUPPORTING_DOC_MAX_COUNT),
        intValue(raw.get("supportingDocMaxSizeMb"), DEFAULT_SUPPORTING_DOC_MAX_SIZE_MB),
        extListValue(raw.get("attachmentDocumentExts"), List.of("docx", "doc", "pdf", "xls", "xlsx", "pptx", "ppt")),
        extListValue(raw.get("attachmentVideoExts"), List.of("mp4", "mov")),
        extListValue(raw.get("attachmentImageExts"), List.of("jpeg", "jpg", "png", "heif")),
        extListValue(raw.get("attachmentArchiveExts"), List.of("zip", "rar", "7z", "tar")));
  }

  private Map<String, Object> parseSettingsMap(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      return defaultSettingsMap();
    }
    try {
      Map<String, Object> parsed = objectMapper.readValue(
          rawJson,
          new TypeReference<LinkedHashMap<String, Object>>() {
          });
      Map<String, Object> merged = defaultSettingsMap();
      merged.putAll(parsed);
      return merged;
    } catch (JsonProcessingException exception) {
      return defaultSettingsMap();
    }
  }

  private String writeSettingsMap(Map<String, Object> settings) {
    try {
      return objectMapper.writeValueAsString(settings);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("系统设置保存失败");
    }
  }

  private Map<String, Object> defaultSettingsMap() {
    Map<String, Object> defaults = new LinkedHashMap<>();
    defaults.put("imageMaxCount", DEFAULT_IMAGE_MAX_COUNT);
    defaults.put("imageMaxSizeMb", DEFAULT_IMAGE_MAX_SIZE_MB);
    defaults.put("attachmentMaxCount", DEFAULT_ATTACHMENT_MAX_COUNT);
    defaults.put("attachmentMaxSizeMb", DEFAULT_ATTACHMENT_MAX_SIZE_MB);
    defaults.put("supportingDocMaxCount", DEFAULT_SUPPORTING_DOC_MAX_COUNT);
    defaults.put("supportingDocMaxSizeMb", DEFAULT_SUPPORTING_DOC_MAX_SIZE_MB);
    defaults.put("attachmentDocumentExts", List.of("docx", "doc", "pdf", "xls", "xlsx", "pptx", "ppt"));
    defaults.put("attachmentVideoExts", List.of("mp4", "mov"));
    defaults.put("attachmentImageExts", List.of("jpeg", "jpg", "png", "heif"));
    defaults.put("attachmentArchiveExts", List.of("zip", "rar", "7z", "tar"));
    return defaults;
  }

  private int normalizeRange(Integer value, int min, int max, String message) {
    if (value == null || value < min || value > max) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private List<String> normalizeExtList(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return List.of();
    }
    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (String part : rawValue.split(",")) {
      String ext = normalizeExt(part);
      if (!ext.isBlank()) {
        result.add(ext);
      }
    }
    return new ArrayList<>(result);
  }

  private List<String> extListValue(Object value, List<String> defaultValue) {
    if (value instanceof List<?> list) {
      LinkedHashSet<String> normalized = new LinkedHashSet<>();
      for (Object item : list) {
        String ext = normalizeExt(item == null ? "" : String.valueOf(item));
        if (!ext.isBlank()) {
          normalized.add(ext);
        }
      }
      return new ArrayList<>(normalized);
    }
    if (value instanceof String text) {
      return normalizeExtList(text);
    }
    return defaultValue;
  }

  private String normalizeExt(String value) {
    if (value == null) {
      return "";
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if (normalized.startsWith(".")) {
      normalized = normalized.substring(1);
    }
    return normalized;
  }

  private String joinExtList(List<String> values) {
    return String.join(",", values);
  }

  private int parseJsonArraySize(String rawJson, String errorMessage) {
    if (rawJson == null || rawJson.isBlank()) {
      return 0;
    }
    try {
      List<Object> items = objectMapper.readValue(
          rawJson,
          objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
      return items.size();
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private List<LinkedHashMap<String, Object>> parseAttachmentList(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(
          rawJson,
          new TypeReference<List<LinkedHashMap<String, Object>>>() {
          });
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("附件数据格式不正确");
    }
  }

  private boolean isAllowedAttachmentExtension(String extension, Settings settings) {
    if (extension.isBlank()) {
      return false;
    }
    return settings.attachmentDocumentExts.contains(extension)
        || settings.attachmentVideoExts.contains(extension)
        || settings.attachmentImageExts.contains(extension)
        || settings.attachmentArchiveExts.contains(extension);
  }

  private String resolveExtension(String filename) {
    if (filename == null || filename.isBlank()) {
      return "";
    }
    int index = filename.lastIndexOf('.');
    if (index < 0 || index >= filename.length() - 1) {
      return "";
    }
    return normalizeExt(filename.substring(index + 1));
  }

  private int intValue(Object value, int defaultValue) {
    if (value instanceof Number number) {
      return number.intValue();
    }
    if (value instanceof String text) {
      try {
        return Integer.parseInt(text.trim());
      } catch (NumberFormatException ignored) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  private String stringValue(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }

  private void validateByMegabytes(long sizeInBytes, int maxSizeMb, String label) {
    long maxBytes = maxSizeMb * 1024L * 1024L;
    if (sizeInBytes > maxBytes) {
      throw new IllegalArgumentException(label + "大小不可超过 " + maxSizeMb + "MB");
    }
  }

  private static final class Settings {
    private final int imageMaxCount;
    private final int imageMaxSizeMb;
    private final int attachmentMaxCount;
    private final int attachmentMaxSizeMb;
    private final int supportingDocMaxCount;
    private final int supportingDocMaxSizeMb;
    private final List<String> attachmentDocumentExts;
    private final List<String> attachmentVideoExts;
    private final List<String> attachmentImageExts;
    private final List<String> attachmentArchiveExts;

    private Settings(
        int imageMaxCount,
        int imageMaxSizeMb,
        int attachmentMaxCount,
        int attachmentMaxSizeMb,
        int supportingDocMaxCount,
        int supportingDocMaxSizeMb,
        List<String> attachmentDocumentExts,
        List<String> attachmentVideoExts,
        List<String> attachmentImageExts,
        List<String> attachmentArchiveExts) {
      this.imageMaxCount = imageMaxCount;
      this.imageMaxSizeMb = imageMaxSizeMb;
      this.attachmentMaxCount = attachmentMaxCount;
      this.attachmentMaxSizeMb = attachmentMaxSizeMb;
      this.supportingDocMaxCount = supportingDocMaxCount;
      this.supportingDocMaxSizeMb = supportingDocMaxSizeMb;
      this.attachmentDocumentExts = attachmentDocumentExts;
      this.attachmentVideoExts = attachmentVideoExts;
      this.attachmentImageExts = attachmentImageExts;
      this.attachmentArchiveExts = attachmentArchiveExts;
    }
  }
}
