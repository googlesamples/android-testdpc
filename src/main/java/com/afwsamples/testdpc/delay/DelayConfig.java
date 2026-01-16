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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration for the delay feature.
 * Stores whether delay is enabled and the delay duration.
 */
public class DelayConfig {
    private static final String PREFS_NAME = "delay_config";
    private static final String KEY_ENABLED = "delay_enabled";
    private static final String KEY_DURATION_VALUE = "delay_duration_value";
    private static final String KEY_DURATION_UNIT = "delay_duration_unit";

    public enum TimeUnit {
        SECONDS(1000L),
        MINUTES(60 * 1000L),
        HOURS(60 * 60 * 1000L);

        private final long millis;

        TimeUnit(long millis) {
            this.millis = millis;
        }

        public long toMillis(long value) {
            return value * millis;
        }
    }

    private final SharedPreferences prefs;

    public DelayConfig(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public long getDurationValue() {
        return prefs.getLong(KEY_DURATION_VALUE, 1);
    }

    public void setDurationValue(long value) {
        prefs.edit().putLong(KEY_DURATION_VALUE, value).apply();
    }

    public TimeUnit getDurationUnit() {
        String unitName = prefs.getString(KEY_DURATION_UNIT, TimeUnit.HOURS.name());
        return TimeUnit.valueOf(unitName);
    }

    public void setDurationUnit(TimeUnit unit) {
        prefs.edit().putString(KEY_DURATION_UNIT, unit.name()).apply();
    }

    /**
     * @return The configured delay duration in milliseconds.
     */
    public long getDelayMillis() {
        return getDurationUnit().toMillis(getDurationValue());
    }

    /**
     * @return Human-readable delay duration string (e.g., "24 hours").
     */
    public String getDelayDisplayString() {
        long value = getDurationValue();
        String unit = getDurationUnit().name().toLowerCase();
        if (value == 1) {
            // Remove trailing 's' for singular
            unit = unit.substring(0, unit.length() - 1);
        }
        return value + " " + unit;
    }
}
