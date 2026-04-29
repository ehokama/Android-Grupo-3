package com.rutea.app.activitiesandviews.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;
import com.rutea.app.activitiesandviews.data.network.TravellerApiService;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    TravellerApiService travellerApiService;

    @Inject
    TokenManager tokenManager;

    private TextView tvName, tvEmail, tvPhone;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);

        fetchProfile();

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_profile_to_editProfile);
        });

        view.findViewById(R.id.btnMyActivities).setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_profile_to_history);
        });

        view.findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            tokenManager.clearSession();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.home_nav_graph, true)
                    .build();
            Navigation.findNavController(view)
                    .navigate(R.id.auth_nav_graph, null, navOptions);
        });
    }

    private void fetchProfile() {
        travellerApiService.getMyProfile().enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(Call<TravellerDto> call, Response<TravellerDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TravellerDto profile = response.body();
                    if (profile.getName() != null) tvName.setText(profile.getName());
                    if (profile.getEmail() != null) tvEmail.setText(profile.getEmail());
                    if (profile.getPhone() != null) tvPhone.setText(profile.getPhone());
                } else {
                    Toast.makeText(getContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TravellerDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
