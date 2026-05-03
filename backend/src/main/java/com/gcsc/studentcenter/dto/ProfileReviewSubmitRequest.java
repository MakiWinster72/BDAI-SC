package com.gcsc.studentcenter.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public class ProfileReviewSubmitRequest {

  private String title;
  private String summary;
  private JsonNode payloadSnapshot;
  private List<Map<String, Object>> changes;

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
