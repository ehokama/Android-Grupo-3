package com.rutea.app.activitiesandviews.ui.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.ui.data.network.DisponibilityApiService;
import com.rutea.app.activitiesandviews.ui.data.network.ReserveApiService;
import com.rutea.app.activitiesandviews.ui.data.network.ReserveRequestFactory;
import com.rutea.app.activitiesandviews.ui.data.network.RetrofitClient;
import com.rutea.app.activitiesandviews.ui.data.network.dto.disponibility.DisponibilityDto;
import com.rutea.app.activitiesandviews.ui.data.network.dto.reserve.ReserveDto;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationFragment extends Fragment {

    private int personas = 1;
    private TextView tvPersonas;
    private final List<DisponibilityDto> selectedDisponibilities = new ArrayList<>();
    private ReserveApiService reserveApiService;
    private DisponibilityApiService disponibilityApiService;
    private Spinner spinnerHorario;
    private Spinner spinnerDia;
    private Spinner spinnerMes;
    private Spinner spinnerAnio;
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
        }
        ((TextView) view.findViewById(R.id.tvNombreActividad)).setText(nombreActividad);

        RetrofitClient.init(requireContext());
        reserveApiService = RetrofitClient.createService(ReserveApiService.class);
        disponibilityApiService = RetrofitClient.createService(DisponibilityApiService.class);

        // --- dia ---
        spinnerDia = view.findViewById(R.id.spinnerDia);
        List<String> dias = new ArrayList<>();
        for (int i = 1; i <= 31; i++) dias.add(String.format("%02d", i));
        spinnerDia.setAdapter(simpleAdapter(dias));

        // --- mes ---
        spinnerMes = view.findViewById(R.id.spinnerMes);
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        spinnerMes.setAdapter(simpleAdapter(java.util.Arrays.asList(meses)));

        // --- año ---
        spinnerAnio = view.findViewById(R.id.spinnerAnio);
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        spinnerAnio.setAdapter(simpleAdapter(
                java.util.Arrays.asList(String.valueOf(anioActual), String.valueOf(anioActual + 1))));

        // --- horario ---
        spinnerHorario = view.findViewById(R.id.spinnerHorario);
        List<String> horarios = new ArrayList<>();
        for (int h = 8; h <= 20; h++) horarios.add(String.format("%02d:00", h));
        spinnerHorario.setAdapter(simpleAdapter(horarios));

        // --- cupo personas ---
        tvPersonas = view.findViewById(R.id.tvPersonas);
        view.findViewById(R.id.btnMenos).setOnClickListener(v -> {
            if (personas > 1) tvPersonas.setText(String.valueOf(--personas));
        });
        view.findViewById(R.id.btnMas).setOnClickListener(v -> {
            if (personas < 20) tvPersonas.setText(String.valueOf(++personas));
        });

        // --- confirmacion ---
        btnConfirmar = view.findViewById(R.id.btnConfirmar);
        loadDisponibilities();
        btnConfirmar.setOnClickListener(v -> {
            if (selectedDisponibilities.isEmpty()) {
                Toast.makeText(requireContext(), "No hay horarios disponibles para esta actividad.", Toast.LENGTH_SHORT).show();
                return;
            }
            int position = spinnerHorario.getSelectedItemPosition();
            if (position < 0 || position >= selectedDisponibilities.size()) {
                Toast.makeText(requireContext(), "Seleccioná un horario válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            DisponibilityDto selected = selectedDisponibilities.get(position);
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

                List<String> labels = selectedDisponibilities.stream()
                        .map(item -> {
                            String[] dateTime = splitDateTime(item.getHour());
                            return dateTime[0] + " - " + dateTime[1] + " (" + item.getDisponibleQuota() + " cupos)";
                        })
                        .collect(Collectors.toList());
                spinnerHorario.setAdapter(simpleAdapter(labels));
                btnConfirmar.setEnabled(true);
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
}