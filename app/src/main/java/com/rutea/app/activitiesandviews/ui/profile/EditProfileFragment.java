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
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.AppDatabase;
import com.rutea.app.activitiesandviews.data.local.db.CachedProfile;
import com.rutea.app.activitiesandviews.data.local.db.CachedProfileDao;
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

    @Inject TravellerApiService travellerApiService;
    @Inject ActivityApiService activityApiService;
    @Inject
    TokenManager tokenManager;

    // Inyectá tu DB o un repo que te dé acceso al DAO
    @Inject
    AppDatabase appDatabase;

    private EditText etEditName, etEditPhone, etEditEmail;
    private ChipGroup cgEditPreferencias;
    private String originalEmail; // email antes de editar

    public EditProfileFragment() { super(R.layout.fragment_edit_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEditName  = view.findViewById(R.id.etEditName);
        etEditPhone = view.findViewById(R.id.etEditPhone);
        cgEditPreferencias = view.findViewById(R.id.cgEditPreferencias);

        loadCategoriesAndProfile();

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile(view));
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
                }
                fetchProfile();
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                fetchProfile();
            }
        });
    }

    private void fetchProfile() {
        travellerApiService.getMyProfile().enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(@NonNull Call<TravellerDto> call, @NonNull Response<TravellerDto> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    TravellerDto profile = response.body();
                    if (profile.getName()  != null) etEditName.setText(profile.getName());
                    if (profile.getPhone() != null) etEditPhone.setText(profile.getPhone());
                    if (profile.getEmail() != null) {
                        etEditEmail.setText(profile.getEmail());
                        originalEmail = profile.getEmail(); // guardamos referencia
                    }
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
                if (isAdded()) Toast.makeText(getContext(), "Error de red al cargar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile(View view) {
        String newName  = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();

        List<String> preferences = new ArrayList<>();
        for (int i = 0; i < cgEditPreferencias.getChildCount(); i++) {
            Chip chip = (Chip) cgEditPreferencias.getChildAt(i);
            if (chip.isChecked()) preferences.add(chip.getText().toString());
        }

        TravellerDto request = new TravellerDto(newName, newEmail, newPhone, preferences);

        travellerApiService.updateMyProfile(request).enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(@NonNull Call<TravellerDto> call, @NonNull Response<TravellerDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    boolean emailChanged = !newEmail.equals(originalEmail);

                    if (emailChanged) {
                        // 1. Actualizar email en TokenManager
                        String currentToken = tokenManager.getToken();
                        tokenManager.saveSession(currentToken, newEmail, newName);

                        // 2. Migrar CachedProfile: borrar viejo (PK viejo) e insertar con nuevo email
                        new Thread(() -> {
                            CachedProfileDao dao = appDatabase.cachedProfileDao();
                            //                            // Borramos el registro anterior por email viejo
                            //                            // Room no tiene delete by field sin @Query, así que reemplazamos
                            //                            // Si tenés @Delete o @Query("DELETE...") usalo; si no, REPLACE alcanza
                            //                            // porque el nuevo email es distinto y el viejo queda huérfano.
                            //                            // Lo más limpio: agregar este método al DAO (ver abajo).
                            dao.deleteByEmail(originalEmail);
                            dao.insertProfile(new CachedProfile(
                                    newEmail, newName, newPhone,
                                    String.join(",", preferences)
                            ));
                        }).start();

                        originalEmail = newEmail;
                    }

                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TravellerDto> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Error de red al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}