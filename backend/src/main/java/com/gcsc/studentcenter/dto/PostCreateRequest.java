package com.gcsc.studentcenter.dto;

import java.util.List;

public class PostCreateRequest {
    private String content;
    private boolean goodNews;
    private boolean privatePost;
    private boolean achievement;
    private List<MediaItemRequest> media;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isGoodNews() {
        return goodNews;
    }

    public void setGoodNews(boolean goodNews) {
        this.goodNews = goodNews;
    }

    public boolean isPrivatePost() {
        return privatePost;
    }

    public void setPrivatePost(boolean privatePost) {
        this.privatePost = privatePost;
    }

    public boolean isAchievement() {
        return achievement;
    }

    public void setAchievement(boolean achievement) {
        this.achievement = achievement;
    }

    public List<MediaItemRequest> getMedia() {
        return media;
    }

    public void setMedia(List<MediaItemRequest> media) {
        this.media = media;
    }
}
