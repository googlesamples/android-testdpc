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

package com.afwsamples.testdpc.policy.keyguard;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.util.concurrent.TimeUnit;

/**
 * This fragment provides functionalities to set policies on keyguard interaction as a profile
 * or device owner.
 */
public final class LockScreenPolicyFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;

    private DevicePolicyManager getDpm() {
        return mDevicePolicyManager;
    }

    private ComponentName getAdmin() {
        return mAdminComponent;
    }

    abstract static class Keys {
        static final String MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe";
        static final String MAX_FAILS_BEFORE_WIPE_ALL = "key_max_fails_before_wipe_aggregate";

        static final String MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
        static final String MAX_TIME_SCREEN_LOCK_ALL = "key_max_time_screen_lock_aggregate";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.lock_screen_policy);

        mDevicePolicyManager = (DevicePolicyManager)
                getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());

        addPreferencesFromResource(R.xml.lock_screen_preferences);

        setup(Keys.MAX_FAILS_BEFORE_WIPE, getDpm().getMaximumFailedPasswordsForWipe(getAdmin()));
        setup(Keys.MAX_TIME_SCREEN_LOCK,
                TimeUnit.MILLISECONDS.toSeconds(getDpm().getMaximumTimeToLock(getAdmin())));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAggregates();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case Keys.MAX_FAILS_BEFORE_WIPE:
                try {
                    final int setting = parseInt((String) newValue);
                    getDpm().setMaximumFailedPasswordsForWipe(getAdmin(), setting);
                    preference.setSummary(setting != 0 ? Integer.toString(setting) : null);
                } catch (NumberFormatException e) {
                    showToast(R.string.not_valid_input);
                    return false;
                }
                break;
            case Keys.MAX_TIME_SCREEN_LOCK:
                try {
                    final long setting = parseLong((String) newValue);
                    getDpm().setMaximumTimeToLock(getAdmin(), TimeUnit.SECONDS.toMillis(setting));
                    preference.setSummary(setting != 0 ? Long.toString(setting) : null);
                } catch (NumberFormatException e) {
                    showToast(R.string.not_valid_input);
                    return false;
                }
                break;
            default:
                return false;
        }

        updateAggregates();
        return true;
    }

    private void updateAggregates() {
        final int maxFailedPasswords = getDpm().getMaximumFailedPasswordsForWipe(null);
        findPreference(Keys.MAX_FAILS_BEFORE_WIPE_ALL).setSummary(
                maxFailedPasswords != 0
                ? Integer.toString(maxFailedPasswords)
                : null);

        final long maxTimeToLock = getDpm().getMaximumTimeToLock(null);
        findPreference(Keys.MAX_TIME_SCREEN_LOCK_ALL).setSummary(
                maxTimeToLock != 0
                ? Long.toString(TimeUnit.MILLISECONDS.toSeconds(maxTimeToLock))
                : null);
    }

    /**
     * Set an initial value. Updates the summary to match.
     */
    private void setup(String key, long adminSetting) {
        Preference pref = findPreference(key);
        pref.setOnPreferenceChangeListener(this);

        if (adminSetting != 0) {
            final String stringSetting = Long.toString(adminSetting);

            if (pref instanceof EditTextPreference) {
                ((EditTextPreference) pref).setText(stringSetting);
            }
            pref.setSummary(stringSetting);
        }
    }

    private int parseInt(String value) throws NumberFormatException {
        return value.length() != 0 ? Integer.parseInt(value) : 0;
    }

    private long parseLong(String value) throws NumberFormatException {
        return value.length() != 0 ? Long.parseLong(value) : 0L;
    }

    private void showToast(int titleId) {
        Toast.makeText(getActivity(), titleId, Toast.LENGTH_SHORT).show();
    }
}
