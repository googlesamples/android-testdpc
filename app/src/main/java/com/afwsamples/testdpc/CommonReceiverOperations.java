/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static com.afwsamples.testdpc.policy.PolicyManagementFragment.OVERRIDE_KEY_SELECTION_KEY;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.afwsamples.testdpc.common.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A class that implements common logic to handle direct and delegated admin callbacks
 * from DeviceAdminReceiver and DelegatedAdminReceiver.
 */
public class CommonReceiverOperations {
    public static final String NETWORK_LOGS_FILE_PREFIX = "network_logs_";

    private static final String TAG = "AdminReceiver";

    public static String onChoosePrivateKeyAlias(Context context, int uid) {
        if (uid == Process.myUid()) {
            // Always show the chooser if we were the one requesting the cert.
            return null;
        }

        String chosenAlias = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OVERRIDE_KEY_SELECTION_KEY, null);
        if (!TextUtils.isEmpty(chosenAlias)) {
            showToast(context, "Substituting private key alias: \"" + chosenAlias + "\"");
            return chosenAlias;
        } else {
            return null;
        }
    }

    @TargetApi(VERSION_CODES.O)
    public static void onNetworkLogsAvailable(Context context, ComponentName admin, long batchToken,
                                              int networkLogsCount) {
        Log.i(TAG, "onNetworkLogsAvailable(), batchToken: " + batchToken
                + ", event count: " + networkLogsCount);

        DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<NetworkEvent> events = null;
        try {
            events = dpm.retrieveNetworkLogs(admin, batchToken);
        } catch (SecurityException e) {
            Log.e(TAG,
                    "Exception while retrieving network logs batch with batchToken: " + batchToken
                    , e);
        }

        if (events == null) {
            Log.e(TAG, "Failed to retrieve network logs batch with batchToken: " + batchToken);
            showToast(context, context.getString(
                    R.string.on_network_logs_available_token_failure, batchToken));
            return;
        }

        showToast(context,
                context.getString(R.string.on_network_logs_available_success, batchToken));

        ArrayList<String> loggedEvents = new ArrayList<>();
        if (Util.SDK_INT >= VERSION_CODES.P) {
            for (NetworkEvent event : events) {
                loggedEvents.add(event.toString());
            }
        } else {
            events.forEach(event -> loggedEvents.add(event.toString()));
        }
        new EventSavingTask(context, batchToken, loggedEvents).execute();
    }

    private static class EventSavingTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private long mBatchToken;
        private List<String> mLoggedEvents;

        public EventSavingTask(Context context, long batchToken, ArrayList<String> loggedEvents) {
            mContext = context;
            mBatchToken = batchToken;
            mLoggedEvents = loggedEvents;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Date timestamp = new Date();
            String filename = NETWORK_LOGS_FILE_PREFIX + mBatchToken + "_" + timestamp.getTime()
                    + ".txt";
            File file = new File(mContext.getExternalFilesDir(null), filename);
            try (OutputStream os = new FileOutputStream(file)) {
                for (String event : mLoggedEvents) {
                    os.write((event + "\n").getBytes());
                }
                Log.d(TAG, "Saved network logs to file: " + filename);
            } catch (IOException e) {
                Log.e(TAG, "Failed saving network events to file" + filename, e);
            }
            return null;
        }
    }

    private static void showToast(Context context, String message) {
        final String appName = context.getString(R.string.app_name);
        Toast.makeText(context, String.format("[%s] %s", appName, message),
                Toast.LENGTH_LONG)
                .show();
    }

}
