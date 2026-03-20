package com.gcsc.studentcenter.dto;

import java.util.Map;

public class AchievementRecordRequest {
    private String imageUrl;
    private Map<String, String> fields;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
