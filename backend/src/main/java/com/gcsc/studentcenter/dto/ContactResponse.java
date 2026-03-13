package com.gcsc.studentcenter.dto;

import java.time.LocalDateTime;

public class ContactResponse {
    private Long id;
    private String name;
    private String office;
    private String duty;
    private String phone;
    private String photoUrl;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContactResponse(
        Long id,
        String name,
        String office,
        String duty,
        String phone,
        String photoUrl,
        Integer sortOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.office = office;
        this.duty = duty;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.sortOrder = sortOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOffice() {
        return office;
    }

    public String getDuty() {
        return duty;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
