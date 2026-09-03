package com.sidhant.fraudmanagement.dto.response;

import com.sidhant.fraudmanagement.enums.UserRole;

public class LoginResponse {

    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    private String token;

    public LoginResponse(
            Long userId,
            String name,
            String email,
            UserRole role,
            String token
    ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}