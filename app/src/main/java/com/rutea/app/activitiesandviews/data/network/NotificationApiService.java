package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.notification.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationApiService {

    @GET("api/notifications/poll")
    Call<List<NotificationDto>> pollNotifications(@Query("timeout") int timeoutSeconds);
}
