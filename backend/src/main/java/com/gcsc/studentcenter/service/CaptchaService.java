package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.dto.CaptchaResponse;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

  private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int CODE_LENGTH = 4;
  private static final int WIDTH = 112;
  private static final int HEIGHT = 46;
  private static final Duration EXPIRES_IN = Duration.ofMinutes(5);

  private final SecureRandom random = new SecureRandom();
  private final Map<String, CaptchaEntry> captchas = new ConcurrentHashMap<>();

  public CaptchaResponse createCaptcha() {
    removeExpiredCaptchas();

    String captchaId = UUID.randomUUID().toString();
    String code = createCode();
    captchas.put(captchaId, new CaptchaEntry(code, Instant.now().plus(EXPIRES_IN)));

    return new CaptchaResponse(
        captchaId,
        "data:image/png;base64," + renderBase64Png(code),
        EXPIRES_IN.toSeconds());
  }

  public void validateCaptcha(String captchaId, String captchaCode) {
    if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
      throw new IllegalArgumentException("请输入图形验证码");
    }

    CaptchaEntry entry = captchas.remove(captchaId);
    if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("验证码已过期，请刷新后重试");
    }

    String normalizedCode = captchaCode.trim().toUpperCase(Locale.ROOT);
    if (!entry.code().equals(normalizedCode)) {
      throw new IllegalArgumentException("验证码错误，请重新输入");
    }
  }

  private String createCode() {
    StringBuilder code = new StringBuilder(CODE_LENGTH);
    for (int i = 0; i < CODE_LENGTH; i += 1) {
      code.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
    }
    return code.toString();
  }

  private String renderBase64Png(String code) {
    BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();

    try {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      graphics.setPaint(new Color(255, 247, 251));
      graphics.fillRect(0, 0, WIDTH, HEIGHT);
      graphics.setColor(new Color(242, 237, 247, 180));
      graphics.fillOval(-18, -20, 70, 60);
      graphics.setColor(new Color(255, 235, 243, 210));
      graphics.fillOval(62, 12, 78, 54);

      for (int i = 0; i < 8; i += 1) {
        graphics.setColor(randomColor(80, 120, 140));
        graphics.setStroke(new BasicStroke(1.0f + random.nextFloat()));
        int x1 = random.nextInt(WIDTH);
        int y1 = random.nextInt(HEIGHT);
        int x2 = random.nextInt(WIDTH);
        int y2 = random.nextInt(HEIGHT);
        graphics.drawLine(x1, y1, x2, y2);
      }

      graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
      for (int i = 0; i < code.length(); i += 1) {
        AffineTransform oldTransform = graphics.getTransform();
        int x = 15 + i * 23;
        int y = 31 + random.nextInt(5) - 2;
        graphics.rotate((random.nextDouble() - 0.5) * 0.45, x, y);
        graphics.setColor(i % 2 == 0 ? new Color(100, 12, 114) : new Color(168, 93, 32));
        graphics.drawString(String.valueOf(code.charAt(i)), x, y);
        graphics.setTransform(oldTransform);
      }

      for (int i = 0; i < 24; i += 1) {
        graphics.setColor(randomColor(70, 110, 170));
        graphics.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
      }

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ImageIO.write(image, "png", output);
      return Base64.getEncoder().encodeToString(output.toByteArray());
    } catch (IOException exception) {
      throw new IllegalStateException("验证码生成失败", exception);
    } finally {
      graphics.dispose();
    }
  }

  private Color randomColor(int redBase, int greenBase, int alpha) {
    return new Color(
        Math.min(255, redBase + random.nextInt(80)),
        Math.min(255, greenBase + random.nextInt(80)),
        Math.min(255, 140 + random.nextInt(80)),
        alpha);
  }

  private void removeExpiredCaptchas() {
    Instant now = Instant.now();
    captchas.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
  }

  private record CaptchaEntry(String code, Instant expiresAt) {
  }
}
