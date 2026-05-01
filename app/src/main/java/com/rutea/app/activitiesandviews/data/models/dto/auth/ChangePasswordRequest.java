package com.rutea.app.activitiesandviews.data.models.dto.auth;

public class ChangePasswordRequest {
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
