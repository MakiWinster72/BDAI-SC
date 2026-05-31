package com.gcsc.studentcenter.config;

import com.gcsc.studentcenter.entity.AppUser;
import com.gcsc.studentcenter.entity.UserRole;
import com.gcsc.studentcenter.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 学生账号在 must_change_password=true 时，仅允许访问改密与读取本人概要，防止绕过前端直达业务接口。
 */
@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

  private static final Set<String> ALLOWED_PATHS = Set.of(
      "/api/auth/change-password",
      "/api/auth/me");

  private final AppUserRepository appUserRepository;

  public MustChangePasswordFilter(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    if (isAllowed(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String username = authentication.getName();
    AppUser user = appUserRepository.findByUsername(username).orElse(null);
    if (user != null
        && user.getRole() == UserRole.STUDENT
        && user.isMustChangePassword()) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"success\":false,\"message\":\"请先修改初始密码\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isAllowed(String path) {
    if (path == null) {
      return false;
    }
    for (String allowed : ALLOWED_PATHS) {
      if (path.equals(allowed) || path.startsWith(allowed + "/")) {
        return true;
      }
    }
    return false;
  }
}
