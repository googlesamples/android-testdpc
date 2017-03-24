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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import java.util.List;

import static com.afwsamples.testdpc.common.preference.DpcPreferenceHelper.NO_CUSTOM_CONSTRIANT;

/**
 * Features related to {@link DevicePolicyManager#bindDeviceAdminServiceAsUser}
 */
public class BindDeviceAdminFragment extends BaseSearchablePolicyPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "BindDeviceAdminFragment";

    private static final String KEY_PO_USER_STATUS = "po_user_status";
    private static final String KEY_HIDE_PO_LAUNCHER_ICON = "hide_po_launcher_icon";

    private static final String KEY_INSTALL_CA_CERTIFICATE = "install_ca_cert";
    private static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 0;

    private UserManager mUserManager;
    private BindDeviceAdminServiceHelper<IProfileOwnerService> mBindDeviceAdminServiceHelper;
    private UserHandle mProfileOwnerUser;
    private DpcSwitchPreference mHideLauncherIconPreference;
    private DpcPreference mInstallCaCertificatePreference;

    @Override
    public int getPreferenceXml() {
        return R.xml.bind_device_admin_policies;
    }

    @Override
    public boolean isAvailable(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isDeviceOwnerApp(context.getPackageName())
                && Util.getBindDeviceAdminTargetUsers(context).size() == 1;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(getPreferenceXml());
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);

        mHideLauncherIconPreference =
                (DpcSwitchPreference) findPreference(KEY_HIDE_PO_LAUNCHER_ICON);
        mHideLauncherIconPreference.setOnPreferenceChangeListener(this);

        mInstallCaCertificatePreference = (DpcPreference) findPreference(
                KEY_INSTALL_CA_CERTIFICATE);
        mInstallCaCertificatePreference.setOnPreferenceClickListener(
                preference -> {
                    Util.showFileViewerForImportingCertificate(this,
                            INSTALL_CA_CERTIFICATE_REQUEST_CODE);
                    return true;
                }
        );

        // Hiding the launcher icon doesn't make sense for secondary users, so we disable the option
        mHideLauncherIconPreference.setCustomConstraint(
                getCustomConstraint(R.string.po_user_is_secondary));

        // Installing certificates makes sense for managed profile and secondary users.
        mInstallCaCertificatePreference.setCustomConstraint(
                getCustomConstraint(NO_CUSTOM_CONSTRIANT));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private CustomConstraint getCustomConstraint(int secondaryUserConstraint) {
        return () ->
        {
            if (mProfileOwnerUser == null) {
                return R.string.require_one_po_to_bind;
            } else if (!isManagedProfileOwner()) {
                return secondaryUserConstraint;
            } else if (!isManagedProfileRunning() || !isManagedProfileUnlocked()) {
                return R.string.managed_profile_not_running_or_unlocked;
            } else {
                return NO_CUSTOM_CONSTRIANT;
            }
        };
    }

    /**
     * Reload the user state and refresh all the preferences.
     */
    private void refresh() {
        // To make UI simpler, this fragment only shows if we can talk to exacly one profile owner.
        List<UserHandle> targetUsers = Util.getBindDeviceAdminTargetUsers(getActivity());
        if (targetUsers.size() == 1) {
            mProfileOwnerUser = Util.getBindDeviceAdminTargetUsers(getActivity()).get(0);
        } else {
            mProfileOwnerUser = null;
            Log.w(TAG, "Expecting to be able to bind to exactly one profile owner, but got "
                    + targetUsers);
        }
        mBindDeviceAdminServiceHelper = new BindDeviceAdminServiceHelper<>(
                getActivity(),
                ProfileOwnerService.class,
                IProfileOwnerService.Stub::asInterface,
                mProfileOwnerUser);

        refreshUserStatePreference();
        mHideLauncherIconPreference.refreshEnabledState();
        if (mHideLauncherIconPreference.isEnabled()) {
            mBindDeviceAdminServiceHelper.crossUserCall(service ->
                    mHideLauncherIconPreference.setChecked(service.isLauncherIconHidden()));
        }
        mInstallCaCertificatePreference.refreshEnabledState();
    }

    @Override
    // TODO: Handle when user is killed after the checking.
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_HIDE_PO_LAUNCHER_ICON:
                if (!mBindDeviceAdminServiceHelper.crossUserCall(
                        service -> service.setLauncherIconHidden((boolean) newValue))) {
                    Toast.makeText(
                            getActivity(), R.string.bind_to_profile_owner_failed, Toast.LENGTH_LONG)
                            .show();
                }
                return true;
        }
        return false;
    }

    private boolean isManagedProfileRunning() {
        return isManagedProfileOwner() && mUserManager.isUserRunning(mProfileOwnerUser);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private boolean isManagedProfileUnlocked() {
        return isManagedProfileOwner() && mUserManager.isUserUnlocked(mProfileOwnerUser);
    }

    private boolean isManagedProfileOwner() {
        return mProfileOwnerUser != null
                && mUserManager.getUserProfiles().contains(mProfileOwnerUser);
    }

    private void refreshUserStatePreference() {
        final @StringRes int stringRes;
        if (mProfileOwnerUser == null) {
            stringRes = R.string.require_one_po_to_bind;
        } else if (!isManagedProfileOwner()) {
            stringRes = R.string.po_user_is_secondary;
        } else if (isManagedProfileUnlocked()) {
            stringRes = R.string.managed_profile_unlocked;
        } else if (isManagedProfileRunning()) {
            stringRes = R.string.managed_profile_running_but_locked;
        } else {
            stringRes = R.string.managed_profile_not_running;
        }

        findPreference(KEY_PO_USER_STATUS).setSummary(getString(stringRes));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INSTALL_CA_CERTIFICATE_REQUEST_CODE) {
                final Uri uri;
                if (data != null && (uri = data.getData()) != null) {
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    AssetFileDescriptor afd;
                    try {
                        afd = contentResolver.openAssetFileDescriptor(uri, "r");
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Could not find certificate file", e);
                        return;
                    }
                    boolean bindSuccess = mBindDeviceAdminServiceHelper.crossUserCall(service -> {
                        boolean isCaInstalled = service.installCaCertificate(afd);
                        String toastMessage = isCaInstalled ?
                                getString(R.string.install_ca_successfully)
                                : getString(R.string.install_ca_fail);
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(),
                                toastMessage, Toast.LENGTH_SHORT).show());
                        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
                    });
                    if (!bindSuccess) {
                        Toast.makeText(getActivity(),
                                R.string.bind_to_profile_owner_failed, Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }
}
