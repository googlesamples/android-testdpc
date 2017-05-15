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

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.preference.DpcPreferenceBase;
import com.afwsamples.testdpc.common.preference.DpcPreferenceHelper;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;

public class UserRestrictionsDisplayFragment extends BaseSearchablePolicyPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "UserRestrictions";

    private DevicePolicyManager mDevicePolicyManager;
    private UserManager mUserManager;
    private ComponentName mAdminComponentName;

    public static UserRestrictionsDisplayFragment newInstance() {
        UserRestrictionsDisplayFragment fragment = new UserRestrictionsDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.user_restrictions_management_title);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootkey) {
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(
                getPreferenceManager().getContext());
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
        constrainPerferences();
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
        try {
            if (newValue.equals(true)) {
                mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
            } else {
                mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
                if (DISALLOW_INSTALL_UNKNOWN_SOURCES.equals(restriction)) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.check_setting_disallow_install_unknown_sources)
                            .setPositiveButton(R.string.check_setting_ok, null)
                            .show();
                }
            }
            updateUserRestriction(restriction);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), R.string.user_restriction_error_msg,
                    Toast.LENGTH_SHORT).show();
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
        boolean disallowed = mUserManager.hasUserRestriction(userRestriction);
        preference.setChecked(disallowed);
    }

    private void constrainPerferences() {
        for (String restriction : UserRestriction.MNC_PLUS_RESTRICTIONS) {
            DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
            pref.setMinSdkVersion(Build.VERSION_CODES.M);
        }
        for (String restriction : UserRestriction.NYC_PLUS_RESTRICTIONS) {
            DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
            pref.setMinSdkVersion(Build.VERSION_CODES.N);
        }
        for (String restriction : UserRestriction.OC_PLUS_RESTRICTIONS) {
            DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
            pref.setMinSdkVersion(Build.VERSION_CODES.O);
        }
        for (String restriction : UserRestriction.PRIMARY_USER_ONLY_RESTRICTIONS) {
            DpcPreferenceBase pref = (DpcPreferenceBase) findPreference(restriction);
            pref.setUserConstraint(DpcPreferenceHelper.USER_PRIMARY_USER);
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
