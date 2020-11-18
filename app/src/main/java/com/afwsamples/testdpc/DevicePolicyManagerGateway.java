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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

import android.os.UserHandle;

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
     * See {@link android.app.admin.DevicePolicyManager#removeUser(android.content.ComponentName, UserHandle)}.
     */
    void removeUser(@NonNull UserHandle userHandle,
            @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * Same as {@link #removeUser(UserHandle, Consumer<Void>, Consumer<Exception>)}, but it uses
     * {@link android.os.UserManager#getSerialNumberForUser(UserHandle)}
     * to get the {@link UserHandle} associated with the {@code serialNumber}.
     */
    void removeUser(@NonNull long serialNumber,
            @NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#lockNow()}.
     */
    void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#wipeData()}.
     */
    void wipeData(int flags, @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * Used on error callbacks to indicate a {@link android.app.admin.DevicePolicyManager} method
     * call failed.
     */
    @SuppressWarnings("serial")
    public static final class InvalidResultException extends Exception {

        private final String mMethod;
        private final String mResult;

        /**
         * Default constructor.
         *
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
}
