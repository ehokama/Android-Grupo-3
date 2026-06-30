package com.rutea.app.activitiesandviews.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.models.dto.notification.NotificationDto;
import com.rutea.app.activitiesandviews.ui.MainActivity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@AndroidEntryPoint
public class NotificationPollingService extends Service {

    private static final String TAG = "NotificationPolling";
    private static final String BASE_URL = "https://xplorenow-api-production.up.railway.app/";
    private static final int NOTIF_ID_FOREGROUND = 1001;
    private static final String CANAL_ID = "xplorenow_alerts";
    private static final String CANAL_NOMBRE = "Recordatorios y avisos";

    public static final String EXTRA_OPEN_HISTORY = "open_history";
    public static final String ACTION_OPEN_HISTORY = "com.rutea.app.OPEN_HISTORY";

    @Inject TokenManager tokenManager;

    private Thread pollingThread;
    private final AtomicInteger notifCounter = new AtomicInteger(2000);
    private final Gson gson = new Gson();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();

        Notification foreground = new NotificationCompat.Builder(this, CANAL_ID)
                .setContentTitle("XplorerNow")
                .setContentText("Escuchando recordatorios y avisos")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID_FOREGROUND, foreground);

        if (pollingThread == null || !pollingThread.isAlive()) {
            pollingThread = new Thread(this::pollLoop);
            pollingThread.start();
        }

        return START_STICKY;
    }

    private void pollLoop() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(65, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        while (!Thread.currentThread().isInterrupted()) {
            String token = tokenManager.getToken();
            if (token == null || token.isEmpty()) {
                sleepQuiet(5000);
                continue;
            }

            Request request = new Request.Builder()
                    .url(BASE_URL + "api/notifications/poll?timeout=30")
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    List<NotificationDto> notifications = gson.fromJson(body,
                            new TypeToken<List<NotificationDto>>() {}.getType());
                    if (notifications != null) {
                        for (NotificationDto n : notifications) {
                            mostrarNotificacion(n);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error en long polling", e);
                sleepQuiet(5000);
            }
        }
    }

    private void mostrarNotificacion(NotificationDto notification) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        int id = notifCounter.incrementAndGet();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_OPEN_HISTORY);
        intent.putExtra(EXTRA_OPEN_HISTORY, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        manager.notify(id, notif);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID, CANAL_NOMBRE, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onDestroy() {
        if (pollingThread != null) pollingThread.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
