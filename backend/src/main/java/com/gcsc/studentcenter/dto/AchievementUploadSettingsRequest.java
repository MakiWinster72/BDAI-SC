package com.gcsc.studentcenter.dto;

public class AchievementUploadSettingsRequest {
    private Integer imageMaxCount;
    private Integer imageMaxSizeMb;
    private Integer attachmentMaxSizeMb;

    public Integer getImageMaxCount() {
        return imageMaxCount;
    }

    public void setImageMaxCount(Integer imageMaxCount) {
        this.imageMaxCount = imageMaxCount;
    }

    public Integer getImageMaxSizeMb() {
        return imageMaxSizeMb;
    }

    public void setImageMaxSizeMb(Integer imageMaxSizeMb) {
        this.imageMaxSizeMb = imageMaxSizeMb;
    }

    public Integer getAttachmentMaxSizeMb() {
        return attachmentMaxSizeMb;
    }

    public void setAttachmentMaxSizeMb(Integer attachmentMaxSizeMb) {
        this.attachmentMaxSizeMb = attachmentMaxSizeMb;
    }
}
