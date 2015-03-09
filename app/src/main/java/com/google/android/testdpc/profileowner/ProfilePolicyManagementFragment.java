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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.testdpc.DeviceAdminReceiver;
import com.google.android.testdpc.R;
import com.google.android.testdpc.common.AppInfoArrayAdapter;
import com.google.android.testdpc.deviceowner.DevicePolicyManagementFragment;
import com.google.android.testdpc.profileowner.addsystemapps.EnableSystemAppsByIntentFragment;
import com.google.android.testdpc.profileowner.apprestrictions.ManageAppRestrictionsFragment;
import com.google.android.testdpc.profileowner.crossprofileintentfilter
        .AddCrossProfileIntentFilterFragment;

import java.util.ArrayList;
import java.util.HashSet;
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
import java.util.Enumeration;
import java.util.List;

/**
 * This fragment provides several functions that are available in a managed profile.
 * These include
 * 1) {@link DevicePolicyManager#addCrossProfileIntentFilter(android.content.ComponentName,
 * android.content.IntentFilter, int)}
 * 2) {@link DevicePolicyManager#clearCrossProfileIntentFilters(android.content.ComponentName)}
 * 3) {@link DevicePolicyManager#setCrossProfileCallerIdDisabled(android.content.ComponentName,
 * boolean)}
 * 4) {@link DevicePolicyManager#getCrossProfileCallerIdDisabled(android.content.ComponentName)}
 * 5) {@link DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)}
 * 6) {@link DevicePolicyManager#getCameraDisabled(android.content.ComponentName)}
 * 7) {@link DevicePolicyManager#wipeData(int)}
 * 8) {@link DevicePolicyManager#addCrossProfileWidgetProvider(android.content.ComponentName,
 * String)}
 * 9) {@link DevicePolicyManager#removeCrossProfileWidgetProvider(android.content.ComponentName,
 * String)}
 * 10) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName,
 * android.content.Intent)}
 * 11) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName, String)}
 * 12) {@link DevicePolicyManager#installKeyPair(android.content.ComponentName,
 * java.security.PrivateKey, java.security.cert.Certificate, String)}
 * 13) {@link DevicePolicyManager#installCaCert(android.content.ComponentName, byte[])}
 * 14) {@link DevicePolicyManager#uninstallAllUserCaCerts(android.content.ComponentName)}
 * 15) {@link DevicePolicyManager#getInstalledCaCerts(android.content.ComponentName)}
 */
