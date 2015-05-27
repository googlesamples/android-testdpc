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

package com.google.android.testdpc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.android.testdpc.policy.PolicyManagementFragment.OVERRIDE_KEY_SELECTION_KEY;

/**
 * Handles events related to the managed profile.
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Enable the profile after provisioning is complete.
        Intent launch = new Intent(context, EnableProfileActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    // TODO: Missing @Override, wait until API goes into SDK.
    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
        Toast.makeText(context, "System update received at: " + receivedTime,
                Toast.LENGTH_LONG).show();
    }

    // TODO: Missing @Override
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
