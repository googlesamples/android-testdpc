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
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.ArraySet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.preference.DpcPreference;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.Q)
public class CrossProfileCalendarFragment extends BaseSearchablePolicyPreferenceFragment implements
    Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String CROSS_PROFILE_CALENDAR_SET_ALLOWED_PACKAGES_KEY =
        "cross_profile_calendar_set_allowed_packages";
    private static final String CROSS_PROFILE_CALENDAR_ALLOW_ALL_PACKAGES_KEY =
        "cross_profile_calendar_allow_all_packages";

    private DevicePolicyManager mDevicePolicyManager;

    private ComponentName mAdminComponentName;
    private DpcPreference mSetAllowedPackagesPreference;
    private DpcSwitchPreference mAllowAllPackagesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDevicePolicyManager = getActivity().getSystemService(DevicePolicyManager.class);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        getActivity().getActionBar().setTitle(R.string.cross_profile_calendar);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.cross_profile_calendar_preferences);

        mSetAllowedPackagesPreference = (DpcPreference) findPreference(
            CROSS_PROFILE_CALENDAR_SET_ALLOWED_PACKAGES_KEY);
        mSetAllowedPackagesPreference.setOnPreferenceClickListener(this);

        mAllowAllPackagesPreference = (DpcSwitchPreference) findPreference(
            CROSS_PROFILE_CALENDAR_ALLOW_ALL_PACKAGES_KEY);
        mAllowAllPackagesPreference.setOnPreferenceChangeListener(this);

        reloadAllowAllPackagesUi();
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case CROSS_PROFILE_CALENDAR_SET_ALLOWED_PACKAGES_KEY:
                showSetPackagesDialog();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case CROSS_PROFILE_CALENDAR_ALLOW_ALL_PACKAGES_KEY:
                mDevicePolicyManager.setCrossProfileCalendarPackages(
                    mAdminComponentName, newValue.equals(true) ? null : Collections.emptySet());
                reloadAllowAllPackagesUi();
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void reloadAllowAllPackagesUi() {
        final Set<String> packages =
            mDevicePolicyManager.getCrossProfileCalendarPackages(mAdminComponentName);
        mAllowAllPackagesPreference.setChecked(packages == null);
        mSetAllowedPackagesPreference.setEnabled(!mAllowAllPackagesPreference.isChecked());
    }

    /**
     * Shows a dialog that asks the user for a set of package names to be allowed.
     */
    private void showSetPackagesDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        final View dialogView = getActivity().getLayoutInflater().inflate(
            R.layout.simple_edittext, null);
        final EditText setPackagesEditText = (EditText) dialogView.findViewById(
            R.id.input);

        setPackagesEditText.setText(String.join(",",
                mDevicePolicyManager.getCrossProfileCalendarPackages(mAdminComponentName)));

        new Builder(getActivity())
            .setTitle(R.string.cross_profile_calendar_set_allowed_packages_title)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                final String packageNamesString = setPackagesEditText.getText().toString();
                final Set<String> packageNames = packageNamesString.isEmpty()
                    ? Collections.emptySet()
                    : new ArraySet<>(Arrays.asList(
                        packageNamesString.replace(" ", "").split(",")));
                mDevicePolicyManager.setCrossProfileCalendarPackages(
                    mAdminComponentName, packageNames);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }
}
