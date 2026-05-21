package com.gcsc.studentcenter.dto;

public class AuditEventRequest {

  private String action;
  private String detail;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }
}
