package com.sample.android.testdpc.policy;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import com.sample.android.testdpc.DeviceAdminReceiver;
import com.sample.android.testdpc.R;

import static android.os.UserManager.ALLOW_PARENT_PROFILE_APP_LINKING;
import static android.os.UserManager.DISALLOW_ADD_USER;
import static android.os.UserManager.DISALLOW_ADJUST_VOLUME;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;
import static android.os.UserManager.DISALLOW_CONFIG_CELL_BROADCASTS;
import static android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;
import static android.os.UserManager.DISALLOW_CREATE_WINDOWS;
import static android.os.UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE;
import static android.os.UserManager.DISALLOW_FACTORY_RESET;
import static android.os.UserManager.DISALLOW_FUN;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA;
import static android.os.UserManager.DISALLOW_NETWORK_RESET;
import static android.os.UserManager.DISALLOW_OUTGOING_CALLS;
import static android.os.UserManager.DISALLOW_REMOVE_USER;
import static android.os.UserManager.DISALLOW_SAFE_BOOT;
import static android.os.UserManager.DISALLOW_SMS;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static android.os.UserManager.DISALLOW_USB_FILE_TRANSFER;

public class UserRestrictionsDisplayFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "UserRestrictions";

    private String[] mUserRestrictionKeys;
    private DevicePolicyManager mDevicePolicyManager;
    private UserManager mUserManager;
    private ComponentName mAdminComponentName;

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
     * These user restrictions are added in MNC.
     */
    private static String[] MNC_PLUS_RESTRICTIONS = {
            DISALLOW_SAFE_BOOT
    };

    public static UserRestrictionsDisplayFragment newInstance() {
        UserRestrictionsDisplayFragment fragment = new UserRestrictionsDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(
                getActivity());
        setPreferenceScreen(preferenceScreen);

        mUserRestrictionKeys = getActivity().getResources().getStringArray(
                R.array.user_restriction_keys);
        String[] userRestrictionTitles = getActivity().getResources().getStringArray(
                R.array.user_restriction_titles);
        final int N = mUserRestrictionKeys.length;
        for (int i = 0; i < N; ++i) {
            SwitchPreference preference = new SwitchPreference(getActivity());
            preference.setTitle(userRestrictionTitles[i]);
            preference.setKey(mUserRestrictionKeys[i]);
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
        getActivity().getActionBar().setTitle(R.string.user_restrictions_management_title);
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
        for (String restriction : mUserRestrictionKeys) {
            updateUserRestriction(restriction);
        }
    }

    private void updateUserRestriction(String userRestriction) {
        SwitchPreference preference = (SwitchPreference) findPreference(userRestriction);
        boolean disallowed = mUserManager.hasUserRestriction(userRestriction);
        preference.setChecked(disallowed);
    }

    private void disableIncompatibleRestrictionsByApiLevel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            for (String restriction : MNC_PLUS_RESTRICTIONS) {
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
    }
}
