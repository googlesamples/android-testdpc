/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.afwsamples.testdpc;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface used to abstract calls to {@link android.app.admin.DevicePolicyManager}.
 *
 * <p>Each of its methods takes 2 callbacks: one called when the underlying call succeeds, the other
 * one called when it throws an exception.
 */
public interface DevicePolicyManagerGateway {

    /**
     * See {@link android.app.admin.DevicePolicyManager#createAndManageUser(android.content.ComponentName, String, android.content.ComponentName, android.os.PersistableBundle, int)}.
     */
    void createAndManageUser(@Nullable String name, int flags,
            @NonNull Consumer<UserHandle> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#setUserIcon(android.content.ComponentName, android.graphics.Bitmap)}.
     */
    void setUserIcon(@NonNull Bitmap icon, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /** @see {@link android.os.UserManager#getUserForSerialNumber(long)}.*/
    @Nullable
    UserHandle getUserHandle(long serialNumber);

    /**
     * See {@link android.app.admin.DevicePolicyManager#removeUser(android.content.ComponentName, UserHandle)}.
     */
    void removeUser(@NonNull UserHandle userHandle, @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#switchUser(android.content.ComponentName, UserHandle)}.
     */
    void switchUser(@NonNull UserHandle userHandle, @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#startUserInBackground(android.content.ComponentName, UserHandle)}.
     */
    void startUserInBackground(@NonNull UserHandle userHandle, @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#stopUser(android.content.ComponentName, UserHandle)}.
     */
    void stopUser(@NonNull UserHandle userHandle, @NonNull Consumer<Integer> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#isAffiliatedUser()}.
     */
    boolean isAffiliatedUser();

    /**
     * See {@link android.app.admin.DevicePolicyManager#setAffiliationIds(android.content.ComponentName, Set)}.
     */
    void setAffiliationIds(@NonNull Set<String> ids);

    /**
     * See {@link android.app.admin.DevicePolicyManager#getAffiliationIds(android.content.ComponentName)}.
     */
    @NonNull
    Set<String> getAffiliationIds();

    /**
     * See {@link android.app.admin.DevicePolicyManager#getUserRestrictions(android.content.ComponentName)}.
     */
    @NonNull
    Set<String> getUserRestrictions();

    /**
     * See {@link android.app.admin.DevicePolicyManager#setUserRestriction(android.content.ComponentName, String)}
     * and {@link android.app.admin.DevicePolicyManager#clearUserRestriction(android.content.ComponentName, String)}.
     */
    void setUserRestriction(@NonNull String userRestriction, boolean enabled,
            @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * Same as {@link #setUserRestriction(String, boolean, Consumer, Consumer)}, but ignoring
     * callbacks.
     */
    void setUserRestriction(@NonNull String userRestriction, boolean enabled);

    /**
     * See {@link android.os.UserManager#hasUserRestriction(String)}.
     */
    boolean hasUserRestriction(@NonNull String userRestriction);

    /**
     * See {@link android.app.admin.DevicePolicyManager#lockNow()}.
     */
    void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#lockNow(int)}.
     */
    void lockNow(int flags, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#reboot()}.
     */
    void reboot(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#wipeData(int)}.
     */
    void wipeData(int flags, @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#requestBugreport(android.content.ComponentName)}.
     */
    void requestBugreport(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#setNetworkLoggingEnabled(android.content.ComponentName, boolean)}.
     */
    void setNetworkLogging(boolean enabled, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * Same as {@link #setNetworkLogging(boolean, Consumer, Consumer)}, but ignoring callbacks.
     */
    void setNetworkLogging(boolean enabled);

    /**
     * See {@link android.app.admin.DevicePolicyManager#setOrganizationName(android.content.ComponentName, String)}.
     */
    void setOrganizationName(@Nullable CharSequence title, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#getOrganizationName(android.content.ComponentName)}.
     */
    @Nullable CharSequence getOrganizationName();

    /**
     * See {@link android.app.admin.DevicePolicyManager#setUserControlDisabledPackages(android.content.ComponentName, List<String>)}.
     */
    void setUserControlDisabledPackages(@Nullable List<String> packages, @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#getUserControlDisabledPackages(android.content.ComponentName)}.
     */
    @NonNull List<String> getUserControlDisabledPackages();

    /*
     * See {@link android.app.admin.DevicePolicyManager#setPermittedInputMethods(
     * android.content.ComponentName, List)}.
     */
    boolean setPermittedInputMethods(List<String> packageNames, @NonNull Consumer<Void> onSuccess,
        @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#setPermittedInputMethods(
     * android.content.ComponentName, List)}.
     */
    boolean setPermittedInputMethods(List<String> packageNames);

    /**
     * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method
     * call failed.
     */
    @SuppressWarnings("serial")
    public static class InvalidResultException extends Exception {

        private final String mMethod;
        private final String mResult;

        /**
         * Default constructor.
         *
         * @param result result of the method call.
         * @param method method name template.
         * @param args method arguments.
         */
        public InvalidResultException(@NonNull String result, @NonNull String method,
                @NonNull Object...args) {
            mResult = result;
            mMethod = String.format(method, args);
        }

        @Override
        public String toString() {
            return "DPM." + mMethod + " returned " + mResult;
        }
    }

    /**
     * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method
     * call that returned {@code false}.
     */
    @SuppressWarnings("serial")
    public static final class FailedOperationException extends InvalidResultException {

        /**
         * Default constructor.
         *
         * @param method method name template.
         * @param args method arguments.
         */
        public FailedOperationException(@NonNull String method, @NonNull Object...args) {
            super("false", method, args);
        }
    }

    /**
     * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method
     * call that returned a user-related error.
     */
    @SuppressWarnings("serial")
    public static final class FailedUserOperationException extends InvalidResultException {

        /**
         * Default constructor.
         *
         * @param status user-related opeartion status.
         * @param method method name template.
         * @param args method arguments.
         */
        public FailedUserOperationException(int status, @NonNull String method,
                @NonNull Object...args) {
            super(userStatusToString(status), method, args);
        }

        private static String userStatusToString(int status) {
            switch (status) {
                case UserManager.USER_OPERATION_SUCCESS:
                    return "SUCCESS";
                case UserManager.USER_OPERATION_ERROR_CURRENT_USER:
                    return "ERROR_CURRENT_USER";
                case UserManager.USER_OPERATION_ERROR_LOW_STORAGE:
                    return "ERROR_LOW_STORAGE";
                case UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE:
                    return "ERROR_MAX_MANAGED_PROFILE";
                case UserManager.USER_OPERATION_ERROR_MAX_RUNNING_USERS:
                    return "ERROR_MAX_RUNNING_USERS";
                case UserManager.USER_OPERATION_ERROR_MAX_USERS:
                    return "ERROR_MAX_USERS";
                case UserManager.USER_OPERATION_ERROR_UNKNOWN:
                    return "ERROR_UNKNOWN";
                default:
                    return "INVALID_STATUS:" + status;
            }
        }
    }
}
