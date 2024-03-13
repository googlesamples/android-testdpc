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
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.preference.PreferenceFragment;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/** Common utility functions. */
public class Util {
  private static final String TAG = "Util";
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  // TODO(b/258511062): change check once U SDK is launched
  private static final boolean IS_RUNNING_U = VERSION.CODENAME.equals("UpsideDownCake");

  /**
   * A replacement for {@link VERSION.SDK_INT} that is compatible with pre-release SDKs
   *
   * <p>This will be set to the version SDK, or {@link VERSION_CODES.CUR_DEVELOPMENT} if the SDK int
   * is not yet assigned.
   */
  public static final int SDK_INT = IS_RUNNING_U ? VERSION_CODES.CUR_DEVELOPMENT : VERSION.SDK_INT;

  // Copied over from RoleManager.ROLE_DEVICE_POLICY_MANAGEMENT, which can't be referenced directly
  // since it's a @SystemAPI.
  private static final String ROLE_DEVICE_POLICY_MANAGEMENT =
      "android.app.role.DEVICE_POLICY_MANAGEMENT";

  /**
   * Format a friendly datetime for the current locale according to device policy documentation. If
   * the timestamp doesn't represent a real date, it will be interpreted as {@code null}.
   *
   * @return A {@link CharSequence} such as "12:35 PM today" or "June 15, 2033", or {@code null} in
   *     the case that {@param timestampMs} equals zero.
   */
  public static CharSequence formatTimestamp(long timestampMs) {
    if (timestampMs == 0) {
      // DevicePolicyManager documentation describes this timestamp as having no effect,
      // so show nothing for this case as the policy has not been set.
      return null;
    }

    return DateUtils.formatSameDayTime(
        timestampMs,
        System.currentTimeMillis(),
        DateUtils.FORMAT_SHOW_WEEKDAY,
        DateUtils.FORMAT_SHOW_TIME);
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
   * Return {@code true} iff we are the profile owner of a managed profile. Note that profile owner
   * can be in primary user and secondary user too.
   */
  @TargetApi(VERSION_CODES.N)
  public static boolean isManagedProfileOwner(Context context) {
    final DevicePolicyManager dpm = getDevicePolicyManager(context);

    if (Util.SDK_INT >= VERSION_CODES.N) {
      try {
        return dpm.isManagedProfile(DeviceAdminReceiver.getReceiverComponentName(context));
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

  public static boolean isManagedProfile(Context context) {
    if (Util.SDK_INT >= VERSION_CODES.R) {
      UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
      return userManager.isManagedProfile();
    }
    // There is no equivalent API before Android R, so fall back to previous behaviour that only
    // works correctly if we are the PO of a managed profile
    return isManagedProfileOwner(context);
  }

  @TargetApi(VERSION_CODES.M)
  public static boolean isPrimaryUser(Context context) {
    if (Util.SDK_INT >= VERSION_CODES.M) {
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

  @TargetApi(VERSION_CODES.O)
  public static boolean isDelegatedApp(Context context) {
    if (Util.SDK_INT < VERSION_CODES.O) {
      return false;
    }

    DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
    return !dpm.getDelegatedScopes(null, context.getPackageName()).isEmpty();
  }

  @TargetApi(VERSION_CODES.S)
  public static boolean isDeviceManagementRoleHolder(Context context) {
    if (Util.SDK_INT < VERSION_CODES.S) {
      return false;
    }
    RoleManager rm = context.getSystemService(RoleManager.class);
    return rm.isRoleHeld(ROLE_DEVICE_POLICY_MANAGEMENT);
  }

  @TargetApi(VERSION_CODES.O)
  public static List<UserHandle> getBindDeviceAdminTargetUsers(Context context) {
    if (Util.SDK_INT < VERSION_CODES.O) {
      return Collections.emptyList();
    }

    final DevicePolicyManager dpm = getDevicePolicyManager(context);
    return dpm.getBindDeviceAdminTargetUsers(DeviceAdminReceiver.getComponentName(context));
  }

  public static void showFileViewer(PreferenceFragment fragment, int requestCode) {
    Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
    certIntent.setTypeAndNormalize("*/*");
    try {
      fragment.startActivityForResult(certIntent, requestCode);
    } catch (ActivityNotFoundException e) {
      Log.e(TAG, "showFileViewer: ", e);
    }
  }

  /** @return If the certificate was successfully installed. */
  public static boolean installCaCertificate(
      InputStream certificateInputStream, DevicePolicyManager dpm, ComponentName admin) {
    try {
      if (certificateInputStream != null) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len = 0;
        while ((len = certificateInputStream.read(buffer)) > 0) {
          byteBuffer.write(buffer, 0, len);
        }
        return dpm.installCaCert(admin, byteBuffer.toByteArray());
      }
    } catch (IOException e) {
      Log.e(TAG, "installCaCertificate: ", e);
    }
    return false;
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

  /** @return Intent for a launcher activity */
  public static Intent getLauncherIntent(Context context) {
    Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
    if (Util.isRunningOnTvDevice(context)) {
      launcherIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
    } else {
      launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    }
    return launcherIntent;
  }

  private static DevicePolicyManager getDevicePolicyManager(Context context) {
    return (DevicePolicyManager) context.getSystemService(Service.DEVICE_POLICY_SERVICE);
  }

  public static boolean hasDelegation(Context context, String delegation) {
    if (Util.SDK_INT < VERSION_CODES.O) {
      return false;
    }
    DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
    return dpm.getDelegatedScopes(null, context.getPackageName()).contains(delegation);
  }

  public static boolean isRunningOnTvDevice(Context context) {
    UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
    return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
  }

  public static boolean isRunningOnAutomotiveDevice(Context context) {
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
  }

  public static void requireAndroidS() {
    if (!isAtLeastS()) {
      throw new IllegalStateException("requires API level S, device's on " + Build.VERSION.SDK_INT);
    }
  }

  public static boolean isAtLeastS() {
    return SDK_INT >= VERSION_CODES.S;
  }

  public static String myProcessName() {
    return SDK_INT >= VERSION_CODES.TIRAMISU ? Process.myProcessName() : "N/A";
  }

  public static boolean isAtLeastT() {
    return SDK_INT >= VERSION_CODES.TIRAMISU;
  }

  public static String lockTaskFeaturesToString(int flags) {
    return flagsToString(DevicePolicyManager.class, "LOCK_TASK_FEATURE_", flags);
  }

  public static String personalAppsSuspensionReasonToString(int reasons) {
    return flagsToString(DevicePolicyManager.class, "PERSONAL_APPS_", reasons);
  }

  public static String grantStateToString(int grantState) {
    return constantToString(DevicePolicyManager.class, "PERMISSION_GRANT_STATE_", grantState);
  }

  public static String keyguardDisabledFeaturesToString(int which) {
    return flagsToString(DevicePolicyManager.class, "KEYGUARD_DISABLE_", which);
  }

  public static String passwordQualityToString(int quality) {
    return constantToString(DevicePolicyManager.class, "PASSWORD_QUALITY_", quality);
  }

  public static String requiredPasswordComplexityToString(int complexity) {
    return constantToString(DevicePolicyManager.class, "PASSWORD_COMPLEXITY_", complexity);
  }

  public static void onSuccessLog(String tag, String template, Object... args) {
    Log.d(tag, String.format(template, args) + " succeeded");
  }

  public static void onErrorLog(String tag, String template, Object... args) {
    Log.e(tag, String.format(template, args) + " failed");
  }

  public static void onErrorLog(String tag, Exception e, String template, Object... args) {
    Log.e(tag, String.format(template, args) + " failed", e);
  }

  // Copied from DebugUtils
  public static String flagsToString(Class<?> clazz, String prefix, int flags) {
    final StringBuilder res = new StringBuilder();
    boolean flagsWasZero = flags == 0;

    for (Field field : clazz.getDeclaredFields()) {
      final int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers)
          && Modifier.isFinal(modifiers)
          && field.getType().equals(int.class)
          && field.getName().startsWith(prefix)) {
        try {
          final int value = field.getInt(null);
          if (value == 0 && flagsWasZero) {
            return constNameWithoutPrefix(prefix, field);
          }
          if (value != 0 && (flags & value) == value) {
            flags &= ~value;
            res.append(constNameWithoutPrefix(prefix, field)).append('|');
          }
        } catch (IllegalAccessException ignored) {
        }
      }
    }
    if (flags != 0 || res.length() == 0) {
      res.append(Integer.toHexString(flags));
    } else {
      res.deleteCharAt(res.length() - 1);
    }
    return res.toString();
  }

  // Copied from DebugUtils
  public static String constantToString(Class<?> clazz, String prefix, int value) {
    for (Field field : clazz.getDeclaredFields()) {
      final int modifiers = field.getModifiers();
      try {
        if (Modifier.isStatic(modifiers)
            && Modifier.isFinal(modifiers)
            && field.getType().equals(int.class)
            && field.getName().startsWith(prefix)
            && field.getInt(null) == value) {
          return constNameWithoutPrefix(prefix, field);
        }
      } catch (IllegalAccessException ignored) {
      }
    }
    return prefix + Integer.toString(value);
  }

  // Copied from DebugUtils
  private static String constNameWithoutPrefix(String prefix, Field field) {
    return field.getName().substring(prefix.length());
  }

  public static void onSuccessShowToast(Context mContext, String template, Object... args) {
    Toast.makeText(
            mContext,
            String.format(template, args),
            Toast.LENGTH_LONG)
        .show();
  }

  public static void onErrorShowToast(Context mContext, Exception e, String template, Object... args) {
    Toast.makeText(
            mContext,
            e.getMessage() + "\n" + String.format(template, args),
            Toast.LENGTH_LONG)
        .show();
  }
}
