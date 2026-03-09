package com.renx.mg.request.dto;

public class LoginResponse {

    private String token;
    private String username;
    private Long userId;
    private Long profileId;
    private String locale;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, Long userId, Long profileId) {
        this(token, username, userId, profileId, "es");
    }

    public LoginResponse(String token, String username, Long userId, Long profileId, String locale) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.profileId = profileId;
        this.locale = locale != null && !locale.isBlank() ? locale : "es";
    }

    public static LoginResponse error(String message) {
        LoginResponse r = new LoginResponse();
        r.setMessage(message);
        return r;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
