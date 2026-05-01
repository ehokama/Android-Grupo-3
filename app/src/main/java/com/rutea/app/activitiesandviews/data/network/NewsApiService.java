package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NewsApiService {

    @GET("api/news")
    Call<List<NewsDto>> getNews();

    @GET("api/news/{id}")
    Call<NewsDto> getNewsById(@Path("id") long id);
}
