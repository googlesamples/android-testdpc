package com.afwsamples.testdpc.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import com.afwsamples.testdpc.R;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private static final String DEFAULT_CHANNEL_ID = "default_testdpc_channel";
    public static final int BUGREPORT_NOTIFICATION_ID = 1;
    public static final int PASSWORD_EXPIRATION_NOTIFICATION_ID = 2;
    public static final int USER_ADDED_NOTIFICATION_ID = 3;
    public static final int USER_REMOVED_NOTIFICATION_ID = 4;
    public static final int USER_STARTED_NOTIFICATION_ID = 5;
    public static final int USER_STOPPED_NOTIFICATION_ID = 6;
    public static final int USER_SWITCHED_NOTIFICATION_ID = 7;
    public static final int PROFILE_OWNER_CHANGED_ID = 8;
    public static final int DEVICE_OWNER_CHANGED_ID = 9;
    public static final int TRANSFER_OWNERSHIP_COMPLETE_ID = 10;
    public static final int TRANSFER_AFFILIATED_PROFILE_OWNERSHIP_COMPLETE_ID = 11;

    public static void showNotification(
            Context context, @StringRes int titleId, String msg, int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = getNotificationBuilder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(titleId))
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .build();
        notificationManager.notify(notificationId, notification);
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context) {
        if (Util.SDK_INT >= VERSION_CODES.O) {
            createDefaultNotificationChannel(context);
        }
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID);
        return builder;
    }

    @RequiresApi(VERSION_CODES.O)
    private static void createDefaultNotificationChannel(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = context.getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID,
                appName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

}
