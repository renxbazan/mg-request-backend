package com.renx.mg.request.dto;

import com.renx.mg.request.model.RequestStatusType;

import java.util.Date;

public class RequestHistoryDTO {
    private Long id;
    private RequestStatusType requestStatus;
    private String comments;
    private Long rating;
    private Date createDate;
    private String userName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RequestStatusType getRequestStatus() { return requestStatus; }
    public void setRequestStatus(RequestStatusType requestStatus) { this.requestStatus = requestStatus; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Long getRating() { return rating; }
    public void setRating(Long rating) { this.rating = rating; }
    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
