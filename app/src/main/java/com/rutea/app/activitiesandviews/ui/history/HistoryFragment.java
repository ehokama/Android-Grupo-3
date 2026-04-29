package com.rutea.app.activitiesandviews.ui.history;

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
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;
import com.rutea.app.activitiesandviews.data.network.ReserveApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private ProgressBar progressHistory;
    private TextView tvStateHistory;

    @Inject
    ReserveApiService reserveApiService;

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rvHistory);
        progressHistory = view.findViewById(R.id.progressHistory);
        tvStateHistory = view.findViewById(R.id.tvStateHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistory();
    }

    private void loadHistory() {
        setState(true, null);
        reserveApiService.getMyHistory().enqueue(new Callback<List<ReserveDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReserveDto>> call, @NonNull Response<List<ReserveDto>> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful() || response.body() == null) {
                    setState(false, "No se pudo cargar el historial.");
                    return;
                }

                List<ReserveDto> reserves = response.body();
                if (reserves.isEmpty()) {
                    rvHistory.setAdapter(new ReserveAdapter(new ArrayList<>(), null));
                    setState(false, "Todavía no tenés historial de viajes.");
                    return;
                }

                ReserveAdapter adapter = new ReserveAdapter(reserves, reserveId -> cancelReserve(reserveId));
                rvHistory.setAdapter(adapter);
                setState(false, null);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReserveDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setState(false, "Error de red al cargar el historial.");
            }
        });
    }

    private void cancelReserve(Long reserveId) {
        setState(true, null);
        reserveApiService.cancelReserve(reserveId).enqueue(new Callback<ReserveDto>() {
            @Override
            public void onResponse(@NonNull Call<ReserveDto> call, @NonNull Response<ReserveDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Reserva cancelada exitosamente", Toast.LENGTH_SHORT).show();
                    loadHistory(); // Reload the list to get updated states
                } else {
                    Toast.makeText(requireContext(), "No se pudo cancelar la reserva", Toast.LENGTH_SHORT).show();
                    loadHistory();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReserveDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red al cancelar", Toast.LENGTH_SHORT).show();
                loadHistory();
            }
        });
    }

    private void setState(boolean loading, @Nullable String message) {
        progressHistory.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (message == null || message.isEmpty()) {
            tvStateHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
        } else {
            tvStateHistory.setText(message);
            tvStateHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        }
    }
}
