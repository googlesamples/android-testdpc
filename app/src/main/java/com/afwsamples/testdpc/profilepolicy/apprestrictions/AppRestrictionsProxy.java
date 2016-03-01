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

package com.afwsamples.testdpc.profilepolicy.apprestrictions;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

import com.afwsamples.testdpc.DeviceAdminReceiver;

/**
 * Before N, only the DPC has permission to set application restrictions via
 * {@link DevicePolicyManager#setApplicationRestrictions(ComponentName, String, Bundle)}.
 *
 * To enable the another package to manage the application restrictions, a bound service is used to
 * pass them to {@link AppRestrictionsProxy}.
 *
 * From N onwards, a given package can be granted permission to manage application restrictions,
 * which removes the need for the proxy code.
 */
public class AppRestrictionsProxy extends Service {

    private final Messenger mMessenger = new Messenger(new AppRestrictionsProxyHandler(this,
            DeviceAdminReceiver.getComponentName(this)));

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}