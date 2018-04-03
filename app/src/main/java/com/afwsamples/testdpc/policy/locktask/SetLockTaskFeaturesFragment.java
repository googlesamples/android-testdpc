/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.afwsamples.testdpc.policy.locktask;

import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_HOME;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.ReflectionUtil;
import com.afwsamples.testdpc.common.preference.DpcSwitchPreference;
import java.util.Map;

/**
 * Implementation of {@link DevicePolicyManager#setLockTaskFeatures} and
 * {@link DevicePolicyManager#getLockTaskFeatures}.
 *
 * Note that this PreferenceScreen doesn't persist any of the Preferences. Instead, the current
 * state of LockTask features is already read from {@link DevicePolicyManager} upon
 * {@link #onResume()}.
 */
public class SetLockTaskFeaturesFragment
        extends BaseSearchablePolicyPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SetLockTaskFeatures";

    /* Preference keys. Must be consistent with lock_task_features_preferences.xml */
    private static final String KEY_SYSTEM_INFO = "lock_task_feature_system_info";
    private static final String KEY_NOTIFICATIONS = "lock_task_feature_notifications";
    private static final String KEY_HOME = "lock_task_feature_home";
    private static final String KEY_OVERVIEW = "lock_task_feature_overview";
    private static final String KEY_GLOBAL_ACTIONS = "lock_task_feature_global_actions";
    private static final String KEY_KEYGUARD = "lock_task_feature_keyguard";

    /** Maps from preference keys to {@link DevicePolicyManager#setLockTaskFeatures}'s flags. */
    private static final ArrayMap<String, Integer> FEATURE_FLAGS = new ArrayMap<>();
    static {
        FEATURE_FLAGS.put(KEY_SYSTEM_INFO, LOCK_TASK_FEATURE_SYSTEM_INFO);
        FEATURE_FLAGS.put(KEY_NOTIFICATIONS, LOCK_TASK_FEATURE_NOTIFICATIONS);
        FEATURE_FLAGS.put(KEY_HOME, LOCK_TASK_FEATURE_HOME);
        FEATURE_FLAGS.put(KEY_OVERVIEW, LOCK_TASK_FEATURE_OVERVIEW);
        FEATURE_FLAGS.put(KEY_GLOBAL_ACTIONS, LOCK_TASK_FEATURE_GLOBAL_ACTIONS);
        FEATURE_FLAGS.put(KEY_KEYGUARD, LOCK_TASK_FEATURE_KEYGUARD);
    }

    private DevicePolicyManager mDpm;
    private ComponentName mAdmin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdmin = DeviceAdminReceiver.getComponentName(getActivity());
        getActivity().getActionBar().setTitle(R.string.set_lock_task_features_title);

        // Need to call super.onCreate() at last. Otherwise this.onCreatePreferences() will be
        // called without mDpm and mAdmin being initialized.
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.lock_task_features_preferences);
        for (String key : FEATURE_FLAGS.keySet()) {
            findPreference(key).setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final int enabledFeatures = getLockTaskFeatures();
        for (Map.Entry<String, Integer> entry : FEATURE_FLAGS.entrySet()) {
            DpcSwitchPreference pref = (DpcSwitchPreference) findPreference(entry.getKey());
            pref.setChecked((enabledFeatures & entry.getValue()) != 0);
        }
        enforceEnablingRestrictions(enabledFeatures);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object val) {
        final String key = pref.getKey();
        if (!FEATURE_FLAGS.containsKey(key)) {
            Log.e(TAG, "Undefined preference key: " + key);
            return false;
        }

        final int flagsBefore = getLockTaskFeatures();
        int flagsAfter = (Boolean) val
                ? flagsBefore | FEATURE_FLAGS.get(key)
                : flagsBefore & ~FEATURE_FLAGS.get(key);
        if ((flagsAfter & LOCK_TASK_FEATURE_HOME) == 0) {
            // Disable OVERVIEW and NOTIFICATION when HOME is disabled
            flagsAfter &= ~(LOCK_TASK_FEATURE_OVERVIEW | LOCK_TASK_FEATURE_NOTIFICATIONS);
        }
        if (flagsAfter != flagsBefore) {
            Log.i(TAG, "LockTask feature flags changing from 0x" + Integer.toHexString(flagsBefore)
                    + " to 0x" + Integer.toHexString(flagsAfter));
            try {
                setLockTaskFeatures(flagsAfter);
                enforceEnablingRestrictions(flagsAfter);
                return true;
            } catch (SecurityException e) {
                Log.e(TAG, "setLockTaskFeatures() can only be called by DO and affiliated PO");
                Toast.makeText(getActivity(), "Requires device owner or affiliated profile owner",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    /** Require DO or affiliated PO privileges. */
    @Override
    public boolean isAvailable(Context context) {
        return true;
    }

    private void enforceEnablingRestrictions(int enabledFeatures) {
        boolean isHomeEnabled = (enabledFeatures & LOCK_TASK_FEATURE_HOME) != 0;
        setPrefEnabledState((DpcSwitchPreference) findPreference(KEY_OVERVIEW), isHomeEnabled);
        setPrefEnabledState((DpcSwitchPreference) findPreference(KEY_NOTIFICATIONS), isHomeEnabled);
    }

    private void setPrefEnabledState(DpcSwitchPreference pref, boolean enabled) {
        pref.setEnabled(enabled);
        if (!pref.isEnabled() && pref.isChecked()) {
            pref.setChecked(false);
        }
    }

    @TargetApi(28)
    private int getLockTaskFeatures() {
        return mDpm.getLockTaskFeatures(mAdmin);
    }

    @TargetApi(28)
    private void setLockTaskFeatures(int flags) {
       mDpm.setLockTaskFeatures(mAdmin, flags);
    }
}
