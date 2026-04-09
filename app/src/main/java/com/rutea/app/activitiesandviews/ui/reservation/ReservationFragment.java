package com.rutea.app.activitiesandviews.ui.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReservationFragment extends Fragment {

    private int personas = 1;
    private TextView tvPersonas;

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

        // Nombre de actividad recibido
        String nombreActividad = getArguments() != null
                ? getArguments().getString("nombre", "Actividad") : "Actividad";
        ((TextView) view.findViewById(R.id.tvNombreActividad)).setText(nombreActividad);

        // --- Spinner Día ---
        Spinner spinnerDia = view.findViewById(R.id.spinnerDia);
        List<String> dias = new ArrayList<>();
        for (int i = 1; i <= 31; i++) dias.add(String.format("%02d", i));
        spinnerDia.setAdapter(simpleAdapter(dias));

        // --- Spinner Mes ---
        Spinner spinnerMes = view.findViewById(R.id.spinnerMes);
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        spinnerMes.setAdapter(simpleAdapter(java.util.Arrays.asList(meses)));

        // --- Spinner Año (actual y siguiente) ---
        Spinner spinnerAnio = view.findViewById(R.id.spinnerAnio);
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        spinnerAnio.setAdapter(simpleAdapter(
                java.util.Arrays.asList(String.valueOf(anioActual), String.valueOf(anioActual + 1))));

        // --- Spinner Horario ---
        Spinner spinnerHorario = view.findViewById(R.id.spinnerHorario);
        List<String> horarios = new ArrayList<>();
        for (int h = 8; h <= 20; h++) horarios.add(String.format("%02d:00", h));
        spinnerHorario.setAdapter(simpleAdapter(horarios));

        // --- Contador personas ---
        tvPersonas = view.findViewById(R.id.tvPersonas);
        view.findViewById(R.id.btnMenos).setOnClickListener(v -> {
            if (personas > 1) tvPersonas.setText(String.valueOf(--personas));
        });
        view.findViewById(R.id.btnMas).setOnClickListener(v -> {
            if (personas < 20) tvPersonas.setText(String.valueOf(++personas));
        });

        // --- Confirmar ---
        view.findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            String dia     = (String) spinnerDia.getSelectedItem();
            String mes     = (String) spinnerMes.getSelectedItem();
            String anio    = (String) spinnerAnio.getSelectedItem();
            String horario = (String) spinnerHorario.getSelectedItem();

            Bundle args = new Bundle();
            args.putString("nombre",    nombreActividad);
            args.putString("fecha",     dia + " de " + mes + " de " + anio);
            args.putString("horario",   horario);
            args.putInt("personas",     personas);

            Navigation.findNavController(view)
                    .navigate(R.id.action_reservation_to_confirmation, args);
        });
    }

    private ArrayAdapter<String> simpleAdapter(List<String> items) {
        return new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
    }
}