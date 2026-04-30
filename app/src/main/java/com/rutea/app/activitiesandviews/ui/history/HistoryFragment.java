package com.rutea.app.activitiesandviews.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedReserve;
import com.rutea.app.activitiesandviews.data.local.db.CachedReserveDao;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;
import com.rutea.app.activitiesandviews.data.network.ReserveApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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
    private ChipGroup chipGroupActiveFilters;
    private MaterialButton btnFilter;
    private HistoryAdapter adapter;

    @Inject ReserveApiService reserveApiService;
    @Inject TokenManager tokenManager;
    @Inject CachedReserveDao cachedReserveDao;

    // Lista completa sin filtrar
    private List<ReserveDto> allReserves = new ArrayList<>();

    // Filtros activos
    private String filterCountry  = "";
    private String filterDateFrom = "";
    private String filterDateTo   = "";

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory              = view.findViewById(R.id.rvHistory);
        progressHistory        = view.findViewById(R.id.progressHistory);
        tvStateHistory         = view.findViewById(R.id.tvStateHistory);
        chipGroupActiveFilters = view.findViewById(R.id.chipGroupActiveFilters);
        btnFilter              = view.findViewById(R.id.btnFilter);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        btnFilter.setOnClickListener(v -> openFilterSheet());

        loadCachedReserves();
        loadHistory();
    }

    // ─── Cache-first ──────────────────────────────────────────────────────────

    private void loadCachedReserves() {
        String email = tokenManager.getEmail();
        if (email == null || email.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<CachedReserve> cached = cachedReserveDao.getByEmail(email);
            if (cached == null || cached.isEmpty() || !isAdded()) return;

            List<ReserveDto> dtos = mapCachedToDtos(cached);
            requireActivity().runOnUiThread(() -> setAllReservesAndShow(dtos));
        });
    }

    private List<ReserveDto> mapCachedToDtos(List<CachedReserve> cached) {
        List<ReserveDto> list = new ArrayList<>();
        for (CachedReserve c : cached) {
            ReserveDto dto = new ReserveDto();
            dto.setIdReserve(c.idReserve);
            dto.setActivityTitle(c.activityTitle);
            dto.setActivityId(c.activityId);
            dto.setReservationDate(c.reservationDate);
            dto.setCreationDate(c.creationDate);
            dto.setNumberOfPeople(c.numberOfPeople);
            dto.setTotalPrice(c.totalPrice);
            dto.setState(c.state);
            list.add(dto);
        }
        return list;
    }

    // ─── Red ─────────────────────────────────────────────────────────────────

    private void loadHistory() {
        setState(true, null);
        reserveApiService.getMyHistory().enqueue(new Callback<List<ReserveDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReserveDto>> call,
                                   @NonNull Response<List<ReserveDto>> response) {
                if (!isAdded()) return;
                setState(false, null);

                if (!response.isSuccessful() || response.body() == null) {
                    if (adapter == null || adapter.getItemCount() == 0)
                        setState(false, "No se pudo cargar el historial.");
                    return;
                }

                List<ReserveDto> reserves = response.body();
                if (reserves.isEmpty()) {
                    setAllReservesAndShow(new ArrayList<>());
                    setState(false, "Todavía no tenés historial de viajes.");
                    return;
                }

                setAllReservesAndShow(reserves);
                saveToCacheAsync(reserves);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReserveDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (adapter == null || adapter.getItemCount() == 0)
                    setState(false, "Sin conexión. No hay historial en caché.");
                else
                    setState(false, null);
            }
        });
    }

    // ─── Datos ───────────────────────────────────────────────────────────────

    /** Guarda la lista completa y aplica los filtros activos. */
    private void setAllReservesAndShow(List<ReserveDto> reserves) {
        allReserves = reserves;
        applyFilters();
    }

    private void showReserves(List<ReserveDto> reserves) {
        adapter = new HistoryAdapter(reserves, this::onCancelClicked);
        rvHistory.setAdapter(adapter);
        tvStateHistory.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
    }

    // ─── Filtros ─────────────────────────────────────────────────────────────

    private void openFilterSheet() {
        ArrayList<String> countries = new ArrayList<>();
        for (ReserveDto r : allReserves) {
            if (r.getCountry() != null && !r.getCountry().isEmpty()
                    && !countries.contains(r.getCountry())) {
                countries.add(r.getCountry());
            }
        }
        Collections.sort(countries);

        HistoryFilterBottomSheet sheet = HistoryFilterBottomSheet.newInstance(
                filterCountry, filterDateFrom, filterDateTo, countries);

        sheet.setFilterListener(new HistoryFilterBottomSheet.FilterListener() {
            @Override
            public void onFiltersApplied(String country, String dateFrom, String dateTo) {
                filterCountry  = country;
                filterDateFrom = dateFrom;
                filterDateTo   = dateTo;
                applyFilters();
                updateFilterChips();
            }

            @Override
            public void onFiltersCleared() {
                filterCountry  = "";
                filterDateFrom = "";
                filterDateTo   = "";
                applyFilters();
                updateFilterChips();
            }
        });

        sheet.show(getChildFragmentManager(), "filter");
    }

    private void applyFilters() {
        List<ReserveDto> filtered = new ArrayList<>();
        for (ReserveDto r : allReserves) {
            if (!matchesCountry(r))   continue;
            if (!matchesDateRange(r)) continue;
            filtered.add(r);
        }

        if (filtered.isEmpty() && !allReserves.isEmpty()) {
            showReserves(new ArrayList<>());
            setState(false, "No hay reservas que coincidan con los filtros.");
        } else {
            showReserves(filtered);
        }
    }

    private boolean matchesCountry(ReserveDto r) {
        if (filterCountry == null || filterCountry.isEmpty()) return true;
        if (r.getCountry() == null) return false;
        return r.getCountry().toLowerCase(Locale.getDefault())
                .contains(filterCountry.toLowerCase(Locale.getDefault()));
    }

    private boolean matchesDateRange(ReserveDto r) {
        String dateStr = r.getReservationDate();
        if (dateStr == null || dateStr.isEmpty()) return true;
        try {
            SimpleDateFormat sdfParse  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfFilter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String day = dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr;
            Date reservDate = sdfParse.parse(day);
            if (reservDate == null) return true;

            if (!filterDateFrom.isEmpty()) {
                Date from = sdfFilter.parse(filterDateFrom);
                if (from != null && reservDate.before(from)) return false;
            }
            if (!filterDateTo.isEmpty()) {
                Date to = sdfFilter.parse(filterDateTo);
                if (to != null && reservDate.after(to)) return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private void updateFilterChips() {
        chipGroupActiveFilters.removeAllViews();
        boolean hasFilters = false;

        if (!filterCountry.isEmpty()) {
            addFilterChip("País: " + filterCountry, () -> {
                filterCountry = "";
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }
        if (!filterDateFrom.isEmpty()) {
            addFilterChip("Desde: " + filterDateFrom, () -> {
                filterDateFrom = "";
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }
        if (!filterDateTo.isEmpty()) {
            addFilterChip("Hasta: " + filterDateTo, () -> {
                filterDateTo = "";
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }

        chipGroupActiveFilters.setVisibility(hasFilters ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String label, Runnable onRemove) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onRemove.run());
        chipGroupActiveFilters.addView(chip);
    }

    // ─── Cancelar ────────────────────────────────────────────────────────────

    private void onCancelClicked(Long reserveId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("¿Estás seguro de que querés cancelar esta reserva?")
                .setPositiveButton("Sí, cancelar", (d, w) -> doCancel(reserveId))
                .setNegativeButton("Volver", null)
                .show();
    }

    private void doCancel(Long reserveId) {
        setState(true, null);
        reserveApiService.cancelReserve(reserveId).enqueue(new Callback<ReserveDto>() {
            @Override
            public void onResponse(@NonNull Call<ReserveDto> call,
                                   @NonNull Response<ReserveDto> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No se pudo cancelar", Toast.LENGTH_SHORT).show();
                }
                loadHistory();
            }

            @Override
            public void onFailure(@NonNull Call<ReserveDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red al cancelar", Toast.LENGTH_SHORT).show();
                loadHistory();
            }
        });
    }

    // ─── Caché ───────────────────────────────────────────────────────────────

    private void saveToCacheAsync(List<ReserveDto> reserves) {
        String email = tokenManager.getEmail();
        if (email == null || email.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            cachedReserveDao.deleteByEmail(email);
            List<CachedReserve> cached = new ArrayList<>();
            for (ReserveDto r : reserves) {
                cached.add(new CachedReserve(
                        r.getIdReserve(),
                        email,
                        r.getActivityTitle(),
                        r.getActivityId(),
                        r.getReservationDate(),
                        r.getNumberOfPeople(),
                        r.getTotalPrice(),
                        r.getState()
                ));
            }
            cachedReserveDao.insertAll(cached);
        });
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private void setState(boolean loading, @Nullable String message) {
        progressHistory.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (message == null || message.isEmpty()) {
            tvStateHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(loading ? View.GONE : View.VISIBLE);
        } else {
            tvStateHistory.setText(message);
            tvStateHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        }
    }
}