package com.renx.mg.request.dto;

public class MeResponse {
    private String username;
    private String locale;
    private Long userId;
    private Long profileId;
    private Boolean employee;
    /** Solo presente cuando el usuario es Company Admin: companyId de su customer. */
    private Long companyId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Boolean getEmployee() { return employee; }
    public void setEmployee(Boolean employee) { this.employee = employee; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}
