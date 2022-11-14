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

package com.afwsamples.testdpc.policy;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.DevicePolicyManagerGateway;
import com.afwsamples.testdpc.DevicePolicyManagerGatewayImpl;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.preference.DpcPreferenceBase;
import com.afwsamples.testdpc.common.preference.DpcPreferenceHelper;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;

public class UserRestrictionsDisplayFragment extends BaseSearchablePolicyPreferenceFragment
    implements Preference.OnPreferenceChangeListener {
  private static final String TAG = "UserRestrictions";

  private DevicePolicyManagerGateway mDevicePolicyManagerGateway;

  public static UserRestrictionsDisplayFragment newInstance() {
    UserRestrictionsDisplayFragment fragment = new UserRestrictionsDisplayFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mDevicePolicyManagerGateway = new DevicePolicyManagerGatewayImpl(getActivity());
    super.onCreate(savedInstanceState);
    getActivity().getActionBar().setTitle(R.string.user_restrictions_management_title);
  }

  @Override
  public void onCreatePreferences(Bundle bundle, String rootkey) {
    PreferenceScreen preferenceScreen =
        getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext());
    setPreferenceScreen(preferenceScreen);

    final Context preferenceContext = getPreferenceManager().getContext();
    for (UserRestriction restriction : UserRestriction.ALL_USER_RESTRICTIONS) {
      DpcSwitchPreference preference = new DpcSwitchPreference(preferenceContext);
      preference.setTitle(restriction.titleResId);
      preference.setKey(restriction.key);
      preference.setOnPreferenceChangeListener(this);
      preferenceScreen.addPreference(preference);
    }

    updateAllUserRestrictions();
    constrainPreferences();
  }

  @Override
  public void onResume() {
    super.onResume();
    updateAllUserRestrictions();
  }

  @Override
  public boolean isAvailable(Context context) {
    return true;
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String restriction = preference.getKey();
    boolean enabled = newValue.equals(true);
    try {
      mDevicePolicyManagerGateway.setUserRestriction(restriction, enabled);
      if (!enabled) {
        if (DISALLOW_INSTALL_UNKNOWN_SOURCES.equals(restriction)
            || UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY.equals(restriction)) {
          new AlertDialog.Builder(getActivity())
              .setMessage(R.string.check_setting_disallow_install_unknown_sources)
              .setPositiveButton(R.string.check_setting_ok, null)
              .show();
        }
      }
      updateUserRestriction(restriction);
      if (UserManager.DISALLOW_UNIFIED_PASSWORD.equals(restriction)) {
        DeviceAdminReceiver.sendPasswordRequirementsChanged(getActivity());
      }
      return true;
    } catch (SecurityException e) {
      Toast.makeText(getActivity(), R.string.user_restriction_error_msg, Toast.LENGTH_SHORT).show();
      Log.e(TAG, "Error occurred while updating user restriction: " + restriction, e);
      return false;
    }
  }

  private void updateAllUserRestrictions() {
    for (UserRestriction restriction : UserRestriction.ALL_USER_RESTRICTIONS) {
      updateUserRestriction(restriction.key);
    }
  }

  private void updateUserRestriction(String userRestriction) {
    DpcSwitchPreference preference = (DpcSwitchPreference) findPreference(userRestriction);
    boolean disallowed = mDevicePolicyManagerGateway.hasUserRestriction(userRestriction);
    preference.setChecked(disallowed);
  }

  private void constrainPreferences() {
    for (String restriction : UserRestriction.MNC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.M);
    }
    for (String restriction : UserRestriction.NYC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.N);
    }
    for (String restriction : UserRestriction.OC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.O);
    }
    for (String restriction : UserRestriction.PIC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.P);
    }
    for (String restriction : UserRestriction.QT_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.Q);
    }
    for (String restriction : UserRestriction.SC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.S);
    }
    for (String restriction : UserRestriction.TM_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.TIRAMISU);
    }
    for (String restriction : UserRestriction.UDC_PLUS_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setMinSdkVersion(VERSION_CODES.UPSIDE_DOWN_CAKE);
    }
    for (String restriction : UserRestriction.PRIMARY_USER_ONLY_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setUserConstraint(DpcPreferenceHelper.USER_PRIMARY_USER);
    }
    for (String restriction : UserRestriction.DEVICE_OWNER_ONLY_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setAdminConstraint(DpcPreferenceHelper.ADMIN_DEVICE_OWNER);
    }
    for (String restriction : UserRestriction.MANAGED_PROFILE_ONLY_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setUserConstraint(DpcPreferenceHelper.USER_MANAGED_PROFILE);
    }
    for (String restriction : UserRestriction.NON_MANAGED_PROFILE_RESTRICTIONS) {
      DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
      pref.setUserConstraint(DpcPreferenceHelper.USER_NOT_MANAGED_PROFILE);
    }
  }
}
