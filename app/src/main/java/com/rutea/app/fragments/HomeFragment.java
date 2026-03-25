package com.rutea.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import com.rutea.app.activities.SearchActivity;

public class HomeFragment extends Fragment {

    private RecyclerView rvToursDestacados;
    private RecyclerView rvCategorias;
    private RecyclerView rvDestinos;

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

        // searchbar — abre SearchActivity (sin navbar)
        view.findViewById(R.id.cvSearch).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SearchActivity.class))
        );

        // conectar con adapters
        rvToursDestacados = view.findViewById(R.id.rvToursDestacados);
        rvCategorias      = view.findViewById(R.id.rvCategorias);
        rvDestinos        = view.findViewById(R.id.rvDestinos);

        // TODO: llamar a la API y setear los adapters
        // TourAdapter tourAdapter = new TourAdapter(tour -> {
        //     Intent intent = new Intent(getActivity(), ActivityDetailActivity.class);
        //     intent.putExtra("actividad_id", tour.getId());
        //     startActivity(intent);
        // });
        // rvToursDestacados.setAdapter(tourAdapter);
    }
}