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

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.ui.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.ui.data.network.RetrofitClient;
import com.rutea.app.activitiesandviews.ui.data.network.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.ui.data.network.dto.common.PageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ActivityApiService activityApiService;

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

        RetrofitClient.init(requireContext());
        activityApiService = RetrofitClient.createService(ActivityApiService.class);

        loadPagedSection(view, R.id.llDestinos, null);
        loadFeaturedSection(view, R.id.llRecomendadas);
        loadPagedSection(view, R.id.llMasReservadas, "rating,desc");
    }

    private void loadPagedSection(View root, int containerId, @Nullable String sort) {
        activityApiService.getActivities(
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                sort
        ).enqueue(new Callback<PageResponse<ActivityDto>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ActivityDto>> call, @NonNull Response<PageResponse<ActivityDto>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    renderCards(root, containerId, response.body().getContent());
                } else {
                    Toast.makeText(requireContext(), "No se pudo cargar actividades.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ActivityDto>> call, @NonNull Throwable throwable) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Error de red al cargar actividades.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFeaturedSection(View root, int containerId) {
        activityApiService.getFeaturedActivities().enqueue(new Callback<List<ActivityDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ActivityDto>> call, @NonNull Response<List<ActivityDto>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    renderCards(root, containerId, response.body());
                    return;
                }
                // fallback a listado general si featured viene vacío.
                loadPagedSection(root, containerId, null);
            }

            @Override
            public void onFailure(@NonNull Call<List<ActivityDto>> call, @NonNull Throwable throwable) {
                if (!isAdded()) {
                    return;
                }
                loadPagedSection(root, containerId, null);
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
            ((ImageView) card.findViewById(R.id.ivCard)).setImageResource(R.drawable.bg_hero_landscape);

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