package com.rutea.app.activitiesandviews.ui.favorites;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavoriteDao;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {

    @Inject CachedFavoriteDao cachedFavoriteDao;
    @Inject TokenManager tokenManager;
    @Inject ActivityApiService activityApiService;

    private FavoriteAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        RecyclerView rv = view.findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoriteAdapter(new FavoriteAdapter.Listener() {
            @Override
            public void onUnfavorite(CachedFavorite favorite) {
                removeFavorite(favorite);
            }

            @Override
            public void onReserve(CachedFavorite favorite) {
                Bundle args = new Bundle();
                args.putLong("activityId", favorite.activityId);
                args.putString("nombre", favorite.title);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_favorites_to_detail, args);
            }

            @Override
            public void onItemClick(CachedFavorite favorite) {
                clearNewFlag(favorite);
                Bundle args = new Bundle();
                args.putLong("activityId", favorite.activityId);
                args.putString("nombre", favorite.title);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_favorites_to_detail, args);
            }
        });

        rv.setAdapter(adapter);
        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        String email = tokenManager.getEmail();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<CachedFavorite> favs = cachedFavoriteDao.getByEmail(email);
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (favs.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    adapter.updateList(favs);
                    checkForUpdates(favs);
                }
            });
        });
    }

    // Re-consulta la API para detectar cambios de precio
    private void checkForUpdates(List<CachedFavorite> favorites) {
        String email = tokenManager.getEmail();
        for (CachedFavorite fav : favorites) {
            activityApiService.getActivityById(fav.activityId).enqueue(new Callback<ActivityDto>() {
                @Override
                public void onResponse(@NonNull Call<ActivityDto> call, @NonNull Response<ActivityDto> response) {
                    if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                    ActivityDto dto = response.body();

                    boolean priceChanged = dto.getPrice() != null && !dto.getPrice().equals(fav.price);
                    if (priceChanged) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            cachedFavoriteDao.updatePrice(fav.activityId, email, dto.getPrice());
                            cachedFavoriteDao.setHasNewUpdate(fav.activityId, email, true);
                            List<CachedFavorite> updated = cachedFavoriteDao.getByEmail(email);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> adapter.updateList(updated));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ActivityDto> call, @NonNull Throwable t) { }
            });
        }
    }

    private void removeFavorite(CachedFavorite favorite) {
        String email = tokenManager.getEmail();
        Executors.newSingleThreadExecutor().execute(() -> {
            cachedFavoriteDao.delete(favorite.activityId, email);
            List<CachedFavorite> updated = cachedFavoriteDao.getByEmail(email);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.updateList(updated);
                    if (updated.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void clearNewFlag(CachedFavorite favorite) {
        if (!favorite.hasNewUpdate) return;
        String email = tokenManager.getEmail();
        Executors.newSingleThreadExecutor().execute(() ->
                cachedFavoriteDao.setHasNewUpdate(favorite.activityId, email, false));
    }
}
