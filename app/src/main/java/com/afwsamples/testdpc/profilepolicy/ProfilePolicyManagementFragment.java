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

package com.afwsamples.testdpc.profilepolicy;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.AppInfoArrayAdapter;
import com.afwsamples.testdpc.common.ColorPicker;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.profilepolicy.crossprofileintentfilter.AddCrossProfileIntentFilterFragment;
import com.afwsamples.testdpc.profilepolicy.crossprofilewidgetprovider.ManageCrossProfileWidgetProviderUtil;

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
 * 8) {@link DevicePolicyManager#setBluetoothContactSharingDisabled(ComponentName, boolean)}
 */
public class ProfilePolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        ColorPicker.OnColorSelectListener {
    // Tag for creating this fragment. This tag can be used to retrieve this fragment.
    public static final String FRAGMENT_TAG = "ProfilePolicyManagementFragment";

    private static final String ADD_CROSS_PROFILE_APP_WIDGETS_KEY = "add_cross_profile_app_widgets";
    private static final String ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY
            = "add_cross_profile_intent_filter";
    private static final String CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY
            = "clear_cross_profile_intent_filters";
    private static final String DISABLE_BLUETOOTH_CONTACT_SHARING_KEY
            = "disable_bluetooth_contact_sharing";
    private static final String DISABLE_CROSS_PROFILE_CALLER_ID_KEY
            = "disable_cross_profile_caller_id";
    private static final String DISABLE_CROSS_PROFILE_CONTACTS_SEARCH_KEY
            = "disable_cross_profile_contacts_search";
    private static final String REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY =
            "remove_cross_profile_app_widgets";
    private static final String REMOVE_PROFILE_KEY = "remove_profile";
    private static final String SET_ORGANIZATION_COLOR_KEY = "set_organization_color";
    private static final String SET_ORGANIZATION_NAME_KEY = "set_organization_name";

    private static final String ORGANIZATION_COLOR_ID = "organizationColor";

    private static String[] MNC_PLUS_PREFERENCES = {
            DISABLE_BLUETOOTH_CONTACT_SHARING_KEY
    };

    private static String[] NYC_PLUS_PREFERENCES = {
            DISABLE_CROSS_PROFILE_CONTACTS_SEARCH_KEY, SET_ORGANIZATION_COLOR_KEY,
            SET_ORGANIZATION_NAME_KEY
    };

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;
    private Preference mAddCrossProfileIntentFilterPreference;
    private Preference mClearCrossProfileIntentFiltersPreference;
    private Preference mRemoveManagedProfilePreference;
    private Preference mAddCrossProfileAppWidgetsPreference;
    private Preference mRemoveCrossProfileAppWidgetsPreference;
    private SwitchPreference mDisableBluetoothContactSharingSwitchPreference;
    private SwitchPreference mDisableCrossProfileCallerIdSwitchPreference;
    private SwitchPreference mDisableCrossProfileContactsSearchSwitchPreference;
    private Preference mSetOrganizationNamePreference;
    private Preference mSetOrganizationColorPreference;

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

        disableIncompatibleManagementOptionsByApiLevel();
        initSwitchPreferences();
        initializeOrganizationInfoPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.profile_management_title);

        String packageName = getActivity().getPackageName();
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);

        if (!isProfileOwner) {
            // Safe net: should never happen.
            showToast(R.string.setup_management_message);
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
            case SET_ORGANIZATION_COLOR_KEY:
                int colorValue = getActivity().getResources().getColor(R.color.teal);
                final CharSequence summary = mSetOrganizationColorPreference.getSummary();
                if (summary != null) {
                    try {
                        colorValue = Color.parseColor(summary.toString());
                    } catch (IllegalArgumentException e) {
                        // Ignore
                    }
                }
                ColorPicker.newInstance(colorValue, FRAGMENT_TAG, ORGANIZATION_COLOR_ID)
                        .show(getFragmentManager(), "colorPicker");
        }
        return false;
    }

    @Override
    @SuppressLint("NewApi")
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case DISABLE_BLUETOOTH_CONTACT_SHARING_KEY:
                boolean disableBluetoothContactSharing = (Boolean) newValue;
                mDevicePolicyManager.setBluetoothContactSharingDisabled(mAdminComponentName,
                        disableBluetoothContactSharing);
                // Reload UI to verify the state of bluetooth contact sharing is set correctly.
                reloadBluetoothContactSharing();
                return true;
            case DISABLE_CROSS_PROFILE_CALLER_ID_KEY:
                boolean disableCrossProfileCallerId = (Boolean) newValue;
                mDevicePolicyManager.setCrossProfileCallerIdDisabled(mAdminComponentName,
                        disableCrossProfileCallerId);
                // Reload UI to verify the state of cross-profile caller Id is set correctly.
                reloadCrossProfileCallerIdDisableUi();
                return true;
            case DISABLE_CROSS_PROFILE_CONTACTS_SEARCH_KEY:
                boolean disableCrossProfileContactsSearch = (Boolean) newValue;
                mDevicePolicyManager.setCrossProfileContactsSearchDisabled(mAdminComponentName,
                        disableCrossProfileContactsSearch);
                // Reload UI to verify the state of cross-profile contacts search is set correctly.
                reloadCrossProfileContactsSearchDisableUi();
                return true;
            case SET_ORGANIZATION_NAME_KEY:
                mDevicePolicyManager.setOrganizationName(mAdminComponentName, (String) newValue);
                mSetOrganizationNamePreference.setSummary((String) newValue);
                return true;
        }
        return false;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onColorSelected(int colorValue, String id) {
        if (ORGANIZATION_COLOR_ID.equals(id)) {
            mDevicePolicyManager.setOrganizationColor(mAdminComponentName, colorValue);
            mSetOrganizationColorPreference.setSummary(
                    String.format(ColorPicker.COLOR_STRING_FORMATTER, colorValue));
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void initializeOrganizationInfoPreferences() {
        mSetOrganizationColorPreference = findPreference(SET_ORGANIZATION_COLOR_KEY);
        mSetOrganizationNamePreference = findPreference(SET_ORGANIZATION_NAME_KEY);

        if (mSetOrganizationColorPreference.isEnabled()) {
            mSetOrganizationColorPreference.setOnPreferenceClickListener(this);
            final int colorValue = mDevicePolicyManager.getOrganizationColor(mAdminComponentName);
            mSetOrganizationColorPreference.setSummary(
                    String.format(ColorPicker.COLOR_STRING_FORMATTER, colorValue));
        }

        if (mSetOrganizationNamePreference.isEnabled()) {
            mSetOrganizationNamePreference.setOnPreferenceChangeListener(this);
            final String name = mDevicePolicyManager.getOrganizationName(mAdminComponentName);
            mSetOrganizationNamePreference.setSummary(name);
        }
    }

    private void showAddCrossProfileIntentFilterFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(
                ProfilePolicyManagementFragment.class.getName()).replace(R.id.container,
                new AddCrossProfileIntentFilterFragment()).commit();
    }

    private void initSwitchPreferences() {
        mDisableBluetoothContactSharingSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_BLUETOOTH_CONTACT_SHARING_KEY);
        mDisableCrossProfileCallerIdSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_CROSS_PROFILE_CALLER_ID_KEY);
        mDisableCrossProfileContactsSearchSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_CROSS_PROFILE_CONTACTS_SEARCH_KEY);
        mDisableBluetoothContactSharingSwitchPreference.setOnPreferenceChangeListener(this);
        mDisableCrossProfileCallerIdSwitchPreference.setOnPreferenceChangeListener(this);
        mDisableCrossProfileContactsSearchSwitchPreference.setOnPreferenceChangeListener(this);
        reloadBluetoothContactSharing();
        reloadCrossProfileCallerIdDisableUi();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void reloadBluetoothContactSharing() {
        if (!mDisableBluetoothContactSharingSwitchPreference.isEnabled()) {
            return;
        }

        boolean isBluetoothContactSharingDisabled = mDevicePolicyManager
                .getBluetoothContactSharingDisabled(mAdminComponentName);
        mDisableBluetoothContactSharingSwitchPreference
                .setChecked(isBluetoothContactSharingDisabled);
    }

    private void reloadCrossProfileCallerIdDisableUi() {
        if (!mDisableCrossProfileCallerIdSwitchPreference.isEnabled()) {
            return;
        }

        boolean isCrossProfileCallerIdDisabled = mDevicePolicyManager
                .getCrossProfileCallerIdDisabled(mAdminComponentName);
        mDisableCrossProfileCallerIdSwitchPreference.setChecked(isCrossProfileCallerIdDisabled);
    }


    @TargetApi(Build.VERSION_CODES.N)
    private void reloadCrossProfileContactsSearchDisableUi() {
        if (!mDisableCrossProfileContactsSearchSwitchPreference.isEnabled()) {
            return;
        }

        boolean isCrossProfileContactsSearchDisabled = mDevicePolicyManager
                .getCrossProfileContactsSearchDisabled(mAdminComponentName);
        mDisableCrossProfileContactsSearchSwitchPreference.setChecked(
                isCrossProfileContactsSearchDisabled);
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

    private void disableIncompatibleManagementOptionsByApiLevel() {
        if (Util.isBeforeM()) {
            // The following options depend on MNC APIs.
            for (String preference : MNC_PLUS_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
        }

        if (Util.isBeforeN()) {
            // The following options depend on NYC APIs.
            for (String preference : NYC_PLUS_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
        }
    }
}
