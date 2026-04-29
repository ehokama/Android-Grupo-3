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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.TravellerApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject
    TravellerApiService travellerApiService;

    @Inject
    ActivityApiService activityApiService;

    private EditText etEditName, etEditPhone;
    private ChipGroup cgEditPreferencias;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEditName = view.findViewById(R.id.etEditName);
        etEditPhone = view.findViewById(R.id.etEditPhone);
        cgEditPreferencias = view.findViewById(R.id.cgEditPreferencias);

        loadCategoriesAndProfile();

        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile(view));
    }

    private void loadCategoriesAndProfile() {
        activityApiService.getCategories().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    for (String category : response.body()) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(category);
                        chip.setCheckable(true);
                        cgEditPreferencias.addView(chip);
                    }
                    // Fetch profile after creating chips so we can check them
                    fetchProfile();
                } else {
                    fetchProfile(); // Fallback if categories fail
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                fetchProfile(); // Fallback if categories fail
            }
        });
    }

    private void fetchProfile() {
        travellerApiService.getMyProfile().enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(@NonNull Call<TravellerDto> call, @NonNull Response<TravellerDto> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    TravellerDto profile = response.body();
                    if (profile.getName() != null) etEditName.setText(profile.getName());
                    if (profile.getPhone() != null) etEditPhone.setText(profile.getPhone());

                    if (profile.getPreferences() != null) {
                        for (int i = 0; i < cgEditPreferencias.getChildCount(); i++) {
                            Chip chip = (Chip) cgEditPreferencias.getChildAt(i);
                            if (profile.getPreferences().contains(chip.getText().toString())) {
                                chip.setChecked(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TravellerDto> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveProfile(View view) {
        String newName = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();

        List<String> preferences = new ArrayList<>();
        for (int i = 0; i < cgEditPreferencias.getChildCount(); i++) {
            Chip chip = (Chip) cgEditPreferencias.getChildAt(i);
            if (chip.isChecked()) {
                preferences.add(chip.getText().toString());
            }
        }

        TravellerDto request = new TravellerDto(newName, newPhone, preferences);

        travellerApiService.updateMyProfile(request).enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(@NonNull Call<TravellerDto> call, @NonNull Response<TravellerDto> response) {
                if (response.isSuccessful() && isAdded()) {
                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigateUp();
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TravellerDto> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al guardar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
