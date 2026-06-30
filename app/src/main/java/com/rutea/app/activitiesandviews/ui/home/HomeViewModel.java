package com.rutea.app.activitiesandviews.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.repository.ActivityRepository;
import com.rutea.app.activitiesandviews.data.repository.NewsRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    public final MutableLiveData<List<ActivityDto>> mostVisited   = new MutableLiveData<>();
    public final MutableLiveData<List<ActivityDto>> recommended   = new MutableLiveData<>();
    public final MutableLiveData<List<ActivityDto>> topRated      = new MutableLiveData<>();
    public final MutableLiveData<List<NewsDto>>     news          = new MutableLiveData<>();
    public final MutableLiveData<Set<Long>>         favoriteIds   = new MutableLiveData<>(new HashSet<>());
    public final MutableLiveData<String>            error         = new MutableLiveData<>();

    private final ActivityRepository activityRepository;
    private final NewsRepository     newsRepository;
    private final TokenManager       tokenManager;

    @Inject
    public HomeViewModel(ActivityRepository activityRepository,
                         NewsRepository newsRepository,
                         TokenManager tokenManager) {
        this.activityRepository = activityRepository;
        this.newsRepository     = newsRepository;
        this.tokenManager       = tokenManager;
    }

    /**
     * Carga favoritos desde Room (background) y luego dispara las llamadas de red en paralelo.
     */
    public void loadAll() {
        String email = tokenManager.getEmail();

        // 1. Favorites from Room (fast, background thread)
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CachedFavorite> favs = activityRepository.getFavoritesSync(email);
            Set<Long> ids = new HashSet<>();
            for (CachedFavorite f : favs) ids.add(f.activityId);
            favoriteIds.postValue(ids);
        });

        // 2. Network calls (Retrofit uses its own thread pool)
        activityRepository.fetchMostVisited(mostVisited, error);
        activityRepository.fetchRecommended(recommended, error);
        activityRepository.fetchTopRated(topRated, error);
        newsRepository.fetchNews(news, error);
    }

    /**
     * Alterna el estado de favorito de una actividad: actualiza la caché Room
     * y refleja el cambio en el LiveData de favoriteIds.
     */
    public void toggleFavorite(ActivityDto activity, String imageUrl) {
        if (activity.getId() == null) return;
        long id     = activity.getId();
        String email = tokenManager.getEmail();

        Set<Long> current = favoriteIds.getValue();
        Set<Long> updated = current != null ? new HashSet<>(current) : new HashSet<>();

        if (updated.contains(id)) {
            updated.remove(id);
            activityRepository.deleteFavorite(id, email);
        } else {
            updated.add(id);
            CachedFavorite fav = new CachedFavorite(
                    id, email,
                    activity.getTitle(),
                    activity.getPrice(),
                    activity.getRating(),
                    activity.getCategory(),
                    activity.getUbicationName(),
                    imageUrl
            );
            activityRepository.insertFavorite(fav);
        }
        favoriteIds.setValue(updated);
    }
}
