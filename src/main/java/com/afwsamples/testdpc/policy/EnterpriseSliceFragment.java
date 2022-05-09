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
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.Preference;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;
import com.afwsamples.testdpc.common.preference.DpcPreference;
import com.afwsamples.testdpc.common.preference.DpcEditTextPreference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@TargetApi(VERSION_CODES.Q)
public class EnterpriseSliceFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

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
  final private PreferentialNetworkServiceConfig.Builder
      mPreferentialNetworkServiceConfigBuilder1 = new PreferentialNetworkServiceConfig.Builder();
  final private PreferentialNetworkServiceConfig.Builder
      mPreferentialNetworkServiceConfigBuilder2 = new PreferentialNetworkServiceConfig.Builder();
  final private PreferentialNetworkServiceConfig.Builder
      mPreferentialNetworkServiceConfigBuilder3 = new PreferentialNetworkServiceConfig.Builder();
  final private PreferentialNetworkServiceConfig.Builder
      mPreferentialNetworkServiceConfigBuilder4 = new PreferentialNetworkServiceConfig.Builder();
  final private PreferentialNetworkServiceConfig.Builder
      mPreferentialNetworkServiceConfigBuilder5 = new PreferentialNetworkServiceConfig.Builder();

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
    mIncludedApps1.setOnPreferenceChangeListener(this);

    mExcludedApps1 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_1_EXCLUDED_APP_KEY);
    mExcludedApps1.setOnPreferenceChangeListener(this);

    mAllowFallbackToDefault1 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_1_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mAllowFallbackToDefault1.setOnPreferenceChangeListener(this);

    mEnable1 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_1_ENABLE);
    mEnable1.setOnPreferenceChangeListener(this);

    mIncludedApps2 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_2_INCLUDED_APP_KEY);
    mIncludedApps2.setOnPreferenceChangeListener(this);

    mExcludedApps2 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_2_EXCLUDED_APP_KEY);
    mExcludedApps2.setOnPreferenceChangeListener(this);

    mAllowFallbackToDefault2 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_2_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mAllowFallbackToDefault2.setOnPreferenceChangeListener(this);

    mEnable2 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_2_ENABLE);
    mEnable2.setOnPreferenceChangeListener(this);

    mIncludedApps3 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_3_INCLUDED_APP_KEY);
    mIncludedApps3.setOnPreferenceChangeListener(this);

    mExcludedApps3 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_3_EXCLUDED_APP_KEY);
    mExcludedApps3.setOnPreferenceChangeListener(this);

    mAllowFallbackToDefault3 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_3_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mAllowFallbackToDefault3.setOnPreferenceChangeListener(this);

    mEnable3 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_3_ENABLE);
    mEnable3.setOnPreferenceChangeListener(this);

    mIncludedApps4 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_4_INCLUDED_APP_KEY);
    mIncludedApps4.setOnPreferenceChangeListener(this);

    mExcludedApps4 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_4_EXCLUDED_APP_KEY);
    mExcludedApps4.setOnPreferenceChangeListener(this);

    mAllowFallbackToDefault4 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_4_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mAllowFallbackToDefault4.setOnPreferenceChangeListener(this);

    mEnable4 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_4_ENABLE);
    mEnable4.setOnPreferenceChangeListener(this);

    mIncludedApps5 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_5_INCLUDED_APP_KEY);
    mIncludedApps5.setOnPreferenceChangeListener(this);

    mExcludedApps5 = (DpcEditTextPreference) findPreference(ENTERPRISE_SLICE_5_EXCLUDED_APP_KEY);
    mExcludedApps5.setOnPreferenceChangeListener(this);

    mAllowFallbackToDefault5 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_5_ALLOW_FALLBACK_TO_DEFAULT_KEY);
    mAllowFallbackToDefault5.setOnPreferenceChangeListener(this);

    mEnable5 =
        (DpcSwitchPreference) findPreference(ENTERPRISE_SLICE_5_ENABLE);
    mEnable5.setOnPreferenceChangeListener(this);

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
        List<PreferentialNetworkServiceConfig> preferentialNetworkServiceConfigs =
            new ArrayList<>();
        try {
          preferentialNetworkServiceConfigs.add(mPreferentialNetworkServiceConfigBuilder1.build());
          if (mPreferentialNetworkServiceConfigBuilder2.build().getNetworkId() ==
                  PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_2) {
            preferentialNetworkServiceConfigs.add(
                mPreferentialNetworkServiceConfigBuilder2.build());
          }
          if (mPreferentialNetworkServiceConfigBuilder3.build().getNetworkId() ==
                  PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_3) {
            preferentialNetworkServiceConfigs.add(
                mPreferentialNetworkServiceConfigBuilder3.build());
          }
          if (mPreferentialNetworkServiceConfigBuilder4.build().getNetworkId() ==
                  PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_4) {
            preferentialNetworkServiceConfigs.add(
                mPreferentialNetworkServiceConfigBuilder4.build());
          }
          if (mPreferentialNetworkServiceConfigBuilder5.build().getNetworkId() ==
                  PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_5) {
            preferentialNetworkServiceConfigs.add(
                mPreferentialNetworkServiceConfigBuilder5.build());
          }
          mDevicePolicyManager.setPreferentialNetworkServiceConfigs(
              preferentialNetworkServiceConfigs);
          showToast("Sent the configurations");
        } catch (Exception e) {
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

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String key = preference.getKey();
    switch (key) {
      case ENTERPRISE_SLICE_1_ALLOW_FALLBACK_TO_DEFAULT_KEY:
        mPreferentialNetworkServiceConfigBuilder1.setFallbackToDefaultConnectionAllowed(
            (Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_1_ENABLE:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder1.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
        }
        mPreferentialNetworkServiceConfigBuilder1.setEnabled((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_1_INCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder1.setIncludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_1_EXCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder1.setExcludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_2_ENABLE:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder1.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
        }
        mPreferentialNetworkServiceConfigBuilder1.setEnabled((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_2_ALLOW_FALLBACK_TO_DEFAULT_KEY:
        mPreferentialNetworkServiceConfigBuilder2.setFallbackToDefaultConnectionAllowed(
            (Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_2_INCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder2.setIncludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_2_EXCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder2.setExcludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_3_ENABLE:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder3.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
        }
        mPreferentialNetworkServiceConfigBuilder3.setEnabled((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_3_ALLOW_FALLBACK_TO_DEFAULT_KEY:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder3.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_3);
        }
        mPreferentialNetworkServiceConfigBuilder3.setFallbackToDefaultConnectionAllowed(
            (Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_3_INCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder3.setIncludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_3_EXCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder3.setExcludedUids(
              getUids((String) newValue));
        break;


      case ENTERPRISE_SLICE_4_ENABLE:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder4.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
        }
        mPreferentialNetworkServiceConfigBuilder4.setEnabled((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_4_ALLOW_FALLBACK_TO_DEFAULT_KEY:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder4.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_4);
        }
        mPreferentialNetworkServiceConfigBuilder4.setFallbackToDefaultConnectionAllowed(
            (Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_4_INCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder4.setIncludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_4_EXCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder4.setExcludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_5_ENABLE:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder5.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_1);
        }
        mPreferentialNetworkServiceConfigBuilder5.setEnabled((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_5_ALLOW_FALLBACK_TO_DEFAULT_KEY:
        if ((Boolean) newValue) {
          mPreferentialNetworkServiceConfigBuilder5.setNetworkId(
              PreferentialNetworkServiceConfig.PREFERENTIAL_NETWORK_ID_5);
        }
        mPreferentialNetworkServiceConfigBuilder5.setFallbackToDefaultConnectionAllowed((Boolean) newValue);
        break;

      case ENTERPRISE_SLICE_5_INCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder5.setIncludedUids(
              getUids((String) newValue));
        break;

      case ENTERPRISE_SLICE_5_EXCLUDED_APP_KEY:
        mPreferentialNetworkServiceConfigBuilder5.setExcludedUids(
              getUids((String) newValue));
        break;
    }
    return true;
  }
}
