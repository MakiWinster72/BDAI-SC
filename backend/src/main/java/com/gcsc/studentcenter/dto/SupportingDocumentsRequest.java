package com.gcsc.studentcenter.dto;

import java.util.List;
import java.util.Map;

public class SupportingDocumentsRequest {

  private List<Map<String, String>> documents;

  public List<Map<String, String>> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Map<String, String>> documents) {
    this.documents = documents;
  }
}
