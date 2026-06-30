package com.rutea.app.activitiesandviews.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.network.NewsApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class NewsRepository {

    private final NewsApiService apiService;

    @Inject
    public NewsRepository(NewsApiService apiService) {
        this.apiService = apiService;
    }

    public void fetchNews(MutableLiveData<List<NewsDto>> liveData,
                          MutableLiveData<String> error) {
        apiService.getNews().enqueue(new Callback<List<NewsDto>>() {
            @Override
            public void onResponse(Call<List<NewsDto>> call,
                                   Response<List<NewsDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<NewsDto>> call, Throwable t) {
                // News is non-critical; silently ignore
            }
        });
    }
}
