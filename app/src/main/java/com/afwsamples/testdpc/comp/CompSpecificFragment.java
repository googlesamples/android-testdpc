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

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.StringRes;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.Util;

/**
 * Provide comp owned managed profile specific features
 */
public class CompSpecificFragment extends BaseSearchablePolicyPreferenceFragment {
    private static final String KEY_PROFILE_STATUS = "profile_status";
    private UserManager mUserManager;
    private UserHandle mProfileUserHandle;

    @Override
    public int getPreferenceXml() {
        return R.xml.comp_policies;
    }

    @Override
    public boolean isAvailable(Context context) {
        return Util.isInCompMode(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(getPreferenceXml());
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        // It would be easier for us to support one profile only in term of UI design.
        mProfileUserHandle = Util.getBindDeviceAdminTargetUsers(getActivity()).get(0);

        findPreference(KEY_PROFILE_STATUS).setSummary(getManagedProfileStatusDisplayString());
    }

    private String getManagedProfileStatusDisplayString() {
        final boolean isUserRunning = mUserManager.isUserRunning(mProfileUserHandle);
        final boolean isUserUnlocked = mUserManager.isUserUnlocked(mProfileUserHandle);
        final @StringRes int stringRes;
        if (isUserUnlocked) {
            stringRes = R.string.profile_is_unlocked;
        } else if (isUserRunning) {
            stringRes = R.string.profile_is_running_but_locked;
        } else {
            stringRes = R.string.profile_is_not_running;
        }
        return getString(stringRes);
    }
}
