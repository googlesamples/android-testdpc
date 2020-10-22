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

import java.util.Arrays;
import java.util.function.Consumer;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.util.Log;

public final class DevicePolicyManagerGatewayImpl implements DevicePolicyManagerGateway {

    private static final String TAG = DevicePolicyManagerGatewayImpl.class.getSimpleName();

    private final DevicePolicyManager mDevicePolicyManager;
    private final ComponentName mAdminComponentName;

    public DevicePolicyManagerGatewayImpl(@NonNull Context context) {
        this(context.getSystemService(DevicePolicyManager.class),
                DeviceAdminReceiver.getComponentName(context));
    }

    public DevicePolicyManagerGatewayImpl(@NonNull DevicePolicyManager dpm,
            @NonNull ComponentName admin) {
        mDevicePolicyManager = dpm;
        mAdminComponentName = admin;

        Log.d(TAG, "constructor: admin=" + mAdminComponentName + ", dpm=" + dpm);
    }

    @Override
    public void createAndManageUser(String name, int flags, Consumer<UserHandle> onSuccess,
            Consumer<Exception> onError) {
        Log.d(TAG, "createAndManageUser(" + name + ", " + flags);

        try {
            UserHandle userHandle = mDevicePolicyManager.createAndManageUser(mAdminComponentName,
                    name, mAdminComponentName, /* adminExtras= */ null, flags);
            if (userHandle != null) {
                onSuccess.accept(userHandle);
            } else {
                onError.accept(new NullResultException("createAndManageUser"));
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void lockNow(Consumer<Void> onSuccess, Consumer<Exception> onError) {
        Log.d(TAG, "lockNow()");

        try {
            mDevicePolicyManager.lockNow();
            onSuccess.accept(null);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public String toString() {
        return "DevicePolicyManagerGatewayImpl[" + mAdminComponentName + "]";
    }
}
