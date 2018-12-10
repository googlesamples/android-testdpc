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
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.AppInfoArrayAdapter;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.ReflectionUtil.ReflectionIsTemporaryException;

import java.util.ArrayList;
import java.util.Set;

/**
 * TODO: Cleanup reflection usages once SDK is updated. b/120765156.
 */
@TargetApi(29)
public class CrossProfileCalendarFragment extends BaseSearchablePolicyPreferenceFragment implements
    Preference.OnPreferenceClickListener {

    private static String LOG_TAG = "CrossProfileCalendarFragment";

    private static final String CROSS_PROFILE_CALENDAR_ADD_PACKAGE_KEY =
        "cross_profile_calendar_add_package";
    private static final String CROSS_PROFILE_CALENDAR_DELETE_PACKAGE_KEY =
        "cross_profile_calendar_delete_package";
    private static final String CROSS_PROFILE_CALENDAR_LIST_PACKAGE_KEY =
        "cross_profile_calendar_list_package";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
            Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        getActivity().getActionBar().setTitle(R.string.cross_profile_calendar);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.cross_profile_calendar_preferences);

        findPreference(CROSS_PROFILE_CALENDAR_ADD_PACKAGE_KEY).setOnPreferenceClickListener(this);
        findPreference(CROSS_PROFILE_CALENDAR_DELETE_PACKAGE_KEY)
            .setOnPreferenceClickListener(this);
        findPreference(CROSS_PROFILE_CALENDAR_LIST_PACKAGE_KEY).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case CROSS_PROFILE_CALENDAR_ADD_PACKAGE_KEY:
                showAddPackageDialog();
                return true;
            case CROSS_PROFILE_CALENDAR_DELETE_PACKAGE_KEY:
                showDeletePackageDialog();
                return true;
            case CROSS_PROFILE_CALENDAR_LIST_PACKAGE_KEY:
                showListPackageDialog();
                return true;
        }
        return false;
    }

    /**
     * Shows a dialog that asks the user for a package name to be whitelisted.
     */
    private void showAddPackageDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        final View dialogView = getActivity().getLayoutInflater().inflate(
            R.layout.simple_edittext, null);
        final EditText addPackageEditText = (EditText) dialogView.findViewById(
            R.id.input);

        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.cross_profile_calendar_add_package)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                final String packageName = addPackageEditText.getText().toString();
                if (packageName.isEmpty()) {
                    showToast(R.string.cross_profile_calendar_no_package);
                    return;
                }
                try {
                    ReflectionUtil.invoke(mDevicePolicyManager, "addCrossProfileCalendarPackage",
                        mAdminComponentName, packageName);
                    showToast(String.format("Successfully whitelisted package %s for cross profile "
                        + "calendar", packageName));
                } catch (ReflectionIsTemporaryException e) {
                    Log.e(LOG_TAG, "Failed to invoke addCrossProfileCalendarPackage", e);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    /**
     * Shows a dialog that asks the user for a package name to be removed from the whitelist.
     */
    private void showDeletePackageDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        final View dialogView = getActivity().getLayoutInflater().inflate(
            R.layout.simple_edittext, null);
        final EditText deletePackageEditText = (EditText) dialogView.findViewById(
            R.id.input);

        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.cross_profile_calendar_delete_package)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                final String packageName = deletePackageEditText.getText().toString();
                if (packageName.isEmpty()) {
                    showToast(R.string.cross_profile_calendar_no_package);
                    return;
                }
                try {
                    boolean succeed = (Boolean) ReflectionUtil.invoke(mDevicePolicyManager,
                        "removeCrossProfileCalendarPackage", mAdminComponentName, packageName);
                    if (succeed) {
                        showToast(String.format("Successfully removed package %s for cross profile "
                            + "calendar", packageName));
                    } else {
                        showToast(String.format("Failed to remove package %s for cross profile "
                            + "calendar", packageName));
                    }
                } catch (ReflectionIsTemporaryException e) {
                    Log.e(LOG_TAG, "Failed to invoke removeCrossProfileCalendarPackage", e);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    /**
     * Shows a dialog that displays all the packages that have been whitelisted.
     */
    private void showListPackageDialog() {
        Set<String> packages = new ArraySet<String>();
        try {
            packages = (Set<String>) ReflectionUtil.invoke(mDevicePolicyManager,
                "getCrossProfileCalendarPackages", mAdminComponentName);
        } catch (ReflectionIsTemporaryException e) {
            Log.e(LOG_TAG, "Failed to invoke getCrossProfileCalendarPackages", e);
        }

        if (packages.isEmpty()) {
            showToast(R.string.cross_profile_calendar_list_package_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                R.id.pkg_name, new ArrayList<String>(packages), true);
            new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.cross_profile_calendar_list_package_title))
                .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        // Do nothing.
                    }
                })
                .show();
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
