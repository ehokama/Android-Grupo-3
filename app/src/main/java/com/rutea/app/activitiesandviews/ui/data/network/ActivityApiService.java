package com.rutea.app.activitiesandviews.ui.data.network;

import com.rutea.app.activitiesandviews.ui.data.network.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.ui.data.network.dto.common.PageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ActivityApiService {
    @GET("api/activities")
    Call<PageResponse<ActivityDto>> getActivities(
            @Query("destination") String destination,
            @Query("category") String category,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("date") String date,
            @Query("page") Integer page,
            @Query("size") Integer size,
            @Query("sort") String sort
    );

    @GET("api/activities/{id}")
    Call<ActivityDto> getActivityById(@Path("id") long id);

    @GET("api/activities/featured")
    Call<List<ActivityDto>> getFeaturedActivities();

    @GET("api/activities/categories")
    Call<List<String>> getCategories();

    @GET("api/activities/search")
    Call<PageResponse<ActivityDto>> searchActivities(
            @Query("keyword") String keyword,
            @Query("page") Integer page,
            @Query("size") Integer size
    );
}
