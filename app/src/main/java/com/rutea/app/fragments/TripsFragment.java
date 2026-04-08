package com.rutea.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// unir luego con firstfragment de augusto, falta completar
public class TripsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("Mis viajes - próximamente");
        tv.setTextSize(18);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }
}