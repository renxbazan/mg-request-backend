package com.renx.mg.request.dto;

import com.renx.mg.request.model.CompanyType;

public class CompanyDTO {
    private Long id;
    private String name;
    private String description;
    private CompanyType companyType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CompanyType getCompanyType() { return companyType; }
    public void setCompanyType(CompanyType companyType) { this.companyType = companyType; }
}
