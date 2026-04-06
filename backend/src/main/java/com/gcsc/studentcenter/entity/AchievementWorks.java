package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_works")
public class AchievementWorks {

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

    @Column(name = "work_name", nullable = false, length = 255)
    private String workName;

    @Column(name = "work_category", length = 64)
    private String workCategory;

    @Column(name = "work_type", length = 64)
    private String workType;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "publish_occasion", length = 255)
    private String publishOccasion;

    @Column(name = "organizer", length = 255)
    private String organizer;

    @Column(name = "impact_scope", length = 64)
    private String impactScope;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

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

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getWorkCategory() {
        return workCategory;
    }

    public void setWorkCategory(String workCategory) {
        this.workCategory = workCategory;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishOccasion() {
        return publishOccasion;
    }

    public void setPublishOccasion(String publishOccasion) {
        this.publishOccasion = publishOccasion;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getImpactScope() {
        return impactScope;
    }

    public void setImpactScope(String impactScope) {
        this.impactScope = impactScope;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
