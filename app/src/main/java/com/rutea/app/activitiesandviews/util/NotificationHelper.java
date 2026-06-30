package com.rutea.app.activitiesandviews.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.rutea.app.activitiesandviews.service.NotificationPollingService;

public final class NotificationHelper {

    private NotificationHelper() {}

    public static void startPollingService(Context context) {
        Intent intent = new Intent(context, NotificationPollingService.class);
        context.startForegroundService(intent);
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
