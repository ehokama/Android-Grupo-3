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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.disponibility.DisponibilityDto;
import com.rutea.app.activitiesandviews.ui.news.NewsAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    // Local copy of favorite IDs, kept in sync with LiveData for use inside renderCards
    private Set<Long> localFavoriteIds = new HashSet<>();

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

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // ── Navegación ──────────────────────────────────────────────────────
        view.findViewById(R.id.etSearch).setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_search)
        );

        view.findViewById(R.id.tvVerTodasNews).setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_news)
        );

        // ── Saludo ──────────────────────────────────────────────────────────
        String username = getArguments() != null ? getArguments().getString("username", "") : "";
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (!username.isEmpty()) tvGreeting.setText("Hola, " + username + " 👋");

        // ── Observadores LiveData ────────────────────────────────────────────
        viewModel.favoriteIds.observe(getViewLifecycleOwner(), ids -> {
            localFavoriteIds = ids != null ? ids : new HashSet<>();
        });

        viewModel.mostVisited.observe(getViewLifecycleOwner(), activities -> {
            if (activities != null) renderCards(view, R.id.llMostVisited, activities);
        });

        viewModel.recommended.observe(getViewLifecycleOwner(), activities -> {
            if (activities != null) renderCards(view, R.id.llRecomendadas, activities);
        });

        viewModel.topRated.observe(getViewLifecycleOwner(), activities -> {
            if (activities != null) renderCards(view, R.id.llTopRated, activities);
        });

        viewModel.news.observe(getViewLifecycleOwner(), newsList -> {
            if (newsList != null) renderNewsCards(view, newsList);
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // ── Carga de datos ───────────────────────────────────────────────────
        viewModel.loadAll();
    }

    // ─── Renderizado de cards de actividades ──────────────────────────────────

    private void renderCards(View root, int containerId, List<ActivityDto> activities) {
        LinearLayout container = root.findViewById(containerId);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        for (ActivityDto activity : activities) {
            View card = inflater.inflate(R.layout.item_category_card, container, false);

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

            // Cupos disponibles
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

            // Corazón de favorito
            ImageButton btnFav = card.findViewById(R.id.btnFavorite);
            boolean isFav = activity.getId() != null && localFavoriteIds.contains(activity.getId());
            btnFav.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            final String finalImageUrl = imageUrl;
            btnFav.setOnClickListener(v -> {
                // Actualización visual inmediata (UI optimista)
                boolean isFavNow = activity.getId() != null
                        && localFavoriteIds.contains(activity.getId());
                btnFav.setImageResource(isFavNow
                        ? R.drawable.ic_favorite_border
                        : R.drawable.ic_favorite_filled);
                // Delegar lógica al ViewModel
                viewModel.toggleFavorite(activity, finalImageUrl);
            });

            // Click → detalle de actividad
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

    // ─── Renderizado de cards de noticias ─────────────────────────────────────

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

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
