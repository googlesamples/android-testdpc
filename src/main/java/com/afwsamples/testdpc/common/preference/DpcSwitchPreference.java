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
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

/**
 * An {@link SwitchPreference} which can be disabled via XML declared constraints.
 *
 * <p>See {@link DpcPreferenceHelper} for details about constraints.
 */
public class DpcSwitchPreference extends SwitchPreference implements DpcPreferenceBase {
  private DpcPreferenceHelper mHelper;

  public DpcSwitchPreference(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mHelper = new DpcPreferenceHelper(context, this, attrs);
  }

  public DpcSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public DpcSwitchPreference(Context context, AttributeSet attrs) {
    this(context, attrs, androidx.preference.R.attr.switchPreferenceStyle);
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
    mHelper.disableIfConstraintsNotMet();
    super.onAttachedToHierarchy(preferenceManager);
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled && !mHelper.constraintsMet()) {
      return; // ignore
    }
    super.setEnabled(enabled);
  }

  @Override
  public void setMinSdkVersion(int version) {
    mHelper.setMinSdkVersion(version);
  }

  @Override
  public void setAdminConstraint(@DpcPreferenceHelper.AdminKind int adminConstraint) {
    mHelper.setAdminConstraint(adminConstraint);
  }

  @Override
  public void clearAdminConstraint() {
    mHelper.clearAdminConstraint();
  }

  @Override
  public void setUserConstraint(@DpcPreferenceHelper.UserKind int userConstraints) {
    mHelper.setUserConstraint(userConstraints);
  }

  @Override
  public void clearUserConstraint() {
    mHelper.clearUserConstraint();
  }

  @Override
  public void clearNonCustomConstraints() {
    mHelper.clearNonCustomConstraints();
  }

  @Override
  public void setCustomConstraint(@Nullable CustomConstraint customConstraint) {
    mHelper.setCustomConstraint(customConstraint);
  }

  @Override
  public void addCustomConstraint(@Nullable CustomConstraint customConstraint) {
    mHelper.addCustomConstraint(customConstraint);
  }

  @Override
  public void refreshEnabledState() {
    mHelper.disableIfConstraintsNotMet();
  }
}
