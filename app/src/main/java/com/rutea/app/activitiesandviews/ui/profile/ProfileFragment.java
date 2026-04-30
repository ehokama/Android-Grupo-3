package com.rutea.app.activitiesandviews.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.local.db.CachedProfile;
import com.rutea.app.activitiesandviews.data.local.db.CachedProfileDao;
import com.rutea.app.activitiesandviews.data.models.dto.traveller.TravellerDto;
import com.rutea.app.activitiesandviews.data.network.TravellerApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

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

    @Inject
    CachedProfileDao cachedProfileDao;

    private TextView tvName, tvEmail, tvPhone;
    private ImageView ivProfile;

    // Launcher para seleccionar imagen de la galería
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveProfilePhoto(imageUri);
                    }
                }
            });

    // Launcher para pedir permiso
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openGallery();
                } else {
                    Toast.makeText(getContext(), "Se necesita permiso para acceder a la galería", Toast.LENGTH_SHORT).show();
                }
            });

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        ivProfile = view.findViewById(R.id.ivProfile);

        // Cargar foto guardada
        loadProfilePhoto();

        // Cache-first: cargar datos locales primero, luego buscar en red
        loadCachedProfile();
        fetchProfile();

        // Tocar la foto para cambiarla
        ivProfile.setOnClickListener(v -> checkPermissionAndOpenGallery());

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_profile_to_editProfile);
        });

        view.findViewById(R.id.btnMyActivities).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("onlyCompleted", true);

            Navigation.findNavController(view)
                    .navigate(R.id.action_profile_to_history, args);
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

    // ─── Cache-first: leer Room ───

    private void loadCachedProfile() {
        String email = tokenManager.getEmail();
        if (email == null || email.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            CachedProfile cached = cachedProfileDao.getByEmail(email);
            if (cached != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (cached.name != null) tvName.setText(cached.name);
                    tvEmail.setText(cached.email);
                    if (cached.phone != null) tvPhone.setText(cached.phone);
                });
            }
        });
    }

    // ─── Fetch del backend y actualizar caché ───

    private void fetchProfile() {
        travellerApiService.getMyProfile().enqueue(new Callback<TravellerDto>() {
            @Override
            public void onResponse(Call<TravellerDto> call, Response<TravellerDto> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    TravellerDto profile = response.body();
                    if (profile.getName() != null) tvName.setText(profile.getName());
                    if (profile.getEmail() != null) tvEmail.setText(profile.getEmail());
                    if (profile.getPhone() != null) tvPhone.setText(profile.getPhone());

                    // Guardar en Room para uso offline
                    saveToCache(profile);
                }
            }

            @Override
            public void onFailure(Call<TravellerDto> call, Throwable t) {
                // No mostrar error si ya hay datos cacheados
            }
        });
    }

    private void saveToCache(TravellerDto profile) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String prefs = "";
            List<String> preferences = profile.getPreferences();
            if (preferences != null) {
                prefs = String.join(",", preferences);
            }
            CachedProfile cached = new CachedProfile(
                    profile.getEmail(),
                    profile.getName(),
                    profile.getPhone(),
                    prefs
            );
            cachedProfileDao.insertProfile(cached);
        });
    }

    // ─── Foto de Perfil ───

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void saveProfilePhoto(Uri imageUri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (in != null) in.close();

            String email = tokenManager.getEmail();
            String fileName = "profile_photo_" + email.replace("@", "_").replace(".", "_") + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            loadProfilePhoto();
            Toast.makeText(getContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al guardar la foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfilePhoto() {
        String email = tokenManager.getEmail();
        if (email == null || email.isEmpty()) return;

        String fileName = "profile_photo_" + email.replace("@", "_").replace(".", "_") + ".jpg";
        File file = new File(requireContext().getFilesDir(), fileName);

        if (file.exists()) {
            Glide.with(this)
                    .load(file)
                    .circleCrop()
                    .into(ivProfile);
        }
    }
}