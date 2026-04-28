package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.reserve.CreateReserveRequest;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReserveApiService {
    @GET("api/reserves/my-history")
    Call<List<ReserveDto>> getMyHistory();

    @POST("api/reserves")
    Call<ReserveDto> createReserve(@Body CreateReserveRequest request);

    @POST("api/reserves/{id}/cancel")
    Call<ReserveDto> cancelReserve(@Path("id") long reserveId);
}
