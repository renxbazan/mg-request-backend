package com.renx.mg.request.dto;

public class UserDTO {
    private Long id;
    private String username;
    private Long customerId;
    private Long profileId;
    private Long siteId;
    private String locale;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
}
