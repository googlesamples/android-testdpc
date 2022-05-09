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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@TargetApi(VERSION_CODES.P)
public class OverrideApnFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

  private static String LOG_TAG = "OverrideApnFragment";
  private static final String INSERT_OVERRIDE_APN_KEY = "insert_override_apn";
  private static final String REMOVE_OVERRIDE_APN_KEY = "remove_override_apn";
  private static final String ENABLE_OVERRIDE_APN_KEY = "enable_override_apn";

  private DevicePolicyManager mDevicePolicyManager;
  private ComponentName mAdminComponentName;

  private SwitchPreference mEnableOverrideApnPreference;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManager =
        (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
    getActivity().getActionBar().setTitle(R.string.override_apn_title);

    super.onCreate(savedInstanceState);
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.override_apn_preferences);

    findPreference(INSERT_OVERRIDE_APN_KEY).setOnPreferenceClickListener(this);
    findPreference(REMOVE_OVERRIDE_APN_KEY).setOnPreferenceClickListener(this);
    mEnableOverrideApnPreference = (SwitchPreference) findPreference(ENABLE_OVERRIDE_APN_KEY);
    mEnableOverrideApnPreference.setOnPreferenceChangeListener(this);
  }

  @Override
  public boolean isAvailable(Context context) {
    return true;
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    String key = preference.getKey();
    switch (key) {
      case INSERT_OVERRIDE_APN_KEY:
        showInsertOverrideApnDialog();
        return true;
      case REMOVE_OVERRIDE_APN_KEY:
        onRemoveOverrideApn();
        return true;
    }
    return false;
  }

  @Override
  @SuppressLint("NewApi")
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String key = preference.getKey();

    switch (key) {
      case ENABLE_OVERRIDE_APN_KEY:
        boolean enabled = (boolean) newValue;
        mDevicePolicyManager.setOverrideApnsEnabled(mAdminComponentName, enabled);
        reloadEnableOverrideApnUi();
        return true;
    }
    return false;
  }

  void setUpSpinner(View dialogView, int viewId, int textArrayId) {
    final Spinner spinner = (Spinner) dialogView.findViewById(viewId);
    final ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            getActivity(), textArrayId, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  void setUpAllSpinners(View dialogView) {
    // Set up spinner for auth type.
    setUpSpinner(dialogView, R.id.apn_auth_type, R.array.apn_auth_type_choices);
    // Set up spinner for protocol.
    setUpSpinner(dialogView, R.id.apn_protocol, R.array.apn_protocol_choices);
    // Set up spinner for roaming protocol.
    setUpSpinner(dialogView, R.id.apn_roaming_protocol, R.array.apn_protocol_choices);
    // Set up spinner for mvno type.
    setUpSpinner(dialogView, R.id.apn_mvno_type, R.array.apn_mvno_type_choices);
    // Set up spinner for carrier enabled.
    setUpSpinner(dialogView, R.id.apn_carrier_enabled, R.array.apn_carrier_enabled_choices);
  }

  void showInsertOverrideApnDialog() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }

    final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.insert_apn, null);
    final EditText entryNameEditText = (EditText) dialogView.findViewById(R.id.apn_entry_name);
    final EditText apnNameEditText = (EditText) dialogView.findViewById(R.id.apn_apn_name);
    final EditText proxyEditText = (EditText) dialogView.findViewById(R.id.apn_proxy);
    final EditText portEditText = (EditText) dialogView.findViewById(R.id.apn_port);
    final EditText mmscEditText = (EditText) dialogView.findViewById(R.id.apn_mmsc);
    final EditText mmsProxyEditText = (EditText) dialogView.findViewById(R.id.apn_mmsproxy);
    final EditText mmsPortEditText = (EditText) dialogView.findViewById(R.id.apn_mmsport);
    final EditText userEditText = (EditText) dialogView.findViewById(R.id.apn_user);
    final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.apn_password);
    final EditText typeEditText = (EditText) dialogView.findViewById(R.id.apn_type);
    final EditText numericEditText = (EditText) dialogView.findViewById(R.id.apn_numeric);
    final EditText networkBitmaskEditText =
        (EditText) dialogView.findViewById(R.id.apn_network_bitmask);
    setUpAllSpinners(dialogView);

    entryNameEditText.setHint(R.string.apn_entry_name_cannot_be_empty);
    apnNameEditText.setHint(R.string.apn_name_cannot_be_empty);
    typeEditText.setHint(R.string.apn_type_cannot_be_zero);

    numericEditText.setHint(R.string.apn_numeric_hint);

    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.insert_override_apn)
        .setView(dialogView)
        .setPositiveButton(
            android.R.string.ok,
            (dialogInterface, i) -> {
              final String entryName = entryNameEditText.getText().toString();
              if (entryName.isEmpty()) {
                showToast(R.string.apn_entry_name_cannot_be_empty);
                return;
              }
              final String apnName = apnNameEditText.getText().toString();
              if (apnName.isEmpty()) {
                showToast(R.string.apn_name_cannot_be_empty);
                return;
              }
              final int apnTypeBitmask = parseInt(typeEditText.getText().toString(), 0);
              if (apnTypeBitmask == 0) {
                showToast(R.string.apn_type_cannot_be_zero);
                return;
              }

              ApnSetting apn =
                  makeApnSetting(
                      numericEditText.getText().toString(),
                      entryName,
                      apnName,
                      inetAddressFromString(proxyEditText.getText().toString()),
                      parseInt(portEditText.getText().toString(), -1),
                      UriFromString(mmscEditText.getText().toString()),
                      inetAddressFromString(mmsProxyEditText.getText().toString()),
                      parseInt(mmsPortEditText.getText().toString(), -1),
                      userEditText.getText().toString(),
                      passwordEditText.getText().toString(),
                      // -1 here as we have extra default choice "Not specified" in the
                      // spinner of auth type, protocol, roaming protocol and mvno type
                      // in case user doesn't want to specify these fields. And
                      // "Not Specified" should be transformed into "-1" in the builder
                      // of ApnSetting.
                      ((Spinner) dialogView.findViewById(R.id.apn_auth_type))
                              .getSelectedItemPosition()
                          - 1,
                      apnTypeBitmask,
                      ((Spinner) dialogView.findViewById(R.id.apn_protocol))
                              .getSelectedItemPosition()
                          - 1,
                      ((Spinner) dialogView.findViewById(R.id.apn_roaming_protocol))
                              .getSelectedItemPosition()
                          - 1,
                      ((Spinner) dialogView.findViewById(R.id.apn_carrier_enabled))
                              .getSelectedItemPosition()
                          == 1,
                      parseInt(networkBitmaskEditText.getText().toString(), 0),
                      ((Spinner) dialogView.findViewById(R.id.apn_mvno_type))
                              .getSelectedItemPosition()
                          - 1);
              int insertedId = mDevicePolicyManager.addOverrideApn(mAdminComponentName, apn);
              if (insertedId == -1) {
                showToast(R.string.insert_override_apn_error);
              } else {
                showToast("Inserted APN id: " + insertedId);
              }
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void onRemoveOverrideApn() {
    List<ApnSetting> apnSettings = mDevicePolicyManager.getOverrideApns(mAdminComponentName);
    for (ApnSetting apn : apnSettings) {
      mDevicePolicyManager.removeOverrideApn(mAdminComponentName, apn.getId());
    }
  }

  private int parseInt(String str, int defaultValue) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private ApnSetting makeApnSetting(
      String operatorNumeric,
      String entryName,
      String apnName,
      InetAddress proxyAddress,
      int proxyPort,
      Uri mmsc,
      InetAddress mmsProxyAddress,
      int mmsProxyPort,
      String user,
      String password,
      int authType,
      int apnTypeBitmask,
      int protocol,
      int roamingProtocol,
      boolean carrierEnabled,
      int networkTypeBitmask,
      int mvnoType) {
    return new ApnSetting.Builder()
        .setOperatorNumeric(operatorNumeric)
        .setEntryName(entryName)
        .setApnName(apnName)
        .setProxyAddress(proxyAddress)
        .setProxyPort(proxyPort)
        .setMmsc(mmsc)
        .setMmsProxyAddress(mmsProxyAddress)
        .setMmsProxyPort(mmsProxyPort)
        .setUser(user)
        .setPassword(password)
        .setAuthType(authType)
        .setApnTypeBitmask(apnTypeBitmask)
        .setProtocol(protocol)
        .setRoamingProtocol(roamingProtocol)
        .setCarrierEnabled(carrierEnabled)
        .setMvnoType(mvnoType)
        .setNetworkTypeBitmask(networkTypeBitmask)
        .build();
  }

  private Uri UriFromString(String uri) {
    return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
  }

  private InetAddress inetAddressFromString(String inetAddress) {
    if (TextUtils.isEmpty(inetAddress)) {
      return null;
    }
    try {
      return InetAddress.getByName(inetAddress);
    } catch (UnknownHostException e) {
      Log.e(LOG_TAG, "Can't parse InetAddress from string: unknown host.");
      showToast(R.string.apn_wrong_inetaddress);
      return null;
    }
  }

  private void reloadEnableOverrideApnUi() {
    boolean enabled = mDevicePolicyManager.isOverrideApnEnabled(mAdminComponentName);
    if (mEnableOverrideApnPreference.isEnabled()) {
      mEnableOverrideApnPreference.setChecked(enabled);
    }
  }

  private void showToast(int msgId, Object... args) {
    showToast(getString(msgId, args), Toast.LENGTH_SHORT);
  }

  private void showToast(String msg) {
    showToast(msg, Toast.LENGTH_SHORT);
  }

  private void showToast(String msg, int duration) {
    Activity activity = getActivity();
    if (activity == null || activity.isFinishing()) {
      return;
    }
    Toast.makeText(activity, msg, duration).show();
  }
}
