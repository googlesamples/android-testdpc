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
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.ReflectionUtil.ReflectionIsTemporaryException;
import com.afwsamples.testdpc.common.Util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.IllegalAccessException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@TargetApi(VERSION_CODES.N)
public class SecurityLogsFragment extends ListFragment {
  private static final String TAG = "ProcessLogsFragment";

  private static final String PRE_REBOOT_KEY = "pre-reboot";

  private static Map<Integer, String> sTagNames = getTagNames();

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
          sb.append(event.getId() + ": ");
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

  private static Map<Integer, String> getTagNames() {
    Map<Integer, String> tagNames = new ArrayMap<Integer, String>();
    for (Field f : SecurityLog.class.getDeclaredFields()) {
      if (f.getName().startsWith("TAG_") && ((f.getModifiers() & Modifier.PUBLIC) > 0)) {
        try {
          tagNames.put(f.getInt(null), f.getName().substring(4));
        } catch (IllegalAccessException e) {
          Log.e(TAG, "Failed to read field " + f, e);
        }
      }
    }
    return tagNames;
  }

  public static String getStringEventTagFromId(int eventId) {
    return sTagNames.getOrDefault(eventId, "UNKNOWN(" + eventId + ")");
  }

  public static void printData(StringBuilder sb, Object data) {
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
