package com.rutea.app.activitiesandviews.ui.data.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit instance;
    private static SessionManager sessionManager;

    private RetrofitClient() {
    }

    public static synchronized void init(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context.getApplicationContext());
        }
        instance = null;
    }

    public static synchronized Retrofit getInstance() {
        if (sessionManager == null) {
            throw new IllegalStateException("RetrofitClient.init(context) must be called first");
        }
        if (instance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(sessionManager))
                    .addInterceptor(loggingInterceptor)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }

    public static SessionManager getSessionManager() {
        if (sessionManager == null) {
            throw new IllegalStateException("RetrofitClient.init(context) must be called first");
        }
        return sessionManager;
    }
}
