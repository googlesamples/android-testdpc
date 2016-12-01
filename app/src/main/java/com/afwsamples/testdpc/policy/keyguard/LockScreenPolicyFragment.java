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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.os.BuildCompat;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.ProfileOrParentFragment;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.common.preference.DpcPreferenceBase;
import com.afwsamples.testdpc.common.preference.DpcPreferenceHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.afwsamples.testdpc.common.preference.DpcPreferenceHelper.NO_CUSTOM_CONSTRIANT;
import static com.afwsamples.testdpc.policy.keyguard.SetTrustAgentConfigFragment.Type;

/**
 * This fragment provides functionalities to set policies on keyguard interaction as a profile
 * or device owner.
 */
public final class LockScreenPolicyFragment extends ProfileOrParentFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = "LockScreenPolicyFragment";

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

        static final String STRONG_AUTH_TIMEOUT = "key_strong_auth_timeout";

        static final String MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
        static final String MAX_TIME_SCREEN_LOCK_ALL = "key_max_time_screen_lock_aggregate";

        static final String KEYGUARD_DISABLE_FINGERPRINT = "keyguard_disable_fingerprint";
        static final String KEYGUARD_DISABLE_REMOTE_INPUT = "keyguard_disable_remote_input";
        static final String KEYGUARD_DISABLE_SECURE_CAMERA = "keyguard_disable_secure_camera";
        static final String KEYGUARD_DISABLE_SECURE_NOTIFICATIONS
                = "keyguard_disable_secure_notifications";
        static final String KEYGUARD_DISABLE_TRUST_AGENTS = "keyguard_disable_trust_agents";
        static final String KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS
                = "keyguard_disable_unredacted_notifications";
        static final String SET_TRUST_AGENT_CONFIG = "key_set_trust_agent_config";

        static final Set<String> NOT_APPLICABLE_TO_PARENT
                = new HashSet<>(Arrays.asList(new String[] {
            KEYGUARD_DISABLE_SECURE_CAMERA,
            KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
            KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
        }));
    }

    private static final Map<String, Integer> KEYGUARD_FEATURES = new ArrayMap<>();
    static {
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

        KEYGUARD_FEATURES.put(Keys.KEYGUARD_DISABLE_REMOTE_INPUT,
                DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(R.string.lock_screen_policy);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        addPreferencesFromResource(getPreferenceXml());
        setupAll();
        disableIncompatibleManagementOptionsInCurrentProfile();
        final int disabledFeatures = getDpm().getKeyguardDisabledFeatures(getAdmin());
        for (Map.Entry<String, Integer> flag : KEYGUARD_FEATURES.entrySet()) {
            setup(flag.getKey(), (disabledFeatures & flag.getValue()) != 0 ? true : false);
        }
    }

    @Override
    public int getPreferenceXml() {
        return R.xml.lock_screen_preferences;
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAggregates();
        findPreference(Keys.STRONG_AUTH_TIMEOUT).setSummary(Long.toString(
                TimeUnit.MILLISECONDS.toSeconds(getRequiredStrongAuthTimeout(getAdmin()))));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEYGUARD_FEATURES.containsKey(preference.getKey())) {
            final int featureFlag = KEYGUARD_FEATURES.get(preference.getKey());
            return updateKeyguardFeatures(featureFlag, (Boolean) newValue);
        }

        switch (preference.getKey()) {
            case Keys.LOCK_SCREEN_MESSAGE:
                setLockScreenMessage(preference, (String) newValue);
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
            case Keys.STRONG_AUTH_TIMEOUT:
                try {
                    final long setting = TimeUnit.SECONDS.toMillis(parseLong((String) newValue));
                    setRequiredStrongAuthTimeout(getAdmin(), setting);
                    preference.setSummary(Long.toString(TimeUnit.MILLISECONDS.toSeconds(
                            getRequiredStrongAuthTimeout(getAdmin()))));
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case Keys.SET_TRUST_AGENT_CONFIG:
                showSetTrustAgentFragment();
                return true;
        }
        return false;
    }

    private void showSetTrustAgentFragment() {
        int type = isParentProfileInstance() ? Type.PARENT : Type.SELF;
        SetTrustAgentConfigFragment fragment = SetTrustAgentConfigFragment.newInstance(type);
        Fragment containerFragment = getParentFragment();
        if (containerFragment == null) {
            containerFragment = this;
        }
        containerFragment.getFragmentManager().beginTransaction()
                .addToBackStack(SetTrustAgentConfigFragment.class.getName())
                .hide(containerFragment)
                .add(R.id.container, fragment)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void setLockScreenMessage(Preference preference, String newValue) {
        getDpm().setDeviceOwnerLockScreenInfo(getAdmin(), newValue);
        preference.setSummary(newValue);
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

    @TargetApi(Build.VERSION_CODES.N)
    private void setupAll() {
        setup(Keys.LOCK_SCREEN_MESSAGE,
                BuildCompat.isAtLeastN() && isDeviceOwner()
                        ? getDpm().getDeviceOwnerLockScreenInfo() : null);
        setup(Keys.MAX_FAILS_BEFORE_WIPE, getDpm().getMaximumFailedPasswordsForWipe(getAdmin()));
        setup(Keys.STRONG_AUTH_TIMEOUT,
                TimeUnit.MILLISECONDS.toSeconds(getRequiredStrongAuthTimeout(getAdmin())));
        setup(Keys.MAX_TIME_SCREEN_LOCK,
                TimeUnit.MILLISECONDS.toSeconds(getDpm().getMaximumTimeToLock(getAdmin())));
        setup(Keys.SET_TRUST_AGENT_CONFIG, null);
    }

    /**
     * Set an initial value. Updates the summary to match.
     */
    private void setup(String key, Object adminSetting) {
        final Preference pref = findPreference(key);
        final DpcPreferenceBase dpcPref = (DpcPreferenceBase) pref;

        // Disable preferences that don't apply to the parent profile
        dpcPref.setCustomConstraint(
                () -> Keys.NOT_APPLICABLE_TO_PARENT.contains(key) && isParentProfileInstance()
                        ? R.string.not_for_parent_profile
                        : NO_CUSTOM_CONSTRIANT
        );

        // We do not allow user to add trust agent config in pre-N devices in managed profile.
        if (!BuildCompat.isAtLeastN() && key.equals(Keys.SET_TRUST_AGENT_CONFIG)) {
            dpcPref.setAdminConstraint(DpcPreferenceHelper.ADMIN_DEVICE_OWNER);
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
        pref.setOnPreferenceClickListener(this);
    }

    private void disableIncompatibleManagementOptionsInCurrentProfile() {
        if (!Util.isAtLeastM()) {
            for (String preference : KEYGUARD_FEATURES.keySet()) {
                ((DpcPreferenceBase) findPreference(preference))
                        .setAdminConstraint(DpcPreferenceHelper.ADMIN_DEVICE_OWNER);
            }
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

    /*
     * Wrapper for calling getDpm().getRequiredStrongAuthTimeout(admin); that's currently hidden
     * in DPM via reflection.
     * TODO(mkarpinski): replace this when API becomes public in Android O
     */
    private long getRequiredStrongAuthTimeout(ComponentName admin) {
        try {
            return (Long) ReflectionUtil.invoke(getDpm(), "getRequiredStrongAuthTimeout",
                    new Class<?>[] {ComponentName.class}, (ComponentName) admin);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to getRequiredStrongAuthTimeout", e);
            return 0;
        }
    }

    /*
     * Wrapper for calling getDpm().setRequiredStrongAuthTimeout(admin, setting); that's currently
     * hidden in DPM via reflection.
     * TODO(mkarpinski): replace this when API becomes public in Android O
     */
    private void setRequiredStrongAuthTimeout(ComponentName admin, long setting) {
        try {
            ReflectionUtil.invoke(getDpm(), "setRequiredStrongAuthTimeout",
                    new Class<?>[] {ComponentName.class, long.class}, (ComponentName) admin,
                    setting);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to setRequiredStrongAuthTimeout", e);
        }
    }
}
