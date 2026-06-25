package com.medication.manage.model;

/**
 * 登录响应
 */
public class LoginResponse {
    private Long userId;
    private String token;
    private String name;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
