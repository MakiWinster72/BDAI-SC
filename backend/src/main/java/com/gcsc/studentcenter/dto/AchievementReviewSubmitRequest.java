package com.gcsc.studentcenter.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public class AchievementReviewSubmitRequest {
    private String category;
    private String action;
    private Long recordId;
    private String title;
    private String summary;
    private AchievementRecordRequest payload;
    private JsonNode payloadSnapshot;
    private List<Map<String, Object>> changes;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public AchievementRecordRequest getPayload() {
        return payload;
    }

    public void setPayload(AchievementRecordRequest payload) {
        this.payload = payload;
    }

    public JsonNode getPayloadSnapshot() {
        return payloadSnapshot;
    }

    public void setPayloadSnapshot(JsonNode payloadSnapshot) {
        this.payloadSnapshot = payloadSnapshot;
    }

    public List<Map<String, Object>> getChanges() {
        return changes;
    }

    public void setChanges(List<Map<String, Object>> changes) {
        this.changes = changes;
    }
}
