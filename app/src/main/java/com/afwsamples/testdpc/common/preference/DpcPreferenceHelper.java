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
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

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
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {ADMIN_NONE, ADMIN_DEVICE_OWNER, ADMIN_PROFILE_OWNER})
    public @interface AdminKind {}
    public static final int ADMIN_NONE = 0x1;
    public static final int ADMIN_DEVICE_OWNER = 0x2;
    public static final int ADMIN_PROFILE_OWNER = 0x4;
    public static final int ADMIN_ANY = ADMIN_NONE | ADMIN_DEVICE_OWNER | ADMIN_PROFILE_OWNER;
    public static final int ADMIN_NOT_NONE = ADMIN_ANY & ~ADMIN_NONE;
    public static final int ADMIN_NOT_DEVICE_OWNER = ADMIN_ANY & ~ADMIN_DEVICE_OWNER;
    public static final int ADMIN_NOT_PROFILE_OWNER = ADMIN_ANY & ~ADMIN_PROFILE_OWNER;
    public static final @AdminKind int ADMIN_DEFAULT = ADMIN_NOT_NONE;
    public static final int NO_CUSTOM_CONSTRIANT = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {USER_PRIMARY_USER, USER_SECONDARY_USER, USER_MANAGED_PROFILE})
    public @interface UserKind {}
    public static final int USER_PRIMARY_USER = 0x1;
    public static final int USER_SECONDARY_USER = 0x2;
    public static final int USER_MANAGED_PROFILE = 0x4;
    public static final int USER_ANY =
            USER_PRIMARY_USER | USER_SECONDARY_USER | USER_MANAGED_PROFILE;
    public static final int USER_NOT_PRIMARY_USER = USER_ANY & ~USER_PRIMARY_USER;
    public static final int USER_NOT_SECONDARY_USER = USER_ANY & ~USER_SECONDARY_USER;
    public static final int USER_NOT_MANAGED_PROFILE = USER_ANY & ~USER_MANAGED_PROFILE;
    public static final @UserKind int USER_DEFAULT = USER_ANY;

    private static final int NUM_ADMIN_KINDS = Integer.bitCount(ADMIN_ANY);
    private static final int NUM_USER_KINDS = Integer.bitCount(USER_ANY);

    private Context mContext;
    private Preference mPreference;

    private CharSequence mConstraintViolationSummary = null;
    private CustomConstraint mCustomConstraint = null;
    private int mMinSdkVersion;
    private @AdminKind int mAdminConstraint;
    private @UserKind int mUserConstraint;

    /**
     * Update this method as {@link Build.VERSION_CODES} and Android releases are updated.
     *
     * @return The version SDK int or {@link Build.VERSION_CODES.CUR_DEVELOPMENT} if the SDK int is
     *         not yet assigned.
     */
    private int getDeviceSdkInt() {
        // TODO(b/117767701): Remove this when Q version code is finalized.
        if (Util.isAtLeastQ()) {
            return Build.VERSION_CODES.CUR_DEVELOPMENT;
        }
        return Build.VERSION.SDK_INT;
    }

    public DpcPreferenceHelper(Context context, Preference preference, AttributeSet attrs) {
        mContext = context;
        mPreference = preference;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DpcPreference);

        mMinSdkVersion = a.getInt(R.styleable.DpcPreference_minSdkVersion, 0);
        if (attrs == null) {
            // Be more lenient when creating the preference from code
            mMinSdkVersion = Build.VERSION_CODES.LOLLIPOP;
        }
        if (mMinSdkVersion == 0) {
            throw new RuntimeException("testdpc:minSdkVersion must be specified.");
        }

        // noinspection ResourceType
        mAdminConstraint = a.getInt(R.styleable.DpcPreference_admin, ADMIN_DEFAULT);
        // noinspection ResourceType
        mUserConstraint = a.getInt(R.styleable.DpcPreference_user, USER_DEFAULT);

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

    /**
     * Set the minimum required API level constraint.
     *
     * @param version The minimum required version.
     */
    public void setMinSdkVersion(int version) {
        mMinSdkVersion = version;
        disableIfConstraintsNotMet();
    }

    /**
     * Set constraints on the admin.
     *
     * @param adminConstraint The admins for which the preference is enabled.
     */
    public void setAdminConstraint(@AdminKind int adminConstraint) {
        mAdminConstraint = adminConstraint;
        disableIfConstraintsNotMet();
    }

    /**
     * Clear constraints on the admin.
     */
    public void clearAdminConstraint() {
        setAdminConstraint(ADMIN_DEFAULT);
    }

    /**
     * Set constraints on the user.
     *
     * @param userConstraint The users for which the preference is enabled.
     */
    public void setUserConstraint(@UserKind int userConstraint) {
        mUserConstraint = userConstraint;
        disableIfConstraintsNotMet();
    }

    /**
     * Clear constraints on the user.
     */
    public void clearUserConstraint() {
        setUserConstraint(USER_DEFAULT);
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
     * Specify a custom constraint by setting a {{@link CustomConstraint}} object. The enabled
     * state of the preference will be updated accordingly.
     */
    public void setCustomConstraint(CustomConstraint customConstraint) {
        mCustomConstraint = customConstraint;
        disableIfConstraintsNotMet();
    }

    /**
     * Remove any custom constraints set by {@link #setCustomConstraint(CustomConstraint)}.
     * <p/>
     * This method is safe to call if there is no current custom constraint.
     */
    public void clearCustomConstraint() {
        setCustomConstraint(null);
    }

    public void disableIfConstraintsNotMet() {
        mConstraintViolationSummary = findConstraintViolation();
        mPreference.setEnabled(constraintsMet());
    }

    /**
     * Check for constraint violations.
     *
     * @return A string describing the constraint violation or {@code null} if no violations were
     * found.
     */
    private CharSequence findConstraintViolation() {
        if (getDeviceSdkInt() < mMinSdkVersion) {
            return mContext.getString(R.string.requires_android_api_level, mMinSdkVersion);
        }

        if (!isEnabledForAdmin(getCurrentAdmin())) {
            return getAdminConstraintSummary();
        }

        if (!isEnabledForUser(getCurrentUser())) {
            return getUserConstraintSummary();
        }

        if (mCustomConstraint != null) {
            @StringRes int strRes = mCustomConstraint.validateConstraint();
            if (strRes != NO_CUSTOM_CONSTRIANT) {
                return mContext.getString(strRes);
            }
        }
        return null;
    }

    private int getCurrentAdmin() {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final String packageName = mContext.getPackageName();

        if (dpm.isDeviceOwnerApp(packageName)) {
            return ADMIN_DEVICE_OWNER;
        }
        if (dpm.isProfileOwnerApp(packageName)) {
            return ADMIN_PROFILE_OWNER;
        }
        return ADMIN_NONE;
    }

    private int getCurrentUser() {
        if (Util.isPrimaryUser(mContext)) {
            return USER_PRIMARY_USER;
        }

        if (Util.isManagedProfileOwner(mContext)) {

            return USER_MANAGED_PROFILE;
        }

        return USER_SECONDARY_USER;
    }

    private boolean isEnabledForAdmin(@AdminKind int admin) {
        return (mAdminConstraint & admin) == admin;
    }

    private boolean isEnabledForUser(@UserKind int user) {
        return (mUserConstraint & user) == user;
    }

    private String getAdminConstraintSummary() {
        final List<String> admins = new ArrayList<>(NUM_ADMIN_KINDS);

        if (isEnabledForAdmin(ADMIN_DEVICE_OWNER)) {
            admins.add(mContext.getString(R.string.device_owner));
        }
        if (isEnabledForAdmin(ADMIN_PROFILE_OWNER)) {
            admins.add(mContext.getString(R.string.profile_owner));
        }

        return joinRequirementList(admins);
    }

    private String getUserConstraintSummary() {
        final List<String> users = new ArrayList<>(NUM_USER_KINDS);

        if (isEnabledForUser(USER_PRIMARY_USER)) {
            users.add(mContext.getString(R.string.primary_user));
        }
        if (isEnabledForUser(USER_SECONDARY_USER)) {
            users.add(mContext.getString(R.string.secondary_user));
        }
        if (isEnabledForUser(USER_MANAGED_PROFILE)) {
            users.add(mContext.getString(R.string.managed_profile));
        }

        return joinRequirementList(users);
    }

    private String joinRequirementList(List<String> items) {
        final StringBuilder sb = new StringBuilder(mContext.getString(R.string.requires));
        final String lastItem = items.remove(items.size() - 1);
        sb.append(TextUtils.join(mContext.getString(R.string.requires_delimiter), items));
        if (!items.isEmpty()) {
            sb.append(mContext.getString(R.string.requires_or));
        }
        sb.append(lastItem);
        return sb.toString();
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
