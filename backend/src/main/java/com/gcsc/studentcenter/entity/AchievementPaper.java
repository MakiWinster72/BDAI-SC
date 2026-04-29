package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_papers")
public class AchievementPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(name = "student_no", length = 32)
    private String studentNo;

    @Column(name = "student_name", length = 64)
    private String studentName;

    @Column(name = "paper_title", nullable = false, length = 255)
    private String paperTitle;

    @Column(name = "journal_name", length = 255)
    private String journalName;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "author_order", length = 64)
    private String authorOrder;

    @Column(name = "indexed", length = 128)
    private String indexed;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "_image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "_attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getPaperTitle() {
        return paperTitle;
    }

    public void setPaperTitle(String paperTitle) {
        this.paperTitle = paperTitle;
    }

    public String getJournalName() {
        return journalName;
    }

    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getAuthorOrder() {
        return authorOrder;
    }

    public void setAuthorOrder(String authorOrder) {
        this.authorOrder = authorOrder;
    }

    public String getIndexed() {
        return indexed;
    }

    public void setIndexed(String indexed) {
        this.indexed = indexed;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
