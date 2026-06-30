package com.rutea.app.activitiesandviews.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavoriteDao;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ActivityRepository {

    private final ActivityApiService apiService;
    private final CachedFavoriteDao favoriteDao;

    @Inject
    public ActivityRepository(ActivityApiService apiService, CachedFavoriteDao favoriteDao) {
        this.apiService = apiService;
        this.favoriteDao = favoriteDao;
    }

    // ─── Room ────────────────────────────────────────────────────────────────

    public List<CachedFavorite> getFavoritesSync(String email) {
        return favoriteDao.getByEmail(email);
    }

    public void insertFavorite(CachedFavorite favorite) {
        Executors.newSingleThreadExecutor().execute(() -> favoriteDao.insert(favorite));
    }

    public void deleteFavorite(long activityId, String email) {
        Executors.newSingleThreadExecutor().execute(() -> favoriteDao.delete(activityId, email));
    }

    // ─── Network ─────────────────────────────────────────────────────────────

    public void fetchMostVisited(MutableLiveData<List<ActivityDto>> liveData,
                                  MutableLiveData<String> error) {
        apiService.getMostVisited().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(Call<List<ActivityDto>> call,
                                   Response<List<ActivityDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    error.postValue("Error cargando destinos");
                }
            }

            @Override
            public void onFailure(Call<List<ActivityDto>> call, Throwable t) {
                error.postValue("No se pudieron cargar los destinos. Revisá tu conexión.");
            }
        });
    }

    public void fetchRecommended(MutableLiveData<List<ActivityDto>> liveData,
                                  MutableLiveData<String> error) {
        apiService.getRecommendations().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(Call<List<ActivityDto>> call,
                                   Response<List<ActivityDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    error.postValue("Error cargando recomendaciones");
                }
            }

            @Override
            public void onFailure(Call<List<ActivityDto>> call, Throwable t) {
                error.postValue("No se pudieron cargar las recomendaciones. Revisá tu conexión.");
            }
        });
    }

    public void fetchTopRated(MutableLiveData<List<ActivityDto>> liveData,
                               MutableLiveData<String> error) {
        apiService.getTopRated().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(Call<List<ActivityDto>> call,
                                   Response<List<ActivityDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    error.postValue("Error cargando mejores puntuadas");
                }
            }

            @Override
            public void onFailure(Call<List<ActivityDto>> call, Throwable t) {
                error.postValue("No se pudieron cargar las mejor puntuadas. Revisá tu conexión.");
            }
        });
    }
}
