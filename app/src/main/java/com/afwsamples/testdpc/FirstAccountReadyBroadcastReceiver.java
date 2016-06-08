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

package com.afwsamples.testdpc;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.afwsamples.testdpc.provision.CheckInState;
import com.afwsamples.testdpc.provision.ProvisioningUtil;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

/**
 * Receiver for FIRST_ACCOUNT_READY_ACTION from Google Play Service.
 */
public class FirstAccountReadyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "FirstAccountReady";

    private static final String FIRST_ACCOUNT_READY_ACTION =
            "com.google.android.work.action.FIRST_ACCOUNT_READY";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received: " + intent.getAction());
        if (FIRST_ACCOUNT_READY_ACTION.equals(intent.getAction())) {
            CheckInState checkInState = new CheckInState(context);
            checkInState.setFirstAccountReady();
            ProvisioningUtil.enableProfile(context);
            // This receiver is disabled in ProvisioningUtil.enableProfile, no more code should
            // be put after it.
        }
    }

    public static void setEnabled(Context context, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, FirstAccountReadyBroadcastReceiver.class),
                (enabled) ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
        );
    }
}