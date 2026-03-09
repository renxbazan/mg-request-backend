package com.renx.mg.request.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class ServiceCategory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	private String name;
	private String description;

	
	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceCategoryId", referencedColumnName = "id", insertable = false, updatable = false)
	private List<ServiceSubCategory> subCategoryList = new ArrayList<ServiceSubCategory>();
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<ServiceSubCategory> getSubCategoryList() {
		return subCategoryList;
	}
	public void setSubCategoryList(List<ServiceSubCategory> subCategoryList) {
		this.subCategoryList = subCategoryList;
	}
	
	
	
}
