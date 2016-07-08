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

import static android.os.UserManager.ALLOW_PARENT_PROFILE_APP_LINKING;
import static android.os.UserManager.DISALLOW_ADD_USER;
import static android.os.UserManager.DISALLOW_ADJUST_VOLUME;
import static android.os.UserManager.DISALLOW_APPS_CONTROL;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;
import static android.os.UserManager.DISALLOW_CONFIG_CELL_BROADCASTS;
import static android.os.UserManager.DISALLOW_CONFIG_CREDENTIALS;
import static android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_CONFIG_VPN;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;
import static android.os.UserManager.DISALLOW_CREATE_WINDOWS;
import static android.os.UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE;
import static android.os.UserManager.DISALLOW_DATA_ROAMING;
import static android.os.UserManager.DISALLOW_DEBUGGING_FEATURES;
import static android.os.UserManager.DISALLOW_FACTORY_RESET;
import static android.os.UserManager.DISALLOW_FUN;
import static android.os.UserManager.DISALLOW_INSTALL_APPS;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS;
import static android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA;
import static android.os.UserManager.DISALLOW_NETWORK_RESET;
import static android.os.UserManager.DISALLOW_OUTGOING_BEAM;
import static android.os.UserManager.DISALLOW_OUTGOING_CALLS;
import static android.os.UserManager.DISALLOW_REMOVE_USER;
import static android.os.UserManager.DISALLOW_SAFE_BOOT;
import static android.os.UserManager.DISALLOW_SET_USER_ICON;
import static android.os.UserManager.DISALLOW_SET_WALLPAPER;
import static android.os.UserManager.DISALLOW_SHARE_LOCATION;
import static android.os.UserManager.DISALLOW_SMS;
import static android.os.UserManager.DISALLOW_UNINSTALL_APPS;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static android.os.UserManager.DISALLOW_USB_FILE_TRANSFER;
import static android.os.UserManager.ENSURE_VERIFY_APPS;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

public class UserRestrictionsDisplayFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "UserRestrictions";

    private DevicePolicyManager mDevicePolicyManager;
    private UserManager mUserManager;
    private ComponentName mAdminComponentName;

    private static final UserRestriction[] ALL_USER_RESTRICTIONS = {
            new UserRestriction(ALLOW_PARENT_PROFILE_APP_LINKING,
                    R.string.allow_parent_profile_app_linking),
            new UserRestriction(DISALLOW_ADD_USER, R.string.disallow_add_user),
            new UserRestriction(DISALLOW_ADJUST_VOLUME, R.string.disallow_adjust_volume),
            new UserRestriction(DISALLOW_APPS_CONTROL, R.string.disallow_apps_control),
            new UserRestriction(DISALLOW_CONFIG_BLUETOOTH, R.string.disallow_config_bluetooth),
            new UserRestriction(DISALLOW_CONFIG_CELL_BROADCASTS,
                    R.string.disallow_config_cell_broadcasts),
            new UserRestriction(DISALLOW_CONFIG_CREDENTIALS, R.string.disallow_config_credentials),
            new UserRestriction(DISALLOW_CONFIG_MOBILE_NETWORKS,
                    R.string.disallow_config_mobile_networks),
            new UserRestriction(DISALLOW_CONFIG_TETHERING, R.string.disallow_config_tethering),
            new UserRestriction(DISALLOW_CONFIG_VPN, R.string.disallow_config_vpn),
            new UserRestriction(DISALLOW_CONFIG_WIFI, R.string.disallow_config_wifi),
            new UserRestriction(DISALLOW_CREATE_WINDOWS, R.string.disallow_create_windows),
            new UserRestriction(DISALLOW_CROSS_PROFILE_COPY_PASTE,
                    R.string.disallow_cross_profile_copy_paste),
            new UserRestriction(DISALLOW_DATA_ROAMING,
                    R.string.disallow_data_roaming),
            new UserRestriction(DISALLOW_DEBUGGING_FEATURES, R.string.disallow_debugging_features),
            new UserRestriction(DISALLOW_FACTORY_RESET, R.string.disallow_factory_reset),
            new UserRestriction(DISALLOW_FUN, R.string.disallow_fun),
            new UserRestriction(DISALLOW_INSTALL_APPS, R.string.disallow_install_apps),
            new UserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES,
                    R.string.disallow_install_unknown_sources),
            new UserRestriction(DISALLOW_MODIFY_ACCOUNTS, R.string.disallow_modify_accounts),
            new UserRestriction(DISALLOW_MOUNT_PHYSICAL_MEDIA,
                    R.string.disallow_mount_physical_media),
            new UserRestriction(DISALLOW_NETWORK_RESET, R.string.disallow_network_reset),
            new UserRestriction(DISALLOW_OUTGOING_BEAM, R.string.disallow_outgoing_beam),
            new UserRestriction(DISALLOW_OUTGOING_CALLS, R.string.disallow_outgoing_calls),
            new UserRestriction(DISALLOW_REMOVE_USER, R.string.disallow_remove_user),
            new UserRestriction(DISALLOW_SAFE_BOOT, R.string.disallow_safe_boot),
            new UserRestriction(DISALLOW_SET_USER_ICON, R.string.disallow_set_user_icon),
            new UserRestriction(DISALLOW_SET_WALLPAPER, R.string.disallow_set_wallpaper),
            new UserRestriction(DISALLOW_SHARE_LOCATION, R.string.disallow_share_location),
            new UserRestriction(DISALLOW_SMS, R.string.disallow_sms),
            new UserRestriction(DISALLOW_UNINSTALL_APPS, R.string.disallow_uninstall_apps),
            new UserRestriction(DISALLOW_UNMUTE_MICROPHONE, R.string.disallow_unmute_microphone),
            new UserRestriction(DISALLOW_USB_FILE_TRANSFER, R.string.disallow_usb_file_transfer),
            new UserRestriction(ENSURE_VERIFY_APPS, R.string.ensure_verify_apps),
    };

    /**
     * Setting these user restrictions only have effect on primary users.
     */
    private static final String[] PRIMARY_USER_ONLY_RESTRICTIONS = {
            DISALLOW_ADD_USER,
            DISALLOW_ADJUST_VOLUME,
            DISALLOW_CONFIG_BLUETOOTH,
            DISALLOW_CONFIG_CELL_BROADCASTS,
            DISALLOW_CONFIG_MOBILE_NETWORKS,
            DISALLOW_CONFIG_TETHERING,
            DISALLOW_CONFIG_WIFI,
            DISALLOW_CREATE_WINDOWS,
            DISALLOW_DATA_ROAMING,
            DISALLOW_FACTORY_RESET,
            DISALLOW_FUN,
            DISALLOW_MOUNT_PHYSICAL_MEDIA,
            DISALLOW_NETWORK_RESET,
            DISALLOW_OUTGOING_CALLS,
            DISALLOW_REMOVE_USER,
            DISALLOW_SAFE_BOOT,
            DISALLOW_SMS,
            DISALLOW_UNMUTE_MICROPHONE,
            DISALLOW_USB_FILE_TRANSFER
    };

    /**
     * Setting these user restrictions only have effect on managed profiles.
     */
    private static final String[] MANAGED_PROFILE_ONLY_RESTRICTIONS = {
            ALLOW_PARENT_PROFILE_APP_LINKING,
            DISALLOW_CROSS_PROFILE_COPY_PASTE
    };

    /**
     * These restrictions are not meant to be used with managed profiles.
     */
    private static String[] NON_MANAGED_PROFILE_RESTRICTIONS = {
            DISALLOW_SET_WALLPAPER
    };

    /**
     * These user restrictions are added in MNC.
     */
    private static String[] MNC_PLUS_RESTRICTIONS = {
            ALLOW_PARENT_PROFILE_APP_LINKING,
            DISALLOW_SAFE_BOOT
    };

    private static String[] NYC_PLUS_RESTRICTIONS = {
            DISALLOW_DATA_ROAMING,
            DISALLOW_SET_USER_ICON,
            DISALLOW_SET_WALLPAPER
    };

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
                getActivity());
        setPreferenceScreen(preferenceScreen);

        for (UserRestriction restriction : ALL_USER_RESTRICTIONS) {
            SwitchPreference preference = new SwitchPreference(getActivity());
            preference.setTitle(restriction.titleResId);
            preference.setKey(restriction.key);
            preference.setOnPreferenceChangeListener(this);
            preferenceScreen.addPreference(preference);
        }

        updateAllUserRestrictions();
        disableIncompatibleRestrictionsByApiLevel();
        disableIncompatibleRestrictionsByUserType();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllUserRestrictions();
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
        for (UserRestriction restriction : ALL_USER_RESTRICTIONS) {
            updateUserRestriction(restriction.key);
        }
    }

    private void updateUserRestriction(String userRestriction) {
        SwitchPreference preference = (SwitchPreference) findPreference(userRestriction);
        boolean disallowed = mUserManager.hasUserRestriction(userRestriction);
        preference.setChecked(disallowed);
    }

    private void disableIncompatibleRestrictionsByApiLevel() {
        if (Util.isBeforeM()) {
            for (String restriction : MNC_PLUS_RESTRICTIONS) {
                findPreference(restriction).setEnabled(false);
            }
        }
        if (Util.isBeforeN()) {
            for (String restriction : NYC_PLUS_RESTRICTIONS) {
                findPreference(restriction).setEnabled(false);
            }
        }
    }

    private void disableIncompatibleRestrictionsByUserType() {
        String pkgName = getActivity().getPackageName();
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(pkgName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(pkgName);
        if (isProfileOwner) {
            for (String restriction : PRIMARY_USER_ONLY_RESTRICTIONS) {
                findPreference(restriction).setEnabled(false);
            }
        } else if (isDeviceOwner) {
            for (String restriction : MANAGED_PROFILE_ONLY_RESTRICTIONS) {
                findPreference(restriction).setEnabled(false);
            }
        }

        if (Util.isManagedProfile(getActivity(), mAdminComponentName)) {
            for (String restriction : NON_MANAGED_PROFILE_RESTRICTIONS) {
                findPreference(restriction).setEnabled(false);
            }
        }
    }

    private static class UserRestriction {
        String key;
        int titleResId;
        public UserRestriction(String key, int titleResId) {
            this.key = key;
            this.titleResId = titleResId;
        }
    }
}
