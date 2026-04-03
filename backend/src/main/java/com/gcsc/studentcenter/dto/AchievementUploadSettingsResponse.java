package com.gcsc.studentcenter.dto;

public class AchievementUploadSettingsResponse {
    private final int imageMaxCount;
    private final int imageMaxSizeMb;
    private final int attachmentMaxSizeMb;

    public AchievementUploadSettingsResponse(
        int imageMaxCount,
        int imageMaxSizeMb,
        int attachmentMaxSizeMb
    ) {
        this.imageMaxCount = imageMaxCount;
        this.imageMaxSizeMb = imageMaxSizeMb;
        this.attachmentMaxSizeMb = attachmentMaxSizeMb;
    }

    public int getImageMaxCount() {
        return imageMaxCount;
    }

    public int getImageMaxSizeMb() {
        return imageMaxSizeMb;
    }

    public int getAttachmentMaxSizeMb() {
        return attachmentMaxSizeMb;
    }
}
