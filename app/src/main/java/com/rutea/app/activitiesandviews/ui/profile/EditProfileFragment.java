package com.rutea.app.activitiesandviews.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;
import com.rutea.app.activitiesandviews.data.network.TravellerApiService;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject
    TravellerApiService travellerApiService;

    private EditText etEditName, etEditPhone;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEditName = view.findViewById(R.id.etEditName);
        etEditPhone = view.findViewById(R.id.etEditPhone);

        fetchProfile();

        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile(view));
    }

    private void fetchProfile() {
        travellerApiService.getMyProfile().enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(Call<TravellerDto> call, Response<TravellerDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TravellerDto profile = response.body();
                    if (profile.getName() != null) etEditName.setText(profile.getName());
                    if (profile.getPhone() != null) etEditPhone.setText(profile.getPhone());
                }
            }

            @Override
            public void onFailure(Call<TravellerDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile(View view) {
        String newName = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();

        TravellerDto request = new TravellerDto(newName, newPhone);

        travellerApiService.updateMyProfile(request).enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(Call<TravellerDto> call, Response<TravellerDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TravellerDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
