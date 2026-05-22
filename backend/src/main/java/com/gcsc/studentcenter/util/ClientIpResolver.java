package com.gcsc.studentcenter.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 从 HTTP 请求解析客户端 IP（反向代理 / Vite dev proxy / VPN 直连）。
 * 跳过回环地址；VPN 分配的 172.x（如 MotionPro）、10.x 等私网段作为客户端 IP 保留。
 */
@Component
public class ClientIpResolver {

  public String resolve(HttpServletRequest request) {
    if (request == null) {
      return null;
    }

    List<String> candidates = collectHeaderCandidates(request);
    appendSingle(candidates, request.getRemoteAddr());

    String ip = firstUsableIp(candidates);
    if (ip != null) {
      return ip;
    }

    return normalizeIp(request.getRemoteAddr());
  }

  private static List<String> collectHeaderCandidates(HttpServletRequest request) {
    List<String> candidates = new ArrayList<>();

    appendCommaSeparated(candidates, request.getHeader("X-Forwarded-For"));
    appendForwardedFor(candidates, request.getHeader("Forwarded"));
    appendSingle(candidates, request.getHeader("X-Real-IP"));
    appendSingle(candidates, request.getHeader("CF-Connecting-IP"));
    appendSingle(candidates, request.getHeader("True-Client-IP"));
    appendSingle(candidates, request.getHeader("X-Client-IP"));

    return candidates;
  }

  private static void appendCommaSeparated(List<String> candidates, String header) {
    if (header == null || header.isBlank()) {
      return;
    }
    for (String part : header.split(",")) {
      candidates.add(part.trim());
    }
  }

  private static void appendSingle(List<String> candidates, String header) {
    if (header != null && !header.isBlank()) {
      candidates.add(header.trim());
    }
  }

  private static void appendForwardedFor(List<String> candidates, String forwarded) {
    if (forwarded == null || forwarded.isBlank()) {
      return;
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
      candidates.add(value);
    }
  }

  private static String firstUsableIp(List<String> rawCandidates) {
    for (String raw : rawCandidates) {
      String ip = normalizeIp(raw);
      if (ip != null && !isLoopback(ip)) {
        return ip;
      }
    }
    return null;
  }

  static boolean isLoopback(String ip) {
    String normalized = normalizeIp(ip);
    if (normalized == null) {
      return true;
    }
    if ("::1".equals(normalized) || "0:0:0:0:0:0:0:1".equals(normalized)) {
      return true;
    }
    return normalized.startsWith("127.");
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
    int portIdx = ip.lastIndexOf(':');
    if (portIdx > 0 && ip.indexOf('.') >= 0 && ip.indexOf(':') == portIdx) {
      ip = ip.substring(0, portIdx);
    }
    return ip.isEmpty() ? null : ip;
  }
}
