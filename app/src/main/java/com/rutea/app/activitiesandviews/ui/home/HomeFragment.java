package com.rutea.app.activitiesandviews.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.common.PageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    ActivityApiService activityApiService;

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

        // Saludo
        String username = getArguments() != null ? getArguments().getString("username", "") : "";
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (!username.isEmpty()) tvGreeting.setText("Hola, " + username + " 👋");

        loadMostVisited(view);
        loadRecommended(view);
        loadTopRated(view);
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(160), dpToPx(120));
            params.setMarginEnd(dpToPx(12));
            card.setLayoutParams(params);

            String title = activity.getTitle() == null || activity.getTitle().isEmpty() ? "Actividad" : activity.getTitle();
            ((TextView) card.findViewById(R.id.tvCardLabel)).setText(title);
            ImageView imageView = card.findViewById(R.id.ivCard);

            if (activity.getImages() != null && !activity.getImages().isEmpty()) {
                Long imageId = activity.getImages().get(0).getIdImage();

                String imageUrl = "http://10.0.2.2:8080/api/images/" + imageId + "/raw";

                Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_hero_landscape)
                        .error(R.drawable.bg_hero_landscape)
                        .into(imageView);

            } else {
                imageView.setImageResource(R.drawable.bg_hero_landscape);
            }

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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}