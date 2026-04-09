package com.rutea.app.activitiesandviews.ui.auth;

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

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnRegistrar = view.findViewById(R.id.btnRegistrar);
        TextView tvYaTengoLogin = view.findViewById(R.id.tvYaTengoLogin);

        btnRegistrar.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_register_to_login);
        });

        tvYaTengoLogin.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_register_to_login));
    }
}