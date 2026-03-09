package com.renx.mg.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RequestCreateDTO {
    @NotNull
    private Long siteId;
    @NotNull
    private Long serviceCategoryId;
    private Long serviceSubCategoryId;
    private String location;
    @NotBlank
    private String description;
    private String priority;

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubCategoryId() { return serviceSubCategoryId; }
    public void setServiceSubCategoryId(Long serviceSubCategoryId) { this.serviceSubCategoryId = serviceSubCategoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
