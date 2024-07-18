/*
 * Copyright (C) 2024 The Android Open Source Project
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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.ReflectionUtil.ReflectionIsTemporaryException;
import com.afwsamples.testdpc.common.preference.DpcPreference;
import java.util.Set;

/** Fragment to control eSIMs. */
@TargetApi(VERSION_CODES.VANILLA_ICE_CREAM)
public class EsimControlFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
  private static final String TAG = EsimControlFragment.class.getSimpleName();
  private static final String DOWNLOAD_ESIM = "download_esim";
  private static final String DELETE_ESIM = "delete_esim";
  private static final String GET_MANAGED_ESIM = "get_managed_esim";
  private static final String ACTION_DOWNLOAD_ESIM = "com.afwsamples.testdpc.esim_download";
  private static final String ACTION_DELETE_ESIM = "com.afwsamples.testdpc.esim_delete";

  private DpcPreference mDownloadEsimPreference;
  private DpcPreference mDeleteEsimPreference;
  private DpcPreference mGetManagedEsimPreference;
  private DevicePolicyManager mDevicePolicyManager;
  private EuiccManager mEuiccManager;

  private String getResultText(int resultCode) {
    if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK) {
      return "EMBEDDED_SUBSCRIPTION_RESULT_OK";
    } else if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR) {
      return "EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR";
    } else if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_ERROR) {
      return "EMBEDDED_SUBSCRIPTION_RESULT_ERROR";
    }
    return "Uknown: " + resultCode;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManager = getActivity().getSystemService(DevicePolicyManager.class);
    mEuiccManager = getActivity().getSystemService(EuiccManager.class);
    getActivity().getActionBar().setTitle(R.string.manage_esim);
    super.onCreate(savedInstanceState);
  }

  private BroadcastReceiver mDownloadESIMReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

          if (!ACTION_DOWNLOAD_ESIM.equals(intent.getAction())) {
            return;
          }
          int detailedCode =
              intent.getIntExtra(EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1);
          int errorCode =
              intent.getIntExtra(EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_ERROR_CODE, -1);
          int resultCode = getResultCode();

          Log.v(
              TAG,
              "Download result: resultCode: "
                  + getResultText(resultCode)
                  + " detailedCode: "
                  + resultCode
                  + " detailedCode: "
                  + detailedCode
                  + " errorCode: "
                  + errorCode);
          showToast("Download result: " + getResultText(resultCode), Toast.LENGTH_LONG);
          if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR) {
            try {
              mEuiccManager.startResolutionActivity(
                  getActivity(),
                  resultCode,
                  intent,
                  PendingIntent.getBroadcast(
                      getActivity(),
                      0,
                      new Intent(ACTION_DOWNLOAD_ESIM),
                      PendingIntent.FLAG_MUTABLE
                          | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT));
            } catch (Exception e) {
              Log.e(TAG, "Failed to start resolution activity", e);
            }
            return;
          }
          getActivity().unregisterReceiver(mDownloadESIMReceiver);
        }
      };

  private BroadcastReceiver mDeleteESIMReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (!ACTION_DELETE_ESIM.equals(intent.getAction())) {
            return;
          }
          int detailedCode =
              intent.getIntExtra(EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1);
          Log.v(
              TAG,
              "Delete result: resultCode: "
                  + getResultText(getResultCode())
                  + " detailedCode: "
                  + detailedCode);

          showToast("Delete result: " + getResultText(getResultCode()), Toast.LENGTH_LONG);
          getActivity().unregisterReceiver(mDeleteESIMReceiver);
        }
      };

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.esim_control_preferences);

    mDownloadEsimPreference = (DpcPreference) findPreference(DOWNLOAD_ESIM);
    mDownloadEsimPreference.setOnPreferenceClickListener(this);

    mDeleteEsimPreference = (DpcPreference) findPreference(DELETE_ESIM);
    mDeleteEsimPreference.setOnPreferenceClickListener(this);

    mGetManagedEsimPreference = (DpcPreference) findPreference(GET_MANAGED_ESIM);
    mGetManagedEsimPreference.setOnPreferenceClickListener(this);
  }

  @Override
  public boolean isAvailable(Context context) {
    return true;
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    return false;
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    String key = preference.getKey();

    switch (key) {
      case DOWNLOAD_ESIM:
        showDownloadEsimUi();
        return true;
      case DELETE_ESIM:
        showDeleteEsimUi();
        return true;
      case GET_MANAGED_ESIM:
        showManagedEsimUi();
        return true;
    }
    return false;
  }

  private void showManagedEsimUi() {
    Set<Integer> managedSubIds = getSubscriptionIds();
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.get_managed_esim_dialog_title)
        .setItems(managedSubIds.stream().map(String::valueOf).toArray(String[]::new), null)
        .show();
  }

  private Set<Integer> getSubscriptionIds() {
    try {
      // TODO: remove reflection code and call directly once V is released.
      return ReflectionUtil.invoke(mDevicePolicyManager, "getSubscriptionIds");
    } catch (ReflectionIsTemporaryException e) {
      Log.e(TAG, "Error invoking getSubscriptionIds", e);
      showToast("Error getting managed esim information.", Toast.LENGTH_LONG);
    }
    return null;
  }

  private void showToast(String msg, int duration) {
    Activity activity = getActivity();
    if (activity == null || activity.isFinishing()) {
      Log.w(TAG, "Not toasting '" + msg + "' as activity is finishing or finished");
      return;
    }
    Log.d(TAG, "Showing toast: " + msg);
    Toast.makeText(activity, msg, duration).show();
  }

  private void showDownloadEsimUi() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }

    final View dialogView =
        getActivity().getLayoutInflater().inflate(R.layout.esim_dialog_layout, null);
    final EditText activationCodeEditText =
        (EditText) dialogView.findViewById(R.id.activation_code);
    final CheckBox activateAfterDownloadCheckBox =
        (CheckBox) dialogView.findViewById(R.id.activate_esim);

    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.esim_activation_code)
        .setView(dialogView)
        .setPositiveButton(
            android.R.string.ok,
            (dialogInterface, i) -> {
              final String activationCodeString = activationCodeEditText.getText().toString();
              startEsimDownload(activationCodeString, activateAfterDownloadCheckBox.isChecked());
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void showDeleteEsimUi() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }

    final View dialogView =
        getActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
    final EditText subIdEditText = dialogView.findViewById(R.id.input);

    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.delete_esim_dialog_title)
        .setView(dialogView)
        .setPositiveButton(
            android.R.string.ok,
            (dialogInterface, i) -> {
              final String subId = subIdEditText.getText().toString();
              deleteEsim(Integer.parseInt(subId));
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void startEsimDownload(String activationCode, boolean switchAfterDownload) {
    ContextCompat.registerReceiver(
        getActivity(),
        mDownloadESIMReceiver,
        new IntentFilter(ACTION_DOWNLOAD_ESIM),
        ContextCompat.RECEIVER_EXPORTED);
    DownloadableSubscription downloadableSubscription =
        DownloadableSubscription.forActivationCode(activationCode);
    PendingIntent pi =
        PendingIntent.getBroadcast(
            getActivity(),
            0,
            new Intent(ACTION_DOWNLOAD_ESIM),
            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
    mEuiccManager.downloadSubscription(downloadableSubscription, switchAfterDownload, pi);
    Log.v(
        TAG,
        "started downloading eSIM, "
            + "activationCode : "
            + activationCode
            + ", switchAfterDownload : "
            + switchAfterDownload);
    showToast("started downloading eSIM", Toast.LENGTH_LONG);
  }

  private void deleteEsim(int subId) {
    ContextCompat.registerReceiver(
        getActivity(),
        mDeleteESIMReceiver,
        new IntentFilter(ACTION_DELETE_ESIM),
        ContextCompat.RECEIVER_EXPORTED);
    PendingIntent pi =
        PendingIntent.getBroadcast(
            getActivity(),
            0,
            new Intent(ACTION_DELETE_ESIM),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    mEuiccManager.deleteSubscription(subId, pi);

    showToast("started deleting eSIM", Toast.LENGTH_LONG);
  }
}
