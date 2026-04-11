package com.rutea.app.activitiesandviews.ui.data.network.dto.auth;

public class OtpVerificationRequest {
    private String email;
    private String otp;

    public OtpVerificationRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}
