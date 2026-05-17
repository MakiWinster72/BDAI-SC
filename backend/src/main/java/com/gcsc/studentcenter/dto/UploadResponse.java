package com.gcsc.studentcenter.dto;

public class UploadResponse {
  private boolean success;
  private String url;
  private String mediaType;
  private String originalName;

  public UploadResponse(boolean success, String url, String mediaType, String originalName) {
    this.success = success;
    this.url = url;
    this.mediaType = mediaType;
    this.originalName = originalName;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getUrl() {
    return url;
  }

  public String getMediaType() {
    return mediaType;
  }

  public String getOriginalName() {
    return originalName;
  }
}
