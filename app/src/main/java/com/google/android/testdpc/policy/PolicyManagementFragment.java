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

package com.google.android.testdpc.policy;

import static android.os.UserManager.DISALLOW_ADD_USER;
import static android.os.UserManager.DISALLOW_ADJUST_VOLUME;
import static android.os.UserManager.DISALLOW_CONFIG_CREDENTIALS;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_DEBUGGING_FEATURES;
import static android.os.UserManager.DISALLOW_FACTORY_RESET;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS;
import static android.os.UserManager.DISALLOW_REMOVE_USER;
import static android.os.UserManager.DISALLOW_SAFE_BOOT;
import static android.os.UserManager.DISALLOW_SHARE_LOCATION;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.security.KeyChain;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.common.AppInfoArrayAdapter;
import com.google.android.testdpc.policy.accessibility.AccessibilityServiceInfoArrayAdapter;
import com.google.android.testdpc.policy.blockuninstallation.BlockUninstallationInfoArrayAdapter;
import com.google.android.testdpc.policy.certificate.DelegatedCertInstallerFragment;
import com.google.android.testdpc.policy.inputmethod.InputMethodInfoArrayAdapter;
import com.google.android.testdpc.policy.locktask.LockTaskAppInfoArrayAdapter;
import com.google.android.testdpc.policy.systemupdatepolicy.SystemUpdatePolicyFragment;
import com.google.android.testdpc.profilepolicy.ProfilePolicyManagementFragment;
import com.google.android.testdpc.profilepolicy.addsystemapps.EnableSystemAppsByIntentFragment;
import com.google.android.testdpc.profilepolicy.apprestrictions.ManageAppRestrictionsFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 * These include
 * 1) {@link DevicePolicyManager#setLockTaskPackages(android.content.ComponentName, String[])}
 * 2) {@link DevicePolicyManager#isLockTaskPermitted(String)}
 * 3) {@link UserManager#DISALLOW_DEBUGGING_FEATURES}
 * 4) {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES}
 * 5) {@link UserManager#DISALLOW_REMOVE_USER}
 * 6) {@link UserManager#DISALLOW_ADD_USER}
 * 7) {@link UserManager#DISALLOW_FACTORY_RESET}
 * 8) {@link UserManager#DISALLOW_CONFIG_CREDENTIALS}
 * 9) {@link UserManager#DISALLOW_SHARE_LOCATION}
 * 10) {@link UserManager#DISALLOW_CONFIG_TETHERING}
 * 11) {@link UserManager#DISALLOW_ADJUST_VOLUME}
 * 12) {@link UserManager#DISALLOW_UNMUTE_MICROPHONE}
 * 13) {@link UserManager#DISALLOW_MODIFY_ACCOUNTS}
 * 14) {@link DevicePolicyManager#clearDeviceOwnerApp(String)}
 * 15) {@link DevicePolicyManager#getPermittedAccessibilityServices(android.content.ComponentName)}
 * 16) {@link DevicePolicyManager#getPermittedInputMethods(android.content.ComponentName)}
 * 17) {@link DevicePolicyManager#setAccountManagementDisabled(android.content.ComponentName,
 * String, boolean)}
 * 18) {@link DevicePolicyManager#getAccountTypesWithManagementDisabled()}
 * 19) {@link DevicePolicyManager#createAndInitializeUser(android.content.ComponentName, String,
 * String, android.content.ComponentName, android.os.Bundle)}
 * 20) {@link DevicePolicyManager#removeUser(android.content.ComponentName, android.os.UserHandle)}
 * 21) {@link DevicePolicyManager#setUninstallBlocked(android.content.ComponentName, String,
 * boolean)}
 * 22) {@link DevicePolicyManager#isUninstallBlocked(android.content.ComponentName, String)}
 * 23) {@link DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)}
 * 24) {@link DevicePolicyManager#getCameraDisabled(android.content.ComponentName)}
 * 25) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName,
 * android.content.Intent)}
 * 26) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName, String)}
 * 27 {@link DevicePolicyManager#setApplicationRestrictions(android.content.ComponentName, String,
 * android.os.Bundle)}
 * 28) {@link DevicePolicyManager#installKeyPair(android.content.ComponentName,
 * java.security.PrivateKey, java.security.cert.Certificate, String)}
 * 29) {@link DevicePolicyManager#installCaCert(android.content.ComponentName, byte[])}
 * 30) {@link DevicePolicyManager#uninstallAllUserCaCerts(android.content.ComponentName)}
 * 31) {@link DevicePolicyManager#getInstalledCaCerts(android.content.ComponentName)}
 * 32) {@link UserManager#DISALLOW_SAFE_BOOT}
 * 33) {@link DevicePolicyManager#setStatusBarDisabled(ComponentName, boolean)}
 * 33) {@link DevicePolicyManager#setKeyguardDisabled(ComponentName, boolean)}
 */
public class PolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    public static final int INSTALL_KEY_CERTIFICATE_REQUEST_CODE = 7689;
    public static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 7690;

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String X509_CERT_TYPE = "X.509";
    public static final String TAG = "PolicyManagementFragment";

    private static final String DEVICE_OWNER_STATUS_KEY = "device_owner_status";
    private static final String MANAGE_LOCK_TASK_LIST_KEY = "manage_lock_task";
    private static final String CHECK_LOCK_TASK_PERMITTED_KEY = "check_lock_task_permitted";
    private static final String START_LOCK_TASK = "start_lock_task";
    private static final String STOP_LOCK_TASK = "stop_lock_task";
    private static final String DISALLOW_INSTALL_DEBUGGING_FEATURE_KEY
            = "disallow_debugging_feature";
    private static final String DISALLOW_INSTALL_UNKNOWN_SOURCES_KEY
            = "disallow_install_unknown_sources";
    private static final String REMOVE_DEVICE_OWNER_KEY = "remove_device_owner";
    private static final String SET_ACCESSIBILITY_SERVICES_KEY = "set_accessibility_services";
    private static final String SET_INPUT_METHODS_KEY = "set_input_methods";
    private static final String SET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "set_disable_account_management";
    private static final String GET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "get_disable_account_management";
    private static final String CREATE_AND_INITIALIZE_USER_KEY = "create_and_initialize_user";
    private static final String REMOVE_USER_KEY = "remove_user";
    private static final String BLOCK_UNINSTALLATION_BY_PKG_KEY = "block_uninstallation_by_pkg";
    private static final String BLOCK_UNINSTALLATION_LIST_KEY = "block_uninstallation_list";
    private static final String DISABLE_CAMERA_KEY = "disable_camera";
    private static final String ENABLE_SYSTEM_APPS_KEY = "enable_system_apps";
    private static final String ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY
            = "enable_system_apps_by_package_name";
    private static final String ENABLE_SYSTEM_APPS_BY_INTENT_KEY = "enable_system_apps_by_intent";
    private static final String MANAGE_APP_RESTRICTIONS_KEY = "manage_app_restrictions";
    private static final String INSTALL_KEY_CERTIFICATE_KEY = "install_key_certificate";
    private static final String INSTALL_CA_CERTIFICATE_KEY = "install_ca_certificate";
    private static final String GET_CA_CERTIFICATES_KEY = "get_ca_certificates";
    private static final String REMOVE_ALL_CERTIFICATES_KEY = "remove_all_ca_certificates";
    private static final String MANAGED_PROFILE_SPECIFIC_POLICIES_KEY = "managed_profile_policies";
    private static final String SYSTEM_UPDATE_POLICY_KEY = "system_update_policy";
    private static final String DELEGATED_CERT_INSTALLER_KEY = "manage_cert_installer";
    private static final String DISABLE_STATUS_BAR = "disable_status_bar";
    private static final String REENABLE_STATUS_BAR = "reenable_status_bar";
    private static final String DISABLE_KEYGUARD = "disable_keyguard";
    private static final String REENABLE_KEYGUARD = "reenable_keyguard";

    private static final String[] PRIMARY_USER_ONLY_RESTRICTIONS = {
            DISALLOW_REMOVE_USER, DISALLOW_ADD_USER, DISALLOW_FACTORY_RESET,
            DISALLOW_CONFIG_TETHERING, DISALLOW_ADJUST_VOLUME, DISALLOW_UNMUTE_MICROPHONE,
            DISALLOW_SAFE_BOOT
    };

    private static final String[] ALL_USER_RESTRICTIONS = {
            DISALLOW_DEBUGGING_FEATURES, DISALLOW_INSTALL_UNKNOWN_SOURCES, DISALLOW_REMOVE_USER,
            DISALLOW_ADD_USER, DISALLOW_FACTORY_RESET, DISALLOW_CONFIG_CREDENTIALS,
            DISALLOW_SHARE_LOCATION, DISALLOW_CONFIG_TETHERING, DISALLOW_ADJUST_VOLUME,
            DISALLOW_UNMUTE_MICROPHONE, DISALLOW_MODIFY_ACCOUNTS, DISALLOW_SAFE_BOOT
    };

    private static final String[] MANAGED_PROFILE_SPECIFIC_OPTIONS = {
            MANAGED_PROFILE_SPECIFIC_POLICIES_KEY
    };

    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private String mPackageName;
    private ComponentName mAdminComponentName;
    private UserManager mUserManager;

    private Preference mManageLockTaskPreference;
    private Preference mCheckLockTaskPermittedPreference;
    private Preference mStartLockTaskPreference;
    private Preference mStopLockTaskPreference;
    private Preference mCreateAndInitializeUserPreference;
    private Preference mRemoveUserPreference;
    private Preference mSystemUpdatePolicyPreference;
    private Preference mDelegatedCertInstallerPreference;
    private Preference mDisableStatusBarPreference;
    private Preference mReenableStatusBarPreference;
    private Preference mDisableKeyguardPreference;
    private Preference mReenableKeyguardPreference;
    private SwitchPreference mDisallowDebuggingFeatureSwitchPreference;
    private SwitchPreference mDisallowInstallUnknownSourcesSwitchPreference;
    private SwitchPreference mDisallowRemoveUserSwitchPreference;
    private SwitchPreference mDisallowAddUserSwitchPreference;
    private SwitchPreference mDisallowFactoryResetSwitchPreference;
    private SwitchPreference mDisallowConfigCredentialsSwitchPreference;
    private SwitchPreference mDisallowShareLocationSwitchPreference;
    private SwitchPreference mDisallowConfigTetheringSwitchPreference;
    private SwitchPreference mDisallowAdjustVolumePreference;
    private SwitchPreference mDisallowUnmuteMicrophonePreference;
    private SwitchPreference mDisallowModifyAccountsPreference;
    private SwitchPreference mDisableCameraSwitchPreference;
    private SwitchPreference mDisallowSafeBootPreference;

    private GetAccessibilityServicesTask mGetAccessibilityServicesTask = null;
    private GetInputMethodsTask mGetInputMethodsTask = null;
    private ShowCaCertificateListTask mShowCaCertificateListTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mPackageManager = getActivity().getPackageManager();
        mPackageName = getActivity().getPackageName();

        addPreferencesFromResource(R.xml.device_policy_header);

        mManageLockTaskPreference = findPreference(MANAGE_LOCK_TASK_LIST_KEY);
        mManageLockTaskPreference.setOnPreferenceClickListener(this);
        mCheckLockTaskPermittedPreference = findPreference(CHECK_LOCK_TASK_PERMITTED_KEY);
        mCheckLockTaskPermittedPreference.setOnPreferenceClickListener(this);
        mStartLockTaskPreference = findPreference(START_LOCK_TASK);
        mStartLockTaskPreference.setOnPreferenceClickListener(this);
        mStopLockTaskPreference = findPreference(STOP_LOCK_TASK);
        mStopLockTaskPreference.setOnPreferenceClickListener(this);
        mDisallowDebuggingFeatureSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_DEBUGGING_FEATURES);
        mDisallowDebuggingFeatureSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowInstallUnknownSourcesSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_INSTALL_UNKNOWN_SOURCES);
        mDisallowInstallUnknownSourcesSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowRemoveUserSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_REMOVE_USER);
        mDisallowRemoveUserSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowAddUserSwitchPreference = (SwitchPreference) findPreference(DISALLOW_ADD_USER);
        mDisallowAddUserSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowFactoryResetSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_FACTORY_RESET);
        mDisallowFactoryResetSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowConfigCredentialsSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_CONFIG_CREDENTIALS);
        mDisallowConfigCredentialsSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowShareLocationSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_SHARE_LOCATION);
        mDisallowShareLocationSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowConfigTetheringSwitchPreference = (SwitchPreference) findPreference(
                DISALLOW_CONFIG_TETHERING);
        mDisallowConfigTetheringSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowAdjustVolumePreference = (SwitchPreference) findPreference(DISALLOW_ADJUST_VOLUME);
        mDisallowAdjustVolumePreference.setOnPreferenceChangeListener(this);
        mDisallowUnmuteMicrophonePreference = (SwitchPreference) findPreference(
                DISALLOW_UNMUTE_MICROPHONE);
        mDisallowUnmuteMicrophonePreference.setOnPreferenceChangeListener(this);
        mDisallowModifyAccountsPreference = (SwitchPreference) findPreference(
                DISALLOW_MODIFY_ACCOUNTS);
        mDisallowModifyAccountsPreference.setOnPreferenceChangeListener(this);
        mCreateAndInitializeUserPreference = findPreference(CREATE_AND_INITIALIZE_USER_KEY);
        mCreateAndInitializeUserPreference.setOnPreferenceClickListener(this);
        mRemoveUserPreference = findPreference(REMOVE_USER_KEY);
        mRemoveUserPreference.setOnPreferenceClickListener(this);
        mDisableCameraSwitchPreference = (SwitchPreference) findPreference(DISABLE_CAMERA_KEY);
        mDisableCameraSwitchPreference.setOnPreferenceChangeListener(this);
        mDisallowSafeBootPreference = (SwitchPreference) findPreference(DISALLOW_SAFE_BOOT);
        mDisallowSafeBootPreference.setOnPreferenceChangeListener(this);
        mSystemUpdatePolicyPreference = findPreference(SYSTEM_UPDATE_POLICY_KEY);
        mSystemUpdatePolicyPreference.setOnPreferenceClickListener(this);
        mDelegatedCertInstallerPreference = findPreference(DELEGATED_CERT_INSTALLER_KEY);
        mDelegatedCertInstallerPreference.setOnPreferenceClickListener(this);
        mDisableStatusBarPreference = findPreference(DISABLE_STATUS_BAR);
        mDisableStatusBarPreference.setOnPreferenceClickListener(this);
        mReenableStatusBarPreference = findPreference(REENABLE_STATUS_BAR);
        mReenableStatusBarPreference.setOnPreferenceClickListener(this);
        mDisableKeyguardPreference = findPreference(DISABLE_KEYGUARD);
        mDisableKeyguardPreference.setOnPreferenceClickListener(this);
        mReenableKeyguardPreference = findPreference(REENABLE_KEYGUARD);
        mReenableKeyguardPreference.setOnPreferenceClickListener(this);
        findPreference(REMOVE_DEVICE_OWNER_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_ACCESSIBILITY_SERVICES_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_INPUT_METHODS_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_BY_PKG_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_LIST_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_INTENT_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGE_APP_RESTRICTIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_KEY_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_CA_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_CA_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_ALL_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGED_PROFILE_SPECIFIC_POLICIES_KEY).setOnPreferenceClickListener(this);
        reloadCameraDisableUi();

        updateUserRestrictionUi(ALL_USER_RESTRICTIONS);
        disableIncompatibleManagementOptionsInCurrentProfile();
        disableIncompatibleManagementOptionsByApiLevel();
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
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case MANAGE_LOCK_TASK_LIST_KEY:
                showManageLockTaskListPrompt();
                return true;
            case CHECK_LOCK_TASK_PERMITTED_KEY:
                showCheckLockTaskPermittedPrompt();
                return true;
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
            case REMOVE_DEVICE_OWNER_KEY:
                showRemoveDeviceOwnerPrompt();
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
                showEnableSystemAppByIntentFragment();
                return true;
            case MANAGE_APP_RESTRICTIONS_KEY:
                showManageAppRestrictionsFragment();
                return true;
            case INSTALL_KEY_CERTIFICATE_KEY:
                showFileViewerForImportingCertificate(INSTALL_KEY_CERTIFICATE_REQUEST_CODE);
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
                showManagedProfileSpecificPolicyFragment();
                return true;
            case SYSTEM_UPDATE_POLICY_KEY:
                showSystemUpdatePolicyFragment();
                return true;
            case DELEGATED_CERT_INSTALLER_KEY:
                showDelegatedCertInstallerFragment();
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
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case DISALLOW_DEBUGGING_FEATURES:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_INSTALL_UNKNOWN_SOURCES:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_REMOVE_USER:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_ADD_USER:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_FACTORY_RESET:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_CONFIG_CREDENTIALS:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_SHARE_LOCATION:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_CONFIG_TETHERING:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_ADJUST_VOLUME:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_UNMUTE_MICROPHONE:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISALLOW_MODIFY_ACCOUNTS:
                setUserRestriction(key, (Boolean) newValue);
                return true;
            case DISABLE_CAMERA_KEY:
                mDevicePolicyManager.setCameraDisabled(mAdminComponentName, (Boolean) newValue);
                // Reload UI to verify the camera is enable / disable correctly.
                reloadCameraDisableUi();
                return true;
            case DISALLOW_SAFE_BOOT:
                setUserRestriction(key, (Boolean) newValue);
                return true;
        }
        return false;
    }

    /**
     * Shows a list of primary user apps in a prompt, the user can set whether lock task is
     * permitted for each app.
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
            showToast(R.string.no_primary_app_available);
            return;
        } else {
            Collections.sort(primaryUserAppList,
                    new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));
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
                    .setTitle(getString(R.string.manage_lock_task))
                    .setView(listView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] lockTaskEnabledArray = appInfoArrayAdapter.getLockTaskList();
                            mDevicePolicyManager.setLockTaskPackages(
                                    DeviceAdminReceiver.getComponentName(getActivity()),
                                    lockTaskEnabledArray);
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

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
        updateUserRestrictionUi(restriction);
    }

    private void updateUserRestrictionUi(String[] userRestrictions) {
        for (String userRestriction : userRestrictions) {
            updateUserRestrictionUi(userRestriction);
        }
    }

    /**
     * Updates the corresponding UI for a given user restriction.
     *
     * @param userRestriction the id of a preference that is going to be updated.
     */
    private void updateUserRestrictionUi(String userRestriction) {
        boolean disallowed = false;
        switch (userRestriction) {
            case DISALLOW_DEBUGGING_FEATURES:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_DEBUGGING_FEATURES);
                mDisallowDebuggingFeatureSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_INSTALL_UNKNOWN_SOURCES:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES);
                mDisallowInstallUnknownSourcesSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_REMOVE_USER:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_REMOVE_USER);
                mDisallowRemoveUserSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_ADD_USER:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_ADD_USER);
                mDisallowAddUserSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_FACTORY_RESET:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_FACTORY_RESET);
                mDisallowFactoryResetSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_CONFIG_CREDENTIALS:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_CONFIG_CREDENTIALS);
                mDisallowConfigCredentialsSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_SHARE_LOCATION:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_SHARE_LOCATION);
                mDisallowShareLocationSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_CONFIG_TETHERING:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_CONFIG_TETHERING);
                mDisallowConfigTetheringSwitchPreference.setChecked(disallowed);
                break;
            case DISALLOW_ADJUST_VOLUME:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_ADJUST_VOLUME);
                mDisallowAdjustVolumePreference.setChecked(disallowed);
                break;
            case DISALLOW_UNMUTE_MICROPHONE:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_UNMUTE_MICROPHONE);
                mDisallowUnmuteMicrophonePreference.setChecked(disallowed);
                break;
            case DISALLOW_MODIFY_ACCOUNTS:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_MODIFY_ACCOUNTS);
                mDisallowModifyAccountsPreference.setChecked(disallowed);
                break;
            case DISALLOW_SAFE_BOOT:
                disallowed = mUserManager.hasUserRestriction(DISALLOW_SAFE_BOOT);
                mDisallowSafeBootPreference.setChecked(disallowed);
                break;
        }
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
            for (String primaryUserRestriction : PRIMARY_USER_ONLY_RESTRICTIONS) {
                findPreference(primaryUserRestriction).setEnabled(false);
            }
            // Only the primary profile can remove the device ownership.
            findPreference(REMOVE_DEVICE_OWNER_KEY).setEnabled(false);
            // Only the device owner in the primary profile can create or remove user.
            mCreateAndInitializeUserPreference.setEnabled(false);
            mRemoveUserPreference.setEnabled(false);
            mManageLockTaskPreference.setEnabled(false);
            mCheckLockTaskPermittedPreference.setEnabled(false);
            mStartLockTaskPreference.setEnabled(false);
            mStopLockTaskPreference.setEnabled(false);
            mDisableStatusBarPreference.setEnabled(false);
            mReenableStatusBarPreference.setEnabled(false);
            mDisableKeyguardPreference.setEnabled(false);
            mReenableKeyguardPreference.setEnabled(false);
            mSystemUpdatePolicyPreference.setEnabled(false);
            deviceOwnerStatusStringId = R.string.this_is_a_managed_profile_owner;
        } else if (isDeviceOwner) {
            // If it's a device owner and running in the primary profile.
            deviceOwnerStatusStringId = R.string.this_is_a_device_owner;
            for (String managedProfileSpecificOption : MANAGED_PROFILE_SPECIFIC_OPTIONS) {
                findPreference(managedProfileSpecificOption).setEnabled(false);
            }
        }
        findPreference(DEVICE_OWNER_STATUS_KEY).setSummary(deviceOwnerStatusStringId);
    }

    private void disableIncompatibleManagementOptionsByApiLevel() {
        if (Build.VERSION.SDK_INT <= VERSION_CODES.LOLLIPOP_MR1
                /*TODO: remove once SDK_INT on device is bumped */
                && (!"MNC".equals(Build.VERSION.CODENAME))) {
            // The following options depend on MNC APIs.
            mStartLockTaskPreference.setEnabled(false);
            mStopLockTaskPreference.setEnabled(false);
            mSystemUpdatePolicyPreference.setEnabled(false);
            mDelegatedCertInstallerPreference.setEnabled(false);
            mDisableStatusBarPreference.setEnabled(false);
            mReenableStatusBarPreference.setEnabled(false);
            mDisableKeyguardPreference.setEnabled(false);
            mReenableKeyguardPreference.setEnabled(false);
            findPreference(DISALLOW_SAFE_BOOT).setEnabled(false);
        }
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
                        mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                        showToast(R.string.enable_system_apps_by_package_name_success_msg,
                                packageName);
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
     * Shows a fragment to pick an intent. The system apps receiving it will be enabled.
     */
    private void showEnableSystemAppByIntentFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, new EnableSystemAppsByIntentFragment()).commit();
    }

    /**
     * Shows the app restriction management fragment.
     */
    private void showManageAppRestrictionsFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, new ManageAppRestrictionsFragment()).commit();
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
                        mDevicePolicyManager.installKeyPair(mAdminComponentName, key, certificate,
                                alias);
                        showToast(R.string.certificate_added, alias);
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
            if (requestCode == PolicyManagementFragment.INSTALL_KEY_CERTIFICATE_REQUEST_CODE) {
                importKeyCertificateFromIntent(data, "");
            } else if (requestCode ==
                    PolicyManagementFragment.INSTALL_CA_CERTIFICATE_REQUEST_CODE) {
                importCaCertificateFromIntent(data);
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
     * Shows the managed profile policy management fragment.
     */
    private void showManagedProfileSpecificPolicyFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, new ProfilePolicyManagementFragment()).commit();
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

    private void showToast(int msgId, Object... args) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (args != null) {
            Toast.makeText(activity, getString(msgId, args), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String msg) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets all the accessibility services. After all the accessibility services are retrieved, the
     * result is displayed in a popup.
     */
    private class GetAccessibilityServicesTask extends
            AsyncTask<Void, Void, List<AccessibilityServiceInfo>> {

        private AccessibilityManager mAccessibilityManager;

        public GetAccessibilityServicesTask() {
            mAccessibilityManager = (AccessibilityManager) getActivity().getSystemService(
                    Context.ACCESSIBILITY_SERVICE);
        }

        @Override
        protected List<AccessibilityServiceInfo> doInBackground(Void... voids) {
            return mAccessibilityManager.getInstalledAccessibilityServiceList();
        }

        @Override
        protected void onPostExecute(List<AccessibilityServiceInfo> accessibilityServicesInfoList) {
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            List<ResolveInfo> accessibilityServicesResolveInfoList
                    = AccessibilityServiceInfoArrayAdapter
                    .getResolveInfoListFromAccessibilityServiceInfoList(
                            accessibilityServicesInfoList);
            Collections.sort(accessibilityServicesResolveInfoList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));
            final AccessibilityServiceInfoArrayAdapter accessibilityServiceInfoArrayAdapter
                    = new AccessibilityServiceInfoArrayAdapter(getActivity(), R.id.pkg_name,
                            accessibilityServicesResolveInfoList);
            ListView listview = new ListView(getActivity());
            listview.setAdapter(accessibilityServiceInfoArrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    accessibilityServiceInfoArrayAdapter.onItemClick(parent, view, pos, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.set_accessibility_services)
                    .setView(listview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<String> permittedAccessibilityServicesArrayList
                                    = accessibilityServiceInfoArrayAdapter
                                    .getSelectedAccessibilityServices();
                            boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(
                                    DeviceAdminReceiver.getComponentName(getActivity()),
                                    permittedAccessibilityServicesArrayList);
                            showToast(result
                                    ? R.string.set_accessibility_services_successful
                                    : R.string.set_accessibility_services_fail);
                        }
                    })
                    .setNeutralButton(R.string.allow_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(
                                    mAdminComponentName, null);
                            showToast(result
                                    ? R.string.all_accessibility_services_enabled
                                    : R.string.set_accessibility_services_fail);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    /**
     * Gets all the input methods and displays them in a prompt.
     */
    private class GetInputMethodsTask extends AsyncTask<Void, Void, List<InputMethodInfo>> {

        private InputMethodManager mInputMethodManager;

        public GetInputMethodsTask() {
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        }

        @Override
        protected List<InputMethodInfo> doInBackground(Void... voids) {
            return mInputMethodManager.getInputMethodList();
        }

        @Override
        protected void onPostExecute(List<InputMethodInfo> inputMethodsInfoList) {
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            List<ResolveInfo> inputMethodsResolveInfoList
                    = InputMethodInfoArrayAdapter.getResolveInfoListFromInputMethodsInfoList(
                    inputMethodsInfoList);
            Collections.sort(inputMethodsResolveInfoList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));

            final InputMethodInfoArrayAdapter inputMethodInfoArrayAdapter
                    = new InputMethodInfoArrayAdapter(getActivity(), R.id.pkg_name,
                            inputMethodsResolveInfoList);
            ListView listview = new ListView(getActivity());
            listview.setAdapter(inputMethodInfoArrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    inputMethodInfoArrayAdapter.onItemClick(parent, view, pos, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.set_input_methods)
                    .setView(listview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<String> permittedAccessibilityServicesArrayList
                                    = inputMethodInfoArrayAdapter
                                    .getPermittedAccessibilityServices();
                            boolean result = mDevicePolicyManager.setPermittedInputMethods(
                                    DeviceAdminReceiver.getComponentName(getActivity()),
                                    permittedAccessibilityServicesArrayList);
                            showToast(result
                                    ? R.string.set_input_methods_successful
                                    : R.string.set_input_methods_fail);
                        }
                    })
                    .setNeutralButton(R.string.allow_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean result = mDevicePolicyManager.setPermittedInputMethods(
                                    mAdminComponentName, null);
                            showToast(result
                                    ? R.string.all_input_methods_enabled
                                    : R.string.set_input_methods_fail);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
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

    private void showSystemUpdatePolicyFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, new SystemUpdatePolicyFragment()).commit();
    }

    private void showDelegatedCertInstallerFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, new DelegatedCertInstallerFragment()).commit();
    }
}
