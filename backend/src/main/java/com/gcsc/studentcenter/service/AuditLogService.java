package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.entity.AuditLog;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.gcsc.studentcenter.util.ClientIpResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

  private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

  private final AuditLogRepository auditLogRepository;
  private final AppUserRepository appUserRepository;
  private final ClientIpResolver clientIpResolver;

  public AuditLogService(
      AuditLogRepository auditLogRepository,
      AppUserRepository appUserRepository,
      ClientIpResolver clientIpResolver) {
    this.auditLogRepository = auditLogRepository;
    this.appUserRepository = appUserRepository;
    this.clientIpResolver = clientIpResolver;
  }

  /** 独立事务提交，避免业务方法抛错时审计记录被回滚 */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AuditLog log(String username, String action, String detail, String ipAddress) {
    AuditLog entry = new AuditLog(username, action, detail, ipAddress, LocalDateTime.now());
    AuditLog saved = auditLogRepository.save(entry);
    logger.info("AUDIT [{}] {}: {} (IP: {})", username, action, detail, ipAddress);
    return saved;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> listPaginated(int page, int size, String search) {
    Pageable pageable = PageRequest.of(page - 1, size);
    String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
    var result = auditLogRepository.findPaginated(searchParam, pageable);

    List<AuditLog> logs = result.getContent();
    Set<String> usernames = logs.stream().map(AuditLog::getUsername).collect(Collectors.toSet());
    Map<String, String> displayNameMap = appUserRepository.findByUsernameIn(usernames).stream()
        .collect(Collectors.toMap(AppUser::getUsername, AppUser::getDisplayName, (a, b) -> a));

    List<Map<String, Object>> enriched = logs.stream().map(log -> {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("id", log.getId());
      entry.put("username", log.getUsername());
      entry.put("displayName", displayNameMap.getOrDefault(log.getUsername(), log.getUsername()));
      entry.put("action", log.getAction());
      entry.put("detail", log.getDetail());
      entry.put("ipAddress", log.getIpAddress());
      entry.put("createdAt", log.getCreatedAt());
      return entry;
    }).collect(Collectors.toList());

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("data", enriched);
    response.put("total", result.getTotalElements());
    response.put("page", page);
    response.put("size", size);
    response.put("pages", result.getTotalPages());
    return response;
  }

  public String resolveIpAddress(HttpServletRequest request) {
    return clientIpResolver.resolve(request);
  }
}
