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
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This fragment provides functionalities to set password constraint policies as a profile
 * or device owner. In the former case, it is also possible to set password constraints on
 * the parent profile.
 *
 * <p>These include:
 * <ul>
 * <li>{@link DevicePolicyManager#setPasswordQuality(ComponentName, int)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLength(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLetters(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumNumeric(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumLowerCase(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumUpperCase(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumSymbols(ComponentName, String)}</li>
 * <li>{@link DevicePolicyManager#setPasswordMinimumNonLetter(ComponentName, String)}</li>
 * </ul>
 */
public final class PasswordConstraintsFragment extends PreferenceFragment implements
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
        final static String QUALITY = "minimum_password_quality";

        final static String MIN_LENGTH = "password_min_length";
        final static String MIN_LETTERS = "password_min_letters";
        final static String MIN_NUMERIC = "password_min_numeric";
        final static String MIN_LOWERCASE = "password_min_lowercase";
        final static String MIN_UPPERCASE = "password_min_uppercase";
        final static String MIN_SYMBOLS = "password_min_symbols";
        final static String MIN_NONLETTER = "password_min_nonletter";
    }

    private static final TreeMap<Integer, Integer> PASSWORD_QUALITIES = new TreeMap<>();
    static {
        // IDs of settings for {@link DevicePolicyManager#setPasswordQuality(ComponentName, int)}.
        final int[] policyIds = new int[] {
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
        };
        // Strings to show for each password quality setting.
        final int[] policyNames = new int[] {
            R.string.password_quality_unspecified,
            R.string.password_quality_something,
            R.string.password_quality_numeric,
            R.string.password_quality_numeric_complex,
            R.string.password_quality_alphabetic,
            R.string.password_quality_alphanumeric,
            R.string.password_quality_complex
        };
        if (policyIds.length != policyNames.length) {
            throw new AssertionError("Number of items in policyIds and policyNames do not match");
        }
        for (int i = 0; i < policyIds.length; i++) {
            PASSWORD_QUALITIES.put(policyIds[i], policyNames[i]);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.password_constraints);

        mDevicePolicyManager = (DevicePolicyManager)
                getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DeviceAdminReceiver.getComponentName(getActivity());

        addPreferencesFromResource(R.xml.password_constraint_preferences);

        // Populate password quality settings - messy because the only API for this requires two
        // separate String[]s.
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> values = new ArrayList<>();
        for (TreeMap.Entry<Integer, Integer> entry : PASSWORD_QUALITIES.entrySet()) {
            values.add(Integer.toString(entry.getKey()));
            entries.add(getString(entry.getValue()));
        }
        ListPreference quality = (ListPreference) findPreference(Keys.QUALITY);
        quality.setEntries(entries.toArray(new CharSequence[0]));
        quality.setEntryValues(values.toArray(new CharSequence[0]));

        // Minimum quality requirement.
        setup(Keys.QUALITY, PASSWORD_QUALITIES.floorKey(getDpm().getPasswordQuality(getAdmin())));

        // Minimum length requirements.
        setup(Keys.MIN_LENGTH, getDpm().getPasswordMinimumLength(getAdmin()));
        setup(Keys.MIN_LETTERS, getDpm().getPasswordMinimumLetters(getAdmin()));
        setup(Keys.MIN_NUMERIC, getDpm().getPasswordMinimumNumeric(getAdmin()));
        setup(Keys.MIN_LOWERCASE, getDpm().getPasswordMinimumLowerCase(getAdmin()));
        setup(Keys.MIN_UPPERCASE, getDpm().getPasswordMinimumUpperCase(getAdmin()));
        setup(Keys.MIN_SYMBOLS, getDpm().getPasswordMinimumSymbols(getAdmin()));
        setup(Keys.MIN_NONLETTER, getDpm().getPasswordMinimumNonLetter(getAdmin()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int value;
        if (newValue instanceof String && ((String) newValue).length() != 0) {
            try {
                value = Integer.parseInt((String) newValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.not_valid_input, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            value = 0;
        }

        // By default, show the new value as a summary.
        CharSequence summary = newValue.toString();

        switch (preference.getKey()) {
            case Keys.QUALITY: {
                final ListPreference list = (ListPreference) preference;
                // Store newValue now so getEntry() can return the new setting
                list.setValue((String) newValue);
                summary = list.getEntry();
                getDpm().setPasswordQuality(getAdmin(), value);
                break;
            }
            case Keys.MIN_LENGTH:
                getDpm().setPasswordMinimumLength(getAdmin(), value);
                break;
            case Keys.MIN_LETTERS:
                getDpm().setPasswordMinimumLetters(getAdmin(), value);
                break;
            case Keys.MIN_NUMERIC:
                getDpm().setPasswordMinimumNumeric(getAdmin(), value);
                break;
            case Keys.MIN_LOWERCASE:
                getDpm().setPasswordMinimumLowerCase(getAdmin(), value);
                break;
            case Keys.MIN_UPPERCASE:
                getDpm().setPasswordMinimumUpperCase(getAdmin(), value);
                break;
            case Keys.MIN_SYMBOLS:
                getDpm().setPasswordMinimumSymbols(getAdmin(), value);
                break;
            case Keys.MIN_NONLETTER:
                getDpm().setPasswordMinimumNonLetter(getAdmin(), value);
                break;
            default:
                return false;
        }

        preference.setSummary(summary);
        sendPasswordRequirementsChanged();
        return true;
    }

    /**
     * Set an initial value. Updates the summary to match.
     */
    private void setup(String key, Object adminSetting) {
        Preference field = findPreference(key);
        field.setOnPreferenceChangeListener(this);

        if (adminSetting == null) {
            return;
        }

        final String stringSetting = adminSetting.toString();
        CharSequence summary = stringSetting;

        if (field instanceof EditTextPreference) {
            EditTextPreference p = (EditTextPreference) field;
            p.setText(stringSetting);
        } else if (field instanceof ListPreference) {
            ListPreference p = (ListPreference) field;
            p.setValue(stringSetting);
            summary = p.getEntry();
        }
        field.setSummary(summary);
    }

    /**
     * Notify the admin receiver that something about the password has changed - in this context,
     * a minimum password requirement policy.
     *
     * This has to be sent manually because the system server only sends broadcasts for changes to
     * the actual password, not any of the constraints related it it.
     *
     * <p>May trigger a show/hide of the notification warning to change the password through
     * Settings.
     */
    private void sendPasswordRequirementsChanged() {
        Intent changedIntent = new Intent(DeviceAdminReceiver.ACTION_PASSWORD_REQUIREMENTS_CHANGED);
        changedIntent.setComponent(getAdmin());
        getContext().sendBroadcast(changedIntent);
    }
}
