/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.afwsamples.testdpc.provision;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
import static com.afwsamples.testdpc.DeviceAdminReceiver.getComponentName;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.util.Log;
import com.afwsamples.testdpc.AddAccountActivity;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.FinalizeActivity;
import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.cosu.EnableCosuActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task executed after provisioning is done indicated by either the
 * {@link DevicePolicyManager#ACTION_PROVISIONING_SUCCESSFUL} activity intent or the
 * {@link android.app.admin.DeviceAdminReceiver#onProfileProvisioningComplete(Context, Intent)}
 * broadcast.
 *
 * <p>Operations performed:
 * <ul>
 *     <li>self-grant all run-time permissions</li>
 *     <li>enable the launcher activity</li>
 *     <li>start waiting for first account ready broadcast</li>
 * </ul>
 */
public class PostProvisioningTask {
    private static final String TAG = "PostProvisioningTask";
    private static final String SETUP_MANAGEMENT_LAUNCH_ACTIVITY =
            "com.afwsamples.testdpc.SetupManagementLaunchActivity";
    private static final String POST_PROV_PREFS = "post_prov_prefs";
    private static final String KEY_POST_PROV_DONE = "key_post_prov_done";
    private static final String KEY_DEVICE_OWNER_STATE =
          "android.app.extra.PERSISTENT_DEVICE_OWNER_STATE";

    private final Context mContext;
    private final DevicePolicyManager mDevicePolicyManager;
    private final SharedPreferences mSharedPrefs;

    public PostProvisioningTask(Context context) {
        mContext = context;
        mDevicePolicyManager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mSharedPrefs = context.getSharedPreferences(POST_PROV_PREFS, Context.MODE_PRIVATE);
    }

    public boolean performPostProvisioningOperations(Intent intent) {
        if (isPostProvisioningDone()) {
            return false;
        }
        markPostProvisioningDone();

        // From M onwards, permissions are not auto-granted, so we need to manually grant
        // permissions for TestDPC.
        if (Util.SDK_INT >= VERSION_CODES.M) {
            autoGrantRequestedPermissionsToSelf();
        }

        // Retreive the admin extras bundle, which we can use to determine the original context for
        // TestDPCs launch.
        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        if (Util.SDK_INT >= VERSION_CODES.O) {
            maybeSetAffiliationIds(extras);
        }

        // If TestDPC asked GmsCore to store its state in the FRP area before factory reset, the
        // state will be handed over to it during the next device setup.
        if (Util.SDK_INT >= VERSION_CODES.O_MR1
            && extras != null
            && extras.containsKey(KEY_DEVICE_OWNER_STATE)) {
            Util.setPersistentDoStateWithApplicationRestriction(
                mContext,
                mDevicePolicyManager,
                DeviceAdminReceiver.getComponentName(mContext),
                extras.getString(KEY_DEVICE_OWNER_STATE));
        }

        // Hide the setup launcher when this app is the admin
        mContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(mContext, SETUP_MANAGEMENT_LAUNCH_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        return true;
    }

    public Intent getPostProvisioningLaunchIntent(Intent intent) {
        // Enable the profile after provisioning is complete.
        Intent launch;

        // Retreive the admin extras bundle, which we can use to determine the original context for
        // TestDPCs launch.
        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        String packageName = mContext.getPackageName();
        boolean synchronousAuthLaunch = LaunchIntentUtil.isSynchronousAuthLaunch(extras);
        boolean cosuLaunch = LaunchIntentUtil.isCosuLaunch(extras);
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName);

        // Drop out quickly if we're neither profile or device owner.
        if (!isProfileOwner && !isDeviceOwner) {
            return null;
        }

        if (cosuLaunch) {
            launch = new Intent(mContext, EnableCosuActivity.class);
            launch.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras);
        } else {
            launch = new Intent(mContext, FinalizeActivity.class);
        }

        if (synchronousAuthLaunch) {
            String accountName = LaunchIntentUtil.getAddedAccountName(extras);
            if (accountName != null) {
                launch.putExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME, accountName);
            }
        }

        // For synchronous auth cases, we can assume accounts are already setup (or will be shortly,
        // as account migration for Profile Owner is asynchronous). For COSU we don't want to show
        // the account option to the user, as no accounts should be added for now.
        // In other cases, offer to add an account to the newly configured device/profile.
        if (!synchronousAuthLaunch && !cosuLaunch) {
            AccountManager accountManager = AccountManager.get(mContext);
            Account[] accounts = accountManager.getAccounts();
            if (accounts != null && accounts.length == 0) {
                // Add account after provisioning is complete.
                Intent addAccountIntent = new Intent(mContext, AddAccountActivity.class);
                addAccountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                addAccountIntent.putExtra(AddAccountActivity.EXTRA_NEXT_ACTIVITY_INTENT, launch);
                return addAccountIntent;
            }
        }

        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launch;
    }

    private void markPostProvisioningDone() {
        mSharedPrefs.edit().putBoolean(KEY_POST_PROV_DONE, true).commit();
    }

    private boolean isPostProvisioningDone() {
        return mSharedPrefs.getBoolean(KEY_POST_PROV_DONE, false);
    }

    @TargetApi(VERSION_CODES.O)
    private void maybeSetAffiliationIds(PersistableBundle extras) {
        if (extras == null) {
            return;
        }
        String affiliationId = extras.getString(LaunchIntentUtil.EXTRA_AFFILIATION_ID);
        if (affiliationId != null) {
            mDevicePolicyManager.setAffiliationIds(
                    getComponentName(mContext),
                    Collections.singleton(affiliationId));
        }
    }

    @TargetApi(VERSION_CODES.M)
    private void autoGrantRequestedPermissionsToSelf() {
        String packageName = mContext.getPackageName();
        ComponentName adminComponentName = getComponentName(mContext);

        List<String> permissions = getRuntimePermissions(mContext.getPackageManager(), packageName);
        for (String permission : permissions) {
            boolean success = mDevicePolicyManager.setPermissionGrantState(adminComponentName,
                    packageName, permission, PERMISSION_GRANT_STATE_GRANTED);
            Log.d(TAG, "Auto-granting " + permission + ", success: " + success);
            if (!success) {
                Log.e(TAG, "Failed to auto grant permission to self: " + permission);
            }
        }
    }

    private List<String> getRuntimePermissions(PackageManager packageManager, String packageName) {
        List<String> permissions = new ArrayList<>();
        PackageInfo packageInfo;
        try {
            packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not retrieve info about the package: " + packageName, e);
            return permissions;
        }

        if (packageInfo != null && packageInfo.requestedPermissions != null) {
            for (String requestedPerm : packageInfo.requestedPermissions) {
                if (isRuntimePermission(packageManager, requestedPerm)) {
                    permissions.add(requestedPerm);
                }
            }
        }
        return permissions;
    }

    private boolean isRuntimePermission(PackageManager packageManager, String permission) {
        try {
            PermissionInfo pInfo = packageManager.getPermissionInfo(permission, 0);
            if (pInfo != null) {
                if ((pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                        == PermissionInfo.PROTECTION_DANGEROUS) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Could not retrieve info about the permission: " + permission);
        }
        return false;
    }
}
