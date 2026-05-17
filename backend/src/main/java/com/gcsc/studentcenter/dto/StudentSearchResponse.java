package com.gcsc.studentcenter.dto;

import java.util.List;

public class StudentSearchResponse {
  private List<StudentSearchItemResponse> items;
  private int page;
  private int size;
  private long total;
  private int totalPages;

  public StudentSearchResponse(
      List<StudentSearchItemResponse> items,
      int page,
      int size,
      long total,
      int totalPages) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.total = total;
    this.totalPages = totalPages;
  }

  public List<StudentSearchItemResponse> getItems() {
    return items;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotal() {
    return total;
  }

  public int getTotalPages() {
    return totalPages;
  }
}
