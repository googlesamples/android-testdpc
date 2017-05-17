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
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.provision.CheckInState;
import com.afwsamples.testdpc.provision.ProvisioningUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Receiver for FIRST_ACCOUNT_READY_ACTION from Google Play Service.
 * Receiver only matters for Managed Profile flow, so we ignore the broadcast in other cases.
 */
public class FirstAccountReadyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "FirstAccountReady";

    private static final String FIRST_ACCOUNT_READY_ACTION =
            "com.google.android.work.action.FIRST_ACCOUNT_READY";

    public static final String FIRST_ACCOUNT_READY_TIMEOUT_ACTION =
            "com.afwsamples.testdpc.FIRST_ACCOUNT_READY_TIMEOUT";

    private static final Set<String> SUPPORTED_ACTIONS = new HashSet<>(
            Arrays.asList(FIRST_ACCOUNT_READY_ACTION, FIRST_ACCOUNT_READY_TIMEOUT_ACTION));

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received: " + action);

        if (!Util.isManagedProfileOwner(context)) {
            Log.d(TAG, "Not a Managed Profile case. Ignoring broadcast.");
            return;
        }

        if (!SUPPORTED_ACTIONS.contains(action)) {
            Log.d(TAG, String.format("Action %s not supported by receiver %s. Ignoring broadcast.",
                    action, getClass().getName()));
            return;
        }

        CheckInState checkInState = new CheckInState(context);
        if (checkInState.getFirstAccountState() == CheckInState.FIRST_ACCOUNT_STATE_PENDING) {
            checkInState.setFirstAccountState(FIRST_ACCOUNT_READY_ACTION.equals(action)
                    ? CheckInState.FIRST_ACCOUNT_STATE_READY
                    : CheckInState.FIRST_ACCOUNT_STATE_TIMEOUT);

            ProvisioningUtil.enableProfile(context);
        }
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