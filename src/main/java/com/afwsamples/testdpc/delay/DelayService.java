/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afwsamples.testdpc.delay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.afwsamples.testdpc.PolicyManagementActivity;
import com.afwsamples.testdpc.R;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background service that monitors pending changes and applies them when ready.
 */
public class DelayService extends Service {
    private static final String TAG = "DelayService";
    private static final String CHANNEL_ID = "delay_service_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL_MS = 10_000; // Check every 10 seconds

    private DelayManager delayManager;
    private ActionExecutor actionExecutor;
    private ExecutorService executor;
    private Handler handler;
    private boolean isRunning = false;

    private final Runnable checkPendingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                checkAndApplyPendingChanges();
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "DelayService created");
        delayManager = DelayManager.getInstance(this);
        actionExecutor = new ActionExecutor(this);
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "DelayService started");

        if (!isRunning) {
            isRunning = true;
            startForeground(NOTIFICATION_ID, createNotification("Delay service active"));
            handler.post(checkPendingRunnable);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "DelayService destroyed");
        isRunning = false;
        handler.removeCallbacks(checkPendingRunnable);
        executor.shutdown();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkAndApplyPendingChanges() {
        executor.execute(() -> {
            try {
                List<PendingChange> readyChanges = delayManager.getReadyChanges();

                if (readyChanges.isEmpty()) {
                    updateNotification("No pending changes");
                    return;
                }

                Log.i(TAG, "Found " + readyChanges.size() + " changes ready to apply");

                for (PendingChange change : readyChanges) {
                    updateNotification("Applying: " + change.description);

                    boolean success = actionExecutor.execute(change);

                    if (success) {
                        delayManager.markCompleted(change);
                        Log.i(TAG, "Completed: " + change.description);
                    } else {
                        delayManager.markFailed(change);
                        Log.e(TAG, "Failed: " + change.description);
                        showFailureNotification(change);
                    }
                }

                updatePendingCountNotification();

            } catch (Exception e) {
                Log.e(TAG, "Error checking pending changes", e);
            }
        });
    }

    private void updatePendingCountNotification() {
        delayManager.getPendingChanges(changes -> {
            if (changes.isEmpty()) {
                updateNotification("No pending changes");
            } else {
                PendingChange next = changes.get(0);
                long remaining = next.getTimeRemainingMillis();
                String timeStr = formatTimeRemaining(remaining);
                updateNotification(changes.size() + " pending. Next in " + timeStr);
            }
        });
    }

    private String formatTimeRemaining(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = minutes / 60;
        return hours + "h " + (minutes % 60) + "m";
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Delay Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors pending policy changes");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String text) {
        Intent intent = new Intent(this, PolicyManagementActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
            .setContentTitle("Test DPC Delay")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(text));
        }
    }

    private void showFailureNotification(PendingChange change) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = createNotification("Failed: " + change.description);
            manager.notify(NOTIFICATION_ID + (int) change.id, notification);
        }
    }

    /**
     * Start the delay service.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, DelayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Stop the delay service.
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, DelayService.class);
        context.stopService(intent);
    }
}
