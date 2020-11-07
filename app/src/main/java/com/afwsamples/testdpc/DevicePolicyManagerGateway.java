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
     * See {@link android.app.admin.DevicePolicyManager#lockNow()}.
     */
    void lockNow(@NonNull Consumer<Void> onSuccess, @NonNull Consumer<Exception> onError);

    /**
     * See {@link android.app.admin.DevicePolicyManager#wipeData()}.
     */
    void wipeData(int flags, @NonNull Consumer<Void> onSuccess,
            @NonNull Consumer<Exception> onError);

    /**
     * Used on error callbacks to indicate that the operation returned {@code null}.
     */
    public static final class NullResultException extends Exception {

        private final String mMethod;

        /**
         * Default constructor.
         *
         * @param method method name (without parenthesis).
         */
        public NullResultException(@NonNull String method) {
            mMethod = method;
        }

        @Override
        public String toString() {
            return "DPM." + mMethod + "() returned null";
        }
    }
}
