package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.voucher.CheckInRequest;
import com.rutea.app.activitiesandviews.data.models.dto.voucher.CheckInResponse;
import com.rutea.app.activitiesandviews.data.models.dto.voucher.VoucherDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VoucherApiService {

    @GET("api/reserves/{id}/voucher")
    Call<VoucherDto> getVoucher(@Path("id") long reserveId);

    @POST("api/reserves/check-in")
    Call<CheckInResponse> checkIn(@Body CheckInRequest request);
}
