package com.rutea.app.activitiesandviews.ui.activityDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetailFragment extends Fragment {

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

        if (selectedActivityId > 0) {
            RetrofitClient.init(requireContext());
            ActivityApiService activityApiService = RetrofitClient.createService(ActivityApiService.class);
            activityApiService.getActivityById(selectedActivityId).enqueue(new Callback<ActivityDto>() {
                @Override
                public void onResponse(@NonNull Call<ActivityDto> call, @NonNull Response<ActivityDto> response) {
                    if (!isAdded()) {
                        return;
                    }
                    ActivityDto dto = response.body();
                    if (!response.isSuccessful() || dto == null) {
                        Toast.makeText(requireContext(), "No se pudo cargar el detalle.", Toast.LENGTH_SHORT).show();
                        return;
                    }

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
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(), "Error de red al cargar detalle.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("nombre", tvNombre.getText().toString());
            args.putLong("activityId", selectedActivityId);
            Navigation.findNavController(view)
                    .navigate(R.id.action_detail_to_reservation, args);
        });
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private String valueOrDefault(Number value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }
}