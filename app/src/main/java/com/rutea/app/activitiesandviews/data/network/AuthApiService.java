package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.auth.AuthRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.data.models.dto.auth.ChangeEmailRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.ChangePasswordRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.OtpRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.OtpVerificationRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/otp/request")
    Call<Void> requestOtp(@Body OtpRequest request);

    @POST("api/auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerificationRequest request);

    @POST("api/auth/change-password")
    Call<Map<String, String>> changePassword(@Body ChangePasswordRequest request);

    @POST("api/auth/change-email")
    Call<AuthResponse> changeEmail(@Body ChangeEmailRequest request);
}
