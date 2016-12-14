/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.comp;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.common.preference.CustomConstraint;
import com.afwsamples.testdpc.common.preference.DpcPreference;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;
import java.io.FileNotFoundException;

import static com.afwsamples.testdpc.common.preference.DpcPreferenceHelper.NO_CUSTOM_CONSTRIANT;

/**
 * Provide comp owned managed profile specific features
 */
public class CompSpecificFragment extends BaseSearchablePolicyPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "CompSpecificFragment";

    private static final String KEY_PROFILE_STATUS = "profile_status";
    private static final String KEY_HIDE_PROFILE_LAUNCHER_ICON = "hide_profile_launcher_icon";

    private static final String KEY_INSTALL_CA_CERTIFICATE = "install_ca_cert";
    private static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 0;

    private UserManager mUserManager;
    private BindDeviceAdminServiceHelper<IProfileOwnerService> mBindDeviceAdminServiceHelper;
    private UserHandle mProfileUserHandle;
    private DpcSwitchPreference mHideLauncherIconPreference;
    private DpcPreference mInstallCaCertificatePreference;

    @Override
    public int getPreferenceXml() {
        return R.xml.comp_policies;
    }

    @Override
    public boolean isAvailable(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isDeviceOwnerApp(context.getPackageName()) && Util.isInCompMode(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(getPreferenceXml());
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        // It would be easier for us to support one profile only in term of UI design.
        mProfileUserHandle = Util.getBindDeviceAdminTargetUsers(getActivity()).get(0);
        mBindDeviceAdminServiceHelper = new BindDeviceAdminServiceHelper<>(
                getActivity(),
                ProfileOwnerService.class,
                IProfileOwnerService.Stub::asInterface,
                mProfileUserHandle);

        final CustomConstraint userRunningConstraint =
                () -> isProfileRunning() ? NO_CUSTOM_CONSTRIANT : R.string.profile_is_not_running;
        mHideLauncherIconPreference =
                (DpcSwitchPreference) findPreference(KEY_HIDE_PROFILE_LAUNCHER_ICON);
        mHideLauncherIconPreference.setOnPreferenceChangeListener(this);
        mHideLauncherIconPreference.setCustomConstraint(userRunningConstraint);

        mInstallCaCertificatePreference = (DpcPreference) findPreference(
                KEY_INSTALL_CA_CERTIFICATE);
        mInstallCaCertificatePreference.setCustomConstraint(userRunningConstraint);

        mInstallCaCertificatePreference.setOnPreferenceClickListener(
                preference -> {
                    Util.showFileViewerForImportingCertificate(this,
                            INSTALL_CA_CERTIFICATE_REQUEST_CODE);
                    return true;
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private String getManagedProfileStatusDisplayString() {
        final @StringRes int stringRes;
        if (isProfileUnlocked()) {
            stringRes = R.string.profile_is_unlocked;
        } else if (isProfileRunning()) {
            stringRes = R.string.profile_is_running_but_locked;
        } else {
            stringRes = R.string.profile_is_not_running;
        }
        return getString(stringRes);
    }

    private void setLauncherIconHidden(boolean hidden) {
        mBindDeviceAdminServiceHelper.crossUserCall(
                service -> service.setLauncherIconHidden(hidden));
    }

    private void refreshLauncherIconHiddenPreference() {
        mHideLauncherIconPreference.refreshEnabledState();
        if (isProfileRunning()) {
            mBindDeviceAdminServiceHelper.crossUserCall(
                    service -> mHideLauncherIconPreference.setChecked(
                            service.isLauncherIconHidden()));
        }
    }

    @Override
    // TODO: Handle when profile is killed after the checking.
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_HIDE_PROFILE_LAUNCHER_ICON:
                setLauncherIconHidden((boolean) newValue);
                return true;
        }
        return false;
    }

    private boolean isProfileRunning() {
        return mUserManager.isUserRunning(mProfileUserHandle);
    }

    private boolean isProfileUnlocked() {
        return mUserManager.isUserUnlocked(mProfileUserHandle);
    }

    private void refreshUserStatePreference() {
        findPreference(KEY_PROFILE_STATUS).setSummary(getManagedProfileStatusDisplayString());
    }

    /**
     * Reload the user state and refresh all the preferences.
     */
    private void refresh() {
        refreshUserStatePreference();
        refreshLauncherIconHiddenPreference();
        mInstallCaCertificatePreference.refreshEnabledState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INSTALL_CA_CERTIFICATE_REQUEST_CODE) {
                Uri uri = null;
                if (data != null && (uri = data.getData()) != null) {
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    AssetFileDescriptor afd;
                    try {
                        afd = contentResolver.openAssetFileDescriptor(uri, "r");
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Could not find certificate file", e);
                        return;
                    }
                    mBindDeviceAdminServiceHelper.crossUserCall(service -> {
                        boolean isCaInstalled = service.installCaCertificate(afd);
                        String toastMessage = isCaInstalled ?
                                getString(R.string.install_ca_successfully)
                                : getString(R.string.install_ca_fail);
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(),
                                toastMessage, Toast.LENGTH_SHORT).show());
                        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }
}
