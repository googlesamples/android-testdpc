/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;

/**
 * Common utility functions used for retrieving information about the provisioning state of the
 * device.
 */
public class ProvisioningStateUtil {
    private ProvisioningStateUtil() {}

    /**
     * @return true if the device is not provisioned (setup wizard hasn't completed) and no device
     *         owner is setup
     */
    public static boolean isDeviceUnprovisionedAndNoDeviceOwner(Context context) {
        return !ProvisioningStateUtil.isDeviceProvisioned(context)
                && !ProvisioningStateUtil.isManaged(context);
    }

    /**
     * @returns true if the device is provisioned, and we're past the point where device owner can
     *               be installed
     */
    public static boolean isDeviceProvisioned(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Global.getInt(contentResolver, Settings.Global.DEVICE_PROVISIONED, 0) != 0;
    }

    /**
     * @return true if the device or profile is already owned by TestDPC
     */
    public static boolean isManagedByTestDPC(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        String packageName = context.getPackageName();

        return devicePolicyManager.isProfileOwnerApp(packageName)
                || devicePolicyManager.isDeviceOwnerApp(packageName);
    }

    /**
     * @return true if the device or profile is already owned
     */
    public static boolean isManaged(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        List<ComponentName> admins = devicePolicyManager.getActiveAdmins();
        if (admins == null) return false;
        for (ComponentName admin : admins) {
            String adminPackageName = admin.getPackageName();
            if (devicePolicyManager.isDeviceOwnerApp(adminPackageName)
                    || devicePolicyManager.isProfileOwnerApp(adminPackageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param action Action to be checked
     * @param context Calling activity's context
     * @return true, if provisioning is allowed for corresponding action
     */
    public static boolean isProvisioningAllowed(Context context, String action) {
        /* TODO: Remove CODENAME check once SDK_INT on device is bumped for N */
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
                && (!("N".equals(Build.VERSION.CODENAME)))) {
            if (ACTION_PROVISION_MANAGED_DEVICE.equals(action)) {
                return (Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
                        ? isDeviceUnprovisionedAndNoDeviceOwner(context) : false;
            }
            if (ACTION_PROVISION_MANAGED_PROFILE.equals(action)) {
                return true;
            }
            return false;
        }
        // DevicePolicyManager.isProvisioningAllowed is added in N.
        DevicePolicyManager dpm = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isProvisioningAllowed(action);
    }
}
