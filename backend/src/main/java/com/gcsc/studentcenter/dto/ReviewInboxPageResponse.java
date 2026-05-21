package com.gcsc.studentcenter.dto;

import java.util.List;
import java.util.Map;

public class ReviewInboxPageResponse {

  private final List<ReviewRequestSummaryResponse> items;
  private final long total;
  private final int page;
  private final int size;
  private final int pages;
  private final Map<String, Long> categoryCounts;

  public ReviewInboxPageResponse(
      List<ReviewRequestSummaryResponse> items,
      long total,
      int page,
      int size,
      int pages,
      Map<String, Long> categoryCounts) {
    this.items = items;
    this.total = total;
    this.page = page;
    this.size = size;
    this.pages = pages;
    this.categoryCounts = categoryCounts;
  }

  public List<ReviewRequestSummaryResponse> getItems() {
    return items;
  }

  public long getTotal() {
    return total;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public int getPages() {
    return pages;
  }

  public Map<String, Long> getCategoryCounts() {
    return categoryCounts;
  }
}
