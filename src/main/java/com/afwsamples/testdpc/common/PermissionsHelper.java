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

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.util.Arrays;
import java.util.List;

/**
 * Checks that the requested permissions are present, and grants any dangerous permissions required.
 */
public class PermissionsHelper {

  public static String TAG = "PermissionsHelper";

  /**
   * Ensures that the passed in permissions are defined in manifest and attempts to grant a
   * permission automatically if it is considered dangerous.
   */
  @RequiresApi(VERSION_CODES.M)
  public static boolean ensureRequiredPermissions(
      String[] requiredPermissions, ComponentName admin, Context context) {
    PackageInfo packageInfo;
    try {
      packageInfo =
          context
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Could not find own package.", e);
      return false;
    }
    List<String> manifestPermissions = Arrays.asList(packageInfo.requestedPermissions);
    for (String expectedPermission : requiredPermissions) {
      if (!manifestPermissions.contains(expectedPermission)) {
        Log.e(TAG, "Missing required permission from manifest: " + expectedPermission);
        return false;
      }
      if (!maybeGrantDangerousPermission(expectedPermission, admin, context)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Attempts to grant a permission automatically if it is considered dangerous - this only happens
   * for PO/DO devices.
   */
  @RequiresApi(VERSION_CODES.M)
  private static boolean maybeGrantDangerousPermission(
      String permission, ComponentName admin, Context context) {
    if (!isPermissionDangerous(permission, context)) {
      return true;
    }
    if (!ProvisioningStateUtil.isManagedByTestDPC(context)) {
      return false;
    }
    if (hasPermissionGranted(admin, context, permission)) {
      return true;
    }
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    return devicePolicyManager.setPermissionGrantState(
        admin,
        context.getPackageName(),
        permission,
        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
  }

  // Min API version required for DevicePolicyManager.getPermissionGrantState
  @RequiresApi(VERSION_CODES.M)
  private static boolean hasPermissionGranted(
      ComponentName componentName, Context context, String permission) {
    DevicePolicyManager devicePolicyManager = context.getSystemService(DevicePolicyManager.class);
    return devicePolicyManager.getPermissionGrantState(
            componentName, context.getPackageName(), permission)
        == DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
  }

  private static boolean isPermissionDangerous(String permission, Context context) {
    PermissionInfo permissionInfo;
    try {
      permissionInfo = context.getPackageManager().getPermissionInfo(permission, 0);
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Failed to look up permission.", e);
      return false;
    }
    return permissionInfo != null
        && (permissionInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
            == PermissionInfo.PROTECTION_DANGEROUS;
  }
}
