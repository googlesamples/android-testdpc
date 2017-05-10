/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.common;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.os.BuildCompat;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Common utility functions.
 */
public class Util {
    private static final String TAG = "Util";
    private  static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int BUGREPORT_NOTIFICATION_ID = 1;
    public static final int PASSWORD_EXPIRATION_NOTIFICATION_ID = 2;
    public static final int USER_ADDED_NOTIFICATION_ID = 3;
    public static final int USER_REMOVED_NOTIFICATION_ID = 4;
    private static final String DEFAULT_CHANNEL_ID = "default_testdpc_channel";

    public static void showNotification(Context context, int titleId, String msg,
            int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "showNotification: " + BuildCompat.isAtLeastO());
        if (BuildCompat.isAtLeastO()) {
            createDefaultNotificationChannel(context, notificationManager);
        }
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(titleId))
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setChannel(DEFAULT_CHANNEL_ID)
                .build();
        notificationManager.notify(notificationId, notification);
    }

    @RequiresApi(VERSION_CODES.O)
    private static void createDefaultNotificationChannel(
            Context context, NotificationManager notificationManager) {
        String appName = context.getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID,
                appName, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Format a friendly datetime for the current locale according to device policy documentation.
     * If the timestamp doesn't represent a real date, it will be interpreted as {@code null}.
     *
     * @return A {@link CharSequence} such as "12:35 PM today" or "June 15, 2033", or {@code null}
     * in the case that {@param timestampMs} equals zero.
     */
    public static CharSequence formatTimestamp(long timestampMs) {
        if (timestampMs == 0) {
            // DevicePolicyManager documentation describes this timestamp as having no effect,
            // so show nothing for this case as the policy has not been set.
            return null;
        }

        return DateUtils.formatSameDayTime(timestampMs, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_WEEKDAY, DateUtils.FORMAT_SHOW_TIME);
    }

    public static void updateImageView(Context context, ImageView imageView, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            // Avoid decoding the entire image if the imageView holding this image is smaller.
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bounds);
            int streamWidth = bounds.outWidth;
            int streamHeight = bounds.outHeight;
            int maxDesiredWidth = imageView.getMaxWidth();
            int maxDesiredHeight = imageView.getMaxHeight();
            int ratio = Math.max(streamWidth / maxDesiredWidth, streamHeight / maxDesiredHeight);
            if (ratio > 1) {
                bounds.inSampleSize = ratio;
            }
            bounds.inJustDecodeBounds = false;

            inputStream = context.getContentResolver().openInputStream(uri);
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, bounds));
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.error_opening_image_file, Toast.LENGTH_SHORT);
        }
    }

    @TargetApi(VERSION_CODES.N)
    public static boolean isManagedProfile(Context context) {
        if (BuildCompat.isAtLeastN()) {
            DevicePolicyManager devicePolicyManager =
                    (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                return devicePolicyManager.isManagedProfile(
                        DeviceAdminReceiver.getComponentName(context));
            } catch (SecurityException e) {
                // This is thrown if there is no active admin so not the managed profile
                return false;
            }
        } else {
            // If user has more than one profile, then we deal with managed profile.
            // Unfortunately there is no public API available to distinguish user profile owner
            // and managed profile owner. Thus using this hack.
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            return userManager.getUserProfiles().size() > 1;
        }
    }

    @TargetApi(VERSION_CODES.M)
    public static boolean isPrimaryUser(Context context) {
        if (isAtLeastM()) {
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            return userManager.isSystemUser();
        } else {
            // Assume only DO can be primary user. This is not perfect but the cases in which it is
            // wrong are uncommon and require adb to set up.
            return isDeviceOwner(context);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static boolean isDeviceOwner(Context context) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isDeviceOwnerApp(context.getPackageName());
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static boolean isProfileOwner(Context context) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isProfileOwnerApp(context.getPackageName());
    }

    public static boolean isAtLeastM() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    @TargetApi(VERSION_CODES.O)
    public static List<UserHandle> getBindDeviceAdminTargetUsers(Context context) {
        if (!BuildCompat.isAtLeastO()) {
            return Collections.emptyList();
        }

        final DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.getBindDeviceAdminTargetUsers(DeviceAdminReceiver.getComponentName(context));
    }

    public static void showFileViewerForImportingCertificate(PreferenceFragment fragment,
            int requestCode) {
        Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
        certIntent.setTypeAndNormalize("*/*");
        try {
            fragment.startActivityForResult(certIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "showFileViewerForImportingCertificate: ", e);
        }
    }

    /**
     * @return If the certificate was successfully installed.
     */
    public static boolean installCaCertificate(InputStream certificateInputStream,
            DevicePolicyManager dpm, ComponentName admin) {
        try {
            if (certificateInputStream != null) {
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len = 0;
                while ((len = certificateInputStream.read(buffer)) > 0) {
                    byteBuffer.write(buffer, 0, len);
                }
                return dpm.installCaCert(admin,
                        byteBuffer.toByteArray());
            }
        } catch (IOException e) {
            Log.e(TAG, "installCaCertificate: ", e);
        }
        return false;
    }
}
