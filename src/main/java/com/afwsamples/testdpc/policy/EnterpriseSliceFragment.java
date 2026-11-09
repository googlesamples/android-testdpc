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
import android.app.admin.DevicePolicyManager;
import android.app.admin.PreferentialNetworkServiceConfig;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.Preference;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.preference.DpcEditTextPreference;
import com.afwsamples.testdpc.common.preference.DpcPreference;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(VERSION_CODES.Q)
public class EnterpriseSliceFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceClickListener {

  private static String TAG = "EnterpriseSliceFragment";
  private static final String ENTERPRISE_SLICE_1_ENABLE =
      "enterprise_slice_1_enable";
  private static final String ENTERPRISE_SLICE_1_ALLOW_FALLBACK_TO_DEFAULT_KEY =
      "enterprise_slice_1_allow_fallback_to_default";
  private static final String ENTERPRISE_SLICE_1_INCLUDED_APP_KEY =
      "enterprise_slice_1_included_apps";
  private static final String ENTERPRISE_SLICE_1_EXCLUDED_APP_KEY =
      "enterprise_slice_1_excluded_apps";

  private static final String ENTERPRISE_SLICE_2_ENABLE =
      "enterprise_slice_2_enable";
  private static final String ENTERPRISE_SLICE_2_ALLOW_FALLBACK_TO_DEFAULT_KEY =
      "enterprise_slice_2_allow_fallback_to_default";
  private static final String ENTERPRISE_SLICE_2_INCLUDED_APP_KEY =
      "enterprise_slice_2_included_apps";
  private static final String ENTERPRISE_SLICE_2_EXCLUDED_APP_KEY =
      "enterprise_slice_2_excluded_apps";

  private static final String ENTERPRISE_SLICE_3_ENABLE =
      "enterprise_slice_3_enable";
  private static final String ENTERPRISE_SLICE_3_ALLOW_FALLBACK_TO_DEFAULT_KEY =
      "enterprise_slice_3_allow_fallback_to_default";
  private static final String ENTERPRISE_SLICE_3_INCLUDED_APP_KEY =
      "enterprise_slice_3_included_apps";
  private static final String ENTERPRISE_SLICE_3_EXCLUDED_APP_KEY =
      "enterprise_slice_3_excluded_apps";


  private static final String ENTERPRISE_SLICE_4_ENABLE =
      "enterprise_slice_4_enable";
  private static final String ENTERPRISE_SLICE_4_ALLOW_FALLBACK_TO_DEFAULT_KEY =
      "enterprise_slice_4_allow_fallback_to_default";
  private static final String ENTERPRISE_SLICE_4_INCLUDED_APP_KEY =
      "enterprise_slice_4_included_apps";
  private static final String ENTERPRISE_SLICE_4_EXCLUDED_APP_KEY =
      "enterprise_slice_4_excluded_apps";


  private static final String ENTERPRISE_SLICE_5_ENABLE =
      "enterprise_slice_5_enable";
  private static final String ENTERPRISE_SLICE_5_ALLOW_FALLBACK_TO_DEFAULT_KEY =
      "enterprise_slice_5_allow_fallback_to_default";
  private static final String ENTERPRISE_SLICE_5_INCLUDED_APP_KEY =
      "enterprise_slice_5_included_apps";
  private static final String ENTERPRISE_SLICE_5_EXCLUDED_APP_KEY =
      "enterprise_slice_5_excluded_apps";

  private static final String ENTERPRISE_SLICE_SUBMIT =
      "enterprise_slice_submit";

  private DevicePolicyManager mDevicePolicyManager;
  private PackageManager mPackageManager;
  private ComponentName mAdminComponentName;

  private DpcEditTextPreference mIncludedApps1;
  private DpcEditTextPreference mExcludedApps1;
  private DpcSwitchPreference mAllowFallbackToDefault1;
  private DpcSwitchPreference mEnable1;

  private DpcEditTextPreference mIncludedApps2;
  private DpcEditTextPreference mExcludedApps2;
  private DpcSwitchPreference mAllowFallbackToDefault2;
  private DpcSwitchPreference mEnable2;

  private DpcEditTextPreference mIncludedApps3;
  private DpcEditTextPreference mExcludedApps3;
  private DpcSwitchPreference mAllowFallbackToDefault3;
  private DpcSwitchPreference mEnable3;

  private DpcEditTextPreference mIncludedApps4;
  private DpcEditTextPreference mExcludedApps4;
  private DpcSwitchPreference mAllowFallbackToDefault4;
  private DpcSwitchPreference mEnable4;

  private DpcEditTextPreference mIncludedApps5;
  private DpcEditTextPreference mExcludedApps5;
  private DpcSwitchPreference mAllowFallbackToDefault5;
  private DpcSwitchPreference mEnable5;

