package com.renx.mg.request.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import com.renx.mg.request.common.Constants;

@Entity
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	private Long serviceCategoryId;
	private Long serviceSubCategoryId;
	private String location;
	private String description;
	private Long siteId;
	private Long userId;
	@Enumerated(EnumType.STRING)
	private RequestStatusType requestStatus;
	private Date createDate;
	private String priority;

	@OneToOne
	@JoinColumn(name = "serviceSubCategoryId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private ServiceSubCategory serviceSubCategory;

	@OneToOne
	@JoinColumn(name = "serviceCategoryId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private ServiceCategory serviceCategory;

	@OneToOne
	@JoinColumn(name = "siteId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private Site site;

	@OneToOne
	@JoinColumn(name = "userId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private User user;

	@OneToMany
	@JoinColumn(name = "requestId", referencedColumnName = "id", unique = true, nullable = true, updatable = false, insertable = false)
	private List<RequestHistory> requestHistoryList;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getServiceSubCategoryId() {
		return serviceSubCategoryId;
	}

	public void setServiceSubCategoryId(Long serviceSubCategoryId) {
		this.serviceSubCategoryId = serviceSubCategoryId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public RequestStatusType getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(RequestStatusType requestStatus) {
		this.requestStatus = requestStatus;
	}

	public ServiceSubCategory getServiceSubCategory() {
		return serviceSubCategory;
	}

	public void setServiceSubCategory(ServiceSubCategory serviceSubCategory) {
		this.serviceSubCategory = serviceSubCategory;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<RequestHistory> getRequestHistoryList() {
		return requestHistoryList;
	}

	public void setRequestHistoryList(List<RequestHistory> requestHistoryList) {
		this.requestHistoryList = requestHistoryList;
	}

	public Long getServiceCategoryId() {
		return serviceCategoryId;
	}

	public void setServiceCategoryId(Long serviceCategoryId) {
		this.serviceCategoryId = serviceCategoryId;
	}

	public ServiceCategory getServiceCategory() {
		return serviceCategory;
	}

	public void setServiceCategory(ServiceCategory serviceCategory) {
		this.serviceCategory = serviceCategory;
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

			ZonedDateTime zonedDate = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.MG_LOCAL_TIMEZONE));

			return dateTimeFormatter.format(zonedDate);
		}
		return null;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	

}
