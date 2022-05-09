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

package com.afwsamples.testdpc.policy;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.ReflectionUtil.ReflectionIsTemporaryException;
import com.afwsamples.testdpc.common.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@TargetApi(VERSION_CODES.N)
public class SecurityLogsFragment extends ListFragment {
  private static final String TAG = "ProcessLogsFragment";

  private static final String PRE_REBOOT_KEY = "pre-reboot";

  private final ArrayList<String> mLogs = new ArrayList<>();
  private ArrayAdapter<String> mAdapter;

  private DevicePolicyManager mDevicePolicyManager;
  private ComponentName mAdminName;
  private boolean mPreReboot;

  public static SecurityLogsFragment newInstance(boolean preReboot) {
    final SecurityLogsFragment fragment = new SecurityLogsFragment();
    final Bundle args = new Bundle();
    args.putBoolean(PRE_REBOOT_KEY, preReboot);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAdminName = DeviceAdminReceiver.getComponentName(getActivity());
    mDevicePolicyManager =
        (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    mAdapter =
        new ArrayAdapter<>(
            getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mLogs);
    mPreReboot = getArguments().getBoolean(PRE_REBOOT_KEY);
    setListAdapter(mAdapter);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mAdapter.add(getString(R.string.security_logs_retrieved_message, new Date().toString()));
    try {
      processEvents(getLogs());
    } catch (SecurityException e) {
      Log.e(TAG, "Exception thrown when trying to retrieve security logs", e);
      mAdapter.add(getString(R.string.exception_retrieving_security_logs));
    }
  }

  private boolean hasSecurityLoggingDelegation() {
    if (Util.SDK_INT < VERSION_CODES.S) {
      return false;
    }

    final String packageName = getActivity().getPackageName();
    List<String> delegations = mDevicePolicyManager.getDelegatedScopes(null, packageName);

    String securityLoggingDelegation = null;
    try {
      securityLoggingDelegation =
          ReflectionUtil.stringConstant(DevicePolicyManager.class, "DELEGATION_SECURITY_LOGGING");
    } catch (ReflectionIsTemporaryException e) {
      Log.w(TAG, "Failed to read DevicePolicyManager.DELEGATION_SECURITY_LOGGING", e);
    }

    return securityLoggingDelegation != null && delegations.contains(securityLoggingDelegation);
  }

  private List<SecurityEvent> getLogs() {
    // If the app has the security logging delegation then the component name is
    // not passed in.
    ComponentName name = hasSecurityLoggingDelegation() ? null : mAdminName;
    return mPreReboot
        ? mDevicePolicyManager.retrievePreRebootSecurityLogs(name)
        : mDevicePolicyManager.retrieveSecurityLogs(name);
  }

  @SuppressWarnings("SimpleDateFormat")
  private void processEvents(List<SecurityEvent> logs) {
    if (logs == null) {
      Log.w(TAG, "logs == null, are you polling too early?");
      final String message =
          getString(
              mPreReboot
                  ? R.string.failed_to_retrieve_pre_reboot_security_logs
                  : R.string.failed_to_retrieve_security_logs);
      mAdapter.add(message);
    } else {
      SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
      Log.d(TAG, "Incoming logs size: " + logs.size());
      for (SecurityEvent event : logs) {
        StringBuilder sb = new StringBuilder();
        if (Util.SDK_INT >= VERSION_CODES.P) {
          sb.append(getEventId(event) + ": ");
        }
        sb.append(getStringEventTagFromId(event.getTag()));
        sb.append(" (")
            .append(formatter.format(new Date(TimeUnit.NANOSECONDS.toMillis(event.getTimeNanos()))))
            .append("): ");
        printData(sb, event.getData());
        mAdapter.add(sb.toString());
      }
      ListView listView = SecurityLogsFragment.this.getListView();
      listView.setSelection(listView.getCount() - 1);
    }
  }

  @TargetApi(VERSION_CODES.P)
  private long getEventId(SecurityEvent event) {
    return event.getId();
  }

  private String getStringEventTagFromId(int eventId) {
    final String eventTag;
    switch (eventId) {
      case SecurityLog.TAG_ADB_SHELL_INTERACTIVE:
        eventTag = "ADB_SHELL_INTERACTIVE";
        break;
      case SecurityLog.TAG_ADB_SHELL_CMD:
        eventTag = "ADB_SHELL_CMD";
        break;
      case SecurityLog.TAG_SYNC_RECV_FILE:
        eventTag = "SYNC_RECV_FILE";
        break;
      case SecurityLog.TAG_SYNC_SEND_FILE:
        eventTag = "SYNC_SEND_FILE";
        break;
      case SecurityLog.TAG_APP_PROCESS_START:
        eventTag = "APP_PROCESS_START";
        break;
      case SecurityLog.TAG_KEYGUARD_DISMISSED:
        eventTag = "KEYGUARD_DISMISSED";
        break;
      case SecurityLog.TAG_KEYGUARD_DISMISS_AUTH_ATTEMPT:
        eventTag = "KEYGUARD_DISMISS_AUTH_ATTEMPT";
        break;
      case SecurityLog.TAG_KEYGUARD_SECURED:
        eventTag = "KEYGUARD_SECURED";
        break;
      case SecurityLog.TAG_OS_STARTUP:
        eventTag = "OS_STARTUP";
        break;
      case SecurityLog.TAG_OS_SHUTDOWN:
        eventTag = "OS_SHUTDOWN";
        break;
      case SecurityLog.TAG_LOGGING_STARTED:
        eventTag = "LOGGING_STARTED";
        break;
      case SecurityLog.TAG_LOGGING_STOPPED:
        eventTag = "LOGGING_STOPPED";
        break;
      case SecurityLog.TAG_MEDIA_MOUNT:
        eventTag = "MEDIA_MOUNT";
        break;
      case SecurityLog.TAG_MEDIA_UNMOUNT:
        eventTag = "MEDIA_UNMOUNT";
        break;
      case SecurityLog.TAG_LOG_BUFFER_SIZE_CRITICAL:
        eventTag = "LOG_BUFFER_SIZE_CRITICAL";
        break;
      case SecurityLog.TAG_PASSWORD_EXPIRATION_SET:
        eventTag = "PASSWORD_EXPIRATION_SET";
        break;
      case SecurityLog.TAG_PASSWORD_COMPLEXITY_SET:
        eventTag = "PASSWORD_COMPLEXITY_SET";
        break;
      case SecurityLog.TAG_PASSWORD_HISTORY_LENGTH_SET:
        eventTag = "PASSWORD_HISTORY_LENGTH_SET";
        break;
      case SecurityLog.TAG_MAX_SCREEN_LOCK_TIMEOUT_SET:
        eventTag = "MAX_SCREEN_LOCK_TIMEOUT_SET";
        break;
      case SecurityLog.TAG_MAX_PASSWORD_ATTEMPTS_SET:
        eventTag = "MAX_PASSWORD_ATTEMPTS_SET";
        break;
      case SecurityLog.TAG_KEYGUARD_DISABLED_FEATURES_SET:
        eventTag = "KEYGUARD_DISABLED_FEATURES_SET";
        break;
      case SecurityLog.TAG_REMOTE_LOCK:
        eventTag = "REMOTE_LOCK";
        break;
      case SecurityLog.TAG_WIPE_FAILURE:
        eventTag = "WIPE_FAILURE";
        break;
      case SecurityLog.TAG_KEY_GENERATED:
        eventTag = "KEY_GENERATED";
        break;
      case SecurityLog.TAG_KEY_IMPORT:
        eventTag = "KEY_IMPORT";
        break;
      case SecurityLog.TAG_KEY_DESTRUCTION:
        eventTag = "KEY_DESTRUCTION";
        break;
      case SecurityLog.TAG_USER_RESTRICTION_ADDED:
        eventTag = "USER_RESTRICTION_ADDED";
        break;
      case SecurityLog.TAG_USER_RESTRICTION_REMOVED:
        eventTag = "USER_RESTRICTION_REMOVED";
        break;
      case SecurityLog.TAG_CERT_AUTHORITY_INSTALLED:
        eventTag = "CERT_AUTHORITY_INSTALLED";
        break;
      case SecurityLog.TAG_CERT_AUTHORITY_REMOVED:
        eventTag = "CERT_AUTHORITY_REMOVED";
        break;
      case SecurityLog.TAG_CRYPTO_SELF_TEST_COMPLETED:
        eventTag = "CRYPTO_SELF_TEST_COMPLETED";
        break;
      case SecurityLog.TAG_KEY_INTEGRITY_VIOLATION:
        eventTag = "KEY_INTEGRITY_VIOLATION";
        break;
      case SecurityLog.TAG_CERT_VALIDATION_FAILURE:
        eventTag = "CERT_VALIDATION_FAILURE";
        break;
      default:
        eventTag = "UNKNOWN(" + eventId + ")";
    }
    return eventTag;
  }

  private void printData(StringBuilder sb, Object data) {
    if (data instanceof Integer
        || data instanceof Long
        || data instanceof Float
        || data instanceof String) {
      sb.append(data.toString()).append(" ");
    } else if (data instanceof Object[]) {
      for (Object item : (Object[]) data) {
        printData(sb, item);
      }
    }
  }
}
