package com.renx.mg.request.dto;

import com.renx.mg.request.model.RequestStatusType;

import java.util.Date;
import java.util.List;

public class RequestDTO {
    private Long id;
    private Long serviceCategoryId;
    private Long serviceSubCategoryId;
    private String location;
    private String description;
    private Long siteId;
    private Long userId;
    private String siteName;
    private String companyName;
    private String requesterName;
    private String assignedStaffName;
    private Boolean canRate;
    private Long rating;
    private RequestStatusType requestStatus;
    private Date createDate;
    private String priority;
    private List<RequestHistoryDTO> history;
    private List<RequestAttachmentDTO> attachments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) { this.serviceCategoryId = serviceCategoryId; }
    public Long getServiceSubCategoryId() { return serviceSubCategoryId; }
    public void setServiceSubCategoryId(Long serviceSubCategoryId) { this.serviceSubCategoryId = serviceSubCategoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }
    public Boolean getCanRate() { return canRate; }
    public void setCanRate(Boolean canRate) { this.canRate = canRate; }
    public Long getRating() { return rating; }
    public void setRating(Long rating) { this.rating = rating; }
    public RequestStatusType getRequestStatus() { return requestStatus; }
    public void setRequestStatus(RequestStatusType requestStatus) { this.requestStatus = requestStatus; }
    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<RequestHistoryDTO> getHistory() { return history; }
    public void setHistory(List<RequestHistoryDTO> history) { this.history = history; }
    public List<RequestAttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<RequestAttachmentDTO> attachments) { this.attachments = attachments; }
}
