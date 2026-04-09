package com.rutea.app.activitiesandviews.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import java.util.Arrays;
import java.util.List;
public class HomeFragment extends Fragment {

    // Modelo simple de actividad
    static class Actividad {
        String nombre, descripcion, horario, ubicacion, costo, guia, empresa;
        int imagen;

        Actividad(String nombre, String descripcion, String horario,
                  String ubicacion, String costo, String guia,
                  String empresa, int imagen) {
            this.nombre = nombre; this.descripcion = descripcion;
            this.horario = horario; this.ubicacion = ubicacion;
            this.costo = costo; this.guia = guia;
            this.empresa = empresa; this.imagen = imagen;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Saludo
        String username = getArguments() != null ? getArguments().getString("username", "") : "";
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (!username.isEmpty()) tvGreeting.setText("Hola, " + username + " 👋");

        // Datos de ejemplo — reemplazá con tu fuente real
        List<Actividad> destinos = Arrays.asList(
                new Actividad("Buenos Aires", "La capital argentina", "09:00 - 18:00",
                        "CABA", "$1500", "Carlos López", "RuteaTravel", R.drawable.bg_hero_landscape),
                new Actividad("Mendoza", "Tierra del vino", "10:00 - 17:00",
                        "Mendoza", "$2000", "Ana Martínez", "VinaTours", R.drawable.bg_hero_landscape)
        );

        List<Actividad> recomendadas = Arrays.asList(
                new Actividad("Trekking en Patagonia", "Aventura en el sur", "07:00 - 15:00",
                        "Bariloche", "$3500", "Pedro Sosa", "AventuraSur", R.drawable.bg_hero_landscape)
        );

        List<Actividad> masReservadas = Arrays.asList(
                new Actividad("Cataratas del Iguazú", "Maravilla natural", "08:00 - 16:00",
                        "Misiones", "$2800", "Laura Díaz", "NaturaTours", R.drawable.bg_hero_landscape)
        );

        // Inflar cards en cada sección
        inflateCards(view, R.id.llDestinos, destinos);
        inflateCards(view, R.id.llRecomendadas, recomendadas);
        inflateCards(view, R.id.llMasReservadas, masReservadas);
    }

    private void inflateCards(View root, int containerId, List<Actividad> actividades) {
        LinearLayout container = root.findViewById(containerId);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Actividad a : actividades) {
            View card = inflater.inflate(R.layout.item_category_card, container, false);

            // Tamaño fijo
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(160), dpToPx(120));
            params.setMarginEnd(dpToPx(12));
            card.setLayoutParams(params);

            ((TextView) card.findViewById(R.id.tvCardLabel)).setText(a.nombre);
            ((ImageView) card.findViewById(R.id.ivCard)).setImageResource(a.imagen);

            card.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("nombre",      a.nombre);
                args.putString("descripcion", a.descripcion);
                args.putString("horario",     a.horario);
                args.putString("ubicacion",   a.ubicacion);
                args.putString("costo",       a.costo);
                args.putString("guia",        a.guia);
                args.putString("empresa",     a.empresa);
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_home_to_detail, args);
            });

            container.addView(card);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}