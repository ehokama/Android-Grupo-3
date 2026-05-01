package com.rutea.app.activitiesandviews.ui.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.DisponibilityApiService;
import com.rutea.app.activitiesandviews.data.network.ReserveApiService;
import com.rutea.app.activitiesandviews.data.network.ReserveRequestFactory;
import com.rutea.app.activitiesandviews.data.models.dto.disponibility.DisponibilityDto;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class ReservationFragment extends Fragment {

    private int personas = 1;
    private TextView tvPersonas;
    private final List<DisponibilityDto> selectedDisponibilities = new ArrayList<>();
    private MaterialCardView cardPrecio;
    private TextView tvPrecioBase;
    private TextView tvDescuentoDetalle;
    private TextView tvPrecioTotal;
    private double activityPrice = -1;
    private String discountMode;
    private int discountPercent;

    @Inject
    ReserveApiService reserveApiService;

    @Inject
    DisponibilityApiService disponibilityApiService;

    @Inject
    ActivityApiService activityApiService;

    private Spinner spinnerHorario;
    private Spinner spinnerFecha;

    // Map to group disponibilities by date string (e.g. "25/12/2026")
    private Map<String, List<DisponibilityDto>> disponibilitiesByDate;
    private List<String> availableDates = new ArrayList<>();
    private List<DisponibilityDto> currentTimesForDate = new ArrayList<>();
    private Button btnConfirmar;
    private String nombreActividad = "Actividad";
    private long activityId = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            nombreActividad = getArguments().getString("nombre", "Actividad");
            activityId = getArguments().getLong("activityId", -1L);
            activityPrice = getArguments().getDouble("activityPrice", -1);
            discountMode = getArguments().getString("discountMode", null);
            discountPercent = getArguments().getInt("discountPercent", 0);
        }
        ((TextView) view.findViewById(R.id.tvNombreActividad)).setText(nombreActividad);

        cardPrecio = view.findViewById(R.id.cardPrecio);
        tvPrecioBase = view.findViewById(R.id.tvPrecioBase);
        tvDescuentoDetalle = view.findViewById(R.id.tvDescuentoDetalle);
        tvPrecioTotal = view.findViewById(R.id.tvPrecioTotal);

        if (activityPrice <= 0 && activityId > 0) {
            activityApiService.getActivityById(activityId).enqueue(new Callback<ActivityDto>() {
                @Override
                public void onResponse(@NonNull Call<ActivityDto> call,
                                       @NonNull Response<ActivityDto> response) {
                    if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                    ActivityDto dto = response.body();
                    if (dto.getTitle() != null && ("Actividad".equals(nombreActividad) || nombreActividad.isEmpty())) {
                        nombreActividad = dto.getTitle();
                        ((TextView) view.findViewById(R.id.tvNombreActividad)).setText(nombreActividad);
                    }
                    if (dto.getPrice() != null && dto.getPrice() > 0) {
                        activityPrice = dto.getPrice();
                        updatePriceSummary();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ActivityDto> call, @NonNull Throwable t) { }
            });
        }

        // --- fecha ---
        spinnerFecha = view.findViewById(R.id.spinnerFecha);
        spinnerFecha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTimeSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- horario ---
        spinnerHorario = view.findViewById(R.id.spinnerHorario);

        // --- cupo personas ---
        tvPersonas = view.findViewById(R.id.tvPersonas);
        view.findViewById(R.id.btnMenos).setOnClickListener(v -> {
            if (personas > 1) {
                tvPersonas.setText(String.valueOf(--personas));
                updatePriceSummary();
            }
        });
        view.findViewById(R.id.btnMas).setOnClickListener(v -> {
            if (personas < 20) {
                tvPersonas.setText(String.valueOf(++personas));
                updatePriceSummary();
            }
        });

        updatePriceSummary();

        // --- confirmacion ---
        btnConfirmar = view.findViewById(R.id.btnConfirmar);
        loadDisponibilities();
        btnConfirmar.setOnClickListener(v -> {
            if (currentTimesForDate.isEmpty()) {
                Toast.makeText(requireContext(), "No hay horarios disponibles para esta fecha.", Toast.LENGTH_SHORT).show();
                return;
            }
            int position = spinnerHorario.getSelectedItemPosition();
            if (position < 0 || position >= currentTimesForDate.size()) {
                Toast.makeText(requireContext(), "Seleccioná un horario válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            DisponibilityDto selected = currentTimesForDate.get(position);
            Integer quota = selected.getDisponibleQuota();
            if (quota != null && personas > quota) {
                Toast.makeText(requireContext(), "No hay cupos suficientes para esa disponibilidad.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirmar.setEnabled(false);
            reserveApiService.createReserve(ReserveRequestFactory.create(selected.getIdDisponibility(), personas))
                    .enqueue(new Callback<ReserveDto>() {
                        @Override
                        public void onResponse(@NonNull Call<ReserveDto> call, @NonNull Response<ReserveDto> response) {
                            btnConfirmar.setEnabled(true);
                            if (!isAdded()) {
                                return;
                            }
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(requireContext(), parseError(response), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ReserveDto reserve = response.body();
                            String[] dateTime = splitDateTime(selected.getHour());

                            Bundle args = new Bundle();
                            args.putString("nombre", nombreActividad);
                            args.putString("fecha", dateTime[0]);
                            args.putString("horario", dateTime[1]);
                            args.putString("guia", "Guía asignado");
                            args.putString("idioma", "Español");
                            args.putInt("personas", reserve.getNumberOfPeople() == null ? personas : reserve.getNumberOfPeople());
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_reservation_to_confirmation, args);
                        }

                        @Override
                        public void onFailure(@NonNull Call<ReserveDto> call, @NonNull Throwable throwable) {
                            btnConfirmar.setEnabled(true);
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), "Error de red al crear la reserva.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadDisponibilities() {
        if (activityId <= 0) {
            Toast.makeText(requireContext(), "No se encontró la actividad seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        disponibilityApiService.getDisponibilities().enqueue(new Callback<List<DisponibilityDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<DisponibilityDto>> call, @NonNull Response<List<DisponibilityDto>> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "No se pudieron cargar disponibilidades.", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedDisponibilities.clear();
                selectedDisponibilities.addAll(response.body().stream()
                        .filter(item -> item.getActivityId() != null && item.getActivityId() == activityId)
                        .filter(item -> item.getDisponibleQuota() != null && item.getDisponibleQuota() > 0)
                        .sorted(Comparator.comparing(DisponibilityDto::getHour, Comparator.nullsLast(String::compareTo)))
                        .collect(Collectors.toList()));

                if (selectedDisponibilities.isEmpty()) {
                    btnConfirmar.setEnabled(false);
                    Toast.makeText(requireContext(), "Esta actividad no tiene horarios con cupo disponible.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Group by date
                disponibilitiesByDate = selectedDisponibilities.stream()
                        .collect(Collectors.groupingBy(item -> splitDateTime(item.getHour())[0]));

                // Extract unique dates preserving the sorted order
                availableDates = selectedDisponibilities.stream()
                        .map(item -> splitDateTime(item.getHour())[0])
                        .distinct()
                        .collect(Collectors.toList());

                // Populate date spinner
                spinnerFecha.setAdapter(simpleAdapter(availableDates));

                // Trigger an initial update for the times spinner
                if (!availableDates.isEmpty()) {
                    updateTimeSpinner(0);
                    btnConfirmar.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DisponibilityDto>> call, @NonNull Throwable throwable) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Error de red al cargar disponibilidades.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTimeSpinner(int datePosition) {
        if (datePosition < 0 || datePosition >= availableDates.size()) return;

        String selectedDate = availableDates.get(datePosition);
        currentTimesForDate = disponibilitiesByDate.getOrDefault(selectedDate, new ArrayList<>());

        List<String> labels = currentTimesForDate.stream()
                .map(item -> {
                    String[] dateTime = splitDateTime(item.getHour());
                    return dateTime[1] + " (" + item.getDisponibleQuota() + " cupos)";
                })
                .collect(Collectors.toList());

        spinnerHorario.setAdapter(simpleAdapter(labels));
    }

    private String[] splitDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isEmpty()) {
            return new String[]{"Fecha no disponible", "Horario no disponible"};
        }
        try {
            LocalDateTime parsed = LocalDateTime.parse(rawDateTime);
            String date = parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()));
            String hour = parsed.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
            return new String[]{date, hour};
        } catch (Exception ignored) {
            if (rawDateTime.contains("T")) {
                String[] parts = rawDateTime.split("T");
                String hour = parts.length > 1 ? parts[1].substring(0, Math.min(5, parts[1].length())) : "Horario no disponible";
                return new String[]{parts[0], hour};
            }
            return new String[]{rawDateTime, "Horario no disponible"};
        }
    }

    private String parseError(Response<?> response) {
        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody == null) {
                return "No se pudo completar la reserva.";
            }
            String raw = errorBody.string();
            JSONObject json = new JSONObject(raw);
            if (json.has("error")) {
                return json.getString("error");
            }
            return "Error " + response.code() + " al crear reserva.";
        } catch (IOException ioe) {
            return "Error " + response.code() + " al crear reserva.";
        } catch (Exception ex) {
            return "No se pudo completar la reserva.";
        }
    }

    private ArrayAdapter<String> simpleAdapter(List<String> items) {
        return new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
    }

    private void updatePriceSummary() {
        if (activityPrice <= 0) {
            cardPrecio.setVisibility(View.GONE);
            return;
        }

        cardPrecio.setVisibility(View.VISIBLE);
        double baseTotal = activityPrice * personas;
        double discountAmount = 0.0;
        String detail = null;

        if (discountPercent > 0 && discountMode != null) {
            if ("SECOND_PERSON".equalsIgnoreCase(discountMode) && personas >= 2) {
                discountAmount = activityPrice * (discountPercent / 100.0);
                detail = "2da persona: -" + formatCurrency(discountAmount)
                        + " (" + discountPercent + "% desc.)";
            } else if ("ALL".equalsIgnoreCase(discountMode)) {
                discountAmount = baseTotal * (discountPercent / 100.0);
                detail = "Descuento " + discountPercent + "%: -" + formatCurrency(discountAmount);
            }
        }

        double total = Math.max(0.0, baseTotal - discountAmount);

        tvPrecioBase.setText("Precio por persona: " + formatCurrency(activityPrice));
        tvPrecioTotal.setText("Total: " + formatCurrency(total));

        if (detail != null) {
            tvDescuentoDetalle.setText(detail);
            tvDescuentoDetalle.setVisibility(View.VISIBLE);
        } else {
            tvDescuentoDetalle.setVisibility(View.GONE);
        }
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "$%.2f", value);
    }
}