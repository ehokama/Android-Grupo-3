package com.rutea.app.activitiesandviews.ui.myActivities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;

import java.util.Arrays;
import java.util.List;

public class MyActivitiesFragment extends Fragment {

    public MyActivitiesFragment() {
        super(R.layout.fragment_my_activities);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvActivities);

        List<Actividad> lista = Arrays.asList(
                new Actividad("Cataratas", "Misiones", "Excursión", "4 hs", "$5000", "15", R.drawable.bg_hero_landscape),
                new Actividad("Mendoza Wine Tour", "Mendoza", "Tour", "6 hs", "$8000", "10", R.drawable.bg_hero_landscape)
        );

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new ActividadAdapter(lista));
    }
}