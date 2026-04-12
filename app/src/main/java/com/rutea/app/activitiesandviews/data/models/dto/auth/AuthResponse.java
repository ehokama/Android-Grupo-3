package com.rutea.app.activitiesandviews.data.models.dto.auth;

public class AuthResponse {
    private String token;
    private String email;
    private String name;

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
