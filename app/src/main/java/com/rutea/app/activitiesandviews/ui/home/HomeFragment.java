package com.rutea.app.activitiesandviews.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavoriteDao;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.disponibility.DisponibilityDto;
import com.rutea.app.activitiesandviews.data.models.dto.common.PageResponse;
import com.rutea.app.activitiesandviews.data.network.NewsApiService;
import com.rutea.app.activitiesandviews.ui.news.NewsAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import java.util.Locale;
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    ActivityApiService activityApiService;

    @Inject
    NewsApiService newsApiService;

    @Inject
    CachedFavoriteDao cachedFavoriteDao;

    @Inject
    TokenManager tokenManager;

    // IDs de actividades favoritas del usuario (cargado al inicio)
    private final Set<Long> favoriteIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.etSearch).setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_search)
        );

        view.findViewById(R.id.tvVerTodasNews).setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_news)
        );

        // Saludo
        String username = getArguments() != null ? getArguments().getString("username", "") : "";
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (!username.isEmpty()) tvGreeting.setText("Hola, " + username + " 👋");

        // Cargar favoritos del usuario primero y luego renderizar las secciones
        String email = tokenManager.getEmail();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CachedFavorite> favs = cachedFavoriteDao.getByEmail(email);
            favoriteIds.clear();
            for (CachedFavorite f : favs) favoriteIds.add(f.activityId);

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                loadMostVisited(view);
                loadRecommended(view);
                loadTopRated(view);
                loadNews(view);
            });
        });
    }

    private void loadMostVisited(View root) {
        activityApiService.getMostVisited().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ActivityDto>> call,
                                   @NonNull Response<List<ActivityDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderCards(root, R.id.llMostVisited, response.body());
                } else {
                    Toast.makeText(requireContext(), "Error cargando destinos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ActivityDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecommended(View root) {
        activityApiService.getRecommendations().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ActivityDto>> call,
                                   @NonNull Response<List<ActivityDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderCards(root, R.id.llRecomendadas, response.body());
                } else {
                    Toast.makeText(requireContext(), "Error cargando recomendaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ActivityDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTopRated(View root) {
        activityApiService.getTopRated().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ActivityDto>> call,
                                   @NonNull Response<List<ActivityDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderCards(root, R.id.llTopRated, response.body());
                } else {
                    Toast.makeText(requireContext(), "Error cargando mejores puntuadas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ActivityDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCards(View root, int containerId, List<ActivityDto> activities) {
        LinearLayout container = root.findViewById(containerId);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        for (ActivityDto activity : activities) {
            View card = inflater.inflate(R.layout.item_category_card, container, false);

            // Título
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(180), dpToPx(220));
            params.setMarginEnd(dpToPx(12));
            card.setLayoutParams(params);

            String title = activity.getTitle() == null || activity.getTitle().isEmpty()
                    ? "Actividad" : activity.getTitle();
            ((TextView) card.findViewById(R.id.tvCardLabel)).setText(title);

            // Categoría
            TextView tvCategory = card.findViewById(R.id.tvCardCategory);
            if (activity.getCategory() != null && !activity.getCategory().isEmpty()) {
                tvCategory.setText(activity.getCategory());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            // Destino
            TextView tvDestino = card.findViewById(R.id.tvCardDestino);
            tvDestino.setText(activity.getUbicationName() != null
                    ? activity.getUbicationName() : "—");

            // Duración
            TextView tvDuration = card.findViewById(R.id.tvCardDuration);
            tvDuration.setText(activity.getDuration() != null
                    ? activity.getDuration() + " min" : "—");

            // Precio
            TextView tvPrice = card.findViewById(R.id.tvCardPrice);
            tvPrice.setText(activity.getPrice() != null
                    ? String.format(Locale.getDefault(), "$%.0f", activity.getPrice()) : "Gratis");

            // Cupos disponibles (suma de disponibilidades futuras)
            TextView tvQuota = card.findViewById(R.id.tvCardQuota);
            Integer availableQuota = getAvailableQuota(activity);
            tvQuota.setText(availableQuota != null ? availableQuota + " cupos" : "Sin cupos");

            // Imagen
            ImageView imageView = card.findViewById(R.id.ivCard);
            String imageUrl = "";
            if (activity.getImages() != null && !activity.getImages().isEmpty()) {
                String url = activity.getImages().get(0).getUrl();
                if (url != null) {
                    imageUrl = url;
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.bg_hero_landscape)
                            .error(R.drawable.bg_hero_landscape)
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.bg_hero_landscape);
                }
            } else {
                imageView.setImageResource(R.drawable.bg_hero_landscape);
            }

            // Click → detalle
            // Corazón de favorito
            ImageButton btnFav = card.findViewById(R.id.btnFavorite);
            boolean isFav = activity.getId() != null && favoriteIds.contains(activity.getId());
            btnFav.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            final String finalImageUrl = imageUrl;
            btnFav.setOnClickListener(v -> toggleFavorite(activity, btnFav, finalImageUrl));

            card.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("activityId", activity.getId() == null ? -1L : activity.getId());
                args.putString("nombre", title);
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_detail, args);
            });

            container.addView(card);
        }
    }

    private void toggleFavorite(ActivityDto activity, ImageButton btnFav, String imageUrl) {
        if (activity.getId() == null) return;
        long id = activity.getId();
        String email = tokenManager.getEmail();
        boolean isFav = favoriteIds.contains(id);

        if (isFav) {
            favoriteIds.remove(id);
            btnFav.setImageResource(R.drawable.ic_favorite_border);
            Executors.newSingleThreadExecutor().execute(() ->
                    cachedFavoriteDao.delete(id, email));
        } else {
            favoriteIds.add(id);
            btnFav.setImageResource(R.drawable.ic_favorite_filled);
            Executors.newSingleThreadExecutor().execute(() -> {
                CachedFavorite fav = new CachedFavorite(
                        id, email,
                        activity.getTitle(),
                        activity.getPrice(),
                        activity.getRating(),
                        activity.getCategory(),
                        activity.getUbicationName(),
                        imageUrl
                );
                cachedFavoriteDao.insert(fav);
            });
        }
    }

    private void loadNews(View root) {
        newsApiService.getNews().enqueue(new Callback<List<NewsDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsDto>> call,
                                   @NonNull Response<List<NewsDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderNewsCards(root, response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsDto>> call, @NonNull Throwable t) { }
        });
    }

    private void renderNewsCards(View root, List<NewsDto> newsList) {
        LinearLayout container = root.findViewById(R.id.llNews);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        for (NewsDto news : newsList) {
            View card = inflater.inflate(R.layout.item_news_card, container, false);

            TextView tvTitle = card.findViewById(R.id.tvNewsCardTitle);
            TextView tvDesc  = card.findViewById(R.id.tvNewsCardDesc);
            TextView tvType  = card.findViewById(R.id.tvNewsCardType);
            ImageView ivImage = card.findViewById(R.id.ivNewsCardImage);

            tvTitle.setText(news.getTitle() != null ? news.getTitle() : "");
            tvDesc.setText(news.getDescription() != null ? news.getDescription() : "");

            String type = news.getType() != null ? news.getType() : "";
            tvType.setText(type);
            tvType.setBackgroundColor(NewsAdapter.typeColor(type));

            String imageUrl = NewsAdapter.resolveImageUrl(news);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_hero_landscape)
                        .error(R.drawable.bg_hero_landscape)
                        .centerCrop()
                        .into(ivImage);
            }

            card.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("newsId", news.getId() != null ? news.getId() : -1L);
                args.putString("newsTitle", news.getTitle());
                args.putString("newsDesc", news.getDescription());
                args.putString("newsContent", news.getContent());
                args.putString("newsType", news.getType());
                args.putString("newsImageUrl", imageUrl);
                args.putString("newsDiscount", news.getDiscount());
                if (news.getActivityId() != null) args.putLong("activityId", news.getActivityId());
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_news_detail, args);
            });

            container.addView(card);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private Integer getAvailableQuota(ActivityDto activity) {
        if (activity.getDisponibilities() == null || activity.getDisponibilities().isEmpty()) {
            return null;
        }
        int total = 0;
        for (DisponibilityDto d : activity.getDisponibilities()) {
            if (d != null && d.getDisponibleQuota() != null && d.getDisponibleQuota() > 0) {
                total += d.getDisponibleQuota();
            }
        }
        return total;
    }
}
