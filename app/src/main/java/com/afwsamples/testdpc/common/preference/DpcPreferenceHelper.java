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

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

/**
 * Helper class to check preference constraints declared in the XML file and disable the preference
 * with an informative message if the constraint does not hold. The API level, admin type (device
 * or profile owner) and user type (primary, managed profile, etc.) can be used as constraints.
 *
 * @attr ref android.R.styleable#DpcPreference_minSdkVersion
 * @attr ref android.R.styleable#DpcPreference_admin
 * @attr ref android.R.styleable#DpcPreference_user
 */
public class DpcPreferenceHelper {
    private Context mContext;
    private Preference mPreference;

    private CharSequence mConstraintViolationSummary = null;
    private CharSequence mCustomConstraintSummary = null;
    private final int mMinSdkVersion;
    private int mAdminConstraint;
    private int mUserConstraint;

    public static final int ADMIN_DEVICE_OWNER = 0x1;
    public static final int ADMIN_PROFILE_OWNER = 0x2;
    public static final int ADMIN_ANY = ADMIN_DEVICE_OWNER | ADMIN_PROFILE_OWNER;

    public static final int USER_PRIMARY_USER = 0x1;
    public static final int USER_MANAGED_PROFILE = 0x2;
    public static final int USER_ANY = USER_PRIMARY_USER | USER_MANAGED_PROFILE;

    public DpcPreferenceHelper(Context context, Preference preference, AttributeSet attrs) {
        mContext = context;
        mPreference = preference;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DpcPreference);

        mMinSdkVersion = a.getInt(R.styleable.DpcPreference_minSdkVersion, 0);
        if (mMinSdkVersion == 0) {
            throw new RuntimeException("testdpc:minSdkVersion must be specified.");
        }

        mAdminConstraint = a.getInt(R.styleable.DpcPreference_admin, ADMIN_ANY);
        mUserConstraint = a.getInt(R.styleable.DpcPreference_user, USER_ANY);

        a.recycle();
    }

    /**
     * Override the summary with any constraint violation messages.
     */
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (!constraintsMet()) {
            final TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
            if (summaryView != null) {
                summaryView.setText(mConstraintViolationSummary);
                summaryView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onAttachedToHierarchy() {
        disableIfConstraintsNotMet();
    }

    /**
     * Set constraints on the admin.
     *
     * @param adminConstraint The admins for which the preference is enabled.
     */
    public void setAdminConstraint(int adminConstraint) {
        mAdminConstraint = adminConstraint;
        disableIfConstraintsNotMet();
    }

    /**
     * Clear constraints on the admin.
     */
    public void clearAdminConstraint() {
        setAdminConstraint(ADMIN_ANY);
    }

    /**
     * Set constraints on the user.
     *
     * @param userConstraint The users for which the preference is enabled.
     */
    public void setUserConstraint(int userConstraint) {
        mUserConstraint = userConstraint;
        disableIfConstraintsNotMet();
    }

    /**
     * Clear constraints on the user.
     */
    public void clearUserConstraint() {
        setUserConstraint(USER_ANY);
    }

    /**
     * Clear the admin and user constraints for this preference.
     * <p/>
     * Custom constraints will remain.
     */
    public void clearNonCustomConstraints() {
        clearAdminConstraint();
        clearUserConstraint();
    }

    /**
     * Disable the preference for a custom reason.
     * <p/>
     * The custom constraint will have higher priority than all other constraints other than the
     * minimum SDK version constraint. Setting multiple custom constraints is not possible and only
     * the most recent will be used.
     * <p/>
     * Use {@link #clearCustomConstraint()} to remove the custom constraint.
     *
     * @param constraintSummary Brief explanation of the constraint violation.
     */
    public void setCustomConstraint(CharSequence constraintSummary) {
        mCustomConstraintSummary = constraintSummary;
        disableIfConstraintsNotMet();
    }

    /**
     * Remove any custom constraints set by {@link #setCustomConstraint(CharSequence)}.
     * <p/>
     * This method is safe to call if there is no current custom constraint.
     */
    public void clearCustomConstraint() {
        setCustomConstraint(null);
    }

    private void disableIfConstraintsNotMet() {
        mConstraintViolationSummary = findContraintViolation();
        mPreference.setEnabled(constraintsMet());
    }

    /**
     * Check for constraint violations.
     *
     * TODO(ascull): change message to say when the preference will be enabled rather than explain
     * why it is currently disabled.
     *
     * @return A string describing the constraint violation or {@code null} if no violations were
     * found.
     */
    private CharSequence findContraintViolation() {
        if (Build.VERSION.SDK_INT < mMinSdkVersion) {
            return mContext.getString(R.string.requires_android_api_level, mMinSdkVersion);
        }

        // Custom constraints have high priority
        if (mCustomConstraintSummary != null) {
            return mCustomConstraintSummary;
        }

        // Admin constraints
        final DevicePolicyManager dpm =
                (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final String packageName = mContext.getPackageName();

        if (isDisabledForAdmin(ADMIN_DEVICE_OWNER) && dpm.isDeviceOwnerApp(packageName)) {
            return mContext.getString(R.string.not_for_device_owner);
        }

        if (isDisabledForAdmin(ADMIN_PROFILE_OWNER) && dpm.isProfileOwnerApp(packageName)) {
            return mContext.getString(R.string.not_for_profile_owner);
        }

        // User constraints
        if (isDisabledForUser(USER_PRIMARY_USER) && Util.isPrimaryUser(mContext)) {
            return mContext.getString(R.string.not_for_primary_user);
        }

        if (isDisabledForUser(USER_MANAGED_PROFILE) && Util.isManagedProfile(
                mContext, DeviceAdminReceiver.getComponentName(mContext))) {
            return mContext.getString(R.string.not_for_managed_profile);
        }

        return null;
    }

    private boolean isDisabledForAdmin(int admin) {
        return (mAdminConstraint & admin) != admin;
    }

    private boolean isDisabledForUser(int user) {
        return (mUserConstraint & user) != user;
    }

    /**
     * Return whether the preference's constraints are met.
     *
     * @return True if there are no violations of the preference's constraints.
     */
    public boolean constraintsMet() {
        return TextUtils.isEmpty(mConstraintViolationSummary);
    }
}
