/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.afwsamples.testdpc.delay;

import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import com.afwsamples.testdpc.R;

/**
 * Settings fragment for configuring the delay feature.
 */
public class DelaySettingsFragment extends PreferenceFragmentCompat {

    private DelayConfig config;
    private DelayManager delayManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.delay_settings, rootKey);

        config = new DelayConfig(requireContext());
        delayManager = DelayManager.getInstance(requireContext());

        setupEnableSwitch();
        setupDurationValue();
        setupDurationUnit();
        setupPendingChangesLink();
    }

    private void setupEnableSwitch() {
        SwitchPreference enablePref = findPreference("delay_enabled");
        if (enablePref == null) return;

        enablePref.setChecked(config.isEnabled());

        enablePref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;

            if (enabled) {
                // Enabling is instant
                delayManager.enableDelay();
                DelayService.start(requireContext());
                Toast.makeText(requireContext(),
                    "Delay enabled. Duration: " + config.getDelayDisplayString(),
                    Toast.LENGTH_LONG).show();
                return true;
            } else {
                // Disabling goes through delay
                if (config.isEnabled()) {
                    delayManager.queueDisableDelay();
                    Toast.makeText(requireContext(),
                        "Disable queued. Will take effect in " + config.getDelayDisplayString(),
                        Toast.LENGTH_LONG).show();
                    return false; // Don't update UI yet
                }
                return true;
            }
        });
    }

    private void setupDurationValue() {
        EditTextPreference valuePref = findPreference("delay_duration_value");
        if (valuePref == null) return;

        valuePref.setText(String.valueOf(config.getDurationValue()));
        valuePref.setSummary(String.valueOf(config.getDurationValue()));

        valuePref.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                long value = Long.parseLong(newValue.toString());
                if (value <= 0) {
                    Toast.makeText(requireContext(),
                        "Duration must be positive", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (config.isEnabled()) {
                    // Queue the change
                    Toast.makeText(requireContext(),
                        "Duration change queued. Will take effect in " + config.getDelayDisplayString(),
                        Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    config.setDurationValue(value);
                    valuePref.setSummary(String.valueOf(value));
                    return true;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                    "Invalid number", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setupDurationUnit() {
        ListPreference unitPref = findPreference("delay_duration_unit");
        if (unitPref == null) return;

        unitPref.setValue(config.getDurationUnit().name());
        unitPref.setSummary(config.getDurationUnit().name());

        unitPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (config.isEnabled()) {
                Toast.makeText(requireContext(),
                    "Unit change queued. Will take effect in " + config.getDelayDisplayString(),
                    Toast.LENGTH_LONG).show();
                return false;
            } else {
                config.setDurationUnit(DelayConfig.TimeUnit.valueOf(newValue.toString()));
                unitPref.setSummary(newValue.toString());
                return true;
            }
        });
    }

    private void setupPendingChangesLink() {
        Preference pendingPref = findPreference("pending_changes");
        if (pendingPref == null) return;

        pendingPref.setOnPreferenceClickListener(preference -> {
            // Navigate to pending changes list
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new PendingChangesFragment())
                .addToBackStack(null)
                .commit();
            return true;
        });
    }
}
