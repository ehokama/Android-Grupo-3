package com.rutea.app.activitiesandviews.ui.confirmation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;

public class ConfirmationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String nombre   = getArguments().getString("nombre", "");
            String fecha    = getArguments().getString("fecha", "");
            String horario  = getArguments().getString("horario", "");
            String guia     = getArguments().getString("guia", "");
            String idioma   = getArguments().getString("idioma", "");
            int personas    = getArguments().getInt("personas", 1);

            ((TextView) view.findViewById(R.id.tvResNombre)).setText(nombre);
            ((TextView) view.findViewById(R.id.tvResFecha)).setText("📅 " + fecha);
            ((TextView) view.findViewById(R.id.tvResHorario)).setText("🕐 " + horario);
            ((TextView) view.findViewById(R.id.tvResGuia)).setText("🧭 " + guia);
            ((TextView) view.findViewById(R.id.tvResIdioma)).setText("🌐 " + idioma);
            ((TextView) view.findViewById(R.id.tvResPersonas)).setText("👥 " + personas + " persona(s)");
        }

        // Botón confirmar pago — por ahora simula éxito y vuelve al home
        view.findViewById(R.id.btnPagar).setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_confirmation_to_home)
        );
    }
}