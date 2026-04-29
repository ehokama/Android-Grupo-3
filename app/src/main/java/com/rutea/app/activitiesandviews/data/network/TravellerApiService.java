package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface TravellerApiService {

    @GET("api/travellers/me")
    Call<TravellerDto> getMyProfile();

    @PUT("api/travellers/me")
    Call<TravellerDto> updateMyProfile(@Body TravellerDto request);
}
