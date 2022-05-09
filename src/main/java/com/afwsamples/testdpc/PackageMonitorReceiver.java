package com.afwsamples.testdpc;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import com.afwsamples.testdpc.common.NotificationUtil;

public class PackageMonitorReceiver extends BroadcastReceiver {
  private static final String TAG = "PackageMonitorReceiver";
  private static final int PACKAGE_CHANGED_NOTIIFICATION_ID = 34857;

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (!Intent.ACTION_PACKAGE_ADDED.equals(action)
        && !Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
      return;
    }
    String packageName = getPackageNameFromIntent(intent);
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
    if (replacing) {
      return;
    }
    String notificationBody = buildNotificationText(context, packageName, action);
    Notification notification =
        NotificationUtil.getNotificationBuilder(context)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.package_changed_notification_title))
            .setContentText(notificationBody)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setOnlyAlertOnce(true)
            .build();
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(PACKAGE_CHANGED_NOTIIFICATION_ID, notification);
  }

  private String getPackageNameFromIntent(Intent intent) {
    if (intent.getData() == null) {
      return null;
    }
    return intent.getData().getSchemeSpecificPart();
  }

  private String buildNotificationText(Context context, String pkgName, String action) {
    int res =
        Intent.ACTION_PACKAGE_ADDED.equals(action)
            ? R.string.package_added_notification_text
            : R.string.package_removed_notification_text;
    return context.getString(res, pkgName);
  }
}
