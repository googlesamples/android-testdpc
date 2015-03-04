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

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;

/**
 * This fragment provides several functions that are available in a managed profile.
 */
public class ProfilePolicyManagementFragment extends PreferenceFragment {

    private static final String MANAGE_DEVICE_POLICIES_KEY = "manage_device_policies";

    private DevicePolicyManager mDevicePolicyManager;

    private ComponentName mAdminComponentName;

    private Preference mManageDevicePoliciesPreference;

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

    }
}
