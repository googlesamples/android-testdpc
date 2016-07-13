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

package com.afwsamples.testdpc.common.preference;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

/**
 * An {@link SwitchPreference} which can be disabled via XML declared constraints.
 *
 * See {@link DpcPreferenceHelper} for details about constraints.
 */
public class DpcSwitchPreference extends SwitchPreference {
    private DpcPreferenceHelper mHelper;

    public DpcSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHelper = new DpcPreferenceHelper(context, this, attrs);
    }

    public DpcSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DpcSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v14.preference.R.attr.switchPreferenceStyle);
    }

    public DpcSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mHelper.onBindViewHolder(holder);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled && !mHelper.constraintsMet()) {
            return; // ignore
        }
        super.setEnabled(enabled);
    }

    public void setAdminConstraint(int adminConstraint) {
        mHelper.setAdminConstraint(adminConstraint);
    }

    public void clearAdminConstraint() {
        mHelper.clearAdminConstraint();
    }

    public void setUserConstraint(int userConstraints) {
        mHelper.setUserConstraint(userConstraints);
    }

    public void clearUserConstraint() {
        mHelper.clearUserConstraint();
    }

    public void clearNonCustomConstraints() {
        mHelper.clearNonCustomConstraints();
    }

    public void setCustomConstraint(CharSequence constraintSummary) {
        mHelper.setCustomConstraint(constraintSummary);
    }

    public void setCustomConstraint(@StringRes int constraintSummaryRes) {
        mHelper.setCustomConstraint(getContext().getString(constraintSummaryRes));
    }

    public void clearCustomConstraint() {
        mHelper.clearCustomConstraint();
    }
}

