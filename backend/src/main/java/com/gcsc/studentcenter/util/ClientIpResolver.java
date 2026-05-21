package com.gcsc.studentcenter.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 从 HTTP 请求解析客户端 IP（支持反向代理 / Vite dev proxy 的 X-Forwarded-*）。
 */
@Component
public class ClientIpResolver {

  public String resolve(HttpServletRequest request) {
    if (request == null) {
      return null;
    }

    String fromForwarded = firstIpFromForwardedHeader(request.getHeader("Forwarded"));
    if (fromForwarded != null) {
      return fromForwarded;
    }

    String fromXff = firstIpFromCommaList(request.getHeader("X-Forwarded-For"));
    if (fromXff != null) {
      return fromXff;
    }

    String xRealIp = normalizeIp(request.getHeader("X-Real-IP"));
    if (xRealIp != null) {
      return xRealIp;
    }

    return normalizeIp(request.getRemoteAddr());
  }

  /** RFC 7239 Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43 */
  private static String firstIpFromForwardedHeader(String forwarded) {
    if (forwarded == null || forwarded.isBlank()) {
      return null;
    }
    for (String segment : forwarded.split(",")) {
      String part = segment.trim();
      int forIdx = part.toLowerCase().indexOf("for=");
      if (forIdx < 0) {
        continue;
      }
      String value = part.substring(forIdx + 4).trim();
      if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
        value = value.substring(1, value.length() - 1);
      }
      String ip = normalizeIp(value);
      if (ip != null) {
        return ip;
      }
    }
    return null;
  }

  private static String firstIpFromCommaList(String header) {
    if (header == null || header.isBlank()) {
      return null;
    }
    for (String part : header.split(",")) {
      String ip = normalizeIp(part);
      if (ip != null) {
        return ip;
      }
    }
    return null;
  }

  static String normalizeIp(String raw) {
    if (raw == null) {
      return null;
    }
    String ip = raw.trim();
    if (ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      return null;
    }
    if (ip.startsWith("::ffff:")) {
      ip = ip.substring("::ffff:".length());
    }
    if (ip.startsWith("[") && ip.endsWith("]") && ip.length() > 2) {
      ip = ip.substring(1, ip.length() - 1);
    }
    return ip.isEmpty() ? null : ip;
  }
}
