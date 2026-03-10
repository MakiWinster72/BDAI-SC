package com.gcsc.studentcenter.dto;

public class MediaItemResponse {
    private Long id;
    private String url;
    private String mediaType;
    private String originalName;

    public MediaItemResponse(Long id, String url, String mediaType, String originalName) {
        this.id = id;
        this.url = url;
        this.mediaType = mediaType;
        this.originalName = originalName;
    }

    public Long getId() {
        return id;
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
