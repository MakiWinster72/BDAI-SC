package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {
    private Long id;
    private String content;
    private boolean goodNews;
    private boolean privatePost;
    private boolean achievement;
    private String authorName;
    private String authorUsername;
    private String authorRole;
    private LocalDateTime createdAt;
    private List<MediaItemResponse> media;

    public PostResponse(
        Long id,
        String content,
        boolean goodNews,
        boolean privatePost,
        boolean achievement,
        String authorName,
        String authorUsername,
        String authorRole,
        LocalDateTime createdAt,
        List<MediaItemResponse> media
    ) {
        this.id = id;
        this.content = content;
        this.goodNews = goodNews;
        this.privatePost = privatePost;
        this.achievement = achievement;
        this.authorName = authorName;
        this.authorUsername = authorUsername;
        this.authorRole = authorRole;
        this.createdAt = createdAt;
        this.media = media;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public boolean isGoodNews() {
        return goodNews;
    }

    public boolean isPrivatePost() {
        return privatePost;
    }

    public boolean isAchievement() {
        return achievement;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<MediaItemResponse> getMedia() {
        return media;
    }
}
