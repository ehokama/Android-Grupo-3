package com.rutea.app.activitiesandviews.ui.activityDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavoriteDao;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class ActivityDetailFragment extends Fragment {

    @Inject
    ActivityApiService activityApiService;

    @Inject
    CachedFavoriteDao cachedFavoriteDao;

    @Inject
    TokenManager tokenManager;

    private ImageButton btnFavorite;
    private boolean isFavorite = false;
    private ActivityDto loadedActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvDescripcion = view.findViewById(R.id.tvDescripcion);
        TextView tvHorario = view.findViewById(R.id.tvHorario);
        TextView tvUbicacion = view.findViewById(R.id.tvUbicacion);
        TextView tvCosto = view.findViewById(R.id.tvCosto);
        TextView tvGuia = view.findViewById(R.id.tvGuia);
        TextView tvEmpresa = view.findViewById(R.id.tvEmpresa);
        btnFavorite = view.findViewById(R.id.btnFavorite);

        long activityId = -1L;
        String fallbackName = "Actividad";
        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId", -1L);
            fallbackName = getArguments().getString("nombre", "Actividad");
        }
        final long selectedActivityId = activityId;
        final String selectedFallbackName = fallbackName;

        tvNombre.setText(selectedFallbackName);
        tvDescripcion.setText("Cargando detalle...");
        tvHorario.setText("🕐 Duración: -");
        tvUbicacion.setText("📍 Ubicación: -");
        tvCosto.setText("💲 Precio: -");
        tvGuia.setText("⭐ Rating: -");
        tvEmpresa.setText("🏷️ Categoría: -");

        // Verificar estado de favorito en Room
        if (selectedActivityId > 0) {
            String email = tokenManager.getEmail();
            Executors.newSingleThreadExecutor().execute(() -> {
                isFavorite = cachedFavoriteDao.isFavorite(selectedActivityId, email) > 0;
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            btnFavorite.setImageResource(isFavorite
                                    ? R.drawable.ic_favorite_filled
                                    : R.drawable.ic_favorite_border));
                }
            });

            activityApiService.getActivityById(selectedActivityId).enqueue(new Callback<ActivityDto>() {
                @Override
                public void onResponse(@NonNull Call<ActivityDto> call, @NonNull Response<ActivityDto> response) {
                    if (!isAdded()) return;
                    ActivityDto dto = response.body();
                    if (!response.isSuccessful() || dto == null) {
                        Toast.makeText(requireContext(), "No se pudo cargar el detalle.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadedActivity = dto;

                    tvNombre.setText(valueOrDefault(dto.getTitle(), selectedFallbackName));
                    tvDescripcion.setText(valueOrDefault(dto.getDescription(), "Sin descripción"));
                    tvHorario.setText("🕐 Duración: " + valueOrDefault(dto.getDuration(), "-") + " hs");
                    tvUbicacion.setText("📍 " + valueOrDefault(dto.getUbicationName(), "Ubicación no disponible"));
                    tvCosto.setText("💲 " + valueOrDefault(dto.getPrice(), "-"));
                    tvGuia.setText("⭐ Rating: " + valueOrDefault(dto.getRating(), "-"));
                    tvEmpresa.setText("🏷️ Categoría: " + valueOrDefault(dto.getCategory(), "-"));
                }

                @Override
                public void onFailure(@NonNull Call<ActivityDto> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Error de red al cargar detalle.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite(selectedActivityId));

        Button btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("nombre", tvNombre.getText().toString());
            args.putLong("activityId", selectedActivityId);
            Navigation.findNavController(view)
                    .navigate(R.id.action_detail_to_reservation, args);
        });
    }

    private void toggleFavorite(long activityId) {
        if (activityId <= 0) return;
        String email = tokenManager.getEmail();

        if (isFavorite) {
            isFavorite = false;
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            Executors.newSingleThreadExecutor().execute(() ->
                    cachedFavoriteDao.delete(activityId, email));
        } else {
            isFavorite = true;
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            Executors.newSingleThreadExecutor().execute(() -> {
                String imageUrl = "";
                if (loadedActivity != null && loadedActivity.getImages() != null
                        && !loadedActivity.getImages().isEmpty()) {
                    Long imgId = loadedActivity.getImages().get(0).getIdImage();
                    imageUrl = "http://10.0.2.2:8080/api/images/" + imgId + "/raw";
                }
                CachedFavorite fav = new CachedFavorite(
                        activityId, email,
                        loadedActivity != null ? loadedActivity.getTitle() : null,
                        loadedActivity != null ? loadedActivity.getPrice() : null,
                        loadedActivity != null ? loadedActivity.getRating() : null,
                        loadedActivity != null ? loadedActivity.getCategory() : null,
                        loadedActivity != null ? loadedActivity.getUbicationName() : null,
                        imageUrl
                );
                cachedFavoriteDao.insert(fav);
            });
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private String valueOrDefault(Number value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }
}
