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

package com.google.android.testdpc.profilepolicy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.common.AppInfoArrayAdapter;
import com.google.android.testdpc.profilepolicy.crossprofileintentfilter
        .AddCrossProfileIntentFilterFragment;
import com.google.android.testdpc.profilepolicy.crossprofilewidgetprovider
        .ManageCrossProfileWidgetProviderUtil;

import java.util.List;

/**
 * This fragment provides several functions that are available in a managed profile.
 * These includes
 * 1) {@link DevicePolicyManager#addCrossProfileIntentFilter(android.content.ComponentName,
 * android.content.IntentFilter, int)}
 * 2) {@link DevicePolicyManager#clearCrossProfileIntentFilters(android.content.ComponentName)}
 * 3) {@link DevicePolicyManager#setCrossProfileCallerIdDisabled(android.content.ComponentName,
 * boolean)}
 * 4) {@link DevicePolicyManager#getCrossProfileCallerIdDisabled(android.content.ComponentName)}
 * 5) {@link DevicePolicyManager#wipeData(int)}
 * 6) {@link DevicePolicyManager#addCrossProfileWidgetProvider(android.content.ComponentName,
 * String)}
 * 7) {@link DevicePolicyManager#removeCrossProfileWidgetProvider(android.content.ComponentName,
 * String)}
 */
public class ProfilePolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY
            = "add_cross_profile_intent_filter";
    private static final String CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY
            = "clear_cross_profile_intent_filters";
    private static final String DISABLE_CROSS_PROFILE_CALLER_ID_KEY
            = "disable_cross_profile_caller_id";
    private static final String REMOVE_PROFILE_KEY = "remove_profile";
    private static final String ADD_CROSS_PROFILE_APP_WIDGETS_KEY = "add_cross_profile_app_widgets";
    private static final String REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY
            = "remove_cross_profile_app_widgets";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;
    private Preference mAddCrossProfileIntentFilterPreference;
    private Preference mClearCrossProfileIntentFiltersPreference;
    private Preference mRemoveManagedProfilePreference;
    private Preference mAddCrossProfileAppWidgetsPreference;
    private Preference mRemoveCrossProfileAppWidgetsPreference;
    private SwitchPreference mDisableCrossProfileCallerIdSwitchPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.profile_policy_header);

        mAddCrossProfileIntentFilterPreference = findPreference(
                ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY);
        mAddCrossProfileIntentFilterPreference.setOnPreferenceClickListener(this);
        mClearCrossProfileIntentFiltersPreference = findPreference(
                CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY);
        mClearCrossProfileIntentFiltersPreference.setOnPreferenceClickListener(this);
        mRemoveManagedProfilePreference = findPreference(REMOVE_PROFILE_KEY);
        mRemoveManagedProfilePreference.setOnPreferenceClickListener(this);
        mAddCrossProfileAppWidgetsPreference = findPreference(ADD_CROSS_PROFILE_APP_WIDGETS_KEY);
        mAddCrossProfileAppWidgetsPreference.setOnPreferenceClickListener(this);
        mRemoveCrossProfileAppWidgetsPreference = findPreference(
                REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY);
        mRemoveCrossProfileAppWidgetsPreference.setOnPreferenceClickListener(this);

        initDisableCrossProfileCallerIdUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.profile_management_title);

        String packageName = getActivity().getPackageName();
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);

        if (!isProfileOwner) {
            // Safe net: should never happen.
            showToast(R.string.setup_profile_message);
            getActivity().finish();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY:
                showAddCrossProfileIntentFilterFragment();
                return true;
            case CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY:
                mDevicePolicyManager.clearCrossProfileIntentFilters(mAdminComponentName);
                showToast(R.string.cross_profile_intent_filters_cleared);
                return true;
            case REMOVE_PROFILE_KEY:
                mRemoveManagedProfilePreference.setEnabled(false);
                mDevicePolicyManager.wipeData(0);
                showToast(R.string.removing_managed_profile);
                // Finish the activity because all other functions will not work after the managed
                // profile is removed.
                getActivity().finish();
            case ADD_CROSS_PROFILE_APP_WIDGETS_KEY:
                showDisabledAppWidgetList();
                return true;
            case REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY:
                showEnabledAppWidgetList();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case DISABLE_CROSS_PROFILE_CALLER_ID_KEY:
                boolean disableCrossProfileCallerId = (Boolean) newValue;
                mDevicePolicyManager.setCrossProfileCallerIdDisabled(mAdminComponentName,
                        disableCrossProfileCallerId);
                // Reload UI to verify the state of cross-profiler caller Id is set correctly.
                reloadCrossProfileCallerIdDisableUi();
                return true;
        }
        return false;
    }

    private void showAddCrossProfileIntentFilterFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(
                ProfilePolicyManagementFragment.class.getName()).replace(R.id.container,
                new AddCrossProfileIntentFilterFragment()).commit();
    }

    private void initDisableCrossProfileCallerIdUi() {
        mDisableCrossProfileCallerIdSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_CROSS_PROFILE_CALLER_ID_KEY);
        mDisableCrossProfileCallerIdSwitchPreference.setOnPreferenceChangeListener(this);
        reloadCrossProfileCallerIdDisableUi();
    }

    private void reloadCrossProfileCallerIdDisableUi() {
        boolean isCrossProfileCallerIdDisabled = mDevicePolicyManager
                .getCrossProfileCallerIdDisabled(mAdminComponentName);
        mDisableCrossProfileCallerIdSwitchPreference.setChecked(isCrossProfileCallerIdDisabled);
    }

    /**
     * Shows a list of work profile apps which have non-enabled widget providers.
     * Clicking any item on the list will enable ALL the widgets from that app.
     *
     * Shows toast if there is no work profile app that has non-enabled widget providers.
     */
    private void showDisabledAppWidgetList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        final List<String> disabledCrossProfileWidgetProvidersList
                = ManageCrossProfileWidgetProviderUtil.getInstance(getActivity())
                .getDisabledCrossProfileWidgetProvidersList();
        if (disabledCrossProfileWidgetProvidersList.isEmpty()) {
            showToast(R.string.all_cross_profile_widget_providers_are_enabled);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.layout.app_row, disabledCrossProfileWidgetProvidersList);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.add_cross_profile_app_widget_providers_title));
            builder.setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pkgName = disabledCrossProfileWidgetProvidersList.get(which);
                    mDevicePolicyManager.addCrossProfileWidgetProvider(mAdminComponentName,
                            pkgName);
                    showToast(getString(R.string.cross_profile_widget_enable, pkgName));
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * Shows a list of work profile apps which have widget providers enabled.
     * Clicking any item on the list will disable ALL the widgets from that app.
     *
     * Shows toast if there is no app that have non-enabled widget providers.
     */
    private void showEnabledAppWidgetList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        final List<String> enabledCrossProfileWidgetProvidersList = mDevicePolicyManager
                .getCrossProfileWidgetProviders(mAdminComponentName);
        if (enabledCrossProfileWidgetProvidersList.isEmpty()) {
            showToast(R.string.all_cross_profile_widget_providers_are_disabled);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.layout.app_row, enabledCrossProfileWidgetProvidersList);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.remove_cross_profile_app_widget_providers_title));
            builder.setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pkgName = enabledCrossProfileWidgetProvidersList.get(which);
                    mDevicePolicyManager.removeCrossProfileWidgetProvider(mAdminComponentName,
                            pkgName);
                    showToast(getString(R.string.cross_profile_widget_disable, pkgName));
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private void showToast(int msgId) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String msg) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
