package com.rutea.app.activitiesandviews.data.models.dto.auth;

import java.util.Set;

public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private Set<String> preferences;

    public RegisterRequest(String email, String password, String name, String phone, Set<String> preferences) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.preferences = preferences;
    }
}
