package com.gcsc.studentcenter.dto;

public class AchievementUploadSettingsRequest {
    private Integer imageMaxCount;
    private Integer imageMaxSizeMb;
    private Integer attachmentMaxCount;
    private Integer attachmentMaxSizeMb;
    private String attachmentDocumentExts;
    private String attachmentVideoExts;
    private String attachmentImageExts;
    private String attachmentArchiveExts;
    private Integer supportingDocMaxCount;
    private Integer supportingDocMaxSizeMb;

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

    public Integer getAttachmentMaxCount() {
        return attachmentMaxCount;
    }

    public void setAttachmentMaxCount(Integer attachmentMaxCount) {
        this.attachmentMaxCount = attachmentMaxCount;
    }

    public String getAttachmentDocumentExts() {
        return attachmentDocumentExts;
    }

    public void setAttachmentDocumentExts(String attachmentDocumentExts) {
        this.attachmentDocumentExts = attachmentDocumentExts;
    }

    public String getAttachmentVideoExts() {
        return attachmentVideoExts;
    }

    public void setAttachmentVideoExts(String attachmentVideoExts) {
        this.attachmentVideoExts = attachmentVideoExts;
    }

    public String getAttachmentImageExts() {
        return attachmentImageExts;
    }

    public void setAttachmentImageExts(String attachmentImageExts) {
        this.attachmentImageExts = attachmentImageExts;
    }

    public String getAttachmentArchiveExts() {
        return attachmentArchiveExts;
    }

    public void setAttachmentArchiveExts(String attachmentArchiveExts) {
        this.attachmentArchiveExts = attachmentArchiveExts;
    }

    public Integer getSupportingDocMaxCount() {
        return supportingDocMaxCount;
    }

    public void setSupportingDocMaxCount(Integer supportingDocMaxCount) {
        this.supportingDocMaxCount = supportingDocMaxCount;
    }

    public Integer getSupportingDocMaxSizeMb() {
        return supportingDocMaxSizeMb;
    }

    public void setSupportingDocMaxSizeMb(Integer supportingDocMaxSizeMb) {
        this.supportingDocMaxSizeMb = supportingDocMaxSizeMb;
    }
}
