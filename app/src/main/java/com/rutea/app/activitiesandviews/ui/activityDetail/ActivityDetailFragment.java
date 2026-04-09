package com.rutea.app.activitiesandviews.ui.activityDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;

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

        String nombre = "";

        if (getArguments() != null) {
            nombre = getArguments().getString("nombre", "Actividad");
            ((TextView) view.findViewById(R.id.tvNombre)).setText(nombre);
            ((TextView) view.findViewById(R.id.tvDescripcion)).setText(getArguments().getString("descripcion", ""));
            ((TextView) view.findViewById(R.id.tvHorario)).setText("🕐 " + getArguments().getString("horario", ""));
            ((TextView) view.findViewById(R.id.tvUbicacion)).setText("📍 " + getArguments().getString("ubicacion", ""));
            ((TextView) view.findViewById(R.id.tvCosto)).setText("💲 " + getArguments().getString("costo", ""));
            ((TextView) view.findViewById(R.id.tvGuia)).setText("🧭 Guía: " + getArguments().getString("guia", ""));
            ((TextView) view.findViewById(R.id.tvEmpresa)).setText("🏢 " + getArguments().getString("empresa", ""));
        }

        final String nombreFinal = nombre;

        Button btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("nombre", nombreFinal);
            Navigation.findNavController(view)
                    .navigate(R.id.action_detail_to_reservation, args);
        });
    }
}