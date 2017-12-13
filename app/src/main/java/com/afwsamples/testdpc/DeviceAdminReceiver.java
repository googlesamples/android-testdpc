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

package com.afwsamples.testdpc;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afwsamples.testdpc.common.NotificationUtil;
import com.afwsamples.testdpc.provision.PostProvisioningTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.afwsamples.testdpc.policy.PolicyManagementFragment.OVERRIDE_KEY_SELECTION_KEY;

/**
 * Handles events related to the managed profile.
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    private static final String TAG = "DeviceAdminReceiver";

    public static final String ACTION_PASSWORD_REQUIREMENTS_CHANGED =
            "com.afwsamples.testdpc.policy.PASSWORD_REQUIREMENTS_CHANGED";

    private static final String LOGS_DIR = "logs";

    private static final String FAILED_PASSWORD_LOG_FILE =
            "failed_pw_attempts_timestamps.log";

    private static final int CHANGE_PASSWORD_NOTIFICATION_ID = 101;
    private static final int PASSWORD_FAILED_NOTIFICATION_ID = 102;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_PASSWORD_REQUIREMENTS_CHANGED:
            case Intent.ACTION_BOOT_COMPLETED:
                updatePasswordQualityNotification(context);
                break;
            default:
               super.onReceive(context, intent);
               break;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onSecurityLogsAvailable(Context context, Intent intent) {
        Log.i(TAG, "onSecurityLogsAvailable() called");
        Toast.makeText(context,
                context.getString(R.string.on_security_logs_available),
                Toast.LENGTH_LONG)
                .show();
    }


    /*
     * TODO: reconsider how to store and present the logs in the future, e.g. save the file into
     * internal memory and show the content in a ListView
     */
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken,
            int networkLogsCount) {
        Log.i(TAG, "onNetworkLogsAvailable(), batchToken: " + batchToken
                + ", event count: " + networkLogsCount);

        DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        List<NetworkEvent> events = null;
        try {
            events = dpm.retrieveNetworkLogs(getComponentName(context), batchToken);
        } catch (SecurityException e) {
            Log.e(TAG,
                "Exception while retrieving network logs batch with batchToken: " + batchToken, e);
        }

        if (events == null) {
            Log.e(TAG, "Failed to retrieve network logs batch with batchToken: " + batchToken);
            Toast.makeText(context,
                    context.getString(R.string.on_network_logs_available_failure, batchToken),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Toast.makeText(context,
                context.getString(R.string.on_network_logs_available_success, batchToken),
                Toast.LENGTH_LONG)
                .show();

        ArrayList<String> loggedEvents = new ArrayList<String>();
        events.forEach(event -> loggedEvents.add(event.toString()));
        new EventSavingTask(context, loggedEvents).execute();
    }

    private static class EventSavingTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private List<String> mLoggedEvents;

        public EventSavingTask(Context context, ArrayList<String> loggedEvents) {
            mContext = context;
            mLoggedEvents = loggedEvents;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String filename = "network_logs_"
                    + new Date().toString().replaceAll("\\s+","_") + ".txt";
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

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        PostProvisioningTask task = new PostProvisioningTask(context);
        if (!task.performPostProvisioningOperations(intent)) {
            return;
        }

        Intent launchIntent = task.getPostProvisioningLaunchIntent(intent);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        } else {
            Log.e(TAG, "DeviceAdminReceiver.onProvisioningComplete() invoked, but ownership "
                    + "not assigned");
            Toast.makeText(context, R.string.device_admin_receiver_failure, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onBugreportSharingDeclined(Context context, Intent intent) {
        Log.i(TAG, "Bugreport sharing declined");
        NotificationUtil.showNotification(context, R.string.bugreport_title,
                context.getString(R.string.bugreport_sharing_declined),
                NotificationUtil.BUGREPORT_NOTIFICATION_ID);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onBugreportShared(final Context context, Intent intent,
            final String bugreportFileHash) {
        Log.i(TAG, "Bugreport shared, hash: " + bugreportFileHash);
        final Uri bugreportUri = intent.getData();

        final PendingResult result = goAsync();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                File outputBugreportFile;
                String message;
                InputStream in;
                OutputStream out;
                try {
                    ParcelFileDescriptor mInputPfd = context.getContentResolver()
                            .openFileDescriptor(bugreportUri, "r");
                    in = new FileInputStream(mInputPfd.getFileDescriptor());
                    outputBugreportFile = new File(context.getExternalFilesDir(null),
                            bugreportUri.getLastPathSegment());
                    out = new FileOutputStream(outputBugreportFile);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.close();
                    message = context.getString(R.string.received_bugreport,
                            outputBugreportFile.getPath(), bugreportFileHash);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    message = context.getString(R.string.received_bugreport_failed_retrieval);
                }
                return message;
            }

            @Override
            protected void onPostExecute(String message) {
                NotificationUtil.showNotification(context, R.string.bugreport_title,
                        message, NotificationUtil.BUGREPORT_NOTIFICATION_ID);
                result.finish();
            }

        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onBugreportFailed(Context context, Intent intent, int failureCode) {
        String failureReason;
        switch (failureCode) {
            case BUGREPORT_FAILURE_FILE_NO_LONGER_AVAILABLE:
                failureReason = context.getString(
                        R.string.bugreport_failure_file_no_longer_available);
                break;
            case BUGREPORT_FAILURE_FAILED_COMPLETING:
                //fall through
            default:
                failureReason = context.getString(
                        R.string.bugreport_failure_failed_completing);
        }
        Log.i(TAG, "Bugreport failed: " + failureReason);
        NotificationUtil.showNotification(context, R.string.bugreport_title,
                context.getString(R.string.bugreport_failure_message, failureReason),
                NotificationUtil.BUGREPORT_NOTIFICATION_ID);
    }


    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onUserAdded(Context context, Intent intent, UserHandle newUser) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        String message = context.getString(R.string.on_user_added_message,
                userManager.getSerialNumberForUser(newUser));
        Log.i(TAG, message);
        NotificationUtil.showNotification(context, R.string.on_user_added_title,
                message,
                NotificationUtil.USER_ADDED_NOTIFICATION_ID);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onUserRemoved(Context context, Intent intent, UserHandle removedUser) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        String message = context.getString(R.string.on_user_removed_message,
                userManager.getSerialNumberForUser(removedUser));
        Log.i(TAG, message);
        NotificationUtil.showNotification(context, R.string.on_user_removed_title, message,
                NotificationUtil.USER_REMOVED_NOTIFICATION_ID);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
        if (receivedTime != -1) {
            DateFormat sdf = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");
            String timeString = sdf.format(new Date(receivedTime));
            Toast.makeText(context, "System update received at: " + timeString,
                    Toast.LENGTH_LONG).show();
        } else {
            // No system update is currently available on this device.
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri,
            String alias) {
        if (uid == Process.myUid()) {
            // Always show the chooser if we were the one requesting the cert.
            return null;
        }

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

    @Deprecated
    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        onPasswordExpiring(context, intent, Process.myUserHandle());
    }

    @TargetApi(Build.VERSION_CODES.O)
    // @Override
    public void onPasswordExpiring(Context context, Intent intent, UserHandle user) {
        if (!Process.myUserHandle().equals(user)) {
            // This password expiration was on another user, for example a parent profile. Skip it.
            return;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        final long timeNow = System.currentTimeMillis();
        final long timeAdminExpires =
                devicePolicyManager.getPasswordExpiration(getComponentName(context));
        final boolean expiredBySelf = (timeNow >= timeAdminExpires && timeAdminExpires != 0);

        NotificationUtil.showNotification(context, R.string.password_expired_title,
                context.getString(expiredBySelf
                        ? R.string.password_expired_by_self
                        : R.string.password_expired_by_others),
                NotificationUtil.PASSWORD_EXPIRATION_NOTIFICATION_ID);
    }

    @Deprecated
    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        onPasswordFailed(context, intent, Process.myUserHandle());
    }

    @TargetApi(Build.VERSION_CODES.O)
    // @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle user) {
        if (!Process.myUserHandle().equals(user)) {
            // This password failure was on another user, for example a parent profile. Ignore it.
            return;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        /*
         * Post a notification to show:
         *  - how many wrong passwords have been entered;
         *  - how many wrong passwords need to be entered for the device to be wiped.
         */
        int attempts = devicePolicyManager.getCurrentFailedPasswordAttempts();
        int maxAttempts = devicePolicyManager.getMaximumFailedPasswordsForWipe(null);

        String title = context.getResources().getQuantityString(
                R.plurals.password_failed_attempts_title, attempts, attempts);

        ArrayList<Date> previousFailedAttempts = getFailedPasswordAttempts(context);
        Date date = new Date();
        previousFailedAttempts.add(date);
        Collections.sort(previousFailedAttempts, Collections.<Date>reverseOrder());
        try {
            saveFailedPasswordAttempts(context, previousFailedAttempts);
        } catch (IOException e) {
            Log.e(TAG, "Unable to save failed password attempts", e);
        }

        String content = maxAttempts == 0
                ? context.getString(R.string.password_failed_no_limit_set)
                : context.getResources().getQuantityString(
                        R.plurals.password_failed_attempts_content, maxAttempts, maxAttempts);

        NotificationCompat.Builder warn = NotificationUtil.getNotificationBuilder(context);
        warn.setSmallIcon(R.drawable.ic_launcher)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(PendingIntent.getActivity(context, /* requestCode */ -1,
                        new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD), /* flags */ 0));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);

        final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        for(Date d : previousFailedAttempts) {
            inboxStyle.addLine(dateFormat.format(d));
        }
        warn.setStyle(inboxStyle);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(PASSWORD_FAILED_NOTIFICATION_ID, warn.build());
    }

    @Deprecated
    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        onPasswordSucceeded(context, intent, Process.myUserHandle());
    }

    @TargetApi(Build.VERSION_CODES.O)
    // @Override
    public void onPasswordSucceeded(Context context, Intent intent, UserHandle user) {
        if (Process.myUserHandle().equals(user)) {
            logFile(context).delete();
        }
    }

    @Deprecated
    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        onPasswordChanged(context, intent, Process.myUserHandle());
    }

    @TargetApi(Build.VERSION_CODES.O)
    // @Override
    public void onPasswordChanged(Context context, Intent intent, UserHandle user) {
        if (Process.myUserHandle().equals(user)) {
            updatePasswordQualityNotification(context);
        }
    }


    private static File logFile(Context context) {
        File parent = context.getDir(LOGS_DIR, Context.MODE_PRIVATE);
        return new File(parent, FAILED_PASSWORD_LOG_FILE);
    }

    private static ArrayList<Date> getFailedPasswordAttempts(Context context) {
        File logFile = logFile(context);
        ArrayList<Date> result = new ArrayList<Date>();

        if(!logFile.exists()) {
            return result;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(logFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null && line.length() > 0) {
                result.add(new Date(Long.parseLong(line)));
            }

            br.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to read failed password attempts", e);
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to close failed password attempts log file", e);
                }
            }
        }

        return result;
    }

    private static void saveFailedPasswordAttempts(Context context, ArrayList<Date> attempts)
            throws IOException {
        File logFile = logFile(context);

        if(!logFile.exists()) {
            logFile.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(logFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for(Date date : attempts) {
            bw.write(Long.toString(date.getTime()));
            bw.newLine();
        }

        bw.close();
    }

    private static void updatePasswordQualityNotification(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        if (!devicePolicyManager.isProfileOwnerApp(context.getPackageName())
                && !devicePolicyManager.isDeviceOwnerApp(context.getPackageName())) {
            // Only try to update the notification if we are a profile or device owner.
            return;
        }

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!devicePolicyManager.isActivePasswordSufficient()) {
            NotificationCompat.Builder warn = NotificationUtil.getNotificationBuilder(context);
            warn.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setTicker(context.getText(R.string.password_not_compliant_title))
                    .setContentTitle(context.getText(R.string.password_not_compliant_title))
                    .setContentText(context.getText(R.string.password_not_compliant_content))
                    .setContentIntent(PendingIntent.getActivity(context, /*requestCode*/ -1,
                            new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD), /*flags*/ 0));
            nm.notify(CHANGE_PASSWORD_NOTIFICATION_ID, warn.getNotification());
        } else {
            nm.cancel(CHANGE_PASSWORD_NOTIFICATION_ID);
        }
    }
}
