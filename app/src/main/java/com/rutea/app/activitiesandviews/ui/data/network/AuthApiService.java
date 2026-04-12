package com.rutea.app.activitiesandviews.ui.data.network;

import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.OtpRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.OtpVerificationRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/otp/request")
    Call<String> requestOtp(@Body OtpRequest request);

    @POST("api/auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerificationRequest request);
}
