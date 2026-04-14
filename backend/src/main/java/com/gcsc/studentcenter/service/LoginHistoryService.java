package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.LastLoginInfo;
import com.gcsc.studentcenter.dto.LoginHistoryResponse;
import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.LoginHistory;
import com.gcsc.studentcenter.repository.AppUserRepository;
import com.gcsc.studentcenter.repository.LoginHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class LoginHistoryService {

    private static final int RETENTION_DAYS = 30;

    private final LoginHistoryRepository loginHistoryRepository;
    private final AppUserRepository appUserRepository;

    public LoginHistoryService(
        LoginHistoryRepository loginHistoryRepository,
        AppUserRepository appUserRepository
    ) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.appUserRepository = appUserRepository;
    }

    public LoginHistory recordLogin(String username, String ipAddress, String userAgent) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElse(null);
        if (user == null) {
            return null;
        }

        LoginHistory history = new LoginHistory();
        history.setUserId(user.getId());
        history.setIpAddress(ipAddress);
        history.setUserAgent(userAgent);
        history.setDeviceName(parseDeviceName(userAgent));
        history.setLoginTime(LocalDateTime.now());
        return loginHistoryRepository.save(history);
    }

    public Optional<LastLoginInfo> getPreviousLogin(String username) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        Optional<LoginHistory> prev = loginHistoryRepository
            .findFirstByUserIdOrderByLoginTimeDesc(user.getId());
        if (prev.isEmpty()) {
            return Optional.empty();
        }
        Optional<LoginHistory> secondLast = loginHistoryRepository
            .findFirstByUserIdAndIdNotOrderByLoginTimeDesc(user.getId(), prev.get().getId());
        if (secondLast.isEmpty()) {
            return Optional.empty();
        }
        LoginHistory lh = secondLast.get();
        return Optional.of(new LastLoginInfo(lh.getIpAddress(), lh.getDeviceName(), lh.getLoginTime()));
    }

    public Page<LoginHistoryResponse> getLoginHistory(String username, int page, int size) {
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Pageable pageable = PageRequest.of(page, size);
        Page<LoginHistory> historyPage = loginHistoryRepository
            .findAllByUserIdOrderByLoginTimeDesc(user.getId(), pageable);
        return historyPage.map(this::toResponse);
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        loginHistoryRepository.deleteByLoginTimeBefore(cutoff);
    }

    private LoginHistoryResponse toResponse(LoginHistory history) {
        return new LoginHistoryResponse(
            history.getIpAddress(),
            history.getDeviceName(),
            history.getLoginTime()
        );
    }

    private String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "未知浏览器";
        }

        String os = parseOs(userAgent);
        String browser = parseBrowser(userAgent);
        if (browser.equals("未知") && os.equals("未知")) {
            return "未知浏览器";
        }
        if (browser.equals("未知")) {
            return os;
        }
        if (os.equals("未知")) {
            return browser;
        }
        return browser + " / " + os;
    }

    private String parseOs(String ua) {
        ua = ua.toLowerCase();
        if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("windows phone")) {
            return "Windows Phone";
        }
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("macintosh") || ua.contains("mac os")) {
            return "macOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "未知";
    }

    private String parseBrowser(String ua) {
        ua = ua.toLowerCase();
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("brave")) {
            return "Brave";
        }
        if (ua.contains("chrome/") && !ua.contains("chromium")) {
            return "Chrome";
        }
        if (ua.contains("safari/") && !ua.contains("chrome")) {
            return "Safari";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("webkit")) {
            return "WebKit";
        }
        return "未知";
    }
}
