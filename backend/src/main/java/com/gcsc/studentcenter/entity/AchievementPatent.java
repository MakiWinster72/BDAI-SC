package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_patents")
public class AchievementPatent {

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

  @Column(name = "patent_name", nullable = false, length = 255)
  private String patentName;

  @Column(name = "patent_type", length = 64)
  private String patentType;

  @Column(name = "grant_no", length = 64)
  private String grantNo;

  @Column(name = "grant_date")
  private LocalDate grantDate;

  @Column(name = "first_inventor", length = 16)
  private String firstInventor;

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

  public String getPatentName() {
    return patentName;
  }

  public void setPatentName(String patentName) {
    this.patentName = patentName;
  }

  public String getPatentType() {
    return patentType;
  }

  public void setPatentType(String patentType) {
    this.patentType = patentType;
  }

  public String getGrantNo() {
    return grantNo;
  }

  public void setGrantNo(String grantNo) {
    this.grantNo = grantNo;
  }

  public LocalDate getGrantDate() {
    return grantDate;
  }

  public void setGrantDate(LocalDate grantDate) {
    this.grantDate = grantDate;
  }

  public String getFirstInventor() {
    return firstInventor;
  }

  public void setFirstInventor(String firstInventor) {
    this.firstInventor = firstInventor;
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
