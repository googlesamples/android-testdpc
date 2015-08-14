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

package com.sample.android.testdpc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sample.android.testdpc.common.LaunchIntentUtil;
import com.sample.android.testdpc.syncauth.FinishSyncAuthDeviceOwnerActivity;
import com.sample.android.testdpc.syncauth.FinishSyncAuthProfileOwnerActivity;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static com.sample.android.testdpc.policy.PolicyManagementFragment.OVERRIDE_KEY_SELECTION_KEY;

/**
 * Handles events related to the managed profile.
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Retreive the admin extras bundle, which we can use to determine the original context for
        // TestDPCs launch.
        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        // Enable the profile after provisioning is complete.
        Intent launch = null;
        String packageName = context.getPackageName();
        boolean synchronousAuthLaunch = LaunchIntentUtil.isSynchronousAuthLaunch(extras);
        boolean isProfileOwner = devicePolicyManager.isProfileOwnerApp(packageName);
        boolean isDeviceOwner = devicePolicyManager.isDeviceOwnerApp(packageName);

        // Drop out quickly if we're neither profile or device owner.
        if (!isProfileOwner && !isDeviceOwner) {
            Log.e("TestDPC", "DeviceAdminReceiver.onProvisioningComplete() invoked, but ownership "
                    + "not assigned");
            Toast.makeText(context, R.string.device_admin_receiver_failure, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (synchronousAuthLaunch) {
            // Synchronous auth cases.
            String accountName = LaunchIntentUtil.getAddedAccountName(extras);
            if (isProfileOwner) {
                launch = new Intent(context, FinishSyncAuthProfileOwnerActivity.class)
                        .putExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME, accountName);
            } else {
                launch = new Intent(context, FinishSyncAuthDeviceOwnerActivity.class)
                        .putExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME, accountName);
            }
        } else {
            // User or NFC launched cases.
            if (isProfileOwner) {
                launch = new Intent(context, EnableProfileActivity.class);
            } else {
                launch = new Intent(context, EnableDeviceOwnerActivity.class);
            }
        }

        // For synchronous auth cases, we can assume accounts are already setup (or will be shortly,
        // as account migration for Profile Owner is asynchronous). In other cases, offer to add an
        // account to the newly configured device/profile.
        if (!synchronousAuthLaunch) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccounts();
            if (accounts != null && accounts.length == 0) {
                // Add account after provisioning is complete.
                Intent addAccountIntent = new Intent(context, AddAccountActivity.class);
                addAccountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                addAccountIntent.putExtra(AddAccountActivity.EXTRA_NEXT_ACTIVITY_INTENT, launch);
                context.startActivity(addAccountIntent);
                return;
            }
        }

        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
        Toast.makeText(context, "System update received at: " + receivedTime,
                Toast.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri,
            String alias) {
        String chosenAlias = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OVERRIDE_KEY_SELECTION_KEY, null);
        if (!TextUtils.isEmpty(chosenAlias)) {
            Toast.makeText(context, "Substituting private key alias: \"" + chosenAlias + "\"",
                    Toast.LENGTH_LONG).show();
            return chosenAlias;
        } else {
            return null;
        }
    }

    /**
     * @param context The context of the application.
     * @return The component name of this component in the given context.
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdminReceiver.class);
    }
}
