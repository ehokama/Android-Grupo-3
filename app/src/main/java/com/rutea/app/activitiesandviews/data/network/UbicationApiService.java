package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.ubication.UbicationDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface UbicationApiService {
    @GET("api/ubications")
    Call<List<UbicationDto>> getUbications();
}