package com.rutea.app.activitiesandviews.ui.myActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.ui.data.network.ReserveApiService;
import com.rutea.app.activitiesandviews.ui.data.network.RetrofitClient;
import com.rutea.app.activitiesandviews.ui.data.network.dto.reserve.ReserveDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyActivitiesFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar progressBar;
    private TextView tvState;
    private ReserveApiService reserveApiService;

    public MyActivitiesFragment() {
        super(R.layout.fragment_my_activities);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rvActivities);
        progressBar = view.findViewById(R.id.progressMyActivities);
        tvState = view.findViewById(R.id.tvStateMyActivities);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        RetrofitClient.init(requireContext());
        reserveApiService = RetrofitClient.createService(ReserveApiService.class);
        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reserveApiService != null) {
            loadHistory();
        }
    }

    private void loadHistory() {
        setState(true, null);
        reserveApiService.getMyHistory().enqueue(new Callback<List<ReserveDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReserveDto>> call, @NonNull Response<List<ReserveDto>> response) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    setState(false, "No se pudieron cargar tus reservas.");
                    return;
                }

                List<ReserveDto> reserves = response.body();
                if (reserves.isEmpty()) {
                    rv.setAdapter(new ActividadAdapter(new ArrayList<>()));
                    setState(false, "Todavía no tenés reservas creadas.");
                    return;
                }

                List<Actividad> mappedActivities = new ArrayList<>();
                for (ReserveDto reserve : reserves) {
                    mappedActivities.add(new Actividad(
                            safeText(reserve.getActivityTitle(), "Actividad"),
                            "Reserva #" + safeText(reserve.getIdReserve(), "-"),
                            safeText(reserve.getState(), "SIN_ESTADO"),
                            formatDateTime(reserve.getCreationDate()),
                            "$" + safeText(reserve.getTotalPrice(), "0"),
                            "Personas: " + safeText(reserve.getNumberOfPeople(), "1"),
                            R.drawable.bg_hero_landscape
                    ));
                }

                rv.setAdapter(new ActividadAdapter(mappedActivities));
                setState(false, null);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReserveDto>> call, @NonNull Throwable throwable) {
                if (!isAdded()) {
                    return;
                }
                setState(false, "Error de red al cargar tus reservas.");
                Toast.makeText(requireContext(), "No se pudo conectar con reservas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setState(boolean loading, @Nullable String message) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (message == null || message.isEmpty()) {
            tvState.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        } else {
            tvState.setText(message);
            tvState.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
    }

    private String formatDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isEmpty()) {
            return "Fecha no disponible";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawDateTime);
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
        } catch (Exception e) {
            return rawDateTime.replace("T", " ");
        }
    }

    private String safeText(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}