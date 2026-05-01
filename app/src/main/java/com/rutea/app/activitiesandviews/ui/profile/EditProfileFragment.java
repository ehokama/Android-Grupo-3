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
import com.rutea.app.activitiesandviews.data.models.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.data.models.dto.auth.ChangeEmailRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.ChangePasswordRequest;
import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.AuthApiService;
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
    @Inject AuthApiService authApiService;
    @Inject
    TokenManager tokenManager;

    // Inyectá tu DB o un repo que te dé acceso al DAO
    @Inject
    AppDatabase appDatabase;

    private EditText etEditName, etEditPhone, etEditEmail;
    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private ChipGroup cgEditPreferencias;
    private String originalEmail; // email antes de editar

    public EditProfileFragment() { super(R.layout.fragment_edit_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEditName  = view.findViewById(R.id.etEditName);
        etEditEmail = view.findViewById(R.id.etEditEmail);
        etEditPhone = view.findViewById(R.id.etEditPhone);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmNewPassword = view.findViewById(R.id.etConfirmNewPassword);
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
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmNewPassword.getText().toString();

        List<String> preferences = new ArrayList<>();
        for (int i = 0; i < cgEditPreferencias.getChildCount(); i++) {
            Chip chip = (Chip) cgEditPreferencias.getChildAt(i);
            if (chip.isChecked()) preferences.add(chip.getText().toString());
        }

        boolean emailChanged = originalEmail != null && !newEmail.equalsIgnoreCase(originalEmail);
        boolean wantsPasswordChange = !newPassword.isBlank() || !confirmPassword.isBlank();

        if (emailChanged || wantsPasswordChange) {
            if (currentPassword.isBlank()) {
                Toast.makeText(requireContext(), "Para cambiar mail o contraseña ingresá tu contraseña actual.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (wantsPasswordChange) {
            if (newPassword.length() < 6) {
                Toast.makeText(requireContext(), "La nueva contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "La confirmación de contraseña no coincide.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        TravellerDto request = new TravellerDto(newName, newEmail, newPhone, preferences);

        travellerApiService.updateMyProfile(request).enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(@NonNull Call<TravellerDto> call, @NonNull Response<TravellerDto> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (emailChanged) {
                    runEmailChangeFlow(view, currentPassword, newEmail, newName, newPhone, preferences, wantsPasswordChange ? newPassword : null);
                } else if (wantsPasswordChange) {
                    migrateCachedProfile(originalEmail, newEmail, newName, newPhone, preferences);
                    runPasswordChangeFlow(view, currentPassword, newPassword, "Perfil y contraseña actualizados.");
                } else {
                    migrateCachedProfile(originalEmail, newEmail, newName, newPhone, preferences);
                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigateUp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TravellerDto> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Error de red al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runEmailChangeFlow(
            View view,
            String currentPassword,
            String newEmail,
            String newName,
            String newPhone,
            List<String> preferences,
            String newPasswordIfAny
    ) {
        authApiService.changeEmail(new ChangeEmailRequest(currentPassword, newEmail))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(requireContext(), "No se pudo cambiar el mail. Verificá tu contraseña.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        AuthResponse auth = response.body();
                        String token = auth.getToken() != null ? auth.getToken() : tokenManager.getToken();
                        String name = auth.getName() != null ? auth.getName() : newName;
                        String oldEmail = originalEmail;
                        tokenManager.saveSession(token, newEmail, name);
                        originalEmail = newEmail;
                        migrateCachedProfile(oldEmail, newEmail, newName, newPhone, preferences);

                        if (newPasswordIfAny != null) {
                            runPasswordChangeFlow(view, currentPassword, newPasswordIfAny, "Perfil, mail y contraseña actualizados.");
                        } else {
                            Toast.makeText(requireContext(), "Perfil y mail actualizados.", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigateUp();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error de red al cambiar mail.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void runPasswordChangeFlow(View view, String currentPassword, String newPassword, String successMessage) {
        authApiService.changePassword(new ChangePasswordRequest(currentPassword, newPassword))
                .enqueue(new Callback<java.util.Map<String, String>>() {
                    @Override
                    public void onResponse(@NonNull Call<java.util.Map<String, String>> call, @NonNull Response<java.util.Map<String, String>> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful()) {
                            Toast.makeText(requireContext(), "No se pudo cambiar la contraseña. Verificá tu contraseña actual.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigateUp();
                    }

                    @Override
                    public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error de red al cambiar contraseña.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void migrateCachedProfile(String oldEmail, String newEmail, String name, String phone, List<String> preferences) {
        new Thread(() -> {
            CachedProfileDao dao = appDatabase.cachedProfileDao();
            if (oldEmail != null && !oldEmail.equalsIgnoreCase(newEmail)) {
                dao.deleteByEmail(oldEmail);
            }
            dao.insertProfile(new CachedProfile(
                    newEmail,
                    name,
                    phone,
                    String.join(",", preferences)
            ));
        }).start();
    }
}