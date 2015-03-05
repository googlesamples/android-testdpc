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

package com.google.android.testdpc.profileowner;

import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.profileowner.crossprofileintentfilter
        .AddCrossProfileIntentFilterFragment;

/**
 * This fragment provides several functions that are available in a managed profile.
 * These includes
 * 1) {@link DevicePolicyManager#addCrossProfileIntentFilter(android.content.ComponentName,
 * android.content.IntentFilter, int)}
 * 2) {@link DevicePolicyManager#clearCrossProfileIntentFilters(android.content.ComponentName)}
 * 3) {@link DevicePolicyManager#setCrossProfileCallerIdDisabled(android.content.ComponentName,
 * boolean)}
 * 4) {@link DevicePolicyManager#getCrossProfileCallerIdDisabled(android.content.ComponentName)}
 * 5) {@link DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)}
 * 6) {@link DevicePolicyManager#getCameraDisabled(android.content.ComponentName)}
 * 7) {@link DevicePolicyManager#wipeData(int)}
 */
public class ProfilePolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY
            = "add_cross_profile_intent_filter";

    private static final String CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY
            = "clear_cross_profile_intent_filters";

    private static final String MANAGE_DEVICE_POLICIES_KEY = "manage_device_policies";

    private static final String DISABLE_CROSS_PROFILE_CALLER_ID_KEY
            = "disable_cross_profile_caller_id";

    private static final String DISABLE_CAMERA_KEY = "disable_camera";

    private static final String REMOVE_PROFILE_KEY = "remove_profile";

    private DevicePolicyManager mDevicePolicyManager;

    private ComponentName mAdminComponentName;

    private Preference mManageDevicePoliciesPreference;

    private Preference mAddCrossProfileIntentFilterPreference;

    private Preference mClearCrossProfileIntentFiltersPreference;

    private Preference mRemoveManagedProfilePreference;

    private SwitchPreference mDisableCrossProfileCallerIdSwitchPreference;

    private SwitchPreference mDisableCameraSwitchPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.profile_policy_header);

        String packageName = getActivity().getPackageName();
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName);

        if (!isProfileOwner) {
            // Safe net: should never happen.
            Toast.makeText(getActivity(), R.string.setup_profile_message, Toast.LENGTH_SHORT)
                    .show();
            getActivity().finish();
            return;
        }

        mManageDevicePoliciesPreference = findPreference(MANAGE_DEVICE_POLICIES_KEY);
        mManageDevicePoliciesPreference.setEnabled(isDeviceOwner);
        if (!isDeviceOwner) {
            // Disable shortcut to the device policy management console if the app is currently not
            // a device owner.
            mManageDevicePoliciesPreference.setSummary(R.string.not_a_device_owner);
        }

        mAddCrossProfileIntentFilterPreference = findPreference(
                ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY);
        mAddCrossProfileIntentFilterPreference.setOnPreferenceClickListener(this);
        mClearCrossProfileIntentFiltersPreference = findPreference(
                CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY);
        mClearCrossProfileIntentFiltersPreference.setOnPreferenceClickListener(this);
        mRemoveManagedProfilePreference = findPreference(REMOVE_PROFILE_KEY);
        mRemoveManagedProfilePreference.setOnPreferenceClickListener(this);

        mDisableCrossProfileCallerIdSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_CROSS_PROFILE_CALLER_ID_KEY);
        mDisableCrossProfileCallerIdSwitchPreference.setOnPreferenceChangeListener(this);
        reloadCrossProfileCallerIdDisableUi();

        mDisableCameraSwitchPreference = (SwitchPreference) findPreference(DISABLE_CAMERA_KEY);
        mDisableCameraSwitchPreference.setOnPreferenceChangeListener(this);
        reloadCameraDisableUi();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY.equals(key)) {
            showAddCrossProfileIntentFilterFragment();
            return true;
        } else if (CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY.equals(key)) {
            mDevicePolicyManager.clearCrossProfileIntentFilters(mAdminComponentName);
            Toast.makeText(getActivity(), getString(R.string.cross_profile_intent_filters_cleared),
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (REMOVE_PROFILE_KEY.equals(key)) {
            mRemoveManagedProfilePreference.setEnabled(false);
            mDevicePolicyManager.wipeData(0);
            Toast.makeText(getActivity(), getString(R.string.removing_managed_profile),
                    Toast.LENGTH_SHORT).show();
            // Finish the activity because all other functions will not work after the managed
            // profile is removed.
            getActivity().finish();
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (DISABLE_CROSS_PROFILE_CALLER_ID_KEY.equals(key)) {
            boolean disableCrossProfileCallerId = (Boolean) newValue;
            mDevicePolicyManager.setCrossProfileCallerIdDisabled(mAdminComponentName,
                    disableCrossProfileCallerId);
            // Reload UI to verify the state of cross-profiler caller Id is set correctly.
            reloadCrossProfileCallerIdDisableUi();
            return true;
        } else if (DISABLE_CAMERA_KEY.equals(key)) {
            boolean disableCamera = (Boolean) newValue;
            mDevicePolicyManager.setCameraDisabled(mAdminComponentName, disableCamera);
            // Reload UI to verify the camera is enable / disable correctly.
            reloadCameraDisableUi();
            return true;
        }
        return false;
    }

    private void showAddCrossProfileIntentFilterFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(ProfilePolicyManagementFragment.class.getName()).replace(
                R.id.container,
                new AddCrossProfileIntentFilterFragment())
                .commit();
    }

    private void reloadCrossProfileCallerIdDisableUi() {
        boolean isCrossProfileCallerIdDisabled = mDevicePolicyManager
                .getCrossProfileCallerIdDisabled(mAdminComponentName);
        mDisableCrossProfileCallerIdSwitchPreference.setChecked(isCrossProfileCallerIdDisabled);
    }

    private void reloadCameraDisableUi() {
        boolean isCameraDisabled = mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
        mDisableCameraSwitchPreference.setChecked(isCameraDisabled);
    }
}
