package com.rutea.app.activitiesandviews.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREF_NAME = "rutea_secure_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    private static final String ENCRYPTED_PREF_NAME = "rutea_encrypted_session";
    private static final String KEY_ENCRYPTED_TOKEN = "encrypted_token";

    private static final String TAG = "TokenManager";

    private final Context context;
    private final SharedPreferences preferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.context = context;
        SharedPreferences encryptedPrefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al crear EncryptedSharedPreferences, usando fallback", e);
            encryptedPrefs = context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
        preferences = encryptedPrefs;
    }

    // --- Sesión principal (cifrada) ---

    public void saveSession(String token, String email, String name) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, name)
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return preferences.getString(KEY_NAME, "");
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_EMAIL)
                .remove(KEY_NAME)
                .apply();
    }

    // --- Flag biométrico ---

    public boolean isBiometricEnabled() {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    // --- Token cifrado para biometría (EncryptedSharedPreferences separado) ---

    public void saveEncryptedToken(String token) {
        try {
            buildEncryptedPrefs().edit().putString(KEY_ENCRYPTED_TOKEN, token).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar token cifrado", e);
        }
    }

    public String getEncryptedToken() {
        try {
            return buildEncryptedPrefs().getString(KEY_ENCRYPTED_TOKEN, null);
        } catch (Exception e) {
            Log.e(TAG, "Error al leer token cifrado", e);
            return null;
        }
    }

    private SharedPreferences buildEncryptedPrefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
