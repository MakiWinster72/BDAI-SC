package com.gcsc.studentcenter.dto;

public class ReviewSettingsRequest {
  private Boolean profileReviewEnabled;
  private Boolean profileReviewAutoApprove;
  private Boolean achievementReviewEnabled;
  private Boolean achievementReviewAutoApprove;

  public Boolean getProfileReviewEnabled() {
    return profileReviewEnabled;
  }

  public void setProfileReviewEnabled(Boolean profileReviewEnabled) {
    this.profileReviewEnabled = profileReviewEnabled;
  }

  public Boolean getProfileReviewAutoApprove() {
    return profileReviewAutoApprove;
  }

  public void setProfileReviewAutoApprove(Boolean profileReviewAutoApprove) {
    this.profileReviewAutoApprove = profileReviewAutoApprove;
  }

  public Boolean getAchievementReviewEnabled() {
    return achievementReviewEnabled;
  }

  public void setAchievementReviewEnabled(Boolean achievementReviewEnabled) {
    this.achievementReviewEnabled = achievementReviewEnabled;
  }

  public Boolean getAchievementReviewAutoApprove() {
    return achievementReviewAutoApprove;
  }

  public void setAchievementReviewAutoApprove(Boolean achievementReviewAutoApprove) {
    this.achievementReviewAutoApprove = achievementReviewAutoApprove;
  }
}
