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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
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

    public static final String FIRST_ACCOUNT_READY_TIMEOUT_ACTION =
            "com.afwsamples.testdpc.FIRST_ACCOUNT_READY_TIMEOUT";

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received: " + action);
        if (FIRST_ACCOUNT_READY_ACTION.equals(action) ||
                FIRST_ACCOUNT_READY_TIMEOUT_ACTION.equals(action)) {
            CheckInState checkInState = new CheckInState(context);
            if (checkInState.getFirstAccountState() == CheckInState.FIRST_ACCOUNT_STATE_PENDING) {
                int nextState;
                if (FIRST_ACCOUNT_READY_ACTION.equals(action)) {
                    nextState = CheckInState.FIRST_ACCOUNT_STATE_READY;
                } else {
                    nextState = CheckInState.FIRST_ACCOUNT_STATE_TIMEOUT;
                }
                checkInState.setFirstAccountState(nextState);
                ProvisioningUtil.enableProfile(context);
            }
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

    /**
     * Enable profile anyway if we cannot receive the broadcast after certain amount time.
     */
    public static void scheduleFirstAccountReadyTimeoutAlarm(Context context, long timeout) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + timeout,
                createFirstAccountReadyTimeoutPendingIntent(context));
    }

    public static void cancelFirstAccountReadyTimeoutAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createFirstAccountReadyTimeoutPendingIntent(context));
    }

    private static PendingIntent createFirstAccountReadyTimeoutPendingIntent(Context context) {
        Intent intent = new Intent(context, FirstAccountReadyBroadcastReceiver.class);
        intent.setAction(FirstAccountReadyBroadcastReceiver.FIRST_ACCOUNT_READY_TIMEOUT_ACTION);
        return PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}