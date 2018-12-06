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
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.os.BuildCompat;
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
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final String BROADCAST_ACTION_FRP_CONFIG_CHANGED =
        "com.google.android.gms.auth.FRP_CONFIG_CHANGED";
    private static final String GMSCORE_PACKAGE = "com.google.android.gms";
    private static final String PERSISTENT_DEVICE_OWNER_STATE = "persistentDeviceOwnerState";

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

    /**
     * Return {@code true} iff we are the profile owner of a managed profile.
     * Note that profile owner can be in primary user and secondary user too.
     */
    @TargetApi(VERSION_CODES.N)
    public static boolean isManagedProfileOwner(Context context) {
        final DevicePolicyManager dpm = getDevicePolicyManager(context);

        if (BuildCompat.isAtLeastN()) {
            try {
                return dpm.isManagedProfile(DeviceAdminReceiver.getComponentName(context));
            } catch (SecurityException ex) {
                // This is thrown if we are neither profile owner nor device owner.
                return false;
            }
        }

        // Pre-N, TestDPC only supports being the profile owner for a managed profile. Other apps
        // may support being a profile owner in other contexts (e.g. a secondary user) which will
        // require further checks.
        return isProfileOwner(context);
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
        final DevicePolicyManager dpm = getDevicePolicyManager(context);
        return dpm.isDeviceOwnerApp(context.getPackageName());
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static boolean isProfileOwner(Context context) {
        final DevicePolicyManager dpm = getDevicePolicyManager(context);
        return dpm.isProfileOwnerApp(context.getPackageName());
    }

    public static boolean isAtLeastM() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    public static boolean isAtLeastQ() {
        return VERSION.CODENAME.length() == 1
            && VERSION.CODENAME.charAt(0) >= 'Q'
            && VERSION.CODENAME.charAt(0) <= 'Z';
    }

    @TargetApi(VERSION_CODES.O)
    public static List<UserHandle> getBindDeviceAdminTargetUsers(Context context) {
        if (!BuildCompat.isAtLeastO()) {
            return Collections.emptyList();
        }

        final DevicePolicyManager dpm = getDevicePolicyManager(context);
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

    /**
     * Returns the persistent device owner state which has been set by the device owner as an app
     * restriction on GmsCore or null if there is no such restriction set.
     */
    @TargetApi(VERSION_CODES.O)
    public static String getPersistentDoStateFromApplicationRestriction(
            DevicePolicyManager dpm, ComponentName admin) {
        Bundle restrictions = dpm.getApplicationRestrictions(admin, GMSCORE_PACKAGE);
        return restrictions.getString(PERSISTENT_DEVICE_OWNER_STATE);
    }

    /**
     * Sets the persistent device owner state by setting a special app restriction on GmsCore and
     * notifies GmsCore about the change by sending a broadcast.
     *
     * @param state The device owner state to be preserved across factory resets. If null, the
     * persistent device owner state and the corresponding restiction are cleared.
     */
    @TargetApi(VERSION_CODES.O)
    public static void setPersistentDoStateWithApplicationRestriction(
            Context context, DevicePolicyManager dpm, ComponentName admin, String state) {
        Bundle restrictions = dpm.getApplicationRestrictions(admin, GMSCORE_PACKAGE);
        if (state == null) {
            // Clear the restriction
            restrictions.remove(PERSISTENT_DEVICE_OWNER_STATE);
        } else {
            // Set the restriction
            restrictions.putString(PERSISTENT_DEVICE_OWNER_STATE, state);
        }
        dpm.setApplicationRestrictions(admin, GMSCORE_PACKAGE, restrictions);
        Intent broadcastIntent = new Intent(BROADCAST_ACTION_FRP_CONFIG_CHANGED);
        broadcastIntent.setPackage(GMSCORE_PACKAGE);
        broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        context.sendBroadcast(broadcastIntent);
    }

    /** @return Intent for the default home activity */
    public static Intent getHomeIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        return intent;
    }

    /** @return IntentFilter for the default home activity */
    public static IntentFilter getHomeIntentFilter() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    private static DevicePolicyManager getDevicePolicyManager(Context context) {
        return (DevicePolicyManager)context.getSystemService(Service.DEVICE_POLICY_SERVICE);
    }

    public static boolean hasDelegation(Context context, String delegation) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.O) {
            return false;
        }
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        return dpm.getDelegatedScopes(null, context.getPackageName()).contains(delegation);
    }


}
