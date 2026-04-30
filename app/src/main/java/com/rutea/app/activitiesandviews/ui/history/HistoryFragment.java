package com.rutea.app.activitiesandviews.ui.history;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedReserve;
import com.rutea.app.activitiesandviews.data.local.db.CachedReserveDao;
import com.rutea.app.activitiesandviews.data.models.dto.review.CreateReviewRequest;
import com.rutea.app.activitiesandviews.data.models.dto.review.ReviewDto;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;
import com.rutea.app.activitiesandviews.data.network.ReserveApiService;
import com.rutea.app.activitiesandviews.data.network.ReviewApiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HistoryFragment extends Fragment {
    private boolean onlyCompleted = false;

    private RecyclerView rvHistory;
    private ProgressBar progressHistory;
    private TextView tvStateHistory;
    private ChipGroup chipGroupActiveFilters;
    private MaterialButton btnFilter;
    private HistoryAdapter adapter;

    @Inject ReserveApiService reserveApiService;
    @Inject ReviewApiService reviewApiService;
    @Inject TokenManager tokenManager;
    @Inject CachedReserveDao cachedReserveDao;
    private final Map<Long, ReviewDto> myReviewsByActivity = new HashMap<>();

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
        if (getArguments() != null) {
            onlyCompleted = getArguments().getBoolean("onlyCompleted", false);
        }
        TextView tvTitle = view.findViewById(R.id.tvTitle);

        if (onlyCompleted) {
            tvTitle.setText("Historial de Actividades (Finalizadas)");
        } else {
            tvTitle.setText("Mis Actividades (Todas)");
        }
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

                loadMyReviewsThenShow(reserves);
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

    private void loadMyReviewsThenShow(List<ReserveDto> reserves) {
        reviewApiService.getMyReviews().enqueue(new Callback<List<ReviewDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReviewDto>> call, @NonNull Response<List<ReviewDto>> response) {
                myReviewsByActivity.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (ReviewDto review : response.body()) {
                        if (review.getActivityId() != null && !myReviewsByActivity.containsKey(review.getActivityId())) {
                            myReviewsByActivity.put(review.getActivityId(), review);
                        }
                    }
                }
                setAllReservesAndShow(reserves);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReviewDto>> call, @NonNull Throwable t) {
                myReviewsByActivity.clear();
                setAllReservesAndShow(reserves);
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
        applyRatingState(reserves);
        adapter = new HistoryAdapter(reserves, this::onCancelClicked, this::onRateClicked, this::openDetail);
        rvHistory.setAdapter(adapter);
        tvStateHistory.setVisibility(View.GONE);
        rvHistory.setVisibility(View.VISIBLE);
    }

    private void openDetail(ReserveDto reserve) {
        Bundle args = ReserveDetailFragment.buildArgs(reserve);
        NavController nav = Navigation.findNavController(requireView());
        nav.navigate(R.id.action_historyFragment_to_reserveDetailFragment, args);
    }
    private void applyRatingState(List<ReserveDto> reserves) {
        LocalDateTime now = LocalDateTime.now();
        for (ReserveDto reserve : reserves) {
            ReviewDto review = reserve.getActivityId() != null ? myReviewsByActivity.get(reserve.getActivityId()) : null;
            boolean alreadyRated = review != null;
            boolean completed = "COMPLETED".equalsIgnoreCase(reserve.getState());
            boolean withinWindow = isWithinRatingWindow(reserve.getReservationDate(), now);

            reserve.setAlreadyRated(alreadyRated);
            reserve.setCanRate(completed && withinWindow && !alreadyRated);

            if (alreadyRated) {
                reserve.setMyActivityRating(review.getRating());
                reserve.setMyGuideRating(review.getGuideRating());
                reserve.setMyComment(review.getComment());
            } else {
                reserve.setMyActivityRating(null);
                reserve.setMyGuideRating(null);
                reserve.setMyComment(null);
            }
        }
    }

    private boolean isWithinRatingWindow(String reservationDate, LocalDateTime now) {
        LocalDateTime activityDate = parseDate(reservationDate);
        return activityDate != null && now.isBefore(activityDate.plusHours(48));
    }

    private LocalDateTime parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        List<DateTimeFormatter> formats = new ArrayList<>();
        formats.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        formats.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()));
        formats.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDateTime.parse(raw.trim(), formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
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
            if (onlyCompleted && !"COMPLETED".equalsIgnoreCase(r.getState())) continue;

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

    private void onRateClicked(ReserveDto reserve) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_activity, null);
        RatingBar rbActivity = dialogView.findViewById(R.id.rbActivityRating);
        RatingBar rbGuide = dialogView.findViewById(R.id.rbGuideRating);
        TextView tvActivityRatingValue = dialogView.findViewById(R.id.tvActivityRatingValue);
        TextView tvGuideRatingValue = dialogView.findViewById(R.id.tvGuideRatingValue);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);
        etComment.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        tvActivityRatingValue.setText(Math.round(rbActivity.getRating()) + "/5");
        tvGuideRatingValue.setText(Math.round(rbGuide.getRating()) + "/5");

        rbActivity.setOnRatingBarChangeListener((ratingBar, rating, fromUser) ->
                tvActivityRatingValue.setText(Math.round(rating) + "/5"));
        rbGuide.setOnRatingBarChangeListener((ratingBar, rating, fromUser) ->
                tvGuideRatingValue.setText(Math.round(rating) + "/5"));

        new AlertDialog.Builder(requireContext())
                .setTitle("Calificar experiencia")
                .setView(dialogView)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    int activityRating = Math.round(rbActivity.getRating());
                    int guideRating = Math.round(rbGuide.getRating());
                    String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

                    if (activityRating < 1 || guideRating < 1) {
                        Toast.makeText(requireContext(), "Tenés que elegir ambas calificaciones (1 a 5).", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitReview(reserve, activityRating, guideRating, comment);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void submitReview(ReserveDto reserve, int activityRating, int guideRating, String comment) {
        if (reserve.getActivityId() == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la actividad.", Toast.LENGTH_SHORT).show();
            return;
        }

        setState(true, null);
        CreateReviewRequest request = new CreateReviewRequest(activityRating, guideRating, comment, reserve.getActivityId());
        reviewApiService.createReview(request).enqueue(new Callback<ReviewDto>() {
            @Override
            public void onResponse(@NonNull Call<ReviewDto> call, @NonNull Response<ReviewDto> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Gracias por tu calificación.", Toast.LENGTH_SHORT).show();
                    loadHistory();
                } else {
                    setState(false, null);
                    Toast.makeText(requireContext(), "No se pudo enviar la calificación.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setState(false, null);
                Toast.makeText(requireContext(), "Error de red al enviar la calificación.", Toast.LENGTH_SHORT).show();
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