  private DpcPreference mSubmit;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManager = getActivity().getSystemService(DevicePolicyManager.class);
    mPackageManager = getActivity().getPackageManager();
    mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
    getActivity().getActionBar().setTitle(R.string.enterprise_slice);
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.enterprise_slice_preferences);

    mIncludedApps1 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_1_INCLUDED_APP_KEY);
    mExcludedApps1 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_1_EXCLUDED_APP_KEY);
    mAllowFallbackToDefault1 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_1_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mEnable1 = (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_1_ENABLE);

    mIncludedApps2 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_2_INCLUDED_APP_KEY);
    mExcludedApps2 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_2_EXCLUDED_APP_KEY);
    mAllowFallbackToDefault2 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_2_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mEnable2 = (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_2_ENABLE);

    mIncludedApps3 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_3_INCLUDED_APP_KEY);
    mExcludedApps3 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_3_EXCLUDED_APP_KEY);
    mAllowFallbackToDefault3 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_3_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mEnable3 = (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_3_ENABLE);

    mIncludedApps4 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_4_INCLUDED_APP_KEY);
    mExcludedApps4 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_4_EXCLUDED_APP_KEY);
    mAllowFallbackToDefault4 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_4_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mEnable4 = (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_4_ENABLE);

    mIncludedApps5 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_5_INCLUDED_APP_KEY);
    mExcludedApps5 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_5_EXCLUDED_APP_KEY);
    mAllowFallbackToDefault5 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_5_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mEnable5 = (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_5_ENABLE);

    mSubmit = (DpcPreference) findPreference(ENTERPRISE_SLICE_SUBMIT);
    mSubmit.setOnPreferenceClickListener(this);


  }

  @Override
  public boolean isAvailable(Context context) {
    return true;
  }

  protected void showToast(String msg) {
    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    String key = preference.getKey();

    switch (key) {
      case ENTERPRISE_SLICE_SUBMIT:
        Log.d(TAG, "ENTERPRISE_SLICE_SUBMIT: ");
        List<PreferentialNetworkServiceConfig> preferentialNetworkServiceConfigs =
            new ArrayList<>();
        try {
          addConfig(
              preferentialNetworkServiceConfigs,
              mEnable1,
              mAllowFallbackToDefault1,
              mIncludedApps1,
              mExcludedApps1,
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
          addConfig(
              preferentialNetworkServiceConfigs,
              mEnable2,
              mAllowFallbackToDefault2,
              mIncludedApps2,
              mExcludedApps2,
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_2);
          addConfig(
              preferentialNetworkServiceConfigs,
              mEnable3,
              mAllowFallbackToDefault3,
              mIncludedApps3,
              mExcludedApps3,
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_3);
          addConfig(
              preferentialNetworkServiceConfigs,
              mEnable4,
              mAllowFallbackToDefault4,
              mIncludedApps4,
              mExcludedApps4,
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_4);
          addConfig(
              preferentialNetworkServiceConfigs,
              mEnable5,
              mAllowFallbackToDefault5,
              mIncludedApps5,
              mExcludedApps5,
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_5);

          Log.d(TAG, "configs: " + preferentialNetworkServiceConfigs);
          for (PreferentialNetworkServiceConfig config : preferentialNetworkServiceConfigs) {
            Log.d(TAG, "config: " + config);
            for (int i : config.getIncludedUids()) {
              Log.d(TAG, "includedUid : " + i);
            }
            for (int i : config.getExcludedUids()) {
              Log.d(TAG, "excludedUid : " + i);
            }
          }
          mDevicePolicyManager.setPreferentialNetworkServiceConfigs(
              preferentialNetworkServiceConfigs);
          showToast("Sent the configurations");

        } catch (Exception e) {
          Log.d(TAG, "Exception : " + e);
          showToast("Exception: " + e);
        }
        return true;
    }
    return false;
  }

  int getUid(String packageName) {
    int uid = 0;
    try {
        uid = mPackageManager.getApplicationInfo(packageName, 0).uid;
    } catch (Exception e) {
        Log.d(TAG, " printStackTrace " + e);
    }
    return uid;
  }

  int[] getUids(String packages) {
    if (packages == null) {
      return new int[0];
    }
    List<String> packagesList = Arrays.asList(packages.split(",", -1));
    int uids[] = new int[packagesList.size()];
    int index = 0;
    for (String packageName : packagesList) {
      uids[index] = getUid(packageName);
      if (uids[index] == 0) {
        return new int[0];
      }
      index++;
    }

    return uids;
  }

  private void addConfig(
      List<PreferentialNetworkServiceConfig> configs,
      DpcSwitchPreference enablePref,
      DpcSwitchPreference fallbackPref,
      DpcEditTextPreference includedPref,
      DpcEditTextPreference excludedPref,
      int networkId) {
    if (enablePref.isChecked()) {
      PreferentialNetworkServiceConfig.Builder builder =
          new PreferentialNetworkServiceConfig.Builder();
      builder.setEnabled(true);
      builder.setNetworkId(networkId);
      builder.setFallbackToDefaultConnectionAllowed(fallbackPref.isChecked());
      builder.setIncludedUids(getUids(includedPref.getText()));
      builder.setExcludedUids(getUids(excludedPref.getText()));
      configs.add(builder.build());
    }
  }
}
