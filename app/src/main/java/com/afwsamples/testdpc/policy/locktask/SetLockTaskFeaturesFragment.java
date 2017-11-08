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

    /*
     * Replace the following int constants LOCK_TASK_FEATURE_* with
     * {@code import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_*} when P SDK
     * is finalized.
     */
    private static final int LOCK_TASK_FEATURE_SYSTEM_INFO;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_SYSTEM_INFO");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_SYSTEM_INFO = flag;
        }
    }
    private static final int LOCK_TASK_FEATURE_NOTIFICATIONS;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_NOTIFICATIONS");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_NOTIFICATIONS = flag;
        }
    }
    private static final int LOCK_TASK_FEATURE_HOME;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_HOME");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_HOME = flag;
        }
    }
    private static final int LOCK_TASK_FEATURE_RECENTS;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_RECENTS");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_RECENTS = flag;
        }
    }
    private static final int LOCK_TASK_FEATURE_GLOBAL_ACTIONS;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_GLOBAL_ACTIONS");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_GLOBAL_ACTIONS = flag;
        }
    }
    private static final int LOCK_TASK_FEATURE_KEYGUARD;
    static {
        int flag = 0;
        try {
            flag = ReflectionUtil.intConstant(
                    DevicePolicyManager.class, "LOCK_TASK_FEATURE_KEYGUARD");
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
        } finally {
            LOCK_TASK_FEATURE_KEYGUARD = flag;
        }
    }

    /* Preference keys. Must be consistent with lock_task_features_preferences.xml */
    private static final String KEY_SYSTEM_INFO = "lock_task_feature_system_info";
    private static final String KEY_NOTIFICATIONS = "lock_task_feature_notifications";
    private static final String KEY_HOME = "lock_task_feature_home";
    private static final String KEY_RECENTS = "lock_task_feature_recents";
    private static final String KEY_GLOBAL_ACTIONS = "lock_task_feature_global_actions";
    private static final String KEY_KEYGUARD = "lock_task_feature_keyguard";

    /** Maps from preference keys to {@link DevicePolicyManager#setLockTaskFeatures}'s flags. */
    private static final ArrayMap<String, Integer> FEATURE_FLAGS = new ArrayMap<>();
    static {
        FEATURE_FLAGS.put(KEY_SYSTEM_INFO, LOCK_TASK_FEATURE_SYSTEM_INFO);
        FEATURE_FLAGS.put(KEY_NOTIFICATIONS, LOCK_TASK_FEATURE_NOTIFICATIONS);
        FEATURE_FLAGS.put(KEY_HOME, LOCK_TASK_FEATURE_HOME);
        FEATURE_FLAGS.put(KEY_RECENTS, LOCK_TASK_FEATURE_RECENTS);
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
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object val) {
        final String key = pref.getKey();
        if (!FEATURE_FLAGS.containsKey(key)) {
            Log.e(TAG, "Undefined preference key: " + key);
            return false;
        }

        final int flagsBefore = getLockTaskFeatures();
        final int flagsAfter = (Boolean) val
                ? flagsBefore | FEATURE_FLAGS.get(key)
                : flagsBefore & ~FEATURE_FLAGS.get(key);
        if (flagsAfter != flagsBefore) {
            Log.i(TAG, "LockTask feature flags changing from 0x" + Integer.toHexString(flagsBefore)
                    + " to 0x" + Integer.toHexString(flagsAfter));
            try {
                setLockTaskFeatures(flagsAfter);
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

    /**
     * Helper method for {@link DevicePolicyManager#getLockTaskFeatures}. Replace reflection by
     * direct method call when P SDK is finalized.
     */
    private int getLockTaskFeatures() {
        try {
            return (int) ReflectionUtil.invoke(mDpm, "getLockTaskFeatures", mAdmin);
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
            Log.e(TAG, "Can't invoke getLockTaskFeatures()", e);
            return 0;
        }
    }

    /**
     * Helper method for {@link DevicePolicyManager#setLockTaskFeatures}. Replace reflection by
     * direct method call when P SDK is finalized.
     */
    private void setLockTaskFeatures(int flags) {
        try {
            ReflectionUtil.invoke(mDpm, "setLockTaskFeatures",
                    new Class[]{ComponentName.class, int.class}, mAdmin, flags);
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
            Log.e(TAG, "Can't invoke setLockTaskFeatures()", e);
        }
    }
}
