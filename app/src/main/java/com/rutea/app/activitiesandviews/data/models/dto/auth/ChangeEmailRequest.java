package com.rutea.app.activitiesandviews.data.models.dto.auth;

public class ChangeEmailRequest {
    private final String currentPassword;
    private final String newEmail;

    public ChangeEmailRequest(String currentPassword, String newEmail) {
        this.currentPassword = currentPassword;
        this.newEmail = newEmail;
    }
}
