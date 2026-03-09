package com.renx.mg.request.dto;

public class MenuItemDTO {
    private Long id;
    private String description;
    private String uri;
    private Integer position;
    /** H = group/header, N = normal link */
    private String type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
