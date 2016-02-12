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

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.AppInfoArrayAdapter;
import com.afwsamples.testdpc.common.MediaDisplayFragment;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.policy.ProcessLogsFragment;
import com.afwsamples.testdpc.policy.blockuninstallation.BlockUninstallationInfoArrayAdapter;
import com.afwsamples.testdpc.policy.certificate.DelegatedCertInstallerFragment;
import com.afwsamples.testdpc.policy.keyguard.LockScreenPolicyFragment;
import com.afwsamples.testdpc.policy.keyguard.PasswordConstraintsFragment;
import com.afwsamples.testdpc.policy.locktask.KioskModeActivity;
import com.afwsamples.testdpc.policy.locktask.LockTaskAppInfoArrayAdapter;
import com.afwsamples.testdpc.policy.networking.AlwaysOnVpnFragment;
import com.afwsamples.testdpc.policy.networking.NetworkUsageStatsFragment;
import com.afwsamples.testdpc.policy.systemupdatepolicy.SystemUpdatePolicyFragment;
import com.afwsamples.testdpc.policy.wifimanagement.WifiConfigCreationDialog;
import com.afwsamples.testdpc.policy.wifimanagement.WifiModificationFragment;
import com.afwsamples.testdpc.profilepolicy.ProfilePolicyManagementFragment;
import com.afwsamples.testdpc.profilepolicy.addsystemapps.EnableSystemAppsByIntentFragment;
import com.afwsamples.testdpc.profilepolicy.apprestrictions.AppRestrictionsManagingPackageFragment;
import com.afwsamples.testdpc.profilepolicy.apprestrictions.ManageAppRestrictionsFragment;
import com.afwsamples.testdpc.profilepolicy.permission.ManageAppPermissionsFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides several device management functions.
 *
 * These include:
 * <ul>
 * <li> {@link DevicePolicyManager#setLockTaskPackages(android.content.ComponentName, String[])} </li>
 * <li> {@link DevicePolicyManager#isLockTaskPermitted(String)} </li>
 * <li> {@link UserManager#DISALLOW_DEBUGGING_FEATURES} </li>
 * <li> {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES} </li>
 * <li> {@link UserManager#DISALLOW_REMOVE_USER} </li>
 * <li> {@link UserManager#DISALLOW_ADD_USER} </li>
 * <li> {@link UserManager#DISALLOW_FACTORY_RESET} </li>
 * <li> {@link UserManager#DISALLOW_CONFIG_CREDENTIALS} </li>
 * <li> {@link UserManager#DISALLOW_SHARE_LOCATION} </li>
 * <li> {@link UserManager#DISALLOW_CONFIG_TETHERING} </li>
 * <li> {@link UserManager#DISALLOW_ADJUST_VOLUME} </li>
 * <li> {@link UserManager#DISALLOW_UNMUTE_MICROPHONE} </li>
 * <li> {@link UserManager#DISALLOW_MODIFY_ACCOUNTS} </li>
 * <li> {@link UserManager#DISALLOW_SAFE_BOOT} </li>
 * <li> {@link UserManager#DISALLOW_OUTGOING_BEAM}} </li>
 * <li> {@link UserManager#DISALLOW_CREATE_WINDOWS}} </li>
 * <li> {@link DevicePolicyManager#clearDeviceOwnerApp(String)} </li>
 * <li> {@link DevicePolicyManager#getPermittedAccessibilityServices(android.content.ComponentName)}
 * </li>
 * <li> {@link DevicePolicyManager#getPermittedInputMethods(android.content.ComponentName)} </li>
 * <li> {@link DevicePolicyManager#setAccountManagementDisabled(android.content.ComponentName,
 *             String, boolean)} </li>
 * <li> {@link DevicePolicyManager#getAccountTypesWithManagementDisabled()} </li>
 * <li> {@link DevicePolicyManager#createAndInitializeUser(android.content.ComponentName, String,
 *             String, android.content.ComponentName, android.os.Bundle)} </li>
 * <li> {@link DevicePolicyManager#removeUser(android.content.ComponentName,
               android.os.UserHandle)} </li>
 * <li> {@link DevicePolicyManager#setUninstallBlocked(android.content.ComponentName, String,
 *             boolean)} </li>
 * <li> {@link DevicePolicyManager#isUninstallBlocked(android.content.ComponentName, String)} </li>
 * <li> {@link DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)} </li>
 * <li> {@link DevicePolicyManager#getCameraDisabled(android.content.ComponentName)} </li>
 * <li> {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName,
 *             android.content.Intent)} </li>
 * <li> {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName, String)} </li>
 * <li> {@link DevicePolicyManager#setApplicationRestrictions(android.content.ComponentName, String,
 *       android.os.Bundle)} </li>
 * <li> {@link DevicePolicyManager#installKeyPair(android.content.ComponentName,
 *             java.security.PrivateKey, java.security.cert.Certificate, String)} </li>
 * <li> {@link DevicePolicyManager#removeKeyPair(android.content.ComponentName, String)} </li>
 * <li> {@link DevicePolicyManager#installCaCert(android.content.ComponentName, byte[])} </li>
 * <li> {@link DevicePolicyManager#uninstallAllUserCaCerts(android.content.ComponentName)} </li>
 * <li> {@link DevicePolicyManager#getInstalledCaCerts(android.content.ComponentName)} </li>
 * <li> {@link DevicePolicyManager#setStatusBarDisabled(ComponentName, boolean)} </li>
 * <li> {@link DevicePolicyManager#setKeyguardDisabled(ComponentName, boolean)} </li>
 * <li> {@link DevicePolicyManager#setPermissionPolicy(android.content.ComponentName, int)} </li>
 * <li> {@link DevicePolicyManager#getPermissionPolicy(android.content.ComponentName)} </li>
 * <li> {@link DevicePolicyManager#setPermissionGrantState(ComponentName, String, String, int) (
 *        android.content.ComponentName, String, String, boolean)} </li>
 * <li> {@link DevicePolicyManager#setScreenCaptureDisabled(ComponentName, boolean)} </li>
 * <li> {@link DevicePolicyManager#getScreenCaptureDisabled(ComponentName)} </li>
 * <li> {@link DevicePolicyManager#setMaximumTimeToLock(ComponentName, long)} </li>
 * <li> {@link DevicePolicyManager#setMaximumFailedPasswordsForWipe(ComponentName, int)} </li>
 * <li> {@link DevicePolicyManager#setApplicationHidden(ComponentName, String, boolean)} </li>
 * <li> {@link DevicePolicyManager#setShortSupportMessage(ComponentName, String)} </li>
 * <li> {@link DevicePolicyManager#setLongSupportMessage(ComponentName, String)} </li>
 * <li> {@link UserManager#DISALLOW_CONFIG_WIFI} </li>
 * </ul>
 */
public class PolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final int INSTALL_KEY_CERTIFICATE_REQUEST_CODE = 7689;
    private static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 7690;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 7691;
    private static final int CAPTURE_VIDEO_REQUEST_CODE = 7692;

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String X509_CERT_TYPE = "X.509";
    public static final String TAG = "PolicyManagementFragment";

    public static final String OVERRIDE_KEY_SELECTION_KEY = "override_key_selection";

    private static final String APP_RESTRICTIONS_MANAGING_PACKAGE_KEY
            = "app_restrictions_managing_package";
    private static final String BLOCK_UNINSTALLATION_BY_PKG_KEY = "block_uninstallation_by_pkg";
    private static final String BLOCK_UNINSTALLATION_LIST_KEY = "block_uninstallation_list";
    private static final String CAPTURE_IMAGE_KEY = "capture_image";
    private static final String CAPTURE_VIDEO_KEY = "capture_video";
    private static final String CHECK_LOCK_TASK_PERMITTED_KEY = "check_lock_task_permitted";
    private static final String CREATE_AND_INITIALIZE_USER_KEY = "create_and_initialize_user";
    private static final String DELEGATED_CERT_INSTALLER_KEY = "manage_cert_installer";
    private static final String DEVICE_OWNER_STATUS_KEY = "device_owner_status";
    private static final String DISABLE_CAMERA_KEY = "disable_camera";
    private static final String DISABLE_KEYGUARD = "disable_keyguard";
    private static final String DISABLE_SCREEN_CAPTURE_KEY = "disable_screen_capture";
    private static final String DISABLE_STATUS_BAR = "disable_status_bar";
    private static final String ENABLE_PROCESS_LOGGING = "enable_process_logging";
    private static final String ENABLE_SYSTEM_APPS_BY_INTENT_KEY = "enable_system_apps_by_intent";
    private static final String ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY
            = "enable_system_apps_by_package_name";
    private static final String ENABLE_SYSTEM_APPS_KEY = "enable_system_apps";
    private static final String GET_CA_CERTIFICATES_KEY = "get_ca_certificates";
    private static final String GET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "get_disable_account_management";
    private static final String HIDE_APPS_KEY = "hide_apps";
    private static final String INSTALL_CA_CERTIFICATE_KEY = "install_ca_certificate";
    private static final String INSTALL_KEY_CERTIFICATE_KEY = "install_key_certificate";
    private static final String INSTALL_NONMARKET_APPS_KEY
            = "install_nonmarket_apps";
    private static final String KEYGUARD_PREFERENCES = "keyguard_preferences";
    private static final String LOCK_SCREEN_POLICY_KEY = "lock_screen_policy";
    private static final String MANAGE_APP_PERMISSIONS_KEY = "manage_app_permissions";
    private static final String MANAGE_APP_RESTRICTIONS_KEY = "manage_app_restrictions";
    private static final String MANAGED_PROFILE_SPECIFIC_POLICIES_KEY = "managed_profile_policies";
    private static final String MANAGE_LOCK_TASK_LIST_KEY = "manage_lock_task";
    private static final String MUTE_AUDIO_KEY = "mute_audio";
    private static final String NETWORK_STATS_KEY = "network_stats";
    private static final String PASSWORD_CONSTRAINTS_KEY = "password_constraints";
    private static final String REBOOT_KEY = "reboot";
    private static final String REENABLE_KEYGUARD = "reenable_keyguard";
    private static final String REENABLE_STATUS_BAR = "reenable_status_bar";
    private static final String REMOVE_ALL_CERTIFICATES_KEY = "remove_all_ca_certificates";
    private static final String REMOVE_DEVICE_OWNER_KEY = "remove_device_owner";
    private static final String REMOVE_KEY_CERTIFICATE_KEY = "remove_key_certificate";
    private static final String REMOVE_USER_KEY = "remove_user";
    private static final String REQUEST_BUGREPORT_KEY = "request_bugreport";
    private static final String REQUEST_PROCESS_LOGS = "request_process_logs";
    private static final String RESET_PASSWORD_KEY = "reset_password";
    private static final String SET_ACCESSIBILITY_SERVICES_KEY = "set_accessibility_services";
    private static final String SET_ALWAYS_ON_VPN_KEY = "set_always_on_vpn";
    private static final String SET_AUTO_TIME_REQUIRED_KEY = "set_auto_time_required";
    private static final String SET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "set_disable_account_management";
    private static final String SET_INPUT_METHODS_KEY = "set_input_methods";
    private static final String SET_LONG_SUPPORT_MESSAGE_KEY = "set_long_support_message";
    private static final String SET_ORGANIZATION_COLOR_KEY = "set_organization_color";
    private static final String SET_ORGANIZATION_NAME_KEY = "set_organization_name";
    private static final String SET_PERMISSION_POLICY_KEY = "set_permission_policy";
    private static final String SET_SHORT_SUPPORT_MESSAGE_KEY = "set_short_support_message";
    private static final String SET_USER_RESTRICTIONS_KEY = "set_user_restrictions";
    private static final String SHOW_WIFI_MAC_ADDRESS_KEY = "show_wifi_mac_address";
    private static final String START_KIOSK_MODE = "start_kiosk_mode";
    private static final String START_LOCK_TASK = "start_lock_task";
    private static final String STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in";
    private static final String STOP_LOCK_TASK = "stop_lock_task";
    private static final String SUSPEND_APPS_KEY = "suspend_apps";
    private static final String SYSTEM_UPDATE_POLICY_KEY = "system_update_policy";
    private static final String UNHIDE_APPS_KEY = "unhide_apps";
    private static final String UNSUSPEND_APPS_KEY = "unsuspend_apps";
    private static final String WIPE_DATA_KEY = "wipe_data";
    private static final String CREATE_WIFI_CONFIGURATION_KEY = "create_wifi_configuration";
    private static final String WIFI_CONFIG_LOCKDOWN_ENABLE_KEY = "enable_wifi_config_lockdown";
    private static final String MODIFY_WIFI_CONFIGURATION_KEY = "modify_wifi_configuration";
    private static final String TAG_WIFI_CONFIG_CREATION = "wifi_config_creation";
    private static final String WIFI_CONFIG_LOCKDOWN_ON = "1";
    private static final String WIFI_CONFIG_LOCKDOWN_OFF = "0";

    private static final long MS_PER_SECOND = 1000;

    private static final String BATTERY_PLUGGED_ANY = Integer.toString(
            BatteryManager.BATTERY_PLUGGED_AC |
            BatteryManager.BATTERY_PLUGGED_USB |
            BatteryManager.BATTERY_PLUGGED_WIRELESS);
    private static final String DONT_STAY_ON = "0";

    private static final String[] PRIMARY_USER_ONLY_PREFERENCES = {
            WIPE_DATA_KEY, REMOVE_DEVICE_OWNER_KEY, CREATE_AND_INITIALIZE_USER_KEY, REMOVE_USER_KEY,
            MANAGE_LOCK_TASK_LIST_KEY, CHECK_LOCK_TASK_PERMITTED_KEY, START_LOCK_TASK,
            STOP_LOCK_TASK, DISABLE_STATUS_BAR, REENABLE_STATUS_BAR, DISABLE_KEYGUARD,
            REENABLE_KEYGUARD, START_KIOSK_MODE, SYSTEM_UPDATE_POLICY_KEY, STAY_ON_WHILE_PLUGGED_IN,
            SHOW_WIFI_MAC_ADDRESS_KEY, REBOOT_KEY, REQUEST_BUGREPORT_KEY, ENABLE_PROCESS_LOGGING,
            REQUEST_PROCESS_LOGS, SET_AUTO_TIME_REQUIRED_KEY
    };

    private static String[] MNC_PLUS_PREFERENCES = {
            OVERRIDE_KEY_SELECTION_KEY, START_LOCK_TASK, STOP_LOCK_TASK, SYSTEM_UPDATE_POLICY_KEY,
            NETWORK_STATS_KEY, DELEGATED_CERT_INSTALLER_KEY, DISABLE_STATUS_BAR,
            REENABLE_STATUS_BAR, DISABLE_KEYGUARD, REENABLE_KEYGUARD, START_KIOSK_MODE,
            SET_PERMISSION_POLICY_KEY, MANAGE_APP_PERMISSIONS_KEY,STAY_ON_WHILE_PLUGGED_IN,
            WIFI_CONFIG_LOCKDOWN_ENABLE_KEY
    };

    private static String[] NYC_PLUS_PREFERENCES = {
            APP_RESTRICTIONS_MANAGING_PACKAGE_KEY, REBOOT_KEY, REMOVE_KEY_CERTIFICATE_KEY,
            SET_ALWAYS_ON_VPN_KEY, SHOW_WIFI_MAC_ADDRESS_KEY, SUSPEND_APPS_KEY, UNSUSPEND_APPS_KEY,
            SET_SHORT_SUPPORT_MESSAGE_KEY, SET_LONG_SUPPORT_MESSAGE_KEY, REQUEST_BUGREPORT_KEY,
            ENABLE_PROCESS_LOGGING, REQUEST_PROCESS_LOGS, SET_ORGANIZATION_COLOR_KEY,
            SET_ORGANIZATION_NAME_KEY
    };

    /**
     * Preferences that are allowed only in MNC+ if it profile owner. This does not restrict device
     * owner.
     */
    private static String[] MANAGED_PROFILE_MNC_PLUS_PREFERENCES = {
            KEYGUARD_PREFERENCES
    };

    private static String[] MANAGED_PROFILE_NYC_PLUS_PREFERENCES = {
            RESET_PASSWORD_KEY
    };

    private static final String[] MANAGED_PROFILE_SPECIFIC_OPTIONS = {
            MANAGED_PROFILE_SPECIFIC_POLICIES_KEY
    };

    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private String mPackageName;
    private ComponentName mAdminComponentName;
    private UserManager mUserManager;

    private SwitchPreference mDisableCameraSwitchPreference;
    private SwitchPreference mDisableScreenCaptureSwitchPreference;
    private SwitchPreference mMuteAudioSwitchPreference;

    private SwitchPreference mStayOnWhilePluggedInSwitchPreference;
    private SwitchPreference mInstallNonMarketAppsPreference;

    private SwitchPreference mEnableProcessLoggingPreference;
    private SwitchPreference mSetAutoTimeRequiredPreference;

    private GetAccessibilityServicesTask mGetAccessibilityServicesTask = null;
    private GetInputMethodsTask mGetInputMethodsTask = null;
    private ShowCaCertificateListTask mShowCaCertificateListTask = null;

    private Uri mImageUri;
    private Uri mVideoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mPackageManager = getActivity().getPackageManager();
        mPackageName = getActivity().getPackageName();

        mImageUri = getStorageUri("image.jpg");
        mVideoUri = getStorageUri("video.mp4");

        addPreferencesFromResource(R.xml.device_policy_header);

        EditTextPreference overrideKeySelectionPreference =
                (EditTextPreference) findPreference(OVERRIDE_KEY_SELECTION_KEY);
        overrideKeySelectionPreference.setOnPreferenceChangeListener(this);
        overrideKeySelectionPreference.setSummary(overrideKeySelectionPreference.getText());
        findPreference(MANAGE_LOCK_TASK_LIST_KEY).setOnPreferenceClickListener(this);
        findPreference(CHECK_LOCK_TASK_PERMITTED_KEY).setOnPreferenceClickListener(this);
        findPreference(START_LOCK_TASK).setOnPreferenceClickListener(this);
        findPreference(STOP_LOCK_TASK).setOnPreferenceClickListener(this);
        findPreference(CREATE_AND_INITIALIZE_USER_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_USER_KEY).setOnPreferenceClickListener(this);
        mDisableCameraSwitchPreference = (SwitchPreference) findPreference(DISABLE_CAMERA_KEY);
        findPreference(CAPTURE_IMAGE_KEY).setOnPreferenceClickListener(this);
        findPreference(CAPTURE_VIDEO_KEY).setOnPreferenceClickListener(this);
        mDisableCameraSwitchPreference.setOnPreferenceChangeListener(this);
        mDisableScreenCaptureSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_SCREEN_CAPTURE_KEY);
        mDisableScreenCaptureSwitchPreference.setOnPreferenceChangeListener(this);
        mMuteAudioSwitchPreference = (SwitchPreference) findPreference(
                MUTE_AUDIO_KEY);
        mMuteAudioSwitchPreference.setOnPreferenceChangeListener(this);
        findPreference(LOCK_SCREEN_POLICY_KEY).setOnPreferenceClickListener(this);
        findPreference(PASSWORD_CONSTRAINTS_KEY).setOnPreferenceClickListener(this);
        findPreference(RESET_PASSWORD_KEY).setOnPreferenceClickListener(this);
        findPreference(SYSTEM_UPDATE_POLICY_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_ALWAYS_ON_VPN_KEY).setOnPreferenceClickListener(this);
        findPreference(NETWORK_STATS_KEY).setOnPreferenceClickListener(this);
        findPreference(DELEGATED_CERT_INSTALLER_KEY).setOnPreferenceClickListener(this);
        findPreference(DISABLE_STATUS_BAR).setOnPreferenceClickListener(this);
        findPreference(REENABLE_STATUS_BAR).setOnPreferenceClickListener(this);
        findPreference(DISABLE_KEYGUARD).setOnPreferenceClickListener(this);
        findPreference(REENABLE_KEYGUARD).setOnPreferenceClickListener(this);
        findPreference(START_KIOSK_MODE).setOnPreferenceClickListener(this);
        mStayOnWhilePluggedInSwitchPreference = (SwitchPreference) findPreference(
                STAY_ON_WHILE_PLUGGED_IN);
        mStayOnWhilePluggedInSwitchPreference.setOnPreferenceChangeListener(this);
        findPreference(WIPE_DATA_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_DEVICE_OWNER_KEY).setOnPreferenceClickListener(this);
        findPreference(REQUEST_BUGREPORT_KEY).setOnPreferenceClickListener(this);
        mEnableProcessLoggingPreference = (SwitchPreference) findPreference(
                ENABLE_PROCESS_LOGGING);
        mEnableProcessLoggingPreference.setOnPreferenceChangeListener(this);
        findPreference(REQUEST_PROCESS_LOGS).setOnPreferenceClickListener(this);
        findPreference(SET_ACCESSIBILITY_SERVICES_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_INPUT_METHODS_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_BY_PKG_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_LIST_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_INTENT_KEY).setOnPreferenceClickListener(this);
        findPreference(HIDE_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(UNHIDE_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(SUSPEND_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(UNSUSPEND_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGE_APP_RESTRICTIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(APP_RESTRICTIONS_MANAGING_PACKAGE_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_KEY_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_KEY_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_CA_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_CA_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_ALL_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGED_PROFILE_SPECIFIC_POLICIES_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_PERMISSION_POLICY_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGE_APP_PERMISSIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(CREATE_WIFI_CONFIGURATION_KEY).setOnPreferenceClickListener(this);
        findPreference(WIFI_CONFIG_LOCKDOWN_ENABLE_KEY).setOnPreferenceChangeListener(this);
        findPreference(MODIFY_WIFI_CONFIGURATION_KEY).setOnPreferenceClickListener(this);
        findPreference(SHOW_WIFI_MAC_ADDRESS_KEY).setOnPreferenceClickListener(this);
        mInstallNonMarketAppsPreference = (SwitchPreference) findPreference(
                INSTALL_NONMARKET_APPS_KEY);
        mInstallNonMarketAppsPreference.setOnPreferenceChangeListener(this);
        findPreference(SET_USER_RESTRICTIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(REBOOT_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_SHORT_SUPPORT_MESSAGE_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_LONG_SUPPORT_MESSAGE_KEY).setOnPreferenceClickListener(this);
        mSetAutoTimeRequiredPreference = (SwitchPreference) findPreference(
                SET_AUTO_TIME_REQUIRED_KEY);
        mSetAutoTimeRequiredPreference.setOnPreferenceChangeListener(this);
        findPreference(SET_ORGANIZATION_COLOR_KEY).setOnPreferenceChangeListener(this);
        findPreference(SET_ORGANIZATION_NAME_KEY).setOnPreferenceChangeListener(this);

        disableIncompatibleManagementOptionsInCurrentProfile();
        disableIncompatibleManagementOptionsByApiLevel();

        reloadCameraDisableUi();
        reloadScreenCaptureDisableUi();
        reloadMuteAudioUi();
        reloadEnableProcessLoggingUi();
        reloadSetAutoTimeRequiredUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.policies_management);

        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(mPackageName);
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(mPackageName);
        if (!isDeviceOwner && !isProfileOwner) {
            showToast(R.string.this_is_not_a_device_owner);
            getActivity().finish();
        }

        // The settings might get changed outside the device policy app,
        // so, we need to make sure the preference gets updated accordingly.
        updateStayOnWhilePluggedInPreference();
        updateInstallNonMarketAppsPreference();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case MANAGE_LOCK_TASK_LIST_KEY:
                showManageLockTaskListPrompt(R.string.lock_task_title,
                        new ManageLockTaskListCallback() {
                            @Override
                            public void onPositiveButtonClicked(String[] lockTaskArray) {
                                mDevicePolicyManager.setLockTaskPackages(
                                        DeviceAdminReceiver.getComponentName(getActivity()),
                                        lockTaskArray);
                            }
                        }
                );
                return true;
            case CHECK_LOCK_TASK_PERMITTED_KEY:
                showCheckLockTaskPermittedPrompt();
                return true;
            case RESET_PASSWORD_KEY:
                showResetPasswordPrompt();
                return false;
            case START_LOCK_TASK:
                getActivity().startLockTask();
                return true;
            case STOP_LOCK_TASK:
                try {
                    getActivity().stopLockTask();
                } catch (IllegalStateException e) {
                    // no lock task present, ignore
                }
                return true;
            case WIPE_DATA_KEY:
                showWipeDataPrompt();
                return true;
            case REMOVE_DEVICE_OWNER_KEY:
                showRemoveDeviceOwnerPrompt();
                return true;
            case REQUEST_BUGREPORT_KEY:
                boolean startedSuccessfully = mDevicePolicyManager.requestBugreport(
                        mAdminComponentName);
                if (!startedSuccessfully) {
                    Context context = getContext();
                    Util.showNotification(context, R.string.bugreport_title,
                            context.getString(R.string.bugreport_failure_throttled),
                            Util.BUGREPORT_NOTIFICATION_ID);
                }
                return true;
            case REQUEST_PROCESS_LOGS:
                showFragment(new ProcessLogsFragment());
                return true;
            case SET_ACCESSIBILITY_SERVICES_KEY:
                // Avoid starting the same task twice.
                if (mGetAccessibilityServicesTask != null && !mGetAccessibilityServicesTask
                        .isCancelled()) {
                    mGetAccessibilityServicesTask.cancel(true);
                }
                mGetAccessibilityServicesTask = new GetAccessibilityServicesTask();
                mGetAccessibilityServicesTask.execute();
                return true;
            case SET_INPUT_METHODS_KEY:
                // Avoid starting the same task twice.
                if (mGetInputMethodsTask != null && !mGetInputMethodsTask.isCancelled()) {
                    mGetInputMethodsTask.cancel(true);
                }
                mGetInputMethodsTask = new GetInputMethodsTask();
                mGetInputMethodsTask.execute();
                return true;
            case SET_DISABLE_ACCOUNT_MANAGEMENT_KEY:
                showSetDisableAccountManagementPrompt();
                return true;
            case GET_DISABLE_ACCOUNT_MANAGEMENT_KEY:
                showDisableAccountTypeList();
                return true;
            case CREATE_AND_INITIALIZE_USER_KEY:
                showCreateUserPrompt();
                return true;
            case REMOVE_USER_KEY:
                showRemoveUserPrompt();
                return true;
            case BLOCK_UNINSTALLATION_BY_PKG_KEY:
                showBlockUninstallationByPackageNamePrompt();
                return true;
            case BLOCK_UNINSTALLATION_LIST_KEY:
                showBlockUninstallationPrompt();
                return true;
            case ENABLE_SYSTEM_APPS_KEY:
                showEnableSystemAppsPrompt();
                return true;
            case ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY:
                showEnableSystemAppByPackageNamePrompt();
                return true;
            case ENABLE_SYSTEM_APPS_BY_INTENT_KEY:
                showFragment(new EnableSystemAppsByIntentFragment());
                return true;
            case HIDE_APPS_KEY:
                showHideAppsPrompt(false);
                return true;
            case UNHIDE_APPS_KEY:
                showHideAppsPrompt(true);
                return true;
            case SUSPEND_APPS_KEY:
                showSuspendAppsPrompt(false);
                return true;
            case UNSUSPEND_APPS_KEY:
                showSuspendAppsPrompt(true);
                return true;
            case MANAGE_APP_RESTRICTIONS_KEY:
                showFragment(new ManageAppRestrictionsFragment());
                return true;
            case APP_RESTRICTIONS_MANAGING_PACKAGE_KEY:
                showFragment(new AppRestrictionsManagingPackageFragment());
                return true;
            case SET_PERMISSION_POLICY_KEY:
                showSetPermissionPolicyDialog();
                return true;
            case MANAGE_APP_PERMISSIONS_KEY:
                showFragment(new ManageAppPermissionsFragment());
                return true;
            case INSTALL_KEY_CERTIFICATE_KEY:
                showFileViewerForImportingCertificate(INSTALL_KEY_CERTIFICATE_REQUEST_CODE);
                return true;
            case REMOVE_KEY_CERTIFICATE_KEY:
                choosePrivateKeyForRemoval();
                return true;
            case INSTALL_CA_CERTIFICATE_KEY:
                showFileViewerForImportingCertificate(INSTALL_CA_CERTIFICATE_REQUEST_CODE);
                return true;
            case GET_CA_CERTIFICATES_KEY:
                showCaCertificateList();
                return true;
            case REMOVE_ALL_CERTIFICATES_KEY:
                mDevicePolicyManager.uninstallAllUserCaCerts(mAdminComponentName);
                showToast(R.string.all_ca_certificates_removed);
                return true;
            case MANAGED_PROFILE_SPECIFIC_POLICIES_KEY:
                showFragment(new ProfilePolicyManagementFragment());
                return true;
            case LOCK_SCREEN_POLICY_KEY:
                showFragment(new LockScreenPolicyFragment.Container());
                return true;
            case PASSWORD_CONSTRAINTS_KEY:
                showFragment(new PasswordConstraintsFragment.Container());
                return true;
            case SYSTEM_UPDATE_POLICY_KEY:
                showFragment(new SystemUpdatePolicyFragment());
                return true;
            case SET_ALWAYS_ON_VPN_KEY:
                showFragment(new AlwaysOnVpnFragment());
                return true;
            case NETWORK_STATS_KEY:
                showFragment(new NetworkUsageStatsFragment());
                return true;
            case DELEGATED_CERT_INSTALLER_KEY:
                showFragment(new DelegatedCertInstallerFragment());
                return true;
            case DISABLE_STATUS_BAR:
                if (!mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, true)) {
                    showToast("Unable to disable status bar when lock password is set.");
                }
                return true;
            case REENABLE_STATUS_BAR:
                mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, false);
                return true;
            case DISABLE_KEYGUARD:
                if (!mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, true)) {
                    // this should not happen
                    showToast("Unable to disable keyguard");
                }
                return true;
            case REENABLE_KEYGUARD:
                mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, false);
                return true;
            case START_KIOSK_MODE:
                showManageLockTaskListPrompt(R.string.kiosk_select_title,
                        new ManageLockTaskListCallback() {
                            @Override
                            public void onPositiveButtonClicked(String[] lockTaskArray) {
                                startKioskMode(lockTaskArray);
                            }
                        }
                );
                return true;
            case CAPTURE_IMAGE_KEY:
                dispatchCaptureIntent(MediaStore.ACTION_IMAGE_CAPTURE,
                        CAPTURE_IMAGE_REQUEST_CODE, mImageUri);
                return true;
            case CAPTURE_VIDEO_KEY:
                dispatchCaptureIntent(MediaStore.ACTION_VIDEO_CAPTURE,
                        CAPTURE_VIDEO_REQUEST_CODE, mVideoUri);
                return true;
            case CREATE_WIFI_CONFIGURATION_KEY:
                showWifiConfigCreationDialog();
                return true;
            case MODIFY_WIFI_CONFIGURATION_KEY:
                showFragment(new WifiModificationFragment());
                return true;
            case SHOW_WIFI_MAC_ADDRESS_KEY:
                showWifiMacAddress();
                return true;
            case SET_USER_RESTRICTIONS_KEY:
                showFragment(new UserRestrictionsDisplayFragment());
                return true;
            case REBOOT_KEY:
                reboot();
                return true;
            case SET_SHORT_SUPPORT_MESSAGE_KEY:
                showFragment(SetSupportMessageFragment.newInstance(
                        SetSupportMessageFragment.TYPE_SHORT));
                return true;
            case SET_LONG_SUPPORT_MESSAGE_KEY:
                showFragment(SetSupportMessageFragment.newInstance(
                        SetSupportMessageFragment.TYPE_LONG));
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        switch (key) {
            case OVERRIDE_KEY_SELECTION_KEY:
                preference.setSummary((String) newValue);
                return true;
            case DISABLE_CAMERA_KEY:
                mDevicePolicyManager.setCameraDisabled(mAdminComponentName, (Boolean) newValue);
                // Reload UI to verify the camera is enable / disable correctly.
                reloadCameraDisableUi();
                return true;
            case ENABLE_PROCESS_LOGGING:
                mDevicePolicyManager.setDeviceLoggingEnabled(mAdminComponentName,
                        (Boolean) newValue);
                reloadEnableProcessLoggingUi();
                return true;
            case DISABLE_SCREEN_CAPTURE_KEY:
                mDevicePolicyManager.setScreenCaptureDisabled(mAdminComponentName,
                        (Boolean) newValue);
                // Reload UI to verify that screen capture was enabled / disabled correctly.
                reloadScreenCaptureDisableUi();
                return true;
            case MUTE_AUDIO_KEY:
                mDevicePolicyManager.setMasterVolumeMuted(mAdminComponentName,
                        (Boolean) newValue);
                reloadMuteAudioUi();
                return true;
            case STAY_ON_WHILE_PLUGGED_IN:
                mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                        Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                        newValue.equals(true) ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
                updateStayOnWhilePluggedInPreference();
                return true;
            case WIFI_CONFIG_LOCKDOWN_ENABLE_KEY:
                mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                        Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN,
                        newValue.equals(Boolean.TRUE) ?
                                WIFI_CONFIG_LOCKDOWN_ON : WIFI_CONFIG_LOCKDOWN_OFF);
                return true;
            case INSTALL_NONMARKET_APPS_KEY:
                mDevicePolicyManager.setSecureSetting(mAdminComponentName,
                        Settings.Secure.INSTALL_NON_MARKET_APPS,
                        newValue.equals(true) ? "1" : "0");
                updateInstallNonMarketAppsPreference();
                return true;
            case SET_AUTO_TIME_REQUIRED_KEY:
                mDevicePolicyManager.setAutoTimeRequired(mAdminComponentName,
                        newValue.equals(true));
                reloadSetAutoTimeRequiredUi();
                return true;
            case SET_ORGANIZATION_COLOR_KEY:
                if (!TextUtils.isEmpty((String) newValue)) {
                    int organizationColor;
                    try {
                        organizationColor = Color.parseColor((String) newValue);
                    } catch (IllegalArgumentException e) {
                        showToast(R.string.color_not_recognized);
                        return false;
                    }
                    mDevicePolicyManager.setOrganizationColor(mAdminComponentName,
                            organizationColor);
                    return true;
                }
                showToast(R.string.empty_color_error);
                return false;
            case SET_ORGANIZATION_NAME_KEY:
                mDevicePolicyManager.setOrganizationName(mAdminComponentName, (String) newValue);
                return true;
        }
        return false;
    }

    /**
     * Dispatches an intent to capture image or video.
     */
    private void dispatchCaptureIntent(String action, int requestCode, Uri storageUri) {
        final Intent captureIntent = new Intent(action);
        if (captureIntent.resolveActivity(mPackageManager) != null) {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, storageUri);
            startActivityForResult(captureIntent, requestCode);
        } else {
            showToast(R.string.camera_app_not_found);
        }
    }

    /**
     * Creates a content uri to be used with the capture intent.
     */
    private Uri getStorageUri(String fileName) {
        final String filePath = getActivity().getFilesDir() + File.separator + "media"
                + File.separator + fileName;
        final File file = new File(filePath);
        // Create the folder if it doesn't exist.
        file.getParentFile().mkdirs();
        return FileProvider.getUriForFile(getActivity(),
                "com.afwsamples.testdpc.fileprovider", file);
    }

    /**
     * Shows a list of primary user apps in a dialog.
     *
     * @param dialogTitle the title to show for the dialog
     * @param callback will be called with the list apps that the user has selected when he closes
     *        the dialog. The callback is not fired if the user cancels.
     */
    private void showManageLockTaskListPrompt(int dialogTitle,
            final ManageLockTaskListCallback callback) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> primaryUserAppList = mPackageManager
                .queryIntentActivities(launcherIntent, 0);
        if (primaryUserAppList.isEmpty()) {
            showToast(R.string.no_primary_app_available);
        } else {
            Collections.sort(primaryUserAppList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));
            final LockTaskAppInfoArrayAdapter appInfoArrayAdapter = new LockTaskAppInfoArrayAdapter(
                    getActivity(), R.id.pkg_name, primaryUserAppList);
            ListView listView = new ListView(getActivity());
            listView.setAdapter(appInfoArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    appInfoArrayAdapter.onItemClick(parent, view, position, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(dialogTitle))
                    .setView(listView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] lockTaskEnabledArray = appInfoArrayAdapter.getLockTaskList();
                            callback.onPositiveButtonClicked(lockTaskEnabledArray);
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
     * Shows a prompt to collect a package name and checks whether the lock task for the
     * corresponding app is permitted or not.
     */
    private void showCheckLockTaskPermittedPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = getActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(getString(R.string.input_package_name_hints));

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.check_lock_task_permitted))
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String packageName = input.getText().toString();
                        boolean isLockTaskPermitted = mDevicePolicyManager
                                .isLockTaskPermitted(packageName);
                        showToast(isLockTaskPermitted
                                ? R.string.check_lock_task_permitted_result_permitted
                                : R.string.check_lock_task_permitted_result_not_permitted);
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

    /**
     * Shows a prompt to ask for a password to reset to and to set whether this requires
     * re-entry before any further changes and/or whether the password needs to be entered during
     * boot to start the user.
     */
    private void showResetPasswordPrompt() {
        View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.reset_password_dialog, null);

        final EditText passwordView = (EditText) dialogView.findViewById(
                R.id.password);
        final CheckBox requireEntry = (CheckBox) dialogView.findViewById(
                R.id.require_password_entry_checkbox);
        final CheckBox requireOnBoot = (CheckBox) dialogView.findViewById(
                R.id.require_password_on_boot_checkbox);

        DialogInterface.OnClickListener resetListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String password = passwordView.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    password = null;
                }

                int flags = 0;
                flags |= requireEntry.isChecked() ?
                        DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY : 0;
                flags |= requireOnBoot.isChecked() ?
                        DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT : 0;

                boolean ok = false;
                try {
                    ok = mDevicePolicyManager.resetPassword(password, flags);
                } catch (IllegalArgumentException iae) {
                    // Bad password, eg. 2 characters where system minimum length is 4.
                    Log.w(TAG, "Failed to reset password", iae);
                }
                showToast(ok ? R.string.password_reset_success : R.string.password_reset_failed);
            }
        };

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reset_password)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, resetListener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows a prompt to ask for confirmation on wiping the data and also provide an option
     * to set if external storage and factory reset protection data also needs to wiped.
     */
    private void showWipeDataPrompt() {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.wipe_data_dialog_prompt, null);
        final CheckBox externalStorageCheckBox = (CheckBox) dialogView.findViewById(
                R.id.external_storage_checkbox);
        final CheckBox resetProtectionCheckBox = (CheckBox) dialogView.findViewById(
                R.id.reset_protection_checkbox);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.wipe_data_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int flags = 0;
                                flags |= (externalStorageCheckBox.isChecked() ?
                                        DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
                                flags |= (resetProtectionCheckBox.isChecked() ?
                                        DevicePolicyManager.WIPE_RESET_PROTECTION_DATA : 0);
                                mDevicePolicyManager.wipeData(flags);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows a prompt to ask for confirmation on removing device owner.
     */
    private void showRemoveDeviceOwnerPrompt() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_device_owner_title)
                .setMessage(R.string.remove_device_owner_confirmation)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDevicePolicyManager.clearDeviceOwnerApp(mPackageName);
                                if (getActivity() != null && !getActivity().isFinishing()) {
                                    showToast(R.string.device_owner_removed);
                                    getActivity().finish();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows a message box with the device wifi mac address.
     */
    private void showWifiMacAddress() {
        final String macAddress = mDevicePolicyManager.getWifiMacAddress();
        final String message = macAddress != null ? macAddress
                : getResources().getString(R.string.show_wifi_mac_address_not_available_msg);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.show_wifi_mac_address_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void setPreferenceChangeListeners(String[] preferenceKeys) {
        for (String key : preferenceKeys) {
            findPreference(key).setOnPreferenceChangeListener(this);
        }
    }

    /**
     * Update the preference switch for {@link Settings.Global#STAY_ON_WHILE_PLUGGED_IN} setting.
     *
     * <p>
     * If either one of the {@link BatteryManager#BATTERY_PLUGGED_AC},
     * {@link BatteryManager#BATTERY_PLUGGED_USB}, {@link BatteryManager#BATTERY_PLUGGED_WIRELESS}
     * values is set, we toggle the preference to true and update the setting value to
     * {@link #BATTERY_PLUGGED_ANY}
     * </p>
     */
    private void updateStayOnWhilePluggedInPreference() {
        if (!mStayOnWhilePluggedInSwitchPreference.isEnabled()) {
            return;
        }

        boolean checked = false;
        final int currentState = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
        checked = (currentState &
                (BatteryManager.BATTERY_PLUGGED_AC |
                BatteryManager.BATTERY_PLUGGED_USB |
                BatteryManager.BATTERY_PLUGGED_WIRELESS)) != 0;
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                checked ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
        mStayOnWhilePluggedInSwitchPreference.setChecked(checked);
    }

    /**
     * Update the preference switch for {@link Settings.Secure#INSTALL_NON_MARKET_APPS} setting.
     *
     * <p>
     * If the user restriction {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES} is set, then
     * we disable this preference.
     * </p>
     */
    public void updateInstallNonMarketAppsPreference() {
        mInstallNonMarketAppsPreference.setEnabled(
                mUserManager.hasUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES) ? false : true);
        int isInstallNonMarketAppsAllowed = Settings.Secure.getInt(
                getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        mInstallNonMarketAppsPreference.setChecked(
                isInstallNonMarketAppsAllowed == 0 ? false : true);
    }

    /**
     * Some functionality only works if this app is device owner. Disable their UIs to avoid
     * confusion.
     */
    private void disableIncompatibleManagementOptionsInCurrentProfile() {
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(mPackageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(mPackageName);
        int deviceOwnerStatusStringId = R.string.this_is_not_a_device_owner;
        if (isProfileOwner) {
            // Some of the management options can only be applied in a primary profile.
            for (String preference : PRIMARY_USER_ONLY_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
            if (Util.isBeforeM()) {
                for (String preference : MANAGED_PROFILE_MNC_PLUS_PREFERENCES) {
                    findPreference(preference).setEnabled(false);
                }
            }
            if (Util.isBeforeN()) {
                for (String preference : MANAGED_PROFILE_NYC_PLUS_PREFERENCES) {
                    findPreference(preference).setEnabled(false);
                }
            }
            deviceOwnerStatusStringId = R.string.this_is_a_managed_profile_owner;
        } else if (isDeviceOwner) {
            // If it's a device owner and running in the primary profile.
            deviceOwnerStatusStringId = R.string.this_is_a_device_owner;
            for (String managedProfileSpecificOption : MANAGED_PROFILE_SPECIFIC_OPTIONS) {
                findPreference(managedProfileSpecificOption).setEnabled(false);
            }
        }
        findPreference(DEVICE_OWNER_STATUS_KEY).setSummary(deviceOwnerStatusStringId);
        if (!isDeviceOwner) {
            findPreference(WIFI_CONFIG_LOCKDOWN_ENABLE_KEY).setEnabled(false);
        }
    }

    private void disableIncompatibleManagementOptionsByApiLevel() {
        if (Util.isBeforeM()) {
            // The following options depend on MNC APIs.
            for (String preference : MNC_PLUS_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
        }
        if (Util.isBeforeN()) {
            for (String preference : NYC_PLUS_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
        }
    }

    /**
     * Shows the default response for future runtime permission requests by applications, and lets
     * the user change the default value.
     */
    private void showSetPermissionPolicyDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View setPermissionPolicyView = getActivity().getLayoutInflater().inflate(
                R.layout.set_permission_policy, null);
        final RadioGroup permissionGroup =
                (RadioGroup) setPermissionPolicyView.findViewById(R.id.set_permission_group);

        int permissionPolicy = mDevicePolicyManager.getPermissionPolicy(mAdminComponentName);
        switch (permissionPolicy) {
            case DevicePolicyManager.PERMISSION_POLICY_PROMPT:
                ((RadioButton) permissionGroup.findViewById(R.id.prompt)).toggle();
                break;
            case DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT:
                ((RadioButton) permissionGroup.findViewById(R.id.accept)).toggle();
                break;
            case DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY:
                ((RadioButton) permissionGroup.findViewById(R.id.deny)).toggle();
                break;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.set_default_permission_policy))
                .setView(setPermissionPolicyView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int policy = 0;
                        int checked = permissionGroup.getCheckedRadioButtonId();
                        switch (checked) {
                            case (R.id.prompt):
                                policy = DevicePolicyManager.PERMISSION_POLICY_PROMPT;
                                break;
                            case (R.id.accept):
                                policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT;
                                break;
                            case (R.id.deny):
                                policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY;
                                break;
                        }
                        mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, policy);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows a prompt that allows entering the account type for which account management should be
     * disabled or enabled.
     */
    private void showSetDisableAccountManagementPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.account_type_hint);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.set_disable_account_management)
                .setView(view)
                .setPositiveButton(R.string.disable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String accountType = input.getText().toString();
                        setDisableAccountManagement(accountType, true);
                    }
                })
                .setNeutralButton(R.string.enable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String accountType = input.getText().toString();
                        setDisableAccountManagement(accountType, false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null /* Nothing to do */)
                .show();
    }

    private void setDisableAccountManagement(String accountType, boolean disabled) {
        if (!TextUtils.isEmpty(accountType)) {
            mDevicePolicyManager.setAccountManagementDisabled(mAdminComponentName, accountType,
                    disabled);
            showToast(disabled
                            ? R.string.account_management_disabled
                            : R.string.account_management_enabled,
                    accountType);
            return;
        }
        showToast(R.string.fail_to_set_account_management);
    }

    /**
     * Shows a list of account types that is disabled for account management.
     */
    private void showDisableAccountTypeList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        String[] disabledAccountTypeList = mDevicePolicyManager
                .getAccountTypesWithManagementDisabled();
        Arrays.sort(disabledAccountTypeList, String.CASE_INSENSITIVE_ORDER);
        if (disabledAccountTypeList == null || disabledAccountTypeList.length == 0) {
            showToast(R.string.no_disabled_account);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.list_of_disabled_account_types)
                    .setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1, android.R.id.text1,
                            disabledAccountTypeList), null)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    /**
     * For user creation:
     * Shows a prompt to ask for the username that would be used for creating a new user.
     */
    private void showCreateUserPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.enter_username_hint);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_and_initialize_user)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = input.getText().toString();
                        String ownerName = getString(R.string.app_name);
                        if (!TextUtils.isEmpty(name)) {
                            UserHandle userHandle = mDevicePolicyManager.createAndInitializeUser(
                                    mAdminComponentName, name, ownerName, mAdminComponentName,
                                    new Bundle());
                            if (userHandle != null) {
                                long serialNumber = mUserManager.getSerialNumberForUser(userHandle);
                                showToast(R.string.user_created, serialNumber);
                                return;
                            }
                            showToast(R.string.failed_to_create_user);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * For user removal:
     * Shows a prompt for a user serial number. The associated user will be removed.
     */
    private void showRemoveUserPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.enter_user_id);
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_user)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean success = false;
                        long serialNumber = -1;
                        try {
                            serialNumber = Long.parseLong(input.getText().toString());
                            UserHandle userHandle = mUserManager
                                    .getUserForSerialNumber(serialNumber);
                            if (userHandle != null) {
                                success = mDevicePolicyManager
                                        .removeUser(mAdminComponentName, userHandle);
                            }
                        } catch (NumberFormatException e) {
                            // Error message is printed in the next line.
                        }
                        showToast(success ? R.string.user_removed : R.string.failed_to_remove_user);
                    }
                })
                .show();
    }

    /**
     * Asks for the package name whose uninstallation should be blocked / unblocked.
     */
    private void showBlockUninstallationByPackageNamePrompt() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(getString(R.string.input_package_name_hints));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.block_uninstallation_title)
                .setView(view)
                .setPositiveButton(R.string.block, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pkgName = input.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)) {
                            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName,
                                    true);
                            showToast(R.string.uninstallation_blocked, pkgName);
                        } else {
                            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
                        }
                    }
                })
                .setNeutralButton(R.string.unblock, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pkgName = input.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)) {
                            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName,
                                    false);
                            showToast(R.string.uninstallation_allowed, pkgName);
                        } else {
                            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void reloadCameraDisableUi() {
        boolean isCameraDisabled = mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
        mDisableCameraSwitchPreference.setChecked(isCameraDisabled);
    }

    private void reloadEnableProcessLoggingUi() {
        if (mEnableProcessLoggingPreference.isEnabled()) {
            boolean isProcessLoggingEnabled = mDevicePolicyManager.getDeviceLoggingEnabled(
                    mAdminComponentName);
            mEnableProcessLoggingPreference.setChecked(isProcessLoggingEnabled);
            findPreference(REQUEST_PROCESS_LOGS).setEnabled(isProcessLoggingEnabled);
        }
    }

    private void reloadScreenCaptureDisableUi() {
        boolean isScreenCaptureDisabled = mDevicePolicyManager.getScreenCaptureDisabled(
                mAdminComponentName);
        mDisableScreenCaptureSwitchPreference.setChecked(isScreenCaptureDisabled);
    }

    private void reloadSetAutoTimeRequiredUi() {
        if (mDevicePolicyManager.isDeviceOwnerApp(mPackageName)) {
            boolean isAutoTimeRequired = mDevicePolicyManager.getAutoTimeRequired();
            mSetAutoTimeRequiredPreference.setChecked(isAutoTimeRequired);
        }
    }

    private void reloadMuteAudioUi() {
        final boolean isAudioMuted = mDevicePolicyManager.isMasterVolumeMuted(mAdminComponentName);
        mMuteAudioSwitchPreference.setChecked(isAudioMuted);
    }

    /**
     * Shows a prompt to ask for package name which is used to enable a system app.
     */
    private void showEnableSystemAppByPackageNamePrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        LinearLayout inputContainer = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.simple_edittext, null);
        final EditText editText = (EditText) inputContainer.findViewById(R.id.input);
        editText.setHint(getString(R.string.enable_system_apps_by_package_name_hints));

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.enable_system_apps_title))
                .setView(inputContainer)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String packageName = editText.getText().toString();
                        try {
                            mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                            showToast(R.string.enable_system_apps_by_package_name_success_msg,
                                    packageName);
                        } catch (IllegalArgumentException e) {
                            showToast(R.string.enable_system_apps_by_package_name_error);
                        } finally {
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows the file viewer for importing a certificate.
     */
    private void showFileViewerForImportingCertificate(int requestCode) {
        Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
        certIntent.setTypeAndNormalize("*/*");
        try {
            startActivityForResult(certIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Imports a certificate to the managed profile. If the provided password failed to decrypt the
     * given certificate, shows a try again prompt. Otherwise, shows a prompt for the certificate
     * alias.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param password The password to decrypt the certificate.
     */
    private void importKeyCertificateFromIntent(Intent intent, String password) {
        importKeyCertificateFromIntent(intent, password, 0 /* first try */);
    }

    /**
     * Imports a certificate to the managed profile. If the provided decryption password is
     * incorrect, shows a try again prompt. Otherwise, shows a prompt for the certificate alias.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param password The password to decrypt the certificate.
     * @param attempts The number of times user entered incorrect password.
     */
    private void importKeyCertificateFromIntent(Intent intent, String password, int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Uri data = null;
        if (intent != null && (data = intent.getData()) != null) {
            // If the password is null, try to decrypt the certificate with an empty password.
            if (password == null) {
                password = "";
            }
            InputStream certificateInputStream;
            try {
                certificateInputStream = getActivity().getContentResolver().openInputStream(data);
                KeyStore keyStore = KeyStore.getInstance(KeyChain.EXTRA_PKCS12);
                keyStore.load(certificateInputStream, password.toCharArray());
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (!TextUtils.isEmpty(alias)) {
                        Certificate certificate = keyStore.getCertificate(alias);
                        PrivateKey privateKey = (PrivateKey) keyStore
                                .getKey(alias, "".toCharArray());
                        showPromptForKeyCertificateAlias(privateKey, certificate, alias);
                    }
                }
            } catch (KeyStoreException | FileNotFoundException | CertificateException
                    | UnrecoverableKeyException | NoSuchAlgorithmException e) {
                Log.e(TAG, "Unable to load key", e);
            } catch (IOException e) {
                showPromptForCertificatePassword(intent, ++attempts);
            } catch (ClassCastException e) {
                showToast(R.string.not_a_key_certificate);
            }
        }
    }

    /**
     * Shows a prompt to ask for the certificate password. If the certificate password is correct,
     * import the private key and certificate.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param attempts The number of times user entered incorrect password.
     */
    private void showPromptForCertificatePassword(final Intent intent, final int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View passwordInputView = getActivity().getLayoutInflater()
                .inflate(R.layout.certificate_password_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.password_input);
        if (attempts > 1) {
            passwordInputView.findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.certificate_password_prompt_title))
                .setView(passwordInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userPassword = input.getText().toString();
                        importKeyCertificateFromIntent(intent, userPassword, attempts);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Shows a prompt to ask for the certificate alias. This alias will be imported together with
     * the private key and certificate.
     *
     * @param key The private key of a certificate.
     * @param certificate The certificate will be imported.
     * @param alias A name that represents the certificate in the profile.
     */
    private void showPromptForKeyCertificateAlias(final PrivateKey key,
            final Certificate certificate, String alias) {
        if (getActivity() == null || getActivity().isFinishing() || key == null
                || certificate == null) {
            return;
        }
        View passwordInputView = getActivity().getLayoutInflater().inflate(
                R.layout.certificate_alias_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.alias_input);
        if (!TextUtils.isEmpty(alias)) {
            input.setText(alias);
            input.selectAll();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.certificate_alias_prompt_title))
                .setView(passwordInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alias = input.getText().toString();
                        if (mDevicePolicyManager.installKeyPair(mAdminComponentName, key,
                                certificate, alias) == true) {
                            showToast(R.string.certificate_added, alias);
                        } else {
                            showToast(R.string.certificate_add_failed, alias);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Selects a private/public key pair to uninstall, using the system dialog to choose
     * an alias.
     *
     * Once the alias is chosen and deleted, a {@link Toast} shows status- success or failure.
     */
    private void choosePrivateKeyForRemoval() {
        KeyChain.choosePrivateKeyAlias(getActivity(), new KeyChainAliasCallback() {
            @Override
            public void alias(String alias) {
                if (alias == null) {
                    // No value was chosen.
                    return;
                }

                final boolean removed =
                        mDevicePolicyManager.removeKeyPair(mAdminComponentName, alias);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (removed) {
                            showToast(R.string.remove_keypair_successfully);
                        } else {
                            showToast(R.string.remove_keypair_fail);
                        }
                    }
                });
            }
        }, /* keyTypes[] */ null, /* issuers[] */ null, /* uri */ null, /* alias */ null);
    }

    /**
     * Imports a CA certificate from the given data URI.
     *
     * @param intent Intent that contains the CA data URI.
     */
    private void importCaCertificateFromIntent(Intent intent) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Uri data = null;
        if (intent != null && (data = intent.getData()) != null) {
            boolean isCaInstalled = false;
            try {
                InputStream certificateInputStream = getActivity().getContentResolver()
                        .openInputStream(data);
                if (certificateInputStream != null) {
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int len = 0;
                    while ((len = certificateInputStream.read(buffer)) > 0) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    isCaInstalled = mDevicePolicyManager.installCaCert(mAdminComponentName,
                            byteBuffer.toByteArray());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            showToast(isCaInstalled ? R.string.install_ca_successfully : R.string.install_ca_fail);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case INSTALL_KEY_CERTIFICATE_REQUEST_CODE:
                    importKeyCertificateFromIntent(data, "");
                    break;
                case INSTALL_CA_CERTIFICATE_REQUEST_CODE:
                    importCaCertificateFromIntent(data);
                    break;
                case CAPTURE_IMAGE_REQUEST_CODE:
                    showFragment(MediaDisplayFragment.newInstance(
                            MediaDisplayFragment.REQUEST_DISPLAY_IMAGE, mImageUri));
                    break;
                case CAPTURE_VIDEO_REQUEST_CODE:
                    showFragment(MediaDisplayFragment.newInstance(
                            MediaDisplayFragment.REQUEST_DISPLAY_VIDEO, mVideoUri));
                    break;
            }
        }
    }

    /**
     * Shows a list of installed CA certificates.
     */
    private void showCaCertificateList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        // Avoid starting the same task twice.
        if (mShowCaCertificateListTask != null && !mShowCaCertificateListTask.isCancelled()) {
            mShowCaCertificateListTask.cancel(true);
        }
        mShowCaCertificateListTask = new ShowCaCertificateListTask();
        mShowCaCertificateListTask.execute();
    }

    /**
     * Displays an alert dialog that allows the user to select applications from all non-system
     * applications installed on the current profile. After the user selects an app, this app can't
     * be uninstallation.
     */
    private void showBlockUninstallationPrompt() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        List<ApplicationInfo> applicationInfoList
                = mPackageManager.getInstalledApplications(0 /* No flag */);
        List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        Collections.sort(applicationInfoList,
                new ApplicationInfo.DisplayNameComparator(mPackageManager));
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            // Ignore system apps because they can't be uninstalled.
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.resolvePackageName = applicationInfo.packageName;
                resolveInfoList.add(resolveInfo);
            }
        }

        final BlockUninstallationInfoArrayAdapter blockUninstallationInfoArrayAdapter
                = new BlockUninstallationInfoArrayAdapter(getActivity(), R.id.pkg_name,
                resolveInfoList);
        ListView listview = new ListView(getActivity());
        listview.setAdapter(blockUninstallationInfoArrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                blockUninstallationInfoArrayAdapter.onItemClick(parent, view, pos, id);
            }
        });

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.block_uninstallation_title)
                .setView(listview)
                .setPositiveButton(R.string.close, null /* Nothing to do */)
                .show();
    }

    /**
     * Shows an alert dialog which displays a list of disabled system apps. Clicking an app in the
     * dialog enables the app.
     */
    private void showEnableSystemAppsPrompt() {
        // Disabled system apps list = {All system apps} - {Enabled system apps}
        final List<String> disabledSystemApps = new ArrayList<String>();
        // This list contains both enabled and disabled apps.
        List<ApplicationInfo> allApps = mPackageManager.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
        // This list contains all enabled apps.
        List<ApplicationInfo> enabledApps =
                mPackageManager.getInstalledApplications(0 /* Default flags */);
        Set<String> enabledAppsPkgNames = new HashSet<String>();
        for (ApplicationInfo applicationInfo : enabledApps) {
            enabledAppsPkgNames.add(applicationInfo.packageName);
        }
        for (ApplicationInfo applicationInfo : allApps) {
            // Interested in disabled system apps only.
            if (!enabledAppsPkgNames.contains(applicationInfo.packageName)
                    && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                disabledSystemApps.add(applicationInfo.packageName);
            }
        }

        if (disabledSystemApps.isEmpty()) {
            showToast(R.string.no_disabled_system_apps);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.id.pkg_name, disabledSystemApps, true);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.enable_system_apps_title))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = disabledSystemApps.get(position);
                            mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                            showToast(R.string.enable_system_apps_by_package_name_success_msg,
                                    packageName);
                        }
                    })
                    .show();
        }
    }

    /**
     * Shows an alert dialog which displays a list hidden / non-hidden apps. Clicking an app in the
     * dialog enables the app.
     */
    private void showHideAppsPrompt(final boolean showHiddenApps) {
        final List<String> showApps = new ArrayList<> ();
        if (showHiddenApps) {
            // Find all hidden packages using the GET_UNINSTALLED_PACKAGES flag
            for (ApplicationInfo applicationInfo : getAllInstalledApplicationsSorted()) {
                if (mDevicePolicyManager.isApplicationHidden(mAdminComponentName,
                        applicationInfo.packageName)) {
                    showApps.add(applicationInfo.packageName);
                }
            }
        } else {
            // Find all non-hidden apps with a launcher icon
            for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
                if (!showApps.contains(res.activityInfo.packageName)
                        && !mDevicePolicyManager.isApplicationHidden(mAdminComponentName,
                                res.activityInfo.packageName)) {
                    showApps.add(res.activityInfo.packageName);
                }
            }
        }

        if (showApps.isEmpty()) {
            showToast(showHiddenApps ? R.string.unhide_apps_empty : R.string.hide_apps_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            final int successResId;
            final int failureResId;
            if (showHiddenApps) {
                // showing a dialog to unhide an app
                dialogTitleResId = R.string.unhide_apps_title;
                successResId = R.string.unhide_apps_success;
                failureResId = R.string.unhide_apps_failure;
            } else {
                // showing a dialog to hide an app
                dialogTitleResId = R.string.hide_apps_title;
                successResId = R.string.hide_apps_success;
                failureResId = R.string.hide_apps_failure;
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(dialogTitleResId))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                                    packageName, !showHiddenApps)) {
                                showToast(successResId, packageName);
                            } else {
                                showToast(getString(failureResId, packageName), Toast.LENGTH_LONG);
                            }
                        }
                    })
                    .show();
        }
    }

    /**
     * Shows an alert dialog which displays a list of suspended/non-suspended apps.
     */
    private void showSuspendAppsPrompt(final boolean showSuspendedApps) {
        final List<String> showApps = new ArrayList<>();
        if (showSuspendedApps) {
            // Find all suspended packages using the GET_UNINSTALLED_PACKAGES flag.
            for (ApplicationInfo applicationInfo : getAllInstalledApplicationsSorted()) {
                if (mDevicePolicyManager.getPackageSuspended(mAdminComponentName,
                        applicationInfo.packageName)) {
                    showApps.add(applicationInfo.packageName);
                }
            }
        } else {
            // Find all non-suspended apps with a launcher icon.
            for (ResolveInfo res : getAllLauncherIntentResolversSorted()) {
                if (!showApps.contains(res.activityInfo.packageName)
                        && !mDevicePolicyManager.getPackageSuspended(mAdminComponentName,
                        res.activityInfo.packageName)) {
                    showApps.add(res.activityInfo.packageName);
                }
            }
        }

        if (showApps.isEmpty()) {
            showToast(showSuspendedApps ?
                    R.string.unsuspend_apps_empty : R.string.suspend_apps_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            final int successResId;
            final int failureResId;
            if (showSuspendedApps) {
                // Showing a dialog to unsuspend an app.
                dialogTitleResId = R.string.unsuspend_apps_title;
                successResId = R.string.unsuspend_apps_success;
                failureResId = R.string.unsuspend_apps_failure;
            } else {
                // Showing a dialog to suspend an app.
                dialogTitleResId = R.string.suspend_apps_title;
                successResId = R.string.suspend_apps_success;
                failureResId = R.string.suspend_apps_failure;
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(dialogTitleResId))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            if (mDevicePolicyManager.setPackageSuspended(mAdminComponentName,
                                    packageName, !showSuspendedApps)) {
                                showToast(successResId, packageName);
                            } else {
                                showToast(getString(failureResId, packageName), Toast.LENGTH_LONG);
                            }
                        }
                    })
                    .show();
        }
    }

    private List<ResolveInfo> getAllLauncherIntentResolversSorted() {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> launcherIntentResolvers = mPackageManager
                .queryIntentActivities(launcherIntent, 0);
        Collections.sort(launcherIntentResolvers,
                new ResolveInfo.DisplayNameComparator(mPackageManager));
        return launcherIntentResolvers;
    }

    private List<ApplicationInfo> getAllInstalledApplicationsSorted() {
        List<ApplicationInfo> allApps = mPackageManager.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
        return allApps;
    }

    private void showToast(int msgId, Object... args) {
        showToast(getString(msgId, args));
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

    /**
     * Gets all the accessibility services. After all the accessibility services are retrieved, the
     * result is displayed in a popup.
     */
    private class GetAccessibilityServicesTask
            extends GetAvailableComponentsTask<AccessibilityServiceInfo> {
        private AccessibilityManager mAccessibilityManager;

        public GetAccessibilityServicesTask() {
            super(getActivity(), R.string.set_accessibility_services);
            mAccessibilityManager = (AccessibilityManager) getActivity().getSystemService(
                    Context.ACCESSIBILITY_SERVICE);
        }

        @Override
        protected List<AccessibilityServiceInfo> doInBackground(Void... voids) {
            return mAccessibilityManager.getInstalledAccessibilityServiceList();
        }

        @Override
        protected List<ResolveInfo> getResolveInfoListFromAvailableComponents(
                List<AccessibilityServiceInfo> accessibilityServiceInfoList) {
            HashSet<String> packageSet = new HashSet<>();
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            for (AccessibilityServiceInfo accessibilityServiceInfo: accessibilityServiceInfoList) {
                ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
                // Some apps may contain multiple accessibility services. Make sure that the package
                // name is unique in the return list.
                if (!packageSet.contains(resolveInfo.serviceInfo.packageName)) {
                    resolveInfoList.add(resolveInfo);
                    packageSet.add(resolveInfo.serviceInfo.packageName);
                }
            }
            return resolveInfoList;
        }

        @Override
        protected List<String> getPermittedComponentsList() {
            return mDevicePolicyManager.getPermittedAccessibilityServices(mAdminComponentName);
        }

        @Override
        protected void setPermittedComponentsList(List<String> permittedAccessibilityServices) {
            boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(
                    mAdminComponentName, permittedAccessibilityServices);
            int successMsgId = (permittedAccessibilityServices == null)
                    ? R.string.all_accessibility_services_enabled
                    : R.string.set_accessibility_services_successful;
            showToast(result ? successMsgId : R.string.set_accessibility_services_fail);
        }
    }

    /**
     * Gets all the input methods and displays them in a prompt.
     */
    private class GetInputMethodsTask extends GetAvailableComponentsTask<InputMethodInfo> {
        private InputMethodManager mInputMethodManager;

        public GetInputMethodsTask() {
            super(getActivity(), R.string.set_input_methods);
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        }

        @Override
        protected List<InputMethodInfo> doInBackground(Void... voids) {
            return mInputMethodManager.getInputMethodList();
        }

        @Override
        protected List<ResolveInfo> getResolveInfoListFromAvailableComponents(
                List<InputMethodInfo> inputMethodsInfoList) {
            List<ResolveInfo> inputMethodsResolveInfoList = new ArrayList<>();
            for (InputMethodInfo inputMethodInfo: inputMethodsInfoList) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.serviceInfo = inputMethodInfo.getServiceInfo();
                resolveInfo.resolvePackageName = inputMethodInfo.getPackageName();
                inputMethodsResolveInfoList.add(resolveInfo);
            }
            return inputMethodsResolveInfoList;
        }

        @Override
        protected List<String> getPermittedComponentsList() {
            return mDevicePolicyManager.getPermittedInputMethods(mAdminComponentName);
        }

        @Override
        protected void setPermittedComponentsList(List<String> permittedInputMethods) {
            boolean result = mDevicePolicyManager.setPermittedInputMethods(mAdminComponentName,
                    permittedInputMethods);
            int successMsgId = (permittedInputMethods == null)
                    ? R.string.all_input_methods_enabled
                    : R.string.set_input_methods_successful;
            showToast(result ? successMsgId : R.string.set_input_methods_fail);
        }
    }

    /**
     * Gets all CA certificates and displays them in a prompt.
     */
    private class ShowCaCertificateListTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            return getCaCertificateSubjectDnList();
        }

        @Override
        protected void onPostExecute(String[] installedCaCertificateDnList) {
            if (getActivity() == null || getActivity().isFinishing()) {
                return;
            }
            if (installedCaCertificateDnList == null) {
                showToast(R.string.no_ca_certificate);
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.installed_ca_title))
                        .setItems(installedCaCertificateDnList, null)
                        .show();
            }
        }

        private String[] getCaCertificateSubjectDnList() {
            List<byte[]> installedCaCerts = mDevicePolicyManager.getInstalledCaCerts(
                    mAdminComponentName);
            String[] caSubjectDnList = null;
            if (installedCaCerts.size() > 0) {
                caSubjectDnList = new String[installedCaCerts.size()];
                int i = 0;
                for (byte[] installedCaCert : installedCaCerts) {
                    try {
                        X509Certificate certificate = (X509Certificate) CertificateFactory
                                .getInstance(X509_CERT_TYPE).generateCertificate(
                                        new ByteArrayInputStream(installedCaCert));
                        caSubjectDnList[i++] = certificate.getSubjectDN().getName();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    }
                }
            }
            return caSubjectDnList;
        }
    }

    private void showFragment(final Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, fragment).commit();
    }

    private void startKioskMode(String[] lockTaskArray) {
        // start locked activity
        Intent launchIntent = new Intent(getActivity(), KioskModeActivity.class);
        launchIntent.putExtra(KioskModeActivity.LOCKED_APP_PACKAGE_LIST, lockTaskArray);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mPackageManager.setComponentEnabledSetting(
                new ComponentName(mPackageName, KioskModeActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        startActivity(launchIntent);
        getActivity().finish();
    }

    private void showWifiConfigCreationDialog() {
        WifiConfigCreationDialog dialog = WifiConfigCreationDialog.newInstance();
        dialog.show(getFragmentManager(), TAG_WIFI_CONFIG_CREATION);
    }

    private void reboot() {
        mDevicePolicyManager.reboot(mAdminComponentName);
    }

     abstract class ManageLockTaskListCallback {
        public abstract void onPositiveButtonClicked(String[] lockTaskArray);
    }
}
