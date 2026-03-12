package com.renx.mg.request.dto;

/**
 * Una asignación de aprobador: usuario asignado como aprobador a nivel COMPANY o SITE.
 */
public class RequestApproverDTO {
    private Long id;
    private Long userId;
    private String userName;
    /** "COMPANY" o "SITE" */
    private String scope;
    private Long companyId;
    private Long siteId;
    private String siteName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
}