public class ProfilePolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    public static final String TAG = "ProfilePolicyManagementFragment";
    public static final int INSTALL_KEY_CERTIFICATE_REQUEST_CODE = 7689;
    public static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 7690;

    private static final String ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY
            = "add_cross_profile_intent_filter";
    private static final String CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY
            = "clear_cross_profile_intent_filters";
    private static final String MANAGE_DEVICE_POLICIES_KEY = "manage_device_policies";
    private static final String DISABLE_CROSS_PROFILE_CALLER_ID_KEY
            = "disable_cross_profile_caller_id";
    private static final String DISABLE_CAMERA_KEY = "disable_camera";
    private static final String REMOVE_PROFILE_KEY = "remove_profile";
    private static final String ADD_CROSS_PROFILE_APP_WIDGETS_KEY = "add_cross_profile_app_widgets";
    private static final String REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY
            = "remove_cross_profile_app_widgets";
    private static final String ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY
            = "enable_system_apps_by_package_name";
    private static final String ENABLE_SYSTEM_APPS_BY_INTENT_KEY = "enable_system_apps_by_intent";
    private static final String INSTALL_KEY_CERTIFICATE_KEY = "install_key_certificate";
    private static final String INSTALL_CA_CERTIFICATE_KEY = "install_ca_certificate";
    private static final String GET_CA_CERTIFICATES_KEY = "get_ca_certificates";
    private static final String REMOVE_ALL_CERTIFICATES_KEY = "remove_all_ca_certificates";
    private static final String MANAGE_APP_RESTRICTIONS_KEY = "manage_app_restrictions";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;
    private Preference mManageDevicePoliciesPreference;
    private Preference mAddCrossProfileIntentFilterPreference;
    private Preference mClearCrossProfileIntentFiltersPreference;
    private Preference mRemoveManagedProfilePreference;
    private Preference mAddCrossProfileAppWidgetsPreference;
    private Preference mRemoveCrossProfileAppWidgetsPreference;
    private Preference mEnableSystemAppByPackageNamePreference;
    private Preference mEnableSystemAppByIntentPreference;
    private Preference mInstallKeyCertificatePreference;
    private Preference mInstallCaCertificatePreference;
    private Preference mGetCaCertificatePreference;
    private Preference mRemoveAllCaCertificatesPreference;
    private Preference mManageAppRestrictionsPreference;
    private SwitchPreference mDisableCrossProfileCallerIdSwitchPreference;
    private SwitchPreference mDisableCameraSwitchPreference;
    private ShowCaCertificateListTask mShowCaCertificateListTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.profile_policy_header);


        mManageDevicePoliciesPreference = findPreference(MANAGE_DEVICE_POLICIES_KEY);
        mManageDevicePoliciesPreference.setOnPreferenceClickListener(this);
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
        mEnableSystemAppByPackageNamePreference = findPreference(
                ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY);
        mEnableSystemAppByPackageNamePreference.setOnPreferenceClickListener(this);
        mEnableSystemAppByIntentPreference = findPreference(ENABLE_SYSTEM_APPS_BY_INTENT_KEY);
        mEnableSystemAppByIntentPreference.setOnPreferenceClickListener(this);
        mDisableCrossProfileCallerIdSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_CROSS_PROFILE_CALLER_ID_KEY);
        mDisableCrossProfileCallerIdSwitchPreference.setOnPreferenceChangeListener(this);
        reloadCrossProfileCallerIdDisableUi();
        mDisableCameraSwitchPreference = (SwitchPreference) findPreference(DISABLE_CAMERA_KEY);
        mDisableCameraSwitchPreference.setOnPreferenceChangeListener(this);
        reloadCameraDisableUi();
        mInstallKeyCertificatePreference = findPreference(INSTALL_KEY_CERTIFICATE_KEY);
        mInstallKeyCertificatePreference.setOnPreferenceClickListener(this);
        mInstallCaCertificatePreference = findPreference(INSTALL_CA_CERTIFICATE_KEY);
        mInstallCaCertificatePreference.setOnPreferenceClickListener(this);
        mGetCaCertificatePreference = findPreference(GET_CA_CERTIFICATES_KEY);
        mGetCaCertificatePreference.setOnPreferenceClickListener(this);
        mRemoveAllCaCertificatesPreference = findPreference(REMOVE_ALL_CERTIFICATES_KEY);
        mRemoveAllCaCertificatesPreference.setOnPreferenceClickListener(this);
        mManageAppRestrictionsPreference = findPreference(MANAGE_APP_RESTRICTIONS_KEY);
        mManageAppRestrictionsPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.profile_management_title);

        String packageName = getActivity().getPackageName();
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName);

        if (!isProfileOwner) {
            // Safe net: should never happen.
            Toast.makeText(getActivity(), R.string.setup_profile_message, Toast.LENGTH_SHORT)
                    .show();
            getActivity().finish();
        }

        mManageDevicePoliciesPreference.setEnabled(isDeviceOwner);
        if (!isDeviceOwner) {
            // Disable shortcut to the device policy management console if the app is currently not
            // a device owner.
            mManageDevicePoliciesPreference.setSummary(
                    getString(R.string.not_a_device_owner, mAdminComponentName.getClassName()));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (ADD_CROSS_PROFILE_INTENT_FILTER_PREFERENCE_KEY.equals(key)) {
            showAddCrossProfileIntentFilterFragment();
            return true;
        } else if (CLEAR_CROSS_PROFILE_INTENT_FILTERS_PREFERENCE_KEY.equals(key)) {
            mDevicePolicyManager.clearCrossProfileIntentFilters(mAdminComponentName);
            Toast.makeText(getActivity(), getString(R.string.cross_profile_intent_filters_cleared),
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (REMOVE_PROFILE_KEY.equals(key)) {
            mRemoveManagedProfilePreference.setEnabled(false);
            mDevicePolicyManager.wipeData(0);
            Toast.makeText(getActivity(), getString(R.string.removing_managed_profile),
                    Toast.LENGTH_SHORT).show();
            // Finish the activity because all other functions will not work after the managed
            // profile is removed.
            getActivity().finish();
        } else if (ADD_CROSS_PROFILE_APP_WIDGETS_KEY.equals(key)) {
            showDisabledAppWidgetList();
            return true;
        } else if (REMOVE_CROSS_PROFILE_APP_WIDGETS_KEY.equals(key)) {
            showEnabledAppWidgetList();
            return true;
        } else if (ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY.equals(key)) {
            showEnableSystemAppByPackageNamePrompt();
            return true;
        } else if (ENABLE_SYSTEM_APPS_BY_INTENT_KEY.equals(key)) {
            showEnableSystemAppByIntentFragment();
            return true;
        } else if (MANAGE_DEVICE_POLICIES_KEY.equals(key)) {
            showDevicePolicyManagementFragment();
            return true;
        } else if (INSTALL_KEY_CERTIFICATE_KEY.equals(key)) {
            showFileViewerForImportingCertificate(INSTALL_KEY_CERTIFICATE_REQUEST_CODE);
            return true;
        } else if (INSTALL_CA_CERTIFICATE_KEY.equals(key)) {
            showFileViewerForImportingCertificate(INSTALL_CA_CERTIFICATE_REQUEST_CODE);
            return true;
        } else if (GET_CA_CERTIFICATES_KEY.equals(key)) {
            showCaCertificateList();
            return true;
        } else if (REMOVE_ALL_CERTIFICATES_KEY.equals(key)) {
            mDevicePolicyManager.uninstallAllUserCaCerts(mAdminComponentName);
            Toast.makeText(getActivity(), R.string.all_ca_certificates_removed, Toast.LENGTH_SHORT)
                    .show();
            return true;
        } else if (MANAGE_APP_RESTRICTIONS_KEY.equals(key)) {
            showManageAppRestrictionsFragment();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (DISABLE_CROSS_PROFILE_CALLER_ID_KEY.equals(key)) {
            boolean disableCrossProfileCallerId = (Boolean) newValue;
            mDevicePolicyManager.setCrossProfileCallerIdDisabled(mAdminComponentName,
                    disableCrossProfileCallerId);
            // Reload UI to verify the state of cross-profiler caller Id is set correctly.
            reloadCrossProfileCallerIdDisableUi();
            return true;
        } else if (DISABLE_CAMERA_KEY.equals(key)) {
            boolean disableCamera = (Boolean) newValue;
            mDevicePolicyManager.setCameraDisabled(mAdminComponentName, disableCamera);
            // Reload UI to verify the camera is enable / disable correctly.
            reloadCameraDisableUi();
            return true;
        }
        return false;
    }

    private void showAddCrossProfileIntentFilterFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(ProfilePolicyManagementFragment.class.getName()).replace(
                R.id.container,
                new AddCrossProfileIntentFilterFragment())
                .commit();
    }

    private void reloadCrossProfileCallerIdDisableUi() {
        boolean isCrossProfileCallerIdDisabled = mDevicePolicyManager
                .getCrossProfileCallerIdDisabled(mAdminComponentName);
        mDisableCrossProfileCallerIdSwitchPreference.setChecked(isCrossProfileCallerIdDisabled);
    }

    private void reloadCameraDisableUi() {
        boolean isCameraDisabled = mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
        mDisableCameraSwitchPreference.setChecked(isCameraDisabled);
    }

    /**
     * Show a list of work profile apps which have non-enabled widget providers.
     * Clicking any item on the list will enable ALL the widgets from that app.
     *
     * Show toast if there is no work profile app that has non-enabled widget providers.
     */
    private void showDisabledAppWidgetList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        final List<String> disabledCrossProfileWidgetProvidersList
                = getDisabledCrossProfileWidgetProvidersList();
        if (disabledCrossProfileWidgetProvidersList.isEmpty()) {
            Toast.makeText(getActivity(), getString(
                    R.string.all_cross_profile_widget_providers_are_enabled), Toast.LENGTH_SHORT)
                    .show();
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
                    Toast.makeText(getActivity(), getString(R.string.cross_profile_widget_enable,
                            pkgName), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * Get a list of work profile apps which have non-enabled cross-profile widget providers.
     *
     * @return a list of package names which have non-enabled cross-profile widget providers.
     */
    private List<String> getDisabledCrossProfileWidgetProvidersList() {
        Context context = getActivity().getApplicationContext();
        HashSet<String> enabledCrossProfileWidgetProvidersSet = new HashSet<String>(
                mDevicePolicyManager.getCrossProfileWidgetProviders(
                        DeviceAdminReceiver.getComponentName(context)));
        HashSet<String> disabledCrossProfileWidgetProvidersSet = new HashSet<String>();
        List<AppWidgetProviderInfo> appWidgetProviderInfoList
                = AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo appWidgetProviderInfo : appWidgetProviderInfoList) {
            if (appWidgetProviderInfo.configure != null &&
                    !enabledCrossProfileWidgetProvidersSet.contains(
                            appWidgetProviderInfo.configure.getPackageName())) {
                disabledCrossProfileWidgetProvidersSet.add(
                        appWidgetProviderInfo.configure.getPackageName());
            }
        }
        return new ArrayList<String>(disabledCrossProfileWidgetProvidersSet);
    }

    /**
     * Show a list of work profile apps which have widget providers enabled.
     * Clicking any item on the list will disable ALL the widgets from that app.
     *
     * Show toast if there is no app that have non-enabled widget providers.
     */
    private void showEnabledAppWidgetList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        final List<String> packagesWithEnabledCrossProfileWidgetsList = mDevicePolicyManager
                .getCrossProfileWidgetProviders(mAdminComponentName);
        if (packagesWithEnabledCrossProfileWidgetsList.isEmpty()) {
            Toast.makeText(getActivity(), getString(
                    R.string.all_cross_profile_widget_providers_are_disabled), Toast.LENGTH_SHORT)
                    .show();
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.layout.app_row, packagesWithEnabledCrossProfileWidgetsList);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.remove_cross_profile_app_widget_providers_title));
            builder.setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pkgName = packagesWithEnabledCrossProfileWidgetsList.get(which);
                    mDevicePolicyManager.removeCrossProfileWidgetProvider(mAdminComponentName,
                            pkgName);
                    Toast.makeText(getActivity(),getString(R.string.cross_profile_widget_disable,
                            pkgName), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * Show a prompt to enable system app.
     */
    private void showEnableSystemAppByPackageNamePrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.enable_system_apps_title));
        LinearLayout inputContainer = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.simple_edittext, null);
        final EditText editText = (EditText) inputContainer.findViewById(R.id.input);
        editText.setHint(getString(R.string.enable_system_apps_by_package_name_hints));
        builder.setView(inputContainer);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final String packageName = editText.getText().toString();
                    mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                    Toast.makeText(getActivity(),getString(
                            R.string.enable_system_apps_by_package_name_success_msg, packageName),
                            Toast.LENGTH_SHORT).show();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Show a fragment to construct an intent for enabling system apps.
     */
    private void showEnableSystemAppByIntentFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(ProfilePolicyManagementFragment.class.getName()).replace(
                        R.id.container, new EnableSystemAppsByIntentFragment()).commit();
    }

    /**
     * Show the device policy management fragment.
     */
    private void showDevicePolicyManagementFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(ProfilePolicyManagementFragment.class.getName()).replace(
                        R.id.container,new DevicePolicyManagementFragment()).commit();
    }

    /**
     * Show the file viewer for importing certificate.
     */
    private void showFileViewerForImportingCertificate(int requestCode) {
        Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
        certIntent.setTypeAndNormalize("*/*");
        try {
            getActivity().startActivityForResult(certIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Import a certificate to the managed profile. If a password provided is incorrect, show a
     * prompt for user input. Otherwise, show a prompt for the certificate alias.
     *
     * @param intent intent that contains the certificate data uri.
     * @param password the password to decrypt the certificate.
     */
    private void importKeyCertificateFromIntent(Intent intent, String password) {
        importKeyCertificateFromIntent(intent, password, 0);
    }

    /**
     * Import a certificate to the managed profile. If a password provided is incorrect, show a
     * prompt for user input. Otherwise, show a prompt for the certificate alias.
     *
     * @param intent intent that contains the certificate data uri.
     * @param password the password to decrypt the certificate.
     * @param attempts the retry attempts
     */
    private void importKeyCertificateFromIntent(Intent intent, String password, int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Uri data = null;
        if (intent != null && (data = intent.getData()) != null) {
            if (password == null) {
                password = "";
            }
            InputStream certificateInputStream;
            try {
                certificateInputStream = getActivity().getContentResolver().openInputStream(data);
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
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
                Toast.makeText(getActivity(), R.string.not_a_key_certificate, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Show a prompt to ask for certificate password. If the certificate password is correct, the
     * private key and certificate would be imported.
     *
     * @param intent intent that contains the certificate data uri.
     * @param attempts the retry attempts
     */
    private void showPromptForCertificatePassword(final Intent intent, final int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.certificate_password_prompt_title));
        View passwordInputView = getActivity().getLayoutInflater()
                .inflate(R.layout.certificate_password_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.password_input);
        if (attempts > 1) {
            passwordInputView.findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
        }
        builder.setView(passwordInputView);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userPassword = input.getText().toString();
                        importKeyCertificateFromIntent(intent, userPassword, attempts);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showPromptForKeyCertificateAlias(final PrivateKey key,
            final Certificate certificate, String alias) {
        if (getActivity() == null || getActivity().isFinishing() || key == null
                || certificate == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.certificate_alias_prompt_title));
        View passwordInputView = getActivity().getLayoutInflater()
                .inflate(R.layout.certificate_alias_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.alias_input);
        if (!TextUtils.isEmpty(alias)) {
            input.setText(alias);
            input.selectAll();
        }
        builder.setView(passwordInputView);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alias = input.getText().toString();
                        mDevicePolicyManager
                                .installKeyPair(mAdminComponentName, key, certificate, alias);
                        Toast.makeText(getActivity(), getString(R.string.certificate_added, alias),
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

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
                    byte[] buffer = new byte[4096];
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
            if (isCaInstalled) {
                Toast.makeText(getActivity(), R.string.install_ca_successfully, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getActivity(), R.string.install_ca_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode
                    == ProfilePolicyManagementFragment.INSTALL_KEY_CERTIFICATE_REQUEST_CODE) {
                importKeyCertificateFromIntent(data, "");
            } else if (requestCode
                    == ProfilePolicyManagementFragment.INSTALL_CA_CERTIFICATE_REQUEST_CODE) {
                importCaCertificateFromIntent(data);
            }
        }
    }

    /**
     * Show a list of installed CA certificates.
     */
    private void showCaCertificateList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (mShowCaCertificateListTask != null && !mShowCaCertificateListTask.isCancelled()) {
            mShowCaCertificateListTask.cancel(true);
        }
        mShowCaCertificateListTask = new ShowCaCertificateListTask();
        mShowCaCertificateListTask.execute();
    }

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
                Toast.makeText(getActivity(), getString(R.string.no_ca_certificate),
                        Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.installed_ca_title));
                builder.setItems(installedCaCertificateDnList, null);
                builder.show();
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
                                .getInstance("X.509").generateCertificate(new ByteArrayInputStream(
                                        installedCaCert));
                        caSubjectDnList[i++] = certificate.getSubjectDN().getName();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    }
                }
            }
            return caSubjectDnList;
        }
    }

    /**
     * Show the app restriction management fragment.
     */
    private void showManageAppRestrictionsFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(ProfilePolicyManagementFragment.class.getName())
                .replace(R.id.container, new ManageAppRestrictionsFragment()).commit();
    }
}
