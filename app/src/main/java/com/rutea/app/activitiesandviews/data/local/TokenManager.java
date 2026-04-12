package com.rutea.app.activitiesandviews.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREF_NAME = "rutea_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";

    private final SharedPreferences preferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

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
        preferences.edit().clear().apply();
    }
}
