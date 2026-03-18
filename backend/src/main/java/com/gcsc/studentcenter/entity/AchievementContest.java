package com.gcsc.studentcenter.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_contests")
public class AchievementContest {

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

    @Column(name = "contest_name", nullable = false, length = 255)
    private String contestName;

    @Column(name = "organizer", length = 255)
    private String organizer;

    @Column(name = "contest_category", length = 64)
    private String contestCategory;

    @Column(name = "award_category", length = 64)
    private String awardCategory;

    @Column(name = "award_level", length = 64)
    private String awardLevel;

    @Column(name = "contest_type", length = 64)
    private String contestType;

    @Column(name = "award_date")
    private LocalDate awardDate;

    @Column(name = "award_count", length = 32)
    private String awardCount;

    @Column(name = "team_members", columnDefinition = "TEXT")
    private String teamMembers;

    @Column(name = "instructors", columnDefinition = "TEXT")
    private String instructors;

    @Column(name = "remark", length = 64)
    private String remark;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

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

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getContestCategory() {
        return contestCategory;
    }

    public void setContestCategory(String contestCategory) {
        this.contestCategory = contestCategory;
    }

    public String getAwardCategory() {
        return awardCategory;
    }

    public void setAwardCategory(String awardCategory) {
        this.awardCategory = awardCategory;
    }

    public String getAwardLevel() {
        return awardLevel;
    }

    public void setAwardLevel(String awardLevel) {
        this.awardLevel = awardLevel;
    }

    public String getContestType() {
        return contestType;
    }

    public void setContestType(String contestType) {
        this.contestType = contestType;
    }

    public LocalDate getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(LocalDate awardDate) {
        this.awardDate = awardDate;
    }

    public String getAwardCount() {
        return awardCount;
    }

    public void setAwardCount(String awardCount) {
        this.awardCount = awardCount;
    }

    public String getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(String teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getInstructors() {
        return instructors;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
