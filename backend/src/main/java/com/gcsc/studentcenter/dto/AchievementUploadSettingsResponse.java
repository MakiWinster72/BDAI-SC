package com.gcsc.studentcenter.dto;

public class AchievementUploadSettingsResponse {
    private final int imageMaxCount;
    private final int imageMaxSizeMb;
    private final int attachmentMaxCount;
    private final int attachmentMaxSizeMb;
    private final String attachmentDocumentExts;
    private final String attachmentVideoExts;
    private final String attachmentImageExts;
    private final String attachmentArchiveExts;

    public AchievementUploadSettingsResponse(
        int imageMaxCount,
        int imageMaxSizeMb,
        int attachmentMaxCount,
        int attachmentMaxSizeMb,
        String attachmentDocumentExts,
        String attachmentVideoExts,
        String attachmentImageExts,
        String attachmentArchiveExts
    ) {
        this.imageMaxCount = imageMaxCount;
        this.imageMaxSizeMb = imageMaxSizeMb;
        this.attachmentMaxCount = attachmentMaxCount;
        this.attachmentMaxSizeMb = attachmentMaxSizeMb;
        this.attachmentDocumentExts = attachmentDocumentExts;
        this.attachmentVideoExts = attachmentVideoExts;
        this.attachmentImageExts = attachmentImageExts;
        this.attachmentArchiveExts = attachmentArchiveExts;
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

    public int getAttachmentMaxCount() {
        return attachmentMaxCount;
    }

    public String getAttachmentDocumentExts() {
        return attachmentDocumentExts;
    }

    public String getAttachmentVideoExts() {
        return attachmentVideoExts;
    }

    public String getAttachmentImageExts() {
        return attachmentImageExts;
    }

    public String getAttachmentArchiveExts() {
        return attachmentArchiveExts;
    }
}
