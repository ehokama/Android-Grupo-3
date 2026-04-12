package com.rutea.app.activitiesandviews.ui.data.network;

import com.rutea.app.activitiesandviews.ui.data.network.dto.disponibility.DisponibilityDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DisponibilityApiService {
    @GET("api/disponibilities")
    Call<List<DisponibilityDto>> getDisponibilities();

    @GET("api/disponibilities/{id}")
    Call<DisponibilityDto> getDisponibilityById(@Path("id") long id);
}
