package com.gcsc.studentcenter.dto;

public class ReviewSettingsResponse {
    private final boolean profileReviewEnabled;
    private final boolean profileReviewAutoApprove;
    private final boolean achievementReviewEnabled;
    private final boolean achievementReviewAutoApprove;

    public ReviewSettingsResponse(
        boolean profileReviewEnabled,
        boolean profileReviewAutoApprove,
        boolean achievementReviewEnabled,
        boolean achievementReviewAutoApprove
    ) {
        this.profileReviewEnabled = profileReviewEnabled;
        this.profileReviewAutoApprove = profileReviewAutoApprove;
        this.achievementReviewEnabled = achievementReviewEnabled;
        this.achievementReviewAutoApprove = achievementReviewAutoApprove;
    }

    public boolean isProfileReviewEnabled() {
        return profileReviewEnabled;
    }

    public boolean isProfileReviewAutoApprove() {
        return profileReviewAutoApprove;
    }

    public boolean isAchievementReviewEnabled() {
        return achievementReviewEnabled;
    }

    public boolean isAchievementReviewAutoApprove() {
        return achievementReviewAutoApprove;
    }
}
