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

package com.google.android.testdpc.deviceowner;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.deviceowner.locktask.LockTaskAppInfoArrayAdapter;

import java.util.List;

/**
 * This fragment provides several device management functions.
 * These include
 * 1) {@link DevicePolicyManager#setLockTaskPackages(android.content.ComponentName, String[])}
 * 2) {@link DevicePolicyManager#isLockTaskPermitted(String)}
 * 3) {@link UserManager#DISALLOW_DEBUGGING_FEATURES}
 * 4) {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES}
 */
public class DevicePolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String MANAGE_LOCK_TASK_LIST_KEY = "manage_lock_task";
    private static final String CHECK_LOCK_TASK_PERMITTED_KEY = "check_lock_task_permitted";
    private static final String DISALLOW_INSTALL_DEBUGGING_FEATURE_KEY
            = "disallow_debugging_feature";
    private static final String DISALLOW_INSTALL_UNKNOWN_SOURCES_KEY
            = "disallow_install_unknown_sources";

    private Preference mManageLockTaskPreference;
    private Preference mCheckLockTaskPermittedPreference;
    private SwitchPreference mDisallowDebuggingFeatureSwitchPreference;
    private SwitchPreference mDisallowInstallUnknownSourcesSwitchPreference;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;
    private UserManager mUserManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        addPreferencesFromResource(R.xml.device_policy_header);

        mManageLockTaskPreference = findPreference(MANAGE_LOCK_TASK_LIST_KEY);
        mManageLockTaskPreference.setOnPreferenceClickListener(this);
        mCheckLockTaskPermittedPreference = findPreference(CHECK_LOCK_TASK_PERMITTED_KEY);
        mCheckLockTaskPermittedPreference.setOnPreferenceClickListener(this);
        mDisallowDebuggingFeatureSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_INSTALL_DEBUGGING_FEATURE_KEY);
        mDisallowDebuggingFeatureSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowInstallUnknownSourcesSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_INSTALL_UNKNOWN_SOURCES_KEY);
        mDisallowInstallUnknownSourcesSwitchPreference.setOnPreferenceChangeListener(this);
        validateUserRestrictionUi(UserManager.DISALLOW_DEBUGGING_FEATURES);
        validateUserRestrictionUi(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.device_management_title);

        String packageName = getActivity().getPackageName();
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName);
        if (!isDeviceOwner) {
            // Safe net: should never happen.
            Toast.makeText(getActivity(), R.string.not_a_device_owner, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mManageLockTaskPreference == preference) {
            showManageLockTaskListPrompt();
            return true;
        } else if (mCheckLockTaskPermittedPreference == preference) {
            showCheckLockTaskPermittedPrompt();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (DISALLOW_INSTALL_DEBUGGING_FEATURE_KEY.equals(key)) {
            boolean disallowDebuggingFeature = (Boolean) newValue;
            setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, disallowDebuggingFeature);
            return true;
        } else if (DISALLOW_INSTALL_UNKNOWN_SOURCES_KEY.equals(key)) {
            boolean disallowInstallUnknownSources = (Boolean) newValue;
            setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                    disallowInstallUnknownSources);
            return true;
        }
        return false;
    }

    /**
     * Show a list of primary user apps in a prompt, indicating whether lock task is permitted for
     * that app.
     */
    private void showManageLockTaskListPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> primaryUserAppList = getActivity().getPackageManager()
                .queryIntentActivities(launcherIntent, 0);
        if (primaryUserAppList.isEmpty()) {
            Toast.makeText(getActivity(), R.string.no_primary_app_available, Toast.LENGTH_SHORT)
                    .show();
        } else {
            final LockTaskAppInfoArrayAdapter appInfoArrayAdapter = new LockTaskAppInfoArrayAdapter(
                    getActivity(), R.layout.lock_task_app_row, primaryUserAppList);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.manage_lock_task))
                    .setAdapter(appInfoArrayAdapter, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] lockTaskEnabledArray = appInfoArrayAdapter.getLockTaskList();
                            mDevicePolicyManager
                                    .setLockTaskPackages(DeviceAdminReceiver.getComponentName(
                                            getActivity()), lockTaskEnabledArray);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }
    }

    /**
     * Show a prompt to collect a package name and check whether the lock task for the corresponding
     * app is permitted or not.
     */
    private void showCheckLockTaskPermittedPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.check_lock_task_permitted));
        View view = getActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(getString(R.string.check_lock_task_permitted_hints));
        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String packageName = input.getText().toString();
                        boolean isLockTaskPermitted = mDevicePolicyManager
                                .isLockTaskPermitted(packageName);
                        String resultMessage = isLockTaskPermitted
                                ? getString(R.string.check_lock_task_permitted_result_permitted)
                                : getString(
                                        R.string.check_lock_task_permitted_result_not_permitted);
                        Toast.makeText(getActivity(), resultMessage, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
        validateUserRestrictionUi(restriction);
    }

    private void validateUserRestrictionUi(String userRestrictions) {
        switch (userRestrictions) {
            case UserManager.DISALLOW_DEBUGGING_FEATURES:
                boolean disallowDebuggingFeature = mUserManager
                        .hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES);
                mDisallowDebuggingFeatureSwitchPreference.setChecked(disallowDebuggingFeature);
                break;
            case UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES:
                boolean disallowInstallUnknownSources = mUserManager.hasUserRestriction(
                        UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
                mDisallowInstallUnknownSourcesSwitchPreference.setChecked(
                        disallowInstallUnknownSources);
                break;
        }
    }
}
