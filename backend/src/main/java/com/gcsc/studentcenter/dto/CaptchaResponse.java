package com.gcsc.studentcenter.dto;

public class CaptchaResponse {

  private final String captchaId;
  private final String imageBase64;
  private final long expiresInSeconds;

  public CaptchaResponse(String captchaId, String imageBase64, long expiresInSeconds) {
    this.captchaId = captchaId;
    this.imageBase64 = imageBase64;
    this.expiresInSeconds = expiresInSeconds;
  }

  public String getCaptchaId() {
    return captchaId;
  }

  public String getImageBase64() {
    return imageBase64;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }
}
