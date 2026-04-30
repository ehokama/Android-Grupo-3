package com.rutea.app.activitiesandviews.di;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.AuthApiService;
import com.rutea.app.activitiesandviews.data.network.DisponibilityApiService;
import com.rutea.app.activitiesandviews.data.network.ReserveApiService;
import com.rutea.app.activitiesandviews.data.network.ReviewApiService;
import com.rutea.app.activitiesandviews.data.network.TravellerApiService;
import com.rutea.app.activitiesandviews.data.local.TokenManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    public static final String ACTION_SESSION_EXPIRED = "com.rutea.app.SESSION_EXPIRED";
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String TAG = "NetworkModule";

    @Provides
    @Singleton
    public OkHttpClient provideOkHttp(TokenManager tokenManager, @ApplicationContext Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                // Interceptor 1: Agrega el token a las peticiones
                .addInterceptor(chain -> {
                    String token = tokenManager.getToken();
                    Request request = chain.request();

                    // No agregar el token a los endpoints de autenticación (login, registro, otp)
                    String path = request.url().encodedPath();
                    boolean isAuthEndpoint = path.contains("/api/auth/");

                    if (token != null && !token.isEmpty() && !isAuthEndpoint) {
                        request = request.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        Log.d(TAG, "Authorization header agregado: Bearer " + token);
                    } else if (isAuthEndpoint) {
                        Log.d(TAG, "Endpoint de auth detectado — omitiendo Authorization header");
                    } else {
                        Log.d(TAG, "Sin token — request sin Authorization header");
                    }
                    return chain.proceed(request);
                })
                // Interceptor 2: Detecta token expirado (401) y fuerza cierre de sesión
                .addInterceptor(chain -> {
                    okhttp3.Response response = chain.proceed(chain.request());
                    if (response.code() == 401) {
                        String path = chain.request().url().encodedPath();
                        boolean isAuthEndpoint = path.contains("/api/auth/");
                        if (!isAuthEndpoint) {
                            Log.w(TAG, "Token expirado detectado (401). Cerrando sesión...");
                            tokenManager.clearSession();
                            Intent intent = new Intent(ACTION_SESSION_EXPIRED);
                            intent.setPackage(context.getPackageName());
                            context.sendBroadcast(intent);
                        }
                    }
                    return response;
                })
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public AuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }

    @Provides
    @Singleton
    public ActivityApiService provideActivityApiService(Retrofit retrofit) {
        return retrofit.create(ActivityApiService.class);
    }

    @Provides
    @Singleton
    public DisponibilityApiService provideDisponibilityApiService(Retrofit retrofit) {
        return retrofit.create(DisponibilityApiService.class);
    }

    @Provides
    @Singleton
    public ReserveApiService provideReserveApiService(Retrofit retrofit) {
        return retrofit.create(ReserveApiService.class);
    }

    @Provides
    @Singleton
    public ReviewApiService provideReviewApiService(Retrofit retrofit) {
        return retrofit.create(ReviewApiService.class);
    }

    @Provides
    @Singleton
    public TravellerApiService provideTravellerApiService(Retrofit retrofit) {
        return retrofit.create(TravellerApiService.class);
    }
}
