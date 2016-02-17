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
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.TwoStatePreference;
import android.util.ArrayMap;
import android.widget.Toast;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ProfileOrParentFragment;
import com.afwsamples.testdpc.common.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This fragment provides functionalities to set policies on keyguard interaction as a profile
 * or device owner.
 */
public final class LockScreenPolicyFragment extends ProfileOrParentFragment implements
        Preference.OnPreferenceChangeListener {

    public static class Container extends ProfileOrParentFragment.Container {
        @Override
        public Class<? extends ProfileOrParentFragment> getContentClass() {
            return LockScreenPolicyFragment.class;
        }
    }

    abstract static class Keys {
        static final String LOCK_SCREEN_MESSAGE = "key_lock_screen_message";

        static final String MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe";
        static final String MAX_FAILS_BEFORE_WIPE_ALL = "key_max_fails_before_wipe_aggregate";

        static final String MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
        static final String MAX_TIME_SCREEN_LOCK_ALL = "key_max_time_screen_lock_aggregate";

        static final String KEYGUARD_FEATURES_CATEGORY = "keyguard_features";

        static final String KEYGUARD_DISABLE_FINGERPRINT = "keyguard_disable_fingerprint";
        static final String KEYGUARD_DISABLE_SECURE_CAMERA = "keyguard_disable_secure_camera";
        static final String KEYGUARD_DISABLE_SECURE_NOTIFICATIONS
                = "keyguard_disable_secure_notifications";
        static final String KEYGUARD_DISABLE_TRUST_AGENTS = "keyguard_disable_trust_agents";
        static final String KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS
                = "keyguard_disable_unredacted_notifications";
        static final String KEYGUARD_DISABLE_WIDGETS = "keyguard_disable_widgets";

        static final Set<String> NOT_APPLICABLE_TO_PARENT
                = new HashSet<>(Arrays.asList(new String[] {
            KEYGUARD_DISABLE_SECURE_CAMERA,
            KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
            KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
            KEYGUARD_DISABLE_WIDGETS
        }));

        static final Set<String> NOT_APPLICABLE_TO_PROFILE
                = new HashSet<>(Arrays.asList(new String[] {
            KEYGUARD_DISABLE_SECURE_CAMERA,
            KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
            KEYGUARD_DISABLE_WIDGETS
        }));

        static final Set<String> DEVICE_OWNER_ONLY
                = new HashSet<>(Arrays.asList(new String[] {
            LOCK_SCREEN_MESSAGE
        }));

        static final Set<String> NYC_PLUS
                = new HashSet<>(Arrays.asList(new String[] {
            LOCK_SCREEN_MESSAGE
        }));
    }

    private static final Map<String, Integer> KEYGUARD_FEATURES = new ArrayMap<>();
    static {
        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_FINGERPRINT,
                DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_SECURE_CAMERA,
                DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
                DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
                DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_TRUST_AGENTS,
                DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_FINGERPRINT,
                DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT);

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_WIDGETS,
                DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.lock_screen_policy);

        addPreferencesFromResource(R.xml.lock_screen_preferences);

        setup(Keys.LOCK_SCREEN_MESSAGE,
                Util.isBeforeN() ? null : getDpm().getDeviceOwnerLockScreenInfo());

        setup(Keys.MAX_FAILS_BEFORE_WIPE, getDpm().getMaximumFailedPasswordsForWipe(getAdmin()));
        setup(Keys.MAX_TIME_SCREEN_LOCK,
                TimeUnit.MILLISECONDS.toSeconds(getDpm().getMaximumTimeToLock(getAdmin())));

        final int disabledFeatures = getDpm().getKeyguardDisabledFeatures(getAdmin());
        for (Map.Entry<String, Integer> flag : KEYGUARD_FEATURES.entrySet()) {
            setup(flag.getKey(), (disabledFeatures & flag.getValue()) != 0 ? true : false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAggregates();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEYGUARD_FEATURES.containsKey(preference.getKey())) {
            final int featureFlag = KEYGUARD_FEATURES.get(preference.getKey());
            return updateKeyguardFeatures(featureFlag, (Boolean) newValue);
        }

        switch (preference.getKey()) {
            case Keys.LOCK_SCREEN_MESSAGE:
                getDpm().setDeviceOwnerLockScreenInfo(getAdmin(), (String) newValue);
                preference.setSummary((String) newValue);
                return true;
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

    private boolean updateKeyguardFeatures(int flag, boolean newValue) {
        int disabledFeatures = getDpm().getKeyguardDisabledFeatures(getAdmin());
        if (newValue) {
            disabledFeatures |= flag;
        } else {
            disabledFeatures &= ~flag;
        }
        getDpm().setKeyguardDisabledFeatures(getAdmin(), disabledFeatures);

        // Verify that the new setting stuck.
        int newDisabledFeatures = getDpm().getKeyguardDisabledFeatures(getAdmin());
        return disabledFeatures == newDisabledFeatures;
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
    private void setup(String key, Object adminSetting) {
        Preference pref = findPreference(key);

        // If the preference is not applicable, just hide it instead.
        if ((Keys.NOT_APPLICABLE_TO_PARENT.contains(key) && isParentProfileInstance())
                || (Keys.NOT_APPLICABLE_TO_PROFILE.contains(key) && isManagedProfileInstance())
                || (Keys.DEVICE_OWNER_ONLY.contains(key) && !isDeviceOwner())
                || (Keys.NYC_PLUS.contains(key) && Util.isBeforeN())) {
            pref.setEnabled(false);
            return;
        }

        // Set up initial state and possibly a descriptive summary of said state.
        if (pref instanceof EditTextPreference) {
            String stringValue = (adminSetting != null ? adminSetting.toString() : null);
            if (adminSetting instanceof Number && "0".equals(stringValue)) {
                stringValue = null;
            }
            ((EditTextPreference) pref).setText(stringValue);
            pref.setSummary(stringValue);
        } else if (pref instanceof TwoStatePreference) {
            ((TwoStatePreference) pref).setChecked((Boolean) adminSetting);
        }

        // Start listening for change events.
        pref.setOnPreferenceChangeListener(this);
    }

    private int parseInt(String value) throws NumberFormatException {
        return value.length() != 0 ? Integer.parseInt(value) : 0;
    }

    private long parseLong(String value) throws NumberFormatException {
        return value.length() != 0 ? Long.parseLong(value) : 0L;
    }

    private boolean isDeviceOwner() {
        return getDpm().isDeviceOwnerApp(getContext().getPackageName());
    }

    private void showToast(int titleId) {
        Toast.makeText(getActivity(), titleId, Toast.LENGTH_SHORT).show();
    }
}
