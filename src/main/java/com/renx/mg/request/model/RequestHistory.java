package com.renx.mg.request.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import com.renx.mg.request.common.Constants;

@Entity
public class RequestHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	private Long requestId;

	@Enumerated(EnumType.STRING)
	private RequestStatusType requestStatus;

	@OneToOne
	@JoinColumn(name = "userId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private User user;

	private Long userId;

	private Long rating;

	private String comments;
	
	private Date createDate;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public RequestStatusType getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(RequestStatusType requestStatus) {
		this.requestStatus = requestStatus;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRating() {
		return rating;
	}

	public void setRating(Long rating) {
		this.rating = rating;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getStringLocalDateTime() {
		
		return getLocalDate(createDate);
	}
	private String getLocalDate(Date date) {
	    if (date != null) {

	      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

	      ZonedDateTime zonedDate =
	          ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.MG_LOCAL_TIMEZONE));

	      return dateTimeFormatter.format(zonedDate);
	    }
	    return null;
	  }
	

}